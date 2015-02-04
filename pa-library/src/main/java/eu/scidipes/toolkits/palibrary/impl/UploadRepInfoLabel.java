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
package eu.scidipes.toolkits.palibrary.impl;

import static eu.scidipes.toolkits.palibrary.utils.MiscUtils.HASH_INIT;
import static eu.scidipes.toolkits.palibrary.utils.MiscUtils.HASH_PRIME;
import static java.lang.String.format;
import static org.apache.commons.lang.Validate.notNull;
import info.digitalpreserve.exceptions.RIException;
import info.digitalpreserve.interfaces.AndRepInfoGroup;
import info.digitalpreserve.interfaces.CurationPersistentIdentifier;
import info.digitalpreserve.interfaces.Manifest;
import info.digitalpreserve.interfaces.OtherRepresentationInformation;
import info.digitalpreserve.interfaces.RegistryObjectType;
import info.digitalpreserve.interfaces.RepInfoGroup;
import info.digitalpreserve.interfaces.RepInfoLabel;
import info.digitalpreserve.interfaces.RepresentationInformation;
import info.digitalpreserve.interfaces.SemanticRepInfo;
import info.digitalpreserve.interfaces.StructureRepInfo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.scidipes.common.framework.core.RegistryStorageUtils;
import eu.scidipes.common.framework.core.impl.BaseRepresentationInformation;
import eu.scidipes.common.framework.core.impl.CoreOtherRepInfo;
import eu.scidipes.common.framework.core.impl.CoreSemanticRepInfo;
import eu.scidipes.common.framework.core.impl.CoreStructureRepInfo;
import eu.scidipes.common.framework.core.impl.ModelObjectFactory;
import eu.scidipes.common.framework.core.impl.XMLRepInfoLabel;
import eu.scidipes.toolkits.palibrary.exceptions.PARuntimeException;

/**
 * <p>
 * Implementation of a Rep Info label for use by the PA. Serialises to XML conforming to the SCIDIP RIL schema.
 * 
 * <p>
 * Compatible with known APA Web and ACS-style registry implementations. Provides methods that allow the instance to be
 * straightforwardly 'worked on' by a client, i.e. to add / remove RI repeatedly.
 * 
 * @see <a
 *      href="http://registry2.scidip-es.eu/repository/SCIDIP/ril_1.1.xsd">http://registry2.scidip-es.eu/repository/SCIDIP/ril_1.1.xsd</a>
 * 
 * @author Tom Bunting
 * 
 */
public class UploadRepInfoLabel extends BaseRepresentationInformation implements RepInfoLabel, Serializable {

	private static final long serialVersionUID = 1L;

	public static enum CoreRIType {

		OTHER {
			@Override
			public Class<? extends RepresentationInformation> getType() {
				return OtherRepresentationInformation.class;
			}

			@Override
			public BaseRepresentationInformation newInstance() {
				return new CoreOtherRepInfo();
			}

			@Override
			public String toString() {
				return "Other RepInfo";
			}
		},
		SEMANTIC {
			@Override
			public Class<? extends RepresentationInformation> getType() {
				return SemanticRepInfo.class;
			}

			@Override
			public BaseRepresentationInformation newInstance() {
				return new CoreSemanticRepInfo();
			}

			@Override
			public String toString() {
				return "Semantic RepInfo";
			}
		},
		STRUCTURAL {
			@Override
			public Class<? extends RepresentationInformation> getType() {
				return StructureRepInfo.class;
			}

			@Override
			public BaseRepresentationInformation newInstance() {
				return new CoreStructureRepInfo();
			}

			@Override
			public String toString() {
				return "Structural RepInfo";
			}
		};

		private static final Map<Class<? extends BaseRepresentationInformation>, CoreRIType> MAP_CLASS_TO_ENUM = new HashMap<>();

		static {
			for (final CoreRIType coreRIType : values()) {
				MAP_CLASS_TO_ENUM.put(coreRIType.newInstance().getClass(), coreRIType);
			}
		}

		public String getName() {
			return name();
		}

		public abstract Class<? extends RepresentationInformation> getType();

		public abstract BaseRepresentationInformation newInstance();

		public static CoreRIType fromClass(final Class<? extends RepresentationInformation> clazz) {
			return MAP_CLASS_TO_ENUM.get(clazz);
		}

		public static CoreRIType[] getValues() {
			return values();
		}

	}

	private static final Logger LOG = LoggerFactory.getLogger(UploadRepInfoLabel.class);

	private final String description;
	private final String displayname;
	private final Calendar lastModified = Calendar.getInstance();

