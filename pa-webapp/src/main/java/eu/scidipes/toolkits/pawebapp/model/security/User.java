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
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * JPA entity bean representing a system user
 * 
 * @author Tom Bunting
 * 
 */
@Entity
@Table(name = "users")
public final class User extends PasswordChange {

	private static final long serialVersionUID = 1L;

	boolean enabled = true;

	/** Hashed in DB */
	private String password;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "username", referencedColumnName = "username"), inverseJoinColumns = @JoinColumn(name = "role", referencedColumnName = "role"))
	private final Set<Role> roles = new HashSet<>();

	@Id
	private String username;

	/**
	 * No-arg constructor required by JPA
	 */
	public User() {
		/* No action */
	}

	public User(final String username, final String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * @return the password
	 */
	public final String getPassword() {
		return password;
	}

	/**
	 * @return the roles
	 */
	public final Set<Role> getRoles() {
		return roles;
	}

	/**
	 * @return the username
	 */
	public final String getUsername() {
		return username;
	}

	/**
	 * @return the enabled
	 */
	public final boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 */
	public final void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public final void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * Clears the contents of the roles member then adds all roles form the passed roles.
	 * 
	 * @param roles
	 */
	public void setRoles(final Set<Role> roles) {
		if (roles != null) {
			synchronized (this.roles) {
				this.roles.clear();
				this.roles.addAll(roles);
			}
		}
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public final void setUsername(final String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return format("User [%s]", username);
	}
}
