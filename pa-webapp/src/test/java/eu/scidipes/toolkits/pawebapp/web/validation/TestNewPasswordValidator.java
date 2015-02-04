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

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import eu.scidipes.toolkits.pa.test.AbstractTest;
import eu.scidipes.toolkits.pawebapp.model.security.PasswordChange;

/**
 * Test class for methods in {@link NewPasswordValidator}
 * 
 * @author Tom Bunting
 * 
 */
public class TestNewPasswordValidator extends AbstractTest {

	private final PasswordChange passwordChange = new PasswordChange();

	private final NewPasswordValidator newPasswordValidator = new NewPasswordValidator();

	private final Errors errors = new BeanPropertyBindingResult(passwordChange, "passwordChange");

	/**
	 * Test method for {@link NewPasswordValidator#supports(Class)} .
	 */
	@Test
	public final void testSupports() {
		assertTrue(newPasswordValidator.supports(PasswordChange.class));
		assertFalse(newPasswordValidator.supports(String.class));
	}

	/**
	 * Test method for {@link NewPasswordValidator#validate(Object, Errors)} .
	 */
	@Test
	public final void testValidateValid() {
		final String validPassword = "adm1n_ny";
		passwordChange.setNewPassword(validPassword);
		passwordChange.setNewPasswordRepeat(validPassword);

		newPasswordValidator.validate(passwordChange, errors);

		assertFalse(errors.hasErrors());
	}

	/**
	 * Test method for {@link NewPasswordValidator#validate(Object, Errors)} .
	 */
	@Test
	public final void testValidateInvalidPassword() {
		final String invalidPassword = "adminpass";
		passwordChange.setNewPassword(invalidPassword);
		passwordChange.setNewPasswordRepeat(invalidPassword);

		newPasswordValidator.validate(passwordChange, errors);

		assertEquals(1, errors.getErrorCount());
		assertEquals(NewPasswordValidator.NEWPASS_INVALID_ERR_CODE, errors.getFieldError("newPassword").getCode());
	}

	/**
	 * Test method for {@link NewPasswordValidator#validate(Object, Errors)} .
	 */
	@Test
	public final void testValidateUnequalRepeat() {
		final String validPassword = "adm1n_ny";

		passwordChange.setNewPassword(validPassword);
		passwordChange.setNewPasswordRepeat("summinelse");

		newPasswordValidator.validate(passwordChange, errors);

		assertEquals(1, errors.getErrorCount());
		assertEquals(NewPasswordValidator.NEWPASSRPT_INVALID_ERR_CODE, errors.getFieldError("newPasswordRepeat")
				.getCode());
	}

}