	private final transient Set<RepresentationInformation> repInfoChildren = new HashSet<>();

	private Long version;

	/**
	 * Instantiates the instance from the passed XML ril string
	 * 
	 * @param repInfoStr
	 *            xml repreesntation of a RIL
	 * @throws PARuntimeException
	 *             if an error occurs during deserialization of the XML ril string
	 */
	public UploadRepInfoLabel(final String repInfoStr) {

		XMLRepInfoLabel xmlRIL = null;

		try {
			xmlRIL = new XMLRepInfoLabel(repInfoStr);
		} catch (final RIException e) {
			throw new PARuntimeException(e.getMessage(), e);
		}

		this.displayname = xmlRIL.getDisplayname();
		this.description = xmlRIL.getDescription();
		setVersion(xmlRIL.getVersion());
		setCpid(xmlRIL.getCpid());

		repInfoChildren.addAll(Arrays.asList(xmlRIL.getRepresentationInformationChildren()));
		xmlRIL = null;
	}

	/**
	 * Creates a new, empty instance setting the given parameters. Adds 3 empty containers for core structural, semantic
	 * or other rep info
	 * 
	 * @param displayname
	 *            the RIL display name
	 * @param description
	 *            the RIL description
	 * @param cpid
	 *            the RIL curation persistence identifer
	 * @throws IllegalArgumentException
	 *             if displayName or description are null or empty
	 * @throws NullPointerException
	 *             if cpid is null
	 */
	public UploadRepInfoLabel(final String displayname, final String description,
			final CurationPersistentIdentifier cpid) {

		if (StringUtils.isBlank(displayname)) {
			throw new IllegalArgumentException("displayName must not be null, empty or whitespace");
		}

		if (StringUtils.isBlank(description)) {
			throw new IllegalArgumentException("description must not be null, empty or whitespace");
		}

		notNull(cpid, "cpid must not be null");

		this.displayname = displayname;
		this.description = description;
		setCpid(cpid);
		setVersion(Long.valueOf(1L));

		/* Create 3 empty child repinfo instances: */
		repInfoChildren.add(ModelObjectFactory.getStructureRepInfo());
		repInfoChildren.add(ModelObjectFactory.getSemanticRepInfo());
		repInfoChildren.add(ModelObjectFactory.getOtherRepInfo());
	}

	private void addRepInfoHelper(final RepresentationInformation newRepInfo, final CoreRIType type) {

		for (final RepresentationInformation repInfo : repInfoChildren) {

			if (type.getType().isAssignableFrom(repInfo.getClass())) {

				if (repInfo.getRepresentationInformation() == null) {

					/* Add a new 'and' container */
					final AndRepInfoGroup andGroup = ModelObjectFactory.getAndRepInfoGroup();
					repInfo.setRepresentationInformation(andGroup);
				}

				final RepInfoGroup repInfoGroup = (RepInfoGroup) repInfo.getRepresentationInformation();

				RepresentationInformation[] newGrandChildren = null;

				if (newRepInfo instanceof RepInfoGroup) {
					newGrandChildren = ((RepInfoGroup) newRepInfo).getRepresentationInformationChildren();
				} else {
					newGrandChildren = new RepresentationInformation[] { newRepInfo };
				}

				/* Merge the two arrays and set: */
				final RepresentationInformation[] currentGrandChildren = repInfoGroup
						.getRepresentationInformationChildren();

				final RepresentationInformation[] newGrandkids = (RepresentationInformation[]) ArrayUtils.addAll(
						currentGrandChildren, newGrandChildren);

				repInfoGroup.setRepresentationInformationChildren(newGrandkids);

			}

		}

	}

	public UploadRepInfoLabel addRepInfo(final Manifest manifest, final CoreRIType coreRIType) {
		final BaseRepresentationInformation ri = coreRIType.newInstance();
		manifestToRI(manifest, ri);
		addRepInfoHelper(ri, coreRIType);
		return this;
	}

