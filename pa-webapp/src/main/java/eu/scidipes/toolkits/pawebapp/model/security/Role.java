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
package eu.scidipes.toolkits.pawebapp.model.security;

import static java.lang.String.format;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * JPA entity bean representing a user role (aka authority)
 * 
 * @author Tom Bunting
 * 
 */
@Entity
@Table(name = "roles")
public final class Role {

	private String description;

	boolean enabled = true;

	@Id
	private String role;

	@ManyToMany(mappedBy = "roles")
	private final Set<User> users = new HashSet<>();

	/**
	 * No-arg constructor required by JPA
	 */
	protected Role() {
		/* No action */
	}

	public Role(final String role) {
		this.role = role;
	}

	/**
	 * @return the description
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * @return the role
	 */
	public final String getRole() {
		return role;
	}

	/**
	 * @return the users
	 */
	public final Set<User> getUsers() {
		return users;
	}

	/**
	 * @return the enabled
	 */
	public final boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	final void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 */
	final void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return format("Role: %s (%s)", role, description);
	}

}
