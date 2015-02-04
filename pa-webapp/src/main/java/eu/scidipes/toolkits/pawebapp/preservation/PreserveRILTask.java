/*
 * Copyright (c) 2011-2013 Alliance for Permanent Access (APA) and its
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

import info.digitalpreserve.interfaces.Registry;
import info.digitalpreserve.interfaces.RegistryObject;
import info.digitalpreserve.interfaces.RepInfoLabel;
import java.util.Date;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.scidipes.common.framework.FrameworkWrapper;
import eu.scidipes.toolkits.palibrary.exceptions.PreservationStorageException;
import eu.scidipes.toolkits.palibrary.impl.DatasetRIL;
import eu.scidipes.toolkits.palibrary.interfaces.Preservable;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationJobItemResult;

/**
 * Callable task to preserve a {@link DatasetRIL} instance as a {@link RepInfoLabel} on a specified {@link Registry}
 * instance.
 * 
 * @author Tom Bunting
 * 
 */
class PreserveRILTask implements Callable<PreservationJobItemResult<Preservable, RegistryObject>> {

	private static final Logger LOG = LoggerFactory.getLogger(PreserveRILTask.class);

	private final DatasetRIL ril;
	private final Registry registry;

	PreserveRILTask(final DatasetRIL ril, final Registry registry) {
		this.ril = ril;
		this.registry = registry;
	}

	@SuppressWarnings("boxing")
	@Override
	public PreservationJobItemResult<Preservable, RegistryObject> call() throws PreservationStorageException {
		LOG.trace("New PreserveRILTask started in thread [{}] at [{}]", Thread.currentThread().getId(),
				new Date().toString());

		try {
			FrameworkWrapper.storeRepInfoLabel(ril.getRil(), registry);

			/* TODO: Revisit: pause should not be necessary but without it the retrieval fails intermittently */
			Thread.sleep(2000);

			final RepInfoLabel stored = FrameworkWrapper.getRepInfoLabel(ril.getRil().getCpid());

			synchronized (ril) {
				ril.setPreserved(true);
				ril.getRil().setVersion(stored.getVersion());
			}

			LOG.debug("Preserved RIL: {} to registry: {}", ril, registry);
			return new PreservationJobItemResultImpl(ril, stored);

		} catch (final Exception e) {
			throw new PreservationStorageException(e, ril);
		}

	}

}
