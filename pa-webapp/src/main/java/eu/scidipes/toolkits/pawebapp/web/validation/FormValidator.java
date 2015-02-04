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
package eu.scidipes.toolkits.pawebapp.web.validation;

import static eu.scidipes.toolkits.palibrary.interfaces.FormFieldType.BYTESTREAM;
import static eu.scidipes.toolkits.palibrary.interfaces.FormFieldType.URI;
import static org.springframework.validation.ValidationUtils.rejectIfEmptyOrWhitespace;

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.scidipes.toolkits.palibrary.interfaces.Form;

/**
 * Validates instance of {@link Form}
 * 
 * @author Tom Bunting
 * 
 */
@Component
public class FormValidator implements Validator {

	@Resource
	private Long maxUploadSize;

	/* Constants for error codes */
	public static final String FILE_REQ_ERR_CODE = "validation.errors.form.filerequired";
	public static final String URI_REQ_ERR_CODE = "validation.errors.form.urirequired";
	public static final String DHT_REQ_ERR_CODE = "validation.errors.form.dataHolderTyperequired";
	public static final String ITEM_FILENAME_REQ_ERR_CODE = "validation.errors.form.itemFileNamerequired";

	public static final String RILCPID_REQ_ERR_CODE = "validation.errors.form.rilcpidrequired";
	public static final String INVALID_URI_ERR_CODE = "validation.errors.form.invaliduri";
	public static final String MAX_LENGTH_URI_ERR_CODE = "validation.errors.form.maxlengthuri";
	public static final String MAX_UPLOAD_SIZE_ERR_CODE = "validation.errors.form.maxuploadsize";

	public static final int URL_MAX_LENGTH = 2000;

	private static final Logger LOG = LoggerFactory.getLogger(FormValidator.class);
	private static final String DATA_HOLDER_FIELD = "dataHolder";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(final Class<?> clazz) {
		return Form.class.isAssignableFrom(clazz);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(final Object target, final Errors errors) {
		final Form form = (Form) target;

		rejectIfEmptyOrWhitespace(errors, "dataHolderType", DHT_REQ_ERR_CODE);
		rejectIfEmptyOrWhitespace(errors, "RILCPID", RILCPID_REQ_ERR_CODE);
		rejectIfEmptyOrWhitespace(errors, "itemFileName", ITEM_FILENAME_REQ_ERR_CODE);

		rejectIfEmptyOrWhitespace(errors, DATA_HOLDER_FIELD, form.getDataHolderType() == BYTESTREAM ? FILE_REQ_ERR_CODE
				: URI_REQ_ERR_CODE);

		if (!errors.hasFieldErrors(DATA_HOLDER_FIELD)) {

			final String dataHolder = errors.getFieldValue(DATA_HOLDER_FIELD).toString();
			boolean validURL = false;

			/* Check its a valid URI location (URL) */
			try {
				new URL(dataHolder);
				validURL = true;

				/* URL is valid and type (URI) is correct so check length */
				if (form.getDataHolderType() == URI && dataHolder.length() > URL_MAX_LENGTH) {
					errors.rejectValue(DATA_HOLDER_FIELD, MAX_LENGTH_URI_ERR_CODE);
					LOG.debug("Rejected valid URL as length exceeds {}", Integer.valueOf(URL_MAX_LENGTH));
					return;
				}

			} catch (final MalformedURLException e) {
				/* Reject invalid URL if type is URI */
				if (form.getDataHolderType() == URI) {
					if (dataHolder.length() < URL_MAX_LENGTH) {
						LOG.debug("Rejected invalid URL '{}' - reason: {}", dataHolder, e.getMessage());
					} else {
						LOG.debug("Rejected invalid URL, length > {}", Integer.valueOf(URL_MAX_LENGTH));
					}
					errors.rejectValue(DATA_HOLDER_FIELD, INVALID_URI_ERR_CODE);
					return;
				}
			}

			if (form.getDataHolderType() == BYTESTREAM) {

				/* URL is valid but type is incorrect, probably due to previous value, so reject */
				if (validURL) {
					errors.rejectValue(DATA_HOLDER_FIELD, FILE_REQ_ERR_CODE);
					return;
				}

				/* Check the size does not exceed max (most likely caught before validation) */
				if (dataHolder != null && dataHolder.length() > maxUploadSize.longValue()) {
					errors.rejectValue(DATA_HOLDER_FIELD, MAX_UPLOAD_SIZE_ERR_CODE);
					return;
				}

			}
		}

	}
}
