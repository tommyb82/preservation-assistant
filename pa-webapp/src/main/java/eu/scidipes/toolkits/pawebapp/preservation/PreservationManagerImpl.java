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

import static org.apache.commons.lang.Validate.notNull;
import info.digitalpreserve.exceptions.RIException;
import info.digitalpreserve.interfaces.Registry;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import eu.scidipes.toolkits.palibrary.exceptions.PreservationException;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationJob;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationManager;
import eu.scidipes.toolkits.pawebapp.repository.DataSetRepository;

/**
 * @author Tom Bunting
 * 
 */
@Service("preservationManager")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PreservationManagerImpl implements PreservationManager {

	private static final Logger LOG = LoggerFactory.getLogger(PreservationManagerImpl.class);

	private final Set<PreservationJob> preservationJobs = Collections.synchronizedSet(new TreeSet<PreservationJob>());

	@Autowired
	private DataSetRepository datasetRepo;

	private PreservationManagerImpl() {
		/* No public construction */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PreservationManager#getPreservationJobs()
	 */
	@Override
	public Set<PreservationJob> getPreservationJobs() {
		return Collections.unmodifiableSet(preservationJobs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PreservationManager#preserve(eu.scidipes.toolkits.palibrary.interfaces.FormsBundle,
	 * info.digitalpreserve.interfaces.Registry)
	 */
	@Override
	public boolean preserve(final FormsBundle formsBundle, final Registry registry) throws PreservationException {

		notNull(formsBundle, "The formsBundle must not be null");
		notNull(registry, "The registry must not be null");

		try {
			if (!registry.isWritable()) {
				registry.authoriseForReadWrite();
				LOG.trace("Registry '{}' tested as writable", registry);
			}
			return preservationJobs.add(PreservationJobImpl.newInstance(formsBundle, registry, datasetRepo));
		} catch (final RIException e) {
			LOG.error(e.getMessage(), e);
			throw new PreservationException(e.getCause());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PreservationManager#deleteJob(String)
	 */
	@Override
	public boolean deleteJob(final String datasetName) {

		PreservationJob jobToDelete = null;

		synchronized (preservationJobs) {
			for (final PreservationJob job : preservationJobs) {
				if (job.getDatasetName().equals(datasetName)) {
					jobToDelete = job;
					break;
				}
			}

			if (jobToDelete == null) {
				return false;
			} else {
				return preservationJobs.remove(jobToDelete);
			}
		}
	}

}
