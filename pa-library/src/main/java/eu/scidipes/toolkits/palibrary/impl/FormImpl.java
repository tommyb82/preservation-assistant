package eu.scidipes.toolkits.palibrary.impl;

import static eu.scidipes.toolkits.palibrary.utils.MiscUtils.HASH_INIT;
import static eu.scidipes.toolkits.palibrary.utils.MiscUtils.HASH_PRIME;
import static org.apache.commons.lang.Validate.notNull;
import info.digitalpreserve.interfaces.CurationPersistentIdentifier;
import info.digitalpreserve.interfaces.RepInfoCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.eclipse.persistence.annotations.IdValidation;
import org.eclipse.persistence.annotations.PrimaryKey;

import com.rits.cloning.Cloner;

import eu.scidipes.toolkits.palibrary.exceptions.PreservationInitialisationException;
import eu.scidipes.toolkits.palibrary.impl.jpa.CPIDConverter;
import eu.scidipes.toolkits.palibrary.impl.jpa.CategoryConverter;
import eu.scidipes.toolkits.palibrary.interfaces.CoreFieldMetadata;
import eu.scidipes.toolkits.palibrary.interfaces.Form;
import eu.scidipes.toolkits.palibrary.interfaces.FormField;
import eu.scidipes.toolkits.palibrary.interfaces.FormFieldType;
import eu.scidipes.toolkits.palibrary.interfaces.FormType;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;

/**
 * @author Simon Berriman
 * @author Tom Bunting
 */
@Entity
@Table(name = "form")
@PrimaryKey(validation = IdValidation.NONE)
public class FormImpl implements Form {

	private static final Logger LOG = Logger.getLogger(FormImpl.class);

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/**
	 * Uses a template to create a new FormImpl instance in the given parent bundle. The template form must not be
	 * persisted, i.e. it must come from a template bundle. All referenced objects are themselves deep-copied and
	 * updated appropriately to back-reference the new instance.
	 * 
	 * @param template
	 *            form to copy from
	 * @param parentBundle
	 *            parent bundle for copy
	 * @return a new FormImpl instance
	 * @throws PreservationInitialisationException
	 * @throws NullPointerException
	 *             if template or parentBundle arguments are null
	 * @throws IllegalStateException
	 *             if the template has a formID set (i.e. has been persisted)
	 */
	public static FormImpl copy(final FormImpl template, final FormsBundle parentBundle)
			throws PreservationInitialisationException {

		notNull(template, "template argument must not be null");
		notNull(parentBundle, "parentBundle argument must not be null");

		if (template.getFormID() != null) {
			throw new IllegalStateException(
					"template form must not have already been persisted (formID found to be not-null)");
		}

		final FormImpl copy = new FormImpl(template.getName(), parentBundle);
		copy.setCategories(template.getCategories());
		copy.setDataHolderType(template.getDataHolderType());
		copy.setDisplayName(template.getDisplayName());
		copy.setGroup(template.getGroup());
		copy.setGroupOrder(template.getGroupOrder());
		copy.setIntroText(template.getIntroText());
		copy.setType(template.getType());

		for (final FormField templateField : template.getFormFields()) {
			copy.getFormFields().add(FormFieldImpl.copy((FormFieldImpl) templateField, copy));
		}

		LOG.debug("Created copy of Form instance: " + template + " with parent bundle: " + parentBundle);

		return copy;
	}

	@ElementCollection
	@Column(name = "category_name")
	@CollectionTable(name = "form_categories", joinColumns = { @JoinColumn(name = "form_id", referencedColumnName = "form_id") })
	@Convert(converter = CategoryConverter.class)
	private final Set<RepInfoCategory> categories = new HashSet<>();

