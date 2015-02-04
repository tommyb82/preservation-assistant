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

import info.digitalpreserve.interfaces.Registry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Domain model to hold {@link Registry} authentication information
 * 
 * @author Tom Bunting
 * 
 */
@Entity
@Table(name = "registry_auth")
public class RegistryAuth {

	@Id
	@Column(name = "registry_uid")
	private String registryUID;

	/* Encrypted values */
	private String principal;
	private String credential;

	@Column(name = "updated_by")
	private String updatedBy;

	/**
	 * No-arg constructor required by JPA
	 */
	protected RegistryAuth() {
		/* No action */
	}

	public RegistryAuth(final String registryUID, final String principal, final String credential,
			final String updatedBy) {
		this.registryUID = registryUID;
		this.principal = principal;
		this.credential = credential;
		this.updatedBy = updatedBy;
	}

	/**
	 * @return the registryUID
	 */
	public final String getRegistryUID() {
		return registryUID;
	}

	/**
	 * Return the principal (which in all likelihood will be encrypted).
	 * 
	 * @return the principal
	 */
	public final String getPrincipal() {
		return principal;
	}

	/**
	 * Return the credential (which in all likelihood will be encrypted).
	 * 
	 * @return the credential
	 */
	public final String getCredential() {
		return credential;
	}

	/**
	 * @return the updatedBy
	 */
	public final String getUpdatedBy() {
		return updatedBy;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof RegistryAuth)) {
			return false;
		}
		final RegistryAuth otherRegAuth = (RegistryAuth) o;
		return registryUID.equals(otherRegAuth.getRegistryUID());
	}

	@Override
	public int hashCode() {
		return registryUID.hashCode();
	}

	@Override
	public String toString() {
		return "RegistryAuth: " + registryUID;
	}

}
