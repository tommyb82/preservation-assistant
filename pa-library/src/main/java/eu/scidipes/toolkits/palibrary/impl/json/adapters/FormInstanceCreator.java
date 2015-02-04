package eu.scidipes.toolkits.palibrary.impl.json.adapters;

import java.lang.reflect.Type;

import com.google.gson.InstanceCreator;

import eu.scidipes.toolkits.palibrary.impl.FormImpl;
import eu.scidipes.toolkits.palibrary.interfaces.Form;

/**
 * GSON instance creator to link {@link Form} to {@link FormImpl}
 * 
 * @author Simon Berriman
 */
public class FormInstanceCreator implements InstanceCreator<FormImpl> {

	/**
	 * @param type
	 * @return
	 * @see com.google.gson.InstanceCreator#createInstance(java.lang.reflect.Type)
	 */
	@Override
	public FormImpl createInstance(final Type type) {
		return new FormImpl();
	}
}
