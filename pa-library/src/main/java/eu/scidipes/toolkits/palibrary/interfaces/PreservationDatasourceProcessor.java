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
package eu.scidipes.toolkits.palibrary.interfaces;

import java.util.Set;

import eu.scidipes.toolkits.palibrary.exceptions.PreservationInitialisationException;

/**
 * Defines a contract for processing a preservation data set content (e.g. an ontology) into a {@link FormsBundle}
 * instance.
 * 
 * @author Tom Bunting
 * 
 */
public interface PreservationDatasourceProcessor {

	/**
	 * Processes a source <code>Object</code> to produce a {@link FormsBundle}. The source could be a file pointer (e.g.
	 * {@link java.io.File}, a byte stream, DOM document or in theory anything.
	 * 
	 * @param source
	 * @return the forms bundle
	 * @throws PreservationInitialisationException
	 */
	FormsBundle sourceToBundle(Object source) throws PreservationInitialisationException;

	/**
	 * Handles discovery of one or more {@link FormsBundle} instances.
	 * 
	 * @return a set of bundles
	 * @throws PreservationInitialisationException
	 */
	Set<FormsBundle> discoverBundles() throws PreservationInitialisationException;

	/**
	 * Name
	 * 
	 * @return name
	 */
	String getName();
}
