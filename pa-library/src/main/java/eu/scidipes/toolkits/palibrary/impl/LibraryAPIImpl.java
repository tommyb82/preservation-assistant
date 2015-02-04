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

import java.util.Set;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import eu.scidipes.toolkits.palibrary.core.FormBundleManager;
import eu.scidipes.toolkits.palibrary.exceptions.PreservationException;
import eu.scidipes.toolkits.palibrary.exceptions.PreservationInitialisationException;
import eu.scidipes.toolkits.palibrary.interfaces.Bundle;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;
import eu.scidipes.toolkits.palibrary.interfaces.LibraryAPI;

/**
 * @author Simon Berriman
 */
@Service("libraryAPI")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LibraryAPIImpl implements LibraryAPI {

	/**
	 * 
	 * @return
	 * @see LibraryAPI#getAvailableFormsBundles()
	 */
	@Override
	public Set<? extends Bundle> getAvailableFormsBundles() {
		return FormBundleManager.getDiscoveredBundles();
	}

	/**
	 * 
	 * @param bundleName
	 * @return
	 * @throws PreservationException
	 * @see LibraryAPI#getEmptyStructureForBundle(java.lang.String)
	 */
	@Override
	public FormsBundle getEmptyStructureForBundle(final String bundleName) throws PreservationException {
		return FormBundleManager.getEmptyBundleFor(bundleName);
	}

	@Override
	public boolean deleteBundle(final FormsBundle bundle) throws PreservationInitialisationException {
		return FormBundleManager.deleteBundle(bundle);
	}
}
