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

import info.digitalpreserve.exceptions.RIException;
import info.digitalpreserve.interfaces.Registry;

import java.security.Principal;
import org.jasypt.util.text.TextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.scidipes.common.framework.FrameworkWrapper;
import eu.scidipes.common.framework.core.http.HTTPAuthCredentialsProvider;
import eu.scidipes.toolkits.pawebapp.model.RegistryAuth;
import eu.scidipes.toolkits.pawebapp.model.RegistryAuthStatus;
import eu.scidipes.toolkits.pawebapp.repository.RegistryAuthRepository;

/**
 * {@link Controller} encapsulating operations on {@link Registry} instances managed through the Framework
 * 
 * @author Tom Bunting
 * 
 */
@Controller
public class RegistryAuthController {

	private static final Logger LOG = LoggerFactory.getLogger(RegistryAuthController.class);

	@Autowired
	private RegistryAuthRepository regAuthRepo;

	@Autowired
	private TextEncryptor textEncryptor;

	/**
	 * Check the current level of access to a given {@link Registry} instance, based on the UID.
	 * 
	 * @param regUID
	 * @return {@link RegistryAuthStatus} a domain object representing the current access level to a registry
	 */
	@RequestMapping("/registries/{regUID}/checkaccess")
	@ResponseBody
	public RegistryAuthStatus checkRegistryAuthStatus(@PathVariable final String regUID) {

		for (final Registry reg : FrameworkWrapper.getEnabledRegistries()) {

			if (regUID.equals(reg.getLocationUID()) && reg.isEnabled()) {

				/* Retrieve and set any saved credentials for this registry prior to authenticating: */
				final RegistryAuth regAuth = regAuthRepo.findOne(regUID);
				if (regAuth != null) {
					final String username = textEncryptor.decrypt(regAuth.getPrincipal());
					final String password = textEncryptor.decrypt(regAuth.getCredential());

					LOG.debug("Retrieved username [{}] and password [{}] for registry [{}]", username, password, regUID);

					final HTTPAuthCredentialsProvider credentialsProvider = new HTTPAuthCredentialsProvider(username,
							password);
					reg.setCredentialsProvider(credentialsProvider);
				}

				final boolean readAuthenticated;
				final boolean writeAuthenticated;

				try {
					reg.authoriseForReadWrite();

				} catch (final RIException riEx) {
					LOG.warn(riEx.getMessage(), riEx);

				} finally {
					readAuthenticated = reg.isAvailable();
					writeAuthenticated = reg.isWritable();
				}

				return new RegistryAuthStatus(regUID, readAuthenticated, writeAuthenticated);
			}
		}
		throw new IllegalArgumentException("Registry with UID '" + regUID + "' could not be found or has been disabled");
	}

	@RequestMapping(value = "/registries/setregcreds", method = RequestMethod.POST)
	@ResponseBody
	public Boolean setRegistryCredentials(@RequestParam final String regUID, @RequestParam final String regPrincipal,
			@RequestParam final String regCred, final Principal user) {
		final RegistryAuth registryAuth = new RegistryAuth(regUID, textEncryptor.encrypt(regPrincipal),
				textEncryptor.encrypt(regCred), user.getName());
		regAuthRepo.saveAndFlush(registryAuth);
		return Boolean.TRUE;
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseBody
	public ResponseEntity<String> handleExceptions(final IllegalArgumentException ex) {
		final String err = ex.getMessage();
		LOG.error(err, ex);
		return new ResponseEntity<String>(err, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
