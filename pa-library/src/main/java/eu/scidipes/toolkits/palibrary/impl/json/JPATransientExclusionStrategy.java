package eu.scidipes.toolkits.palibrary.impl.json;

import javax.persistence.Transient;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * JSON field exclusion strategy for JPA 'Transient' annotated fields
 * 
 * @author Simon Berriman
 */
public class JPATransientExclusionStrategy implements ExclusionStrategy {

	public JPATransientExclusionStrategy() {
		// No op
	}

	/**
	 * Excludes JPA 'Transient' annotated fields
	 * 
	 * @param f
	 *            Java field attributes
	 * @return <code>true</code> if JPA 'Transient' annotated field
	 * @see com.google.gson.ExclusionStrategy#shouldSkipField(com.google.gson.FieldAttributes)
	 */
	@Override
	public boolean shouldSkipField(final FieldAttributes f) {
		return f.getAnnotation(Transient.class) != null;
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
