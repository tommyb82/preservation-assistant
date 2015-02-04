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

import info.digitalpreserve.exceptions.RIException;
import info.digitalpreserve.interfaces.RepInfoCategory;

import java.text.ParseException;
import java.util.Locale;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang.StringUtils;
import org.springframework.format.Formatter;

import eu.scidipes.common.framework.FrameworkWrapper;
import eu.scidipes.toolkits.palibrary.exceptions.PARuntimeException;

/**
 * Dual use converter between {@link RepInfoCategory} and <code>String</code>. Implements both the JPA
 * {@link AttributeConverter} and Spring {@link Formatter} to avoid duplication of printing and parsing logic.
 * 
 * @author Tom Bunting
 * 
 */
@Converter
public class CategoryConverter implements AttributeConverter<RepInfoCategory, String>, Formatter<RepInfoCategory> {

	@Override
	public String convertToDatabaseColumn(final RepInfoCategory category) {
		return categoryToStringHelper(category);
	}

	@Override
	public RepInfoCategory convertToEntityAttribute(final String categoryName) {
		return stringToCategoryHelper(categoryName);
	}

	@Override
	public String print(final RepInfoCategory category, final Locale locale) {
		return categoryToStringHelper(category);
	}

	@Override
	public RepInfoCategory parse(final String categoryName, final Locale locale) throws ParseException {
		return stringToCategoryHelper(categoryName);
	}

	/**
	 * Creates a new {@link RepInfoCategory} from a category name <code>String</code>
	 * 
	 * @param categoryName
	 * @return a new category
	 */
	private RepInfoCategory stringToCategoryHelper(final String categoryName) {
		try {
			return StringUtils.isBlank(categoryName) ? null : FrameworkWrapper.getRepInfoCategoryByName(categoryName);

		} catch (final RIException e) {
			throw new PARuntimeException("Exception converting String [" + categoryName + "] into RepInfoCategory", e);
		}
	}

	/**
	 * Returns a <code>String</code> representing the name of the passed {@link RepInfoCategory}
	 * 
	 * @param category
	 * @return the category name
	 */
	private String categoryToStringHelper(final RepInfoCategory category) {
		return category == null ? "" : category.getName();
	}

}
