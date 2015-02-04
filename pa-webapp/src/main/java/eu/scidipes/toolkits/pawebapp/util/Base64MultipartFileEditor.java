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
package eu.scidipes.toolkits.pawebapp.util;

import java.beans.PropertyEditorSupport;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.springframework.web.multipart.MultipartFile;

/**
 * Simple {@link PropertyEditorSupport} subclass to Base64-encode the binary content of a {@link MultipartFile} instance
 * as a <code>String</code>
 * 
 * @author Tom Bunting
 * 
 */
public class Base64MultipartFileEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(final String text) {
		setValue(text);
	}

	@Override
	public void setValue(final Object value) {
		if (value instanceof MultipartFile) {
			final MultipartFile multipartFile = (MultipartFile) value;
			try {
				super.setValue(Base64.encodeBase64String(multipartFile.getBytes()));
			} catch (final IOException ex) {
				throw new IllegalArgumentException("Cannot read contents of multipart file", ex);
			}
		} else {
			super.setValue(value);
		}
	}
}
