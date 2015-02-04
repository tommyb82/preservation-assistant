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
package eu.scidipes.toolkits.pawebapp.web;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.scidipes.toolkits.pawebapp.model.security.PasswordChange;
import eu.scidipes.toolkits.pawebapp.model.security.User;
import eu.scidipes.toolkits.pawebapp.repository.UserRepository;
import eu.scidipes.toolkits.pawebapp.web.validation.PasswordChangeValidator;

/**
 * {@link Controller} encapsulating non-administrative operations on PA users, e.g. changing password
 * 
 * @author Tom Bunting
 * 
 */
@Controller
@SessionAttributes({ "user", "passwordChange" })
@RequestMapping("/user")
public final class UserController {

	private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@RequestMapping
	public String profile() {
		return "/users/profile";
	}

	@RequestMapping(value = "/changepassword", method = RequestMethod.GET)
	public String changePassword(final Model model) {
		model.addAttribute("passwordChange", new PasswordChange());
		return "/users/changepassword";
	}

	@RequestMapping(value = "/changepassword", method = RequestMethod.POST)
	public String saveChangePassword(@ModelAttribute final PasswordChange passwordChange, final BindingResult result,
			final SessionStatus status, final RedirectAttributes redirectAttrs, final Principal principal) {
		final User user = userRepo.findOne(principal.getName());

		new PasswordChangeValidator(user, passwordEncoder).validate(passwordChange, result);

		if (result.hasErrors()) {
			return "/users/changepassword";
		}

		final String encodedNewPassword = passwordEncoder.encode(passwordChange.getNewPassword());

		if (user != null) {
			user.setPassword(encodedNewPassword);
			userRepo.save(user);

			LOG.debug("Changed password for user: {}", user.getUsername());
		} else {
			return "redirect:/logout";
		}

		status.setComplete();
		redirectAttrs.addFlashAttribute("msgKey", "users.changepassword.success");
		return "redirect:/user/";
	}

}
