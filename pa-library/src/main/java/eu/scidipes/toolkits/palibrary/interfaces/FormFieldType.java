/**
 *
 */
package eu.scidipes.toolkits.palibrary.interfaces;

import java.io.Serializable;

/**
 * Expected field types required for Registry objects
 * 
 * @author Simon Berriman
 */
public enum FormFieldType implements Comparable<FormFieldType>, Serializable {
	/** Plain text field */
	TEXT,
	/** URI-validated text field */
	URI,
	/** InputStream of bytes */
	BYTESTREAM,
	/** Date format validated text field */
	DATETIME,
	/** List of plain text values */
	CSVLIST;
}
