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

import static org.springframework.web.bind.annotation.RequestMethod.POST;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.scidipes.toolkits.pawebapp.model.security.Role;
import eu.scidipes.toolkits.pawebapp.model.security.User;
import eu.scidipes.toolkits.pawebapp.repository.RoleRepository;
import eu.scidipes.toolkits.pawebapp.repository.UserRepository;
import eu.scidipes.toolkits.pawebapp.web.validation.UserValidator;

/**
 * {@link Controller} encapsulating user management operations (add, edit, delete etc)
 * 
 * @author Tom Bunting
 * 
 */
@Controller
@SessionAttributes({ "newUser" })
@RequestMapping("/admin/users")
public final class UserManagementController {

	private static final Logger LOG = LoggerFactory.getLogger(UserManagementController.class);

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@ModelAttribute("allRoles")
	public Iterable<Role> populateRoles() {
		return roleRepository.findAll();
	}

	@ModelAttribute("allUsers")
	public Iterable<User> populateUsers() {
		return userRepo.findAll();
	}

	@RequestMapping
	public String home(final Model model) {
		model.addAttribute("newUser", new User());
		return "/admin/users";
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public String add(@ModelAttribute("newUser") final User newUser, final BindingResult result,
			final SessionStatus status, final RedirectAttributes redirectAttrs) {

		new UserValidator(userRepo).validate(newUser, result);

		if (result.hasErrors()) {
			LOG.debug("Errors encountered in newUser");
			return "/admin/users";
		}

		newUser.setPassword(passwordEncoder.encode(newUser.getNewPassword()));
		userRepo.save(newUser);

		status.setComplete();
		redirectAttrs.addFlashAttribute("msgKey", "users.management.new.success");
		return "redirect:/admin/users";
	}

	@RequestMapping(value = "/delete", method = POST)
	public String delete(final String delusername, final Model model, final RedirectAttributes redirectAttrs) {

		userRepo.delete(delusername);
		redirectAttrs.addFlashAttribute("msgKey", "users.management.edit.deleted");
		return "redirect:/admin/users";
	}

	@ResponseBody
	@RequestMapping(value = "/editactive", method = POST)
	public boolean editactive(final String editactiveusername, final boolean editactive) {
		final User user = userRepo.findOne(editactiveusername);
		user.setEnabled(editactive);

		/* Save and verify: */
		if (userRepo.save(user).isEnabled() == editactive) {
			LOG.debug("{} was {}", user, editactive ? "activated" : "deactivated");
			return true;
		} else {
			LOG.debug("Unable to change active status of {}", user);
			return false;
		}
	}

}
