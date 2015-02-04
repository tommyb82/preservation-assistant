package eu.scidipes.toolkits.palibrary.impl.json.adapters;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import eu.scidipes.toolkits.palibrary.impl.FormImpl;
import eu.scidipes.toolkits.palibrary.interfaces.Form;

/**
 * Deserialiser for JSON arrays of {@link Form} objects, such that the correct implementation type is used.
 * 
 * @author Simon Berriman
 */
public class FormListDeserializer implements JsonDeserializer<List<Form>> {

	/**
	 * @param json
	 * @param typeOfT
	 * @param context
	 * @return
	 * @throws JsonParseException
	 * @see com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type,
	 *      com.google.gson.JsonDeserializationContext)
	 */
	@Override
	public List<Form> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
			throws JsonParseException {

		// Get input JSON array
		final JsonArray ja = json.getAsJsonArray();
		// Create equally sized output array of interface objects
		final Form[] fa = new Form[ja.size()];

		int idx = 0;
		for (final JsonElement je : ja) {
			// Sequentially use default deserialiser for each object in the array, with the type set to the correct
			// interface implementation
			fa[idx++] = context.deserialize(je, FormImpl.class);
		}

		// Return as a fixed sized list
		return Arrays.asList(fa);
	}
}
