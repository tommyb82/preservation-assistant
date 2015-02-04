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
package eu.scidipes.toolkits.palibrary.impl.jpa;

import info.digitalpreserve.interfaces.CurationPersistentIdentifier;

import java.text.ParseException;
import java.util.Locale;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang.StringUtils;
import org.springframework.format.Formatter;

import eu.scidipes.common.framework.FrameworkWrapper;

/**
 * Dual use converter between between {@link CurationPersistentIdentifier} and <code>String</code>. Implements both the
 * JPA {@link AttributeConverter} and Spring {@link Formatter} to avoid duplication of printing and parsing logic.
 * 
 * @author Tom Bunting
 * 
 */
@Converter
public class CPIDConverter implements AttributeConverter<CurationPersistentIdentifier, String>,
		Formatter<CurationPersistentIdentifier> {

	@Override
	public String convertToDatabaseColumn(final CurationPersistentIdentifier cpid) {
		return cpidToStringHelper(cpid);
	}

	@Override
	public CurationPersistentIdentifier convertToEntityAttribute(final String cpidUID) {
		return stringToCpidHelper(cpidUID);
	}

	@Override
	public String print(final CurationPersistentIdentifier cpid, final Locale locale) {
		return cpidToStringHelper(cpid);
	}

	@Override
	public CurationPersistentIdentifier parse(final String cpidUID, final Locale locale) throws ParseException {
		return stringToCpidHelper(cpidUID);
	}

	/**
	 * Creates a new {@link CurationPersistentIdentifier} from a UID <code>String</code>
	 * 
	 * @param cpidUID
	 * @return a new CPID
	 */
	private CurationPersistentIdentifier stringToCpidHelper(final String cpidUID) {
		return StringUtils.isBlank(cpidUID) ? null : FrameworkWrapper.getCPID(cpidUID);
	}

	/**
	 * Returns a <code>String</code> representing the UID of the passed {@link CurationPersistentIdentifier}
	 * 
	 * @param cpid
	 * @return the UID of the CPID object
	 */
	private String cpidToStringHelper(final CurationPersistentIdentifier cpid) {
		return cpid == null ? "" : cpid.getUID();
	}

}
