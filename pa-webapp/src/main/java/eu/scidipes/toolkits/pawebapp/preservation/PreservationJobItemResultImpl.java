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
package eu.scidipes.toolkits.pawebapp.preservation;

import static org.thymeleaf.util.Validate.notNull;

import info.digitalpreserve.interfaces.RegistryObject;
import eu.scidipes.toolkits.palibrary.interfaces.Preservable;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationJobItemResult;

/**
 * Basic implementation of a preservation job item result with {@link RegistryObject} as the result type.
 * 
 * @author Tom Bunting
 * 
 */
class PreservationJobItemResultImpl implements PreservationJobItemResult<Preservable, RegistryObject> {

	private final Preservable preservable;
	private final RegistryObject preservedObject;
	private final Throwable thrown;

	PreservationJobItemResultImpl(final Preservable preservable, final RegistryObject preservedObject) {
		notNull(preservable, "preservable must not be null");
		notNull(preservedObject, "preservedObject must not be null");

		this.preservable = preservable;
		this.preservedObject = preservedObject;
		this.thrown = null;
	}

	PreservationJobItemResultImpl(final Preservable preservable, final Throwable throwable) {
		notNull(preservable, "preservable must not be null");
		notNull(throwable, "throwable must not be null");

		this.preservable = preservable;
		this.preservedObject = null;
		this.thrown = throwable;
	}

	@Override
	public Preservable getPreservable() {
		return preservable;
	}

	@Override
	public RegistryObject getPreservedObject() {
		return preservedObject;
	}

	@Override
	public Throwable getThrown() {
		return thrown;
	}

	@Override
	public Boolean isSucceeded() {
		return Boolean.valueOf(preservedObject != null);
	}

	@Override
	public String toString() {
		return String.format("PreservationJobItem (Preservation item: %s, Thrown: %s, Succeeded: %b)", preservable,
				thrown == null ? "-" : thrown.getMessage(), isSucceeded());
	}
}
