/**
 *
 */
package eu.scidipes.toolkits.palibrary.interfaces;

import info.digitalpreserve.interfaces.CurationPersistentIdentifier;
import info.digitalpreserve.interfaces.RepInfoCategory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents an individual within an ontology that will be preserved in a registry as a manifest.
 * 
 * @author Simon Berriman
 * @author Tom Bunting
 */
public interface Form extends Comparable<Form>, Preservable, Serializable {

	FormsBundle getParentBundle();

	Integer getFormID();

	int getGroupOrder();

	String getName();

	String getDisplayName();

	String getItemFileName();

	String getIntroText();

	String getDataHolder();

	String getGroup();

	FormType getType();

	FormFieldType getDataHolderType();

	Map<CoreFieldMetadata, String> getDataHolderMetadata();

	List<FormField> getFormFields();

	Set<RepInfoCategory> getCategories();

	CurationPersistentIdentifier getRILCPID();

	CurationPersistentIdentifier getManifestCPID();

	boolean isPreserved();
}
