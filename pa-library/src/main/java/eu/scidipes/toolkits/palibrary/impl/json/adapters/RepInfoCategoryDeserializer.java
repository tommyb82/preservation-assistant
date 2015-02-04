package eu.scidipes.toolkits.palibrary.impl.json.adapters;

import info.digitalpreserve.interfaces.RepInfoCategory;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import eu.scidipes.common.framework.FrameworkWrapper;
import eu.scidipes.toolkits.palibrary.exceptions.PARuntimeException;

/**
 * Deserialiser for correct creation and caching of a RepInfo Category within the Framework
 * 
 * @author Simon Berriman
 */
public class RepInfoCategoryDeserializer implements JsonDeserializer<RepInfoCategory> {

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
	public RepInfoCategory deserialize(final JsonElement json, final Type typeOfT,
			final JsonDeserializationContext context) throws JsonParseException {

		try {
			final JsonElement nameElem = json.getAsJsonObject().get("name");
			if (nameElem == null) {
				throw new PARuntimeException("Bad RepInfo Category");
			}

			final String catName = nameElem.getAsString();
			return FrameworkWrapper.getRepInfoCategoryByName(catName);

		} catch (final Exception e) {
			throw new JsonParseException(e);
		}
	}
}
