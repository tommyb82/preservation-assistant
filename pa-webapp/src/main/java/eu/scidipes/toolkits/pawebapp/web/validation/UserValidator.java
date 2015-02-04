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

import eu.scidipes.toolkits.pawebapp.model.security.User;
import eu.scidipes.toolkits.pawebapp.repository.UserRepository;

/**
 * Validates an {@link User} instance
 * 
 * @author Tom Bunting
 * 
 */
public class UserValidator extends NewPasswordValidator {

	private static final Logger LOG = LoggerFactory.getLogger(UserValidator.class);

	protected static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]{5,10}$";

	/* Constants for error codes */
	public static final String USERNAME_REQ_ERR_CODE = "validation.errors.user.usernamerqd";
	public static final String USERNAME_EXISTS_CODE = "validation.errors.user.usernameexists";
	public static final String USERNAME_INVALID_CODE = "validation.errors.user.usernameinvalid";
	public static final String NO_ROLES_SELECTED = "validation.errors.user.noroles";

	private final UserRepository userRepo;

	public UserValidator(final UserRepository userRepo) {
		this.userRepo = userRepo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Validator#supports(Class)
	 */
	@Override
	public boolean supports(final Class<?> clazz) {
		return User.class.isAssignableFrom(clazz);
	}

	/**
	 * Validates an {@link User} instance.
	 * 
	 * @throws IllegalStateException
	 *             if the passed target is not an instance of User
	 * @see Validator#validate(Object, Errors)
	 */
	@Override
	public void validate(final Object target, final Errors errors) {

		if (!(target instanceof User)) {
			throw new IllegalArgumentException("target must be instace of User");
		}

		final User user = (User) target;

		/* Reject if any fields empty: */
		rejectIfEmptyOrWhitespace(errors, "username", USERNAME_REQ_ERR_CODE);

		if (!errors.hasErrors()) {

			/* Check the username is valid: */
			if (!user.getUsername().matches(USERNAME_PATTERN)) {
				errors.rejectValue("username", USERNAME_INVALID_CODE);
				LOG.debug("Rejected invalid username: {}", user.getUsername());
			}

			/* Check username not already in use: */
			if (userRepo.exists(user.getUsername())) {
				errors.rejectValue("username", USERNAME_EXISTS_CODE);
				LOG.debug("Rejected in-use username: {}", user.getUsername());
			}

			/* Validate the password fields: */
			super.validate(user, errors);

			/* Check at least one role has been selected: */
			if (user.getRoles().isEmpty()) {
				errors.rejectValue("roles", NO_ROLES_SELECTED);
				LOG.debug("Rejected invalid roles field (none selected)");
			}
		}

	}

}
