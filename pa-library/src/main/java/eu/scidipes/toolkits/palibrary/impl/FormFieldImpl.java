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
package eu.scidipes.toolkits.palibrary.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.persistence.annotations.IdValidation;
import org.eclipse.persistence.annotations.PrimaryKey;

import eu.scidipes.toolkits.palibrary.exceptions.PreservationInitialisationException;
import eu.scidipes.toolkits.palibrary.interfaces.Form;
import eu.scidipes.toolkits.palibrary.interfaces.FormField;

/**
 * @author Simon Berriman
 * @author Tom Bunting
 */
@Entity
@Table(name = "form_field")
@PrimaryKey(validation = IdValidation.NONE)
public class FormFieldImpl implements FormField {

	private static final Logger LOG = Logger.getLogger(FormFieldImpl.class);

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/**
	 * Uses a template to create a new FormFieldImpl instance in the given parent form. The template field must not be
	 * persisted, i.e. it must come from a template bundle.
	 * 
	 * @param template
	 * @param parentForm
	 * @return a new FormFieldImpl instance based on the template
	 * @throws PreservationInitialisationException
	 * @throws IllegalArgumentException
	 *             if template or parentForm arguments are null
	 * @throws IllegalStateException
	 *             if the template has a fieldID set (i.e. has been persisted)
	 */
	public static FormFieldImpl copy(final FormFieldImpl template, final Form parentForm)
			throws PreservationInitialisationException {

		if (template == null || parentForm == null) {
			throw new IllegalArgumentException("template and parentForm parameters must not be null");
		}

		if (template.getFieldID() != null) {
			throw new IllegalStateException(
					"template form field must not have already been persisted (fieldID found to be not-null)");
		}

		final FormFieldImpl copy = new FormFieldImpl(template.getDisplayName(), parentForm);
		copy.setHelpText(template.getHelpText());
		copy.setMax(template.getMax());
		copy.setMin(template.getMin());
		copy.setDefaultValue(template.getDefaultValue());

		LOG.debug("Created copy of FormField instance: " + template);

		return copy;
	}

	@Column(name = "default_value")
	private String defaultValue;

	@Column(name = "display_name")
	private String displayName;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "field_id")
	private Integer fieldID;

	@Column(name = "help_text")
	private String helpText;

	@Column(name = "max_value")
	private String max;

	@Column(name = "min_value")
	private String min;

	@ManyToOne(targetEntity = FormImpl.class)
	@JoinColumn(name = "form_id", referencedColumnName = "form_id")
	private Form parentForm;

	@Column(name = "field_value")
	private String value;

	/**
	 * No-arg constructor required by JPA
	 */
	public FormFieldImpl() {
		/* No action */
	}

	/**
	 * Creates a new FormField with a display name and a parent form
	 * 
	 * @param displayName
	 * @param parentForm
	 * @throws PreservationInitialisationException
	 * 
	 */
	public FormFieldImpl(final String displayName, final Form parentForm) throws PreservationInitialisationException {

		if (StringUtils.trimToNull(displayName) == null) {
			throw new PreservationInitialisationException("Form field displayName must not be null or empty");
		}

		if (parentForm == null) {
			throw new PreservationInitialisationException("Form Field must specify valid parent");
		}

		this.displayName = displayName;
		this.parentForm = parentForm;
	}

	/**
	 * @param o
	 * @return
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final FormField o) {
		final int left = getFieldID() != null ? getFieldID().intValue() : Integer.MAX_VALUE;
		final int right = o.getFieldID() != null ? o.getFieldID().intValue() : Integer.MIN_VALUE;
		return Integer.signum(left - right);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof FormFieldImpl)) {
			return false;
		}

		final FormFieldImpl otherFormField = (FormFieldImpl) o;

		/* If this instance has been persisted we can compare by fieldID */
		if (getFieldID() != null) {
			return getFieldID().equals(otherFormField.getFieldID());
		}

		return parentForm.equals(otherFormField.getParentForm()) && displayName.equals(otherFormField.displayName);
	}

	/**
	 * Returns the defaultValue
	 * 
	 * @return the defaultValue
	 */
	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Returns the displayName
	 * 
	 * @return the displayName
	 */
	@Override
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns the fieldID
	 * 
	 * @return the fieldID
	 */
	@Override
	public Integer getFieldID() {
		return fieldID;
	}

	/**
	 * Returns the helpText
	 * 
	 * @return the helpText
	 */
	@Override
	public String getHelpText() {
		return helpText;
	}

	/**
	 * Returns the max
	 * 
	 * @return the max
	 */
	@Override
	public String getMax() {
		return max;
	}

	/**
	 * Returns the min
	 * 
	 * @return the min
	 */
	@Override
	public String getMin() {
		return min;
	}

	/**
	 * Returns the parentForm
	 * 
	 * @return the parentForm
	 */
	@Override
	public Form getParentForm() {
		return parentForm;
	}

	/**
	 * Returns the value
	 * 
	 * @return the value
	 */
	@Override
	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result *= 31 + (fieldID != null ? fieldID.intValue() : 0);
		result *= 31 + displayName.hashCode();
		return 31 * result + parentForm.hashCode();
	}

	/**
	 * Sets the defaultValue
	 * 
	 * @param defaultValue
	 *            the defaultValue to set
	 */
	public final void setDefaultValue(final String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Sets the helpText
	 * 
	 * @param helpText
	 *            the helpText to set
	 */
	public final void setHelpText(final String helpText) {
		this.helpText = helpText;
	}

	/**
	 * Sets the max
	 * 
	 * @param max
	 *            the max to set
	 */
	public void setMax(final String max) {
		this.max = max;
	}

	/**
	 * Sets the min
	 * 
	 * @param min
	 *            the min to set
	 */
	public void setMin(final String min) {
		this.min = min;
	}

	/**
	 * Sets the value
	 * 
	 * @param value
	 *            the value to set
	 */
	@Override
	public final void setValue(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("FormField '%s: %s' [ID: %s]", parentForm.getName(), displayName,
				fieldID != null ? fieldID : " - ");
	}
}
