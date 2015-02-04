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
package eu.scidipes.toolkits.pawebapp.web.validation;

import static org.springframework.validation.ValidationUtils.rejectIfEmptyOrWhitespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;

import eu.scidipes.toolkits.pawebapp.model.security.PasswordChange;
import eu.scidipes.toolkits.pawebapp.model.security.User;

/**
 * Validates the current and new password elements of a {@link PasswordChange} instance
 * 
 * @author Tom Bunting
 * 
 */
public class PasswordChangeValidator extends NewPasswordValidator {

	private static final Logger LOG = LoggerFactory.getLogger(PasswordChangeValidator.class);

	/* Constants for error codes */
	public static final String CURRPASS_REQ_ERR_CODE = "validation.errors.passwordchange.currpassrqd";
	public static final String CURRPASS_MISMATCH_ERR_CODE = "validation.errors.passwordchange.currpassmismatch";

	private final User user;

	private final PasswordEncoder passwordEncoder;

	/**
	 * Instantiates a new <code>PasswordChangeValidator</code> instance with an {@link User} and a
	 * {@link PasswordEncoder} instance for use during the validation process.
	 * 
	 * @param user
	 * @param passwordEncoder
	 * 
	 * @throws IllegalArgumentException
	 *             if either user or passwordEncoder are null
	 */
	public PasswordChangeValidator(final User user, final PasswordEncoder passwordEncoder) {
		if (user == null) {
			throw new IllegalArgumentException("user must not be null");
		}
		if (passwordEncoder == null) {
			throw new IllegalArgumentException("passwordEncoder must not be null");
		}
		this.user = user;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void validate(final Object target, final Errors errors) {

		if (!(target instanceof PasswordChange)) {
			throw new IllegalArgumentException("target must be instace of PasswordChange");
		}

		final PasswordChange passwordChange = (PasswordChange) target;

		/* Reject if any fields empty: */
		rejectIfEmptyOrWhitespace(errors, "currentPassword", CURRPASS_REQ_ERR_CODE);

		if (!errors.hasErrors()) {
			/* Validation step 1: current password must be correct */
			if (!passwordEncoder.matches(passwordChange.getCurrentPassword(), user.getPassword())) {
				errors.rejectValue("currentPassword", CURRPASS_MISMATCH_ERR_CODE);
				LOG.debug("Rejected current password due to mismatch");
			}

			/* Validation step 2: check the new password is valid */
			super.validate(target, errors);
		}

	}
}
