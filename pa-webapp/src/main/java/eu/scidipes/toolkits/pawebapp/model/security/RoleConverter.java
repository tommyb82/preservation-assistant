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
package eu.scidipes.toolkits.pawebapp.model.security;

import java.text.ParseException;
import java.util.Locale;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;

import eu.scidipes.toolkits.pawebapp.repository.RoleRepository;

/**
 * @author Tom Bunting
 * 
 */
@Converter
public final class RoleConverter implements AttributeConverter<Role, String>, Formatter<Role> {

	@Autowired
	private RoleRepository roleRepo;

	@Override
	public String convertToDatabaseColumn(final Role role) {
		return roleToStringHelper(role);
	}

	@Override
	public Role convertToEntityAttribute(final String role) {
		return stringToRoleHelper(role);
	}

	@Override
	public String print(final Role role, final Locale locale) {
		return roleToStringHelper(role);
	}

	@Override
	public Role parse(final String role, final Locale locale) throws ParseException {
		return stringToRoleHelper(role);
	}

	private String roleToStringHelper(final Role role) {
		return role.getRole();
	}

	private Role stringToRoleHelper(final String role) {
		return roleRepo.findOne(role);
	}

}
