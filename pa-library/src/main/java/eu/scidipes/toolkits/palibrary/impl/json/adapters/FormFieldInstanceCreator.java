package eu.scidipes.toolkits.palibrary.impl.json.adapters;

import java.lang.reflect.Type;

import com.google.gson.InstanceCreator;

import eu.scidipes.toolkits.palibrary.impl.FormFieldImpl;
import eu.scidipes.toolkits.palibrary.interfaces.FormField;

/**
 * GSON instance creator to link {@link FormField} to {@link FormFieldImpl}
 * 
 * @author Simon Berriman
 */
public class FormFieldInstanceCreator implements InstanceCreator<FormField> {

	/**
	 * @param type
	 * @return
	 * @see com.google.gson.InstanceCreator#createInstance(java.lang.reflect.Type)
	 */
	@Override
	public FormField createInstance(final Type type) {
		return new FormFieldImpl();
	}
}
