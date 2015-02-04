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

import info.digitalpreserve.interfaces.Registry;

import java.util.Set;

import eu.scidipes.toolkits.palibrary.exceptions.PreservationException;

/**
 * Manages preservation operations as a collection of {@link PreservationJob}s.
 * 
 * @author Tom Bunting
 * 
 */
public interface PreservationManager {

	/**
	 * Return a read-only collection of ongoing or complete preservation jobs
	 * 
	 * @return ongoing or completed preservation jobs
	 */
	Set<PreservationJob> getPreservationJobs();

	/**
	 * Submits a new job for preservation (a job comprises a {@link FormsBundle} instance and a writable
	 * {@link Registry} instance
	 * 
	 * @param formsBundle
	 * @param registry
	 * @return true if the job was successfully started, false if not (another job for the same data set may be in
	 *         progress)
	 * @throws PreservationException
	 *             if the given registry is not currently writable or some IO error occurs
	 */
	boolean preserve(final FormsBundle formsBundle, final Registry registry) throws PreservationException;

	/**
	 * Deletes the job corresponding to the given datasetName
	 * 
	 * @param datasetName
	 * @return true if job was deleted, false otherwise
	 */
	boolean deleteJob(final String datasetName);

}
