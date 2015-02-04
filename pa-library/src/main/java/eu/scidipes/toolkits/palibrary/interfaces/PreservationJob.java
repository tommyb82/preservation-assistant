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

import java.util.Map;

/**
 * Defines a dataset preservation operation that may be in-progress or complete.
 * 
 * @author Tom Bunting
 * 
 */
public interface PreservationJob {

	/**
	 * Gets the current count of completed tasks. When this value equals the result of
	 * {@link #getPreservationJobItemMap().size()} then the job is considered complete
	 * 
	 * @return the number of completed jobs
	 */
	int getCompleted();

	/**
	 * Get the name of the data set being preserved by this job
	 * 
	 * @return the name of the data set being preserved
	 */
	String getDatasetName();

	/**
	 * Gets a map with the {@link Form} instances to be preserved as keys and {@link PreservationJobItemResult}
	 * instances as values. Note that the values may be null until each individual preservation operation has completed.
	 * 
	 * @return map of forms to preservation job items
	 */
	Map<Preservable, PreservationJobItemResult<Preservable, ?>> getPreservationJobItemMap();

	/**
	 * Get the UID of the {@link Registry} instance to which this job will preserve to
	 * 
	 * @return the registry UID
	 */
	String getRegistryUID();

}
