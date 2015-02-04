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

import java.util.Set;

import eu.scidipes.toolkits.palibrary.exceptions.PreservationInitialisationException;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationDatasourceProcessor;

/**
 * @author Tom Bunting
 * 
 */
public class DummyProcessor implements PreservationDatasourceProcessor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.scidipes.toolkits.palibrary.interfaces.PreservationDatasourceProcessor#sourceToBundle(java.lang.Object)
	 */
	@Override
	public FormsBundle sourceToBundle(final Object source) throws PreservationInitialisationException {
		throw new IllegalStateException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.scidipes.toolkits.palibrary.interfaces.PreservationDatasourceProcessor#getName()
	 */
	@Override
	public String getName() {
		return "Dummy processor";
	}

	@Override
	public Set<FormsBundle> discoverBundles() throws PreservationInitialisationException {
		throw new IllegalStateException("Not implemented");
	}

}
