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
package eu.scidipes.toolkits.palibrary.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.log4j.Logger;

import eu.scidipes.toolkits.palibrary.interfaces.PreservationDatasourceProcessor;

/**
 * Uses Java's SPI to discover and manage {@link PreservationDatasourceProcessor} implementations
 * 
 * @author Tom Bunting
 * 
 */
public enum SourceProcessorManager {

	/**
	 * The singleton instance
	 */
	INSTANCE;

	private final Logger log = Logger.getLogger(SourceProcessorManager.class);

	private final ServiceLoader<PreservationDatasourceProcessor> serviceLoader;

	private final Map<String, PreservationDatasourceProcessor> repo = new HashMap<>();

	private SourceProcessorManager() {
		serviceLoader = ServiceLoader.load(PreservationDatasourceProcessor.class);
		populateRepo();
		log.info("SourceProcessorManager instance initialised");
	}

	/**
	 * Clears the internal repository then re-populates it from the service loader.
	 * 
	 * @throws IllegalStateException
	 *             if the service loader is null
	 */
	private void populateRepo() {
		repo.clear();

		if (serviceLoader == null) {
			throw new IllegalStateException("Service loader has not been initialized");
		}

		for (final PreservationDatasourceProcessor processor : serviceLoader) {
			repo.put(processor.getName(), processor);
		}

		if (log.isDebugEnabled()) {
			log.debug("Populated source processor repository with " + repo.size() + " implementations");
		}
	}

	/**
	 * Calls {@link ServiceLoader#reload()} to re-discover {@link PreservationDatasourceProcessor} implementations and
	 * re-populates the internal instance map
	 */
	public void reload() {
		synchronized (this) {
			serviceLoader.reload();
			populateRepo();
		}
	}

	/**
	 * Returns a {@link Map} of source processors keyed by their name
	 * 
	 * @return a read-only source processor instance map
	 */
	public Map<String, PreservationDatasourceProcessor> getProcessors() {
		return Collections.unmodifiableMap(repo);
	}

}
