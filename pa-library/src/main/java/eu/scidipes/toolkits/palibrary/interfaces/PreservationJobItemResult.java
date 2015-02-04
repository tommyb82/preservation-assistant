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

/**
 * Represents the result of a preservation operation on a single item. Either {@link #getPreservedObject()} or
 * {@link #getThrown()} should return not-null, but never both.
 * 
 * @author Tom Bunting
 * 
 * @param <T>
 *            the type of object to preserve
 * @param <P>
 *            the preserved object type
 */
public interface PreservationJobItemResult<T extends Preservable, P> {

	/**
	 * Get the <code>T</code> instance for which preservation was attempted (e.g. a {@link Form})
	 * 
	 * @return the object to be preserved
	 */
	T getPreservable();

	/**
	 * If the preservation operation was successful, returns the <code>P</code> instance representing the preserved
	 * object as returned from the remote registry
	 * 
	 * @return the preserved object
	 */
	P getPreservedObject();

	/**
	 * If the preservation operation was not successful, returns a {@link Throwable} describing nature of the problem
	 * 
	 * @return
	 */
	Throwable getThrown();

	/**
	 * Convenience method indicating whether or not the preservation operation succeeded.
	 * 
	 * @return
	 */
	Boolean isSucceeded();
}