	@JsonIgnore
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "data_holder")
	private String dataHolder;

	@ElementCollection
	@MapKeyEnumerated(EnumType.STRING)
	@MapKeyColumn(name = "metadata_key")
	@Column(name = "metadata_value")
	@CollectionTable(name = "data_holder_metadata", joinColumns = { @JoinColumn(name = "form_id", referencedColumnName = "form_id") })
	private final Map<CoreFieldMetadata, String> dataHolderMetadata = new HashMap<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "data_holder_type")
	/** Default to file upload type */
	private FormFieldType dataHolderType = FormFieldType.BYTESTREAM;

	@Column(name = "display_name")
	private String displayName;

	@JsonIgnore
	@OneToMany(mappedBy = "parentForm", targetEntity = FormFieldImpl.class, cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<FormField> formFields = new ArrayList<>();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "form_id")
	private Integer formID;

	@Column(name = "form_group")
	/** Used for display purposes */
	private String group;

	@Column(name = "group_order")
	private int groupOrder;

	@Column(name = "intro_text")
	private String introText = "";

	@Column(name = "item_file_name")
	private String itemFileName = "";

	@Column(name = "manifest_cpid")
	@Convert(converter = CPIDConverter.class)
	private CurationPersistentIdentifier manifestCPID;

	@Column(name = "form_name")
	private String name;

	@JsonIgnore
	@ManyToOne(targetEntity = FormsBundleImpl.class)
	@JoinColumn(name = "dataset_name", referencedColumnName = "dataset_name")
	private FormsBundle parentBundle;

	@Basic
	private boolean preserved = false;

	@Column(name = "ril_cpid")
	@Convert(converter = CPIDConverter.class)
	private CurationPersistentIdentifier rilCPID;

	@Enumerated(EnumType.STRING)
	@Column(name = "form_type")
	/** No default for the form type */
	private FormType type;

	/**
	 * No-arg constructor required by JPA
	 */
	public FormImpl() {
		/* No action */
	}

	/**
	 * Creates a new Form with a name and a parent bundle
	 * 
	 * @param name
	 * @param parentBundle
	 * @throws PreservationInitialisationException
	 * 
	 */
	public FormImpl(final String name, final FormsBundle parentBundle) throws PreservationInitialisationException {

		if (StringUtils.trimToNull(name) == null) {
			throw new PreservationInitialisationException("Form name must not be null or empty");
		}

		if (parentBundle == null) {
			throw new PreservationInitialisationException("Form must specify valid parent");
		}

		this.name = name;
		this.parentBundle = parentBundle;
	}

	/**
	 * Compares this against another form instance. If the forms are in different bundles, comparison is only made
	 * between bundles, otherwise it is based on the form name and if necessary the item file name. This is consistent
	 * with the implementations of equals and hashcode.
	 * 
	 * @param o
	 *            other form
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final Form o) {
		if (parentBundle.equals(o.getParentBundle())) {

			if (name.equals(o.getName())) {
				return itemFileName.compareTo(o.getItemFileName());
			} else {
				return name.compareTo(o.getName());
			}

		} else {
			return parentBundle.compareTo(o.getParentBundle());
		}
	}

	/**
	 * Tests for semantic equality against another Form instance. Two forms are considered equal when they are in the
	 * same bundle and have the same form ID
	 * 
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof FormImpl)) {
			return false;
		}

		final FormImpl otherForm = (FormImpl) o;

		/* If both instances have been persisted we can compare by formID */
		if (getFormID() != null && otherForm.getFormID() != null) {
			return getFormID().equals(otherForm.getFormID());
		}

		return parentBundle.equals(otherForm.getParentBundle()) && name.equals(otherForm.getName())
				&& itemFileName.equals(otherForm.getItemFileName());
	}

	/**
	 * Return the categories
	 * 
	 * @return the categories
	 */
	@Override
	public final Set<RepInfoCategory> getCategories() {
		return categories;
	}

	/**
	 * Returns the dataHolder
	 * 
	 * @return the dataHolder
	 */
	@Override
	public String getDataHolder() {
		return dataHolder;
	}

	/**
	 * Return the dataHolderMetaData
	 * 
	 * @return the dataHolderMetadata
	 */
	@Override
	public final Map<CoreFieldMetadata, String> getDataHolderMetadata() {
		return dataHolderMetadata;
	}

	/**
	 * Returns the dataHolderType
	 * 
	 * @return the dataHolderType
	 */
	@Override
	public FormFieldType getDataHolderType() {
		return dataHolderType;
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
	 * Returns the formFields
	 * 
	 * @return the formFields
	 */
	@Override
	public List<FormField> getFormFields() {
		return formFields;
	}

	/**
	 * Returns the formID
	 * 
	 * @return the formID
	 */
	@Override
	public Integer getFormID() {
		return formID;
	}

	/**
	 * Return the form group
	 * 
	 * @return the form group
	 */
	@Override
	public String getGroup() {
		return group;
	}

	/**
	 * @return the groupOrder
	 */
	@Override
	public final int getGroupOrder() {
		return groupOrder;
	}

	/**
	 * Returns the introText
	 * 
	 * @return the introText
	 */
	@Override
	public String getIntroText() {
		return introText;
	}

	/**
	 * Get the itemFileName
	 * 
	 * @return the itemFileName
	 */
	@Override
	public final String getItemFileName() {
		return itemFileName;
	}

	@Override
	public CurationPersistentIdentifier getManifestCPID() {
		return manifestCPID;
	}

	/**
	 * Return the form name
	 * 
	 * @return the name
	 */
	@Override
	public final String getName() {
		return name;
	}

	/**
	 * Returns the parentBundle
	 * 
	 * @return the parentBundle
	 */
	@Override
	public FormsBundle getParentBundle() {
		return parentBundle;
	}

	/**
	 * Returns the RIL CPID
	 * 
	 * @return the RIL CPID
	 */
	@Override
	public CurationPersistentIdentifier getRILCPID() {
		return rilCPID;
	}

	/**
	 * Returns the form type
	 * 
	 * @return the form type
	 */
	@Override
	public FormType getType() {
		return type;
	}

	/**
	 * Generates a hashcode based on the formID (if persisted) plus the other key attributes
	 * 
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = HASH_INIT;
		result *= HASH_PRIME + (formID != null ? formID.intValue() : 0);
		result *= HASH_PRIME + itemFileName.hashCode();
		result *= HASH_PRIME + name.hashCode();
		return HASH_PRIME * result + parentBundle.hashCode();
	}

	@Override
	public boolean isPreserved() {
		return preserved;
	}

	/**
	 * Resets the mutable properties of this form instance to their defaults. Method is synchronized on this instance.
	 */
	public synchronized void reset() {
		dataHolder = null;
		dataHolderMetadata.clear();
		itemFileName = "";
		manifestCPID = null;
		rilCPID = null;
		preserved = false;
		categories.clear();

		for (final FormField field : formFields) {
			((FormFieldImpl) field).setValue(null);
		}
	}

	/**
	 * Defensive public setter for categories, to support Spring data binding.
	 * 
	 * @param categories
	 */
	public final void setCategories(final Set<RepInfoCategory> categories) {
		final Set<RepInfoCategory> copyCats = new Cloner().deepClone(categories);
		synchronized (this.categories) {
			this.categories.clear();

			if (copyCats != null && !copyCats.isEmpty()) {
				this.categories.addAll(copyCats);
			}
		}
	}

	/**
	 * Sets the dataHolder
	 * 
	 * @param dataHolder
	 *            the dataHolder to set
	 */
	public final void setDataHolder(final String dataHolder) {
		if (StringUtils.trimToNull(dataHolder) != null) {
			this.dataHolder = dataHolder;
		}
	}

	/**
	 * Sets the dataHolderType
	 * 
	 * @param dataHolderType
	 *            the dataHolderType to set
	 */
	public final void setDataHolderType(final FormFieldType dataHolderType) {
		if (dataHolderType != null) {
			this.dataHolderType = dataHolderType;
		} else {
			throw new IllegalArgumentException("dataHolderType must not be null");
		}
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
	 * Set the group
	 * 
	 * @param group
	 *            the group to set
	 */
	public final void setGroup(final String group) {
		this.group = group;
	}

	/**
	 * Set the group order
	 * 
	 * @param groupOrder
	 *            the groupOrder to set
	 */
	public final void setGroupOrder(final int groupOrder) {
		this.groupOrder = groupOrder;
	}

	/**
	 * Sets the introText
	 * 
	 * @param introText
	 *            the introText to set
	 */
	public final void setIntroText(final String introText) {
		this.introText = introText;
	}

	/**
	 * Set the itemFileName
	 * 
	 * @param itemFileName
	 *            the itemFileName to set
	 */
	public final void setItemFileName(final String itemFileName) {
		this.itemFileName = itemFileName;
	}

	/**
	 * Set the CPID of for the manifest representing this form instance.
	 * 
	 * @param manifestCPID
	 *            the preservedManifestCPID to set
	 */
	public final void setManifestCPID(final CurationPersistentIdentifier manifestCPID) {
		this.manifestCPID = manifestCPID;
	}

	/**
	 * Sets the preserved flag indicating the manifest and digital object represented by this instance has been
	 * preserved to a registry-repository
	 * 
	 * @param preserved
	 *            the preserved to set
	 */
	public final void setPreserved(final boolean preserved) {
		this.preserved = preserved;
	}

	/**
	 * Sets the RIL CPID
	 * 
	 * @param rilCPID
	 *            the RIL CPID to set
	 */
	public void setRILCPID(final CurationPersistentIdentifier rilCPID) {
		this.rilCPID = rilCPID;
	}

	/**
	 * Set the type
	 * 
	 * @param type
	 *            the type to set
	 */
	public final void setType(final FormType type) {
		this.type = type;
	}

	/**
	 * @return the string representation of this form
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Form '%s' [ID: %s]", name, formID != null ? formID : " - ");
	}

	@Override
	public String getPreservableTypeID() {
		return "MF";
	}

}
