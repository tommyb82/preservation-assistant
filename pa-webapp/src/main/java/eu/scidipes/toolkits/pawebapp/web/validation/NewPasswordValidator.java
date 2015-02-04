/*
 * Copyright (c) 2011-2013 Alliance for Permanent Access (APA) and its
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
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.scidipes.toolkits.pawebapp.model.security.PasswordChange;

/**
 * Validates only the new password elements of an {@link PasswordChange} instance
 * 
 * @author Tom Bunting
 * 
 */
public class NewPasswordValidator implements Validator {

	private static final Logger LOG = LoggerFactory.getLogger(NewPasswordValidator.class);

	/* Minimum 6 chars, 1+ letter, 1+ digit, 1+ special char */
	protected static final String PASSWORD_PATTERN = "^.*(?=.{6,10})(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!#$%&|_@?\"]).*$";

	/* Constants for error codes */
	public static final String NEWPASS_REQ_ERR_CODE = "validation.errors.passwordchange.newpassrqd";
	public static final String NEWPASSRPT_REQ_ERR_CODE = "validation.errors.passwordchange.newpassrptrqd";

	public static final String NEWPASS_INVALID_ERR_CODE = "validation.errors.passwordchange.newpassinvalid";
	public static final String NEWPASSRPT_INVALID_ERR_CODE = "validation.errors.passwordchange.newpassrptinvalid";

	/*
	 * (non-Javadoc)
	 * 
	 * @see Validator#supports(Class)
	 */
	@Override
	public boolean supports(final Class<?> clazz) {
		return PasswordChange.class.isAssignableFrom(clazz);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Validator#validate(Object, Errors)
	 */
	@Override
	public void validate(final Object target, final Errors errors) {
		final PasswordChange passwordChange = (PasswordChange) target;

		/* Reject if any fields empty: */
		rejectIfEmptyOrWhitespace(errors, "newPassword", NEWPASS_REQ_ERR_CODE);
		rejectIfEmptyOrWhitespace(errors, "newPasswordRepeat", NEWPASSRPT_REQ_ERR_CODE);

		if (!errors.hasErrors()) {
			/* Validation step 1: check the new password is valid */
			if (!passwordChange.getNewPassword().matches(PASSWORD_PATTERN)) {
				errors.rejectValue("newPassword", NEWPASS_INVALID_ERR_CODE);
				LOG.debug("Rejected invalid new password");
			}

			/* Validation step 2: new password repeat must equal new password */
			if (!passwordChange.getNewPassword().equals(passwordChange.getNewPasswordRepeat())) {
				errors.rejectValue("newPasswordRepeat", NEWPASSRPT_INVALID_ERR_CODE);
				LOG.debug("Rejected invalid new password repeat");
			}
		}
	}

}
