package eu.scidipes.toolkits.palibrary.impl.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * JSON field exclusion strategy to not follow fields pointing back to their parent objects. This is accomplished rather
 * naïvely by simply looking for fields whose names begin with the string 'parent'. This is sufficient for the PA's use.
 * 
 * @author Simon Berriman
 */
public class ParentExclusionStrategy implements ExclusionStrategy {

	public ParentExclusionStrategy() {
		// No op
	}

	/**
	 * Excludes fields whose name begins with 'parent'. Comparison is case insensitive.
	 * 
	 * @param f
	 *            Java field attributes
	 * @return <code>true</code> if field name begins with 'parent'
	 * @see com.google.gson.ExclusionStrategy#shouldSkipField(com.google.gson.FieldAttributes)
	 */
	@Override
	public boolean shouldSkipField(final FieldAttributes f) {
		return f.getName().toLowerCase().startsWith("parent");
	}

	/**
	 * Returns <code>false</code>. This exclusion strategy doesn't skip on the basis of return type
	 * 
	 * @param clazz
	 * @return false
	 * @see com.google.gson.ExclusionStrategy#shouldSkipClass(java.lang.Class)
	 */
	@Override
	public boolean shouldSkipClass(final Class<?> clazz) {
		return false;
	}
}