	public UploadRepInfoLabel addRepInfo(final RepresentationInformation repInfo, final CoreRIType coreRIType) {
		addRepInfoHelper(repInfo, coreRIType);
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof UploadRepInfoLabel)) {
			return false;
		}
		final UploadRepInfoLabel otherRIL = (UploadRepInfoLabel) o;

		return serialiseToString().equals(otherRIL.serialiseToString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see info.digitalpreserve.interfaces.RepInfoLabel#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see info.digitalpreserve.interfaces.RepInfoLabel#getDisplayname()
	 */
	@Override
	public String getDisplayname() {
		return displayname;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see info.digitalpreserve.interfaces.RepInfoLabel#getLastModified()
	 */
	@Override
	public Calendar getLastModified() {
		return lastModified;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see info.digitalpreserve.interfaces.RegistryObject#getRegistryObjectType()
	 */
	@Override
	public RegistryObjectType getRegistryObjectType() {
		return RegistryObjectType.RIL;
	}

	@Override
	public RepresentationInformation[] getRepresentationInformationChildren() {
		return repInfoChildren.toArray(new RepresentationInformation[] {});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see info.digitalpreserve.interfaces.RepInfoLabel#getVersion()
	 */
	@Override
	public Long getVersion() {
		return version;
	}

	@Override
	public int hashCode() {
		return HASH_INIT * HASH_PRIME + getCpid().hashCode();
	}

	@Override
	public boolean isGroupEmpty() {
		return repInfoChildren.isEmpty();
	}

	/**
	 * Sets the CPID, RIL type and categories from the passed {@link Manifest} into the ri
	 * 
	 * @param manifest
	 * @param ri
	 */
	private void manifestToRI(final Manifest manifest, final BaseRepresentationInformation ri) {
		ri.setCpid(manifest.getManifestCpid());
		ri.setTypeCpid(manifest.getRILCpid());
		ri.getCategories().addAll(manifest.getCategories());
	}

	/**
	 * Remove a manifest from the specified core repinfo section in the label
	 * 
	 * @param manifest
	 * @param coreRIType
	 * @return this, for method chaining
	 */
	public UploadRepInfoLabel removeRepInfo(final Manifest manifest, final CoreRIType coreRIType) {
		final BaseRepresentationInformation ri = coreRIType.newInstance();
		manifestToRI(manifest, ri);
		removeRepInfoHelper(ri, coreRIType);
		return this;
	}

	/**
	 * Removes repinfo from the specified core section in the label
	 * 
	 * @param ri
	 *            to be removed
	 * @param coreRIType
	 * @return this, for method chaining
	 */
	public UploadRepInfoLabel removeRepInfo(final RepresentationInformation ri, final CoreRIType coreRIType) {
		removeRepInfoHelper(ri, coreRIType);
		return this;
	}

	private void removeRepInfoHelper(final RepresentationInformation riToRemove, final CoreRIType type) {

		for (final RepresentationInformation repInfo : repInfoChildren) {

			if (type.getType().isAssignableFrom(repInfo.getClass()) && repInfo.getRepresentationInformation() != null) {

				final RepInfoGroup repInfoGroup = (RepInfoGroup) repInfo.getRepresentationInformation();
				final RepresentationInformation[] currentGrandChildren = repInfoGroup
						.getRepresentationInformationChildren();

				final int idx = ArrayUtils.indexOf(currentGrandChildren, riToRemove);

				LOG.debug("Index of riToRemove in currentGrandChildren is: " + idx);

				if (idx > -1) {
					repInfoGroup.setRepresentationInformationChildren((RepresentationInformation[]) ArrayUtils.remove(
							currentGrandChildren, idx));
					LOG.debug(format("riToRemove [%s] at index [%d] was removed from %s core RI", riToRemove,
							Integer.valueOf(idx), type.toString()));
				} else {
					LOG.debug(format("riToRemove [%s] was not found in %s core RI", riToRemove, type.toString()));
				}
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see info.digitalpreserve.interfaces.RepInfoLabel#serialiseToString()
	 */
	@Override
	public String serialiseToString() {
		return RegistryStorageUtils.rilToXMLString(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see info.digitalpreserve.interfaces.RepInfoLabel#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(final String description) {
		throw new UnsupportedOperationException("Property description cannot be updated");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see info.digitalpreserve.interfaces.RepInfoLabel#setDisplayname(java.lang.String)
	 */
	@Override
	public void setDisplayname(final String displayname) {
		throw new UnsupportedOperationException("Property displayname cannot be updated");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see info.digitalpreserve.interfaces.RepInfoLabel#setLastModified(java.util.Calendar)
	 */
	@Override
	public void setLastModified(final Calendar lastModified) {
		throw new UnsupportedOperationException("Cannot set lastModified for new RILs, it is set internally");
	}

	/**
	 * Method has no affect in this implementation other then to log a warning.
	 */
	@Override
	public void setRepresentationInformationChildren(final RepresentationInformation[] ri) {
		LOG.warn("Dropping attempt to set all rep info children; this operation is not supported in PreservationRepInfoLabel");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see info.digitalpreserve.interfaces.RepInfoLabel#setVersion(java.lang.Long)
	 */
	@Override
	public void setVersion(final Long version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return String.format("%s [%s]", getClass().getSimpleName(), getCpid());
	}

}
