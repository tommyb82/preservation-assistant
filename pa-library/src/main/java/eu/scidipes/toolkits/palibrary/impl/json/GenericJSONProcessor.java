package eu.scidipes.toolkits.palibrary.impl.json;

import info.digitalpreserve.interfaces.CurationPersistentIdentifier;
import info.digitalpreserve.interfaces.RepInfoCategory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import eu.scidipes.toolkits.palibrary.exceptions.PreservationInitialisationException;
import eu.scidipes.toolkits.palibrary.impl.AbstractFileSourceProcessor;
import eu.scidipes.toolkits.palibrary.impl.FormFieldImpl;
import eu.scidipes.toolkits.palibrary.impl.FormImpl;
import eu.scidipes.toolkits.palibrary.impl.FormsBundleImpl;
import eu.scidipes.toolkits.palibrary.impl.json.adapters.CPIDDeserializer;
import eu.scidipes.toolkits.palibrary.impl.json.adapters.FormFieldInstanceCreator;
import eu.scidipes.toolkits.palibrary.impl.json.adapters.FormFieldListDeserializer;
import eu.scidipes.toolkits.palibrary.impl.json.adapters.FormInstanceCreator;
import eu.scidipes.toolkits.palibrary.impl.json.adapters.FormListDeserializer;
import eu.scidipes.toolkits.palibrary.impl.json.adapters.RepInfoCategoryDeserializer;
import eu.scidipes.toolkits.palibrary.interfaces.Form;
import eu.scidipes.toolkits.palibrary.interfaces.FormField;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationDatasourceProcessor;

/**
 * Implementation of {@link PreservationDatasourceProcessor} for JSON representations of the Form Bundle hierarchy
 * 
 * @author Simon Berriman
 */
public class GenericJSONProcessor extends AbstractFileSourceProcessor {

	/** Logger */
	private static final Logger LOG = Logger.getLogger(GenericJSONProcessor.class);

	private final GsonBuilder gsonBuilder;
	private final Field parentBundleField;
	private final Field parentFormField;

	/**
	 * Constructs processor with Json builder and the necessary adapters for the Form Bundle hierarchy, as implemented
	 * with GSON.
	 */
	public GenericJSONProcessor() {

		// Types of the generic Lists which name interfaces, as these need specific adapters
		final Type formListType = new TypeToken<List<Form>>() {}.getType();
		final Type formFieldsListType = new TypeToken<List<FormField>>() {}.getType();

		gsonBuilder = new GsonBuilder()
				.setExclusionStrategies(new ParentExclusionStrategy(), new JPATransientExclusionStrategy())
				.registerTypeAdapter(Form.class, new FormInstanceCreator())
				.registerTypeAdapter(FormField.class, new FormFieldInstanceCreator())
				.registerTypeAdapter(formListType, new FormListDeserializer())
				.registerTypeAdapter(formFieldsListType, new FormFieldListDeserializer())
				.registerTypeAdapter(RepInfoCategory.class, new RepInfoCategoryDeserializer())
				.registerTypeAdapter(CurationPersistentIdentifier.class, new CPIDDeserializer());

		try {
			// Get a reflected reference to the parent fields to use when manually populating after deserialisation
			parentBundleField = FormImpl.class.getDeclaredField("parentBundle");
			parentBundleField.setAccessible(true);

			parentFormField = FormFieldImpl.class.getDeclaredField("parentForm");
			parentFormField.setAccessible(true);

		} catch (NoSuchFieldException | SecurityException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	/**
	 * Interprets a source object as a {@link File} containing a single, complete JSON Form Bundle hierarchy, and
	 * returns it as a template FormBundle
	 * 
	 * @param source
	 *            a {@link File} containing a single, complete JSON Form Bundle hierarchy
	 * @return a template FormBundle from JSON file
	 * @throws PreservationInitialisationException
	 * @see eu.scidipes.toolkits.palibrary.interfaces.PreservationDatasourceProcessor#sourceToBundle(java.lang.Object)
	 */
	@Override
	public FormsBundle sourceToBundle(final Object source) throws PreservationInitialisationException {

		// Check source is a file
		if (!(source instanceof File)) {
			throw new PreservationInitialisationException("JSON source object must be an instance of: "
					+ File.class.getName());
		}

		try {
			// Read file as UTF-8
			final Reader sourceReader = new InputStreamReader(new FileInputStream((File) source), "UTF-8");
			// Create a new GSON parser for this job
			final Gson gson = gsonBuilder.create();
			// Deserialise into a FormBundle, ignoring parent back references
			final FormsBundle jsonFormBundle = gson.fromJson(sourceReader, FormsBundleImpl.class);

			LOG.debug("Form bundle successfully deserialised. Now setting parent back references...");

			// Now set parent back references manually using reflection
			synchronized (parentBundleField) {
				for (final Form form : jsonFormBundle.getForms()) {
					parentBundleField.set(form, jsonFormBundle);

					for (final FormField formField : form.getFormFields()) {
						parentFormField.set(formField, form);
					}
				}
			}

			LOG.info("Form bundle '" + jsonFormBundle.getBundleName() + "' successfully created");
			return jsonFormBundle;

		} catch (final Exception e) {
			throw new PreservationInitialisationException(e);
		}
	}

	/**
	 * Processor name
	 * 
	 * @return Processor name
	 * @see eu.scidipes.toolkits.palibrary.interfaces.PreservationDatasourceProcessor#getName()
	 */
	@Override
	public String getName() {
		return "Generic JSON Processor";
	}
}
