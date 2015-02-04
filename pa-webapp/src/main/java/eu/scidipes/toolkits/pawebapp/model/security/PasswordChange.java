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

import java.io.Serializable;

/**
 * Simple bean class for use as DTO in password change processes
 * 
 * @author Tom Bunting
 * 
 */
public class PasswordChange implements Serializable {

	private static final long serialVersionUID = 1L;

	private String currentPassword;

	private String newPassword;

	private String newPasswordRepeat;

	/**
	 * Default zero-arg constructor
	 */
	public PasswordChange() {
		/* Default zero-arg constructor */
	}

	/**
	 * Convenience constructor for setting all properties
	 * 
	 * @param currentPassword
	 * @param newPassword
	 * @param newPasswordRepeat
	 */
	public PasswordChange(final String currentPassword, final String newPassword, final String newPasswordRepeat) {
		setCurrentPassword(currentPassword);
		setNewPassword(newPassword);
		setNewPasswordRepeat(newPasswordRepeat);
	}

	/**
	 * Return the current password
	 * 
	 * @return the currentPassword
	 */
	public final String getCurrentPassword() {
		return currentPassword;
	}

	/**
	 * Return the new password
	 * 
	 * @return the newPassword
	 */
	public final String getNewPassword() {
		return newPassword;
	}

	/**
	 * Return the new password repeat
	 * 
	 * @return the newPasswordRepeat
	 */
	public final String getNewPasswordRepeat() {
		return newPasswordRepeat;
	}

	/**
	 * Sets the current password
	 * 
	 * @param currentPassword
	 *            the currentPassword to set
	 * 
	 * @throws IllegalArgumentException
	 *             if currentPassword is null or empty
	 */
	public final void setCurrentPassword(final String currentPassword) {
		this.currentPassword = currentPassword;
	}

	/**
	 * Sets the new password
	 * 
	 * @param newPassword
	 *            the newPassword to set
	 * 
	 * @throws IllegalArgumentException
	 *             if newPassword is null or empty
	 */
	public final void setNewPassword(final String newPassword) {
		this.newPassword = newPassword;
	}

	/**
	 * Sets the new password repeat
	 * 
	 * @param newPasswordRepeat
	 *            the newPasswordRepeat to set
	 * 
	 * @throws IllegalArgumentException
	 *             if newPasswordRepeat is null or empty
	 */
	public final void setNewPasswordRepeat(final String newPasswordRepeat) {
		this.newPasswordRepeat = newPasswordRepeat;
	}

}
