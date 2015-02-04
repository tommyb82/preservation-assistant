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

import static java.lang.String.format;
import info.digitalpreserve.interfaces.RepInfoLabel;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.scidipes.toolkits.palibrary.impl.UploadRepInfoLabel;

/**
 * JPA converter implementation for {@link RepInfoLabel} to/from <code>String</code> conversions.
 * 
 * @author Tom Bunting
 * 
 */
@Converter
public class RepInfoLabelConverter implements AttributeConverter<UploadRepInfoLabel, String> {

	private static final Logger LOG = LoggerFactory.getLogger(RepInfoLabelConverter.class);

	@Override
	public String convertToDatabaseColumn(final UploadRepInfoLabel ril) {
		LOG.trace(format("Converting RepInfoLabel with CPID: %s to string", ril.getCpid()));
		return ril.serialiseToString();
	}

	@Override
	public UploadRepInfoLabel convertToEntityAttribute(final String repinfoStr) {
		LOG.trace(format("Converting rep info string: %s to UploadRepInfoLabel instance", repinfoStr));
		return new UploadRepInfoLabel(repinfoStr);
	}

}
