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
package eu.scidipes.toolkits.pawebapp.model;

/**
 * Simple data transfer object class for indicating whether a particular registry (identified by its UID) is currently
 * inaccessible, authenticated for read-only or accessible for both read and write operations
 * 
 * @author Tom Bunting
 * 
 */
public class RegistryAuthStatus {

	private final String registryUID;

	private final boolean readAuthenticated;

	private final boolean writeAuthenticated;

	public RegistryAuthStatus(final String registryUID, final boolean readAuthenticated,
			final boolean writeAuthenticated) {
		this.registryUID = registryUID;
		this.readAuthenticated = readAuthenticated;
		this.writeAuthenticated = writeAuthenticated;
	}

	/**
	 * Return the registry UID
	 * 
	 * @return the registryUID
	 */
	public final String getRegistryUID() {
		return registryUID;
	}

	/**
	 * Return the read authenticated flag
	 * 
	 * @return the readAuthenticated
	 */
	public final boolean isReadAuthenticated() {
		return readAuthenticated;
	}

	/**
	 * Return the write-authenticated flag
	 * 
	 * @return the writeAuthenticated
	 */
	public final boolean isWriteAuthenticated() {
		return writeAuthenticated;
	}

}
