package eu.scidipes.toolkits.palibrary.impl.json.adapters;

import info.digitalpreserve.interfaces.CurationPersistentIdentifier;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import eu.scidipes.common.framework.FrameworkWrapper;

/**
 * Deserialiser for correct creation and caching of a CPID within the Framework
 * 
 * @author Simon Berriman
 */
public class CPIDDeserializer implements JsonDeserializer<CurationPersistentIdentifier> {

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
	public CurationPersistentIdentifier deserialize(final JsonElement json, final Type typeOfT,
			final JsonDeserializationContext context) throws JsonParseException {

		final JsonElement cpidStrElem = json.getAsJsonObject().get("cpidStr");
		if (cpidStrElem != null) {
			return FrameworkWrapper.getCPID(cpidStrElem.getAsString());
		} else {
			throw new JsonParseException("Bad RIL CPID");
		}
	}
}
