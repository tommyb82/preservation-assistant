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
package eu.scidipes.toolkits.palibrary.utils;

import java.util.concurrent.Future;

/**
 * Miscellaneous utility methods and constants
 * 
 * @author Tom Bunting
 * @author Brian Goetz (from 'Java Concurrency In Practise')
 * 
 */
public final class MiscUtils {

	/** Arbitrary non-zero initial value to minimise collisions when initial fields hash to 0 */
	public static final int HASH_INIT = 17;

	/** Odd prime for hash multiplication */
	public static final int HASH_PRIME = 31;

	private MiscUtils() {
		/* Private constructor */
	}

	/**
	 * Encapsulate some messy exception-handling logic when unknown {@link Throwable}s are thrown from methods such as
	 * {@link Future#get()}
	 * 
	 * @param throwable
	 *            the throwable to launder
	 * @return runtime exception if that's what the throwable is
	 */
	public static RuntimeException launderThrowable(final Throwable throwable) {
		if (throwable instanceof RuntimeException) {
			return (RuntimeException) throwable;
		} else if (throwable instanceof Error) {
			throw (Error) throwable;
		} else {
			throw new IllegalStateException("Not unchecked", throwable);
		}
	}

}
