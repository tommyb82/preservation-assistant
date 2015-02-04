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
package eu.scidipes.toolkits.palibrary.impl;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.scidipes.toolkits.palibrary.exceptions.PreservationInitialisationException;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationDatasourceProcessor;

/**
 * Base implementation of {@link PreservationDatasourceProcessor} to provide common source file loading functionality.
 * Subclasses must provide their own processing logic.
 * 
 * @author Tom Bunting
 * 
 */
public abstract class AbstractFileSourceProcessor implements PreservationDatasourceProcessor {

	private static final Logger LOG = Logger.getLogger(AbstractFileSourceProcessor.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.scidipes.toolkits.palibrary.interfaces.PreservationDatasourceProcessor#discoverBundles()
	 */
	@Override
	public Set<FormsBundle> discoverBundles() throws PreservationInitialisationException {
		final String sourceRootPath = System.getProperty("pa.sources.path");

		final File sourceRoot = new File(sourceRootPath);

		if (!sourceRoot.exists() || !sourceRoot.isDirectory() || !sourceRoot.canWrite()) {
			throw new PreservationInitialisationException("Sources directory [" + sourceRootPath
					+ "] defined by sys property 'pa.sources.path' must exist and must be writable");
		}

		final String processorId = getClass().getSimpleName();
		final String subFolderPath = sourceRootPath + File.separatorChar + processorId;
		final File subFolder = new File(subFolderPath);

		if (!subFolder.exists()) {
			subFolder.mkdir();
			LOG.info("Created source subdirectory '" + subFolder.toString() + "' for processor: " + processorId);
		}

		final Set<FormsBundle> bundles = new HashSet<>();

		if (subFolder.list().length > 0) {

			for (final File source : subFolder.listFiles()) {
				if (!source.isDirectory() && source.canRead()) {
					try {
						final FormsBundle bundle = sourceToBundle(source);

						/* Use the file name to inform the bundle of its source: */
						((FormsBundleImpl) bundle).setTemplateSource(source.getName());
						bundles.add(bundle);
						LOG.info("Successfully processed source file: " + source.toString() + " with processor: "
								+ processorId);
					} catch (final Exception e) {
						LOG.warn("Processor " + processorId + " encountered an error processing source file: "
								+ source.toString());
						LOG.warn(e.toString(), e);
					}
				}
			}

		} else {
			LOG.info("No source files found for processor: " + processorId);
		}

		return bundles;
	}

}
