/*
 * Copyright (c) 2011-2014 Alliance for Permanent Access (APA) and its
 * contributors. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * The APA licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.scidipes.toolkits.pawebapp.preservation;

import static eu.scidipes.toolkits.palibrary.utils.MiscUtils.launderThrowable;
import static java.lang.String.format;
import static org.apache.commons.lang.Validate.notNull;
import info.digitalpreserve.interfaces.Registry;
import info.digitalpreserve.interfaces.RegistryObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.scidipes.toolkits.palibrary.exceptions.PreservationStorageException;
import eu.scidipes.toolkits.palibrary.impl.DatasetRIL;
import eu.scidipes.toolkits.palibrary.impl.FormsBundleImpl;
import eu.scidipes.toolkits.palibrary.impl.UploadRepInfoLabel;
import eu.scidipes.toolkits.palibrary.interfaces.Form;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;
import eu.scidipes.toolkits.palibrary.interfaces.Preservable;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationJob;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationJobItemResult;
import eu.scidipes.toolkits.pawebapp.repository.DataSetRepository;

/**
 * Core implementation of a preservation job.
 * 
 * @author Tom Bunting
 * 
 */
final class PreservationJobImpl implements PreservationJob, Runnable, Comparable<PreservationJobImpl> {

	private static final Logger LOG = LoggerFactory.getLogger(PreservationJobImpl.class);

	/**
	 * Creates a new preservation job and starts it in a single threaded {@link ExecutorService}
	 * 
	 * @param formsBundle
	 *            the formsBundle to preserve
	 * @param registry
	 *            the registry to preserve to
	 * @param datasetRepo
	 * @return the new job instance
	 * @throws NullPointerException
	 *             if any of the parameters are null
	 */
	public static PreservationJobImpl newInstance(final FormsBundle formsBundle, final Registry registry,
			final DataSetRepository datasetRepo) {

		notNull(formsBundle, "The formsBundle must not be null");
		notNull(registry, "The registry must not be null");
		notNull(datasetRepo, "The datasetRepo must not be null");

		final PreservationJobImpl job = new PreservationJobImpl(formsBundle, registry, datasetRepo);
		job.submitJobItems();
		Executors.newSingleThreadExecutor().execute(job);
		return job;
	}

	private final AtomicInteger completed = new AtomicInteger();

	private final CompletionService<PreservationJobItemResult<Preservable, RegistryObject>> completionService = new ExecutorCompletionService<>(
			Executors.newCachedThreadPool());

	private final FormsBundle dataset;

	private final String datasetName;

	private final DataSetRepository datasetRepo;
	private final Map<Preservable, PreservationJobItemResult<Preservable, ?>> preservationJobItemMap = Collections
			.synchronizedMap(new HashMap<Preservable, PreservationJobItemResult<Preservable, ?>>());

	private final Registry registry;

	private PreservationJobImpl(final FormsBundle dataset, final Registry registry, final DataSetRepository datasetRepo) {
		this.dataset = dataset;
		this.datasetName = dataset.getDatasetName();
		this.registry = registry;
		this.datasetRepo = datasetRepo;
	}

	@Override
	public int compareTo(final PreservationJobImpl o) {
		return datasetName.compareTo(o.getDatasetName());
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof PreservationJobImpl)) {
			return false;
		}

		return datasetName.equals(((PreservationJobImpl) o).getDatasetName());
	}

	@Override
	public int getCompleted() {
		return completed.get();
	}

	@Override
	public String getDatasetName() {
		return datasetName;
	}

	@Override
	public Map<Preservable, PreservationJobItemResult<Preservable, ?>> getPreservationJobItemMap() {
		return Collections.unmodifiableMap(preservationJobItemMap);
	}

	@Override
	public String getRegistryUID() {
		return registry.getLocationUID();
	}

	@Override
	public int hashCode() {
		return datasetName.hashCode();
	}

	@Override
	public void run() {

		LOG.info(format("Preservation job for dataset: '%s' started", dataset.getDatasetName()));

		final int numJobItems = preservationJobItemMap.size();

		for (int i = 0; i < numJobItems; i++) {
			try {
				final PreservationJobItemResult<Preservable, ?> presObj = completionService.take().get();
				preservationJobItemMap.put(presObj.getPreservable(), presObj);

				LOG.debug("Added ({}) to preservationJobItemMap", presObj);

			} catch (final InterruptedException e) {
				LOG.error(e.getMessage(), e);
				Thread.currentThread().interrupt();
			} catch (final ExecutionException e) {
				LOG.error(e.getMessage(), e);
				final Throwable cause = e.getCause();

				if (cause instanceof PreservationStorageException) {
					/* Wrap the preservation exception in a job item and add to the list: */
					final PreservationStorageException pse = (PreservationStorageException) cause;

					LOG.error(pse.getCause().getMessage(), pse.getCause());

					preservationJobItemMap.put(pse.getPreservable(),
							new PreservationJobItemResultImpl(pse.getPreservable(), pse.getCause()));
				} else {
					throw launderThrowable(cause);
				}
			}
			completed.incrementAndGet();
		}

		LOG.info(format(
				"Preservation job for dataset: '%s' finished preserving %d manifests, digital objects and RILs",
				dataset.getDatasetName(), Integer.valueOf(numJobItems)));

		/* Update the RILs in the persistence context: */
		for (final DatasetRIL ril : dataset.getRils()) {
			datasetRepo.updateDatasetRIL((UploadRepInfoLabel) ril.getRil(), ril.getRilCPID());
		}

		datasetRepo.save((FormsBundleImpl) dataset);
	}

	private void submitJobItems() {

		/* Submit the forms to be uploaded as manifests: */
		for (final Form form : dataset.getForms()) {

			/* Skip 'empty' forms: */
			if (StringUtils.isNotEmpty(form.getDataHolder())) {
				completionService.submit(new PreserveFormManifestTask(form, registry));
				preservationJobItemMap.put(form, null);
			}
		}

		/* Submit the dataset groups (RILs) to be uploaded as RepInfoLabel instances: */
		for (final DatasetRIL ril : dataset.getRils()) {
			completionService.submit(new PreserveRILTask(ril, registry));
			preservationJobItemMap.put(ril, null);
		}
	}

}
