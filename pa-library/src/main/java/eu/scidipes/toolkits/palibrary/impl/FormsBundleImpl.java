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

import static eu.scidipes.toolkits.palibrary.utils.MiscUtils.HASH_INIT;
import static eu.scidipes.toolkits.palibrary.utils.MiscUtils.HASH_PRIME;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import eu.scidipes.toolkits.palibrary.exceptions.PreservationInitialisationException;
import eu.scidipes.toolkits.palibrary.interfaces.Bundle;
import eu.scidipes.toolkits.palibrary.interfaces.Form;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;

/**
 * @author Simon Berriman
 * @author Tom Bunting
 */
@Entity
@Table(name = "form_bundle")
public class FormsBundleImpl implements FormsBundle {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	@Column(name = "bundle_name")
	private String bundleName;

	@Id
	@Column(name = "dataset_name")
	private String datasetName;

	@Column(name = "display_name")
	private String displayName;

	@OneToMany(mappedBy = "parentBundle", targetEntity = FormImpl.class, cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<Form> forms = new ArrayList<>();

	@Column(name = "processor_name")
	private String processorName;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "dataset_name", referencedColumnName = "dataset_name", nullable = false)
	private final Set<DatasetRIL> rils = new HashSet<>();

	/**
	 * Searches this datasets collection of rils for a match based on the passed ril CPID <code>String</code>
	 * 
	 * @param rilCPID
	 *            the rilCPID to search with
	 * @return the matching ril, if found, otherwise null
	 */
	public DatasetRIL getDatasetRIL(final String rilCPID) {

		notEmpty(rilCPID, "rilCPID argument must not be null or empty");

		for (final DatasetRIL ril : rils) {
			if (ril.getRilCPID().equals(rilCPID)) {
				return ril;
			}
		}
		return null;
	}

	/**
	 * Removes a ril from the collection
	 * 
	 * @param ril
	 *            to be removed
	 * @return true if the ril was removed
	 */
	public boolean deleteRIL(final DatasetRIL ril) {
		notNull(ril, "ril to be deleted must not be null");
		return rils.remove(ril);
	}

	/**
	 * Adds a ril to the collection
	 * 
	 * @param ril
	 * @return true if the ril was added
	 */
	public boolean addRil(final DatasetRIL ril) {
		notNull(ril, "ril to be added must not be null");
		return rils.add(ril);
	}

	/** Only used at the template bundle level, not persisted for each data set */
	@Transient
	private String templateSource;

	/**
	 * No-arg constructor required by JPA. DO NOT CALL from user code
	 */
	protected FormsBundleImpl() {
		/* No action */
	}

	public FormsBundleImpl(final String bundleName, final String processorName)
			throws PreservationInitialisationException {

		if (StringUtils.isBlank(bundleName) || StringUtils.isBlank(processorName)) {
			throw new PreservationInitialisationException(
					"bundleName and processorName must be supplied and must not be null or empty");
		}

		this.bundleName = bundleName.trim();
		this.processorName = processorName.trim();
	}

	/**
	 * Adds a form to the collection if it does not already exist.
	 * 
	 * @param form
	 *            to be added
	 * @return true (as specified by {@link Collection#add})
	 */
	public boolean addForm(final Form form) {
		if (form != null && !forms.contains(form)) {
			return forms.add(form);
		}
		throw new IllegalArgumentException("The form to be added must not exist in the list and must not be null");
	}

	/**
	 * Consistent with {@link FormsBundleImpl#equals(Object)}, compares first by bundle name and if that is found to be
	 * equal, compares by datasetName properties.
	 * 
	 * @param otherBundle
	 * @return
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final Bundle otherBundle) {
		final int bundleCompare = bundleName.compareTo(otherBundle.getBundleName());

		if (bundleCompare != 0) {
			return bundleCompare;
		}
		/* Same bundle so check datasetName */
		if (datasetName != null) {
			return datasetName.compareTo(otherBundle.getDatasetName());
		}

		/* Same non-persisted bundle instances */
		return 0;
	}

	/**
	 * Removes an element from the forms collection
	 * 
	 * @param form
	 *            to be deleted
	 * @return true if the element was found and removed from the list
	 */
	public boolean deleteForm(final Form form) {
		Validate.notNull(form, "argument form must not be null");
		return forms.remove(form);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof FormsBundle)) {
			return false;
		}
		final FormsBundle otherBundle = (FormsBundle) o;

		if (datasetName == null) {
			/* Not yet persisted */
			return bundleName.equals(otherBundle.getBundleName());
		} else {
			/* Persisted */
			return bundleName.equals(otherBundle.getBundleName()) && datasetName.equals(otherBundle.getDatasetName());
		}

	}

	/**
	 * Returns the bundleID
	 * 
	 * @return the bundleID
	 */
	@Override
	public String getBundleName() {
		return bundleName;
	}

	@Override
	public String getDatasetName() {
		return datasetName;
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
	 * Returns an structurally unmodifiable list of forms
	 * 
	 * @return the forms, wrapped in {@link Collections#unmodifiableList(List)}
	 */
	@Override
	public List<Form> getForms() {
		return Collections.unmodifiableList(forms);
	}

	@Override
	public String getProcessorName() {
		return processorName;
	}

	/**
	 * Returns an structurally unmodifiable <code>Set</code> of dataset rils
	 * 
	 * @return the rils, wrapped in {@link Collections#unmodifiableSet(Set)}
	 */
	@Override
	public final Set<DatasetRIL> getRils() {
		return Collections.unmodifiableSet(rils);
	}

	@Override
	public String getTemplateSource() {
		return templateSource == null ? "" : templateSource;
	}

	@Override
	public int hashCode() {
		int result = HASH_INIT;
		result *= HASH_PRIME + (datasetName != null ? datasetName.hashCode() : 0);
		return HASH_PRIME * result + bundleName.hashCode();
	}

	/**
	 * Sets the datasetName
	 * 
	 * @param datasetName
	 *            the datasetName to set
	 */
	public final void setDatasetName(final String datasetName) {
		if (StringUtils.isBlank(datasetName)) {
			throw new IllegalArgumentException("datasetName must not be null or empty");
		}
		this.datasetName = datasetName;
	}

	/**
	 * Sets the displayName
	 * 
	 * @param displayName
	 *            the displayName to set
	 */
	public final void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Replaces a {@link Form} instance in the collection with the given form instance
	 * 
	 * @param form
	 *            to be replaced in the collection
	 * @see List#set(int, Object)
	 */
	public void setForm(final Form form) {
		if (form != null && forms.contains(form)) {
			forms.set(forms.indexOf(form), form);
		}
	}

	/**
	 * Sets a value for the transient (non-persisted) templateSource property
	 * 
	 * @param templateSource
	 *            the templateSource to set
	 */
	public final void setTemplateSource(final String templateSource) {
		this.templateSource = templateSource;
	}

	@Override
	public String toString() {
		// Forces NPE if bundleName is null
		return String.format("Bundle: '%s', Dataset: '%s'", bundleName, datasetName != null ? datasetName : "-");
	}
}
