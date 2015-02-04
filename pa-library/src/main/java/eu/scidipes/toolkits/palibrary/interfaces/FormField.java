/**
 *
 */
package eu.scidipes.toolkits.palibrary.interfaces;

import java.io.Serializable;

/**
 * @author Simon Berriman
 */
public interface FormField extends Comparable<FormField>, Serializable {

	Form getParentForm();

	Integer getFieldID();

	String getDisplayName();

	String getHelpText();

	String getDefaultValue();

	String getValue();

	String getMax();

	String getMin();

	void setValue(String value);

}
