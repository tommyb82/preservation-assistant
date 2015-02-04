package eu.scidipes.toolkits.palibrary.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.rits.cloning.Cloner;

import eu.scidipes.toolkits.palibrary.exceptions.PreservationException;
import eu.scidipes.toolkits.palibrary.exceptions.PreservationInitialisationException;
import eu.scidipes.toolkits.palibrary.impl.FormsBundleImpl;
import eu.scidipes.toolkits.palibrary.interfaces.Bundle;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationDatasourceProcessor;

/**
 * @author Simon Berriman
 * @author Tom Bunting
 * 
 */
public class FormBundleManager {

	/** Logger */
	private static final Logger LOG = Logger.getLogger(FormBundleManager.class);
	private static final Cloner CLONER = new Cloner();
	private static final FormBundleManager SINGLETON;

	static {
		try {
			SINGLETON = new FormBundleManager();
		} catch (final PreservationException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	/**
	 * @return
	 */
	public static Set<? extends Bundle> getDiscoveredBundles() {
		synchronized (SINGLETON) {
			return Collections.unmodifiableSet(new LinkedHashSet<>(SINGLETON.bundles));
		}
	}

	/**
	 * 
	 * @param bundleName
	 * @return
	 * @throws PreservationException
	 */
	public static FormsBundle getEmptyBundleFor(final String bundleName) throws PreservationException {
		final FormsBundle bundleToFetch = new FormsBundleImpl(bundleName, "_");

		synchronized (SINGLETON) {
			final int bundleIdx = SINGLETON.bundles.indexOf(bundleToFetch);
			if (bundleIdx < 0) {
				throw new PreservationException(bundleName + " not found");
			}
			final FormsBundle masterBundle = SINGLETON.bundles.get(bundleIdx);
			return CLONER.deepClone(masterBundle);
		}
	}

	/**
	 * Removes a bundle from the internal collection
	 * 
	 * @param bundle
	 *            the bundle to delete
	 * @return boolean indicating the success of the operation
	 * @throws PreservationInitialisationException
	 */
	public static boolean deleteBundle(final FormsBundle bundle) throws PreservationInitialisationException {
		synchronized (SINGLETON.bundles) {
			return SINGLETON.bundles.remove(bundle);
		}
	}

	public static void addBundle(final FormsBundle bundle) {
		synchronized (SINGLETON.bundles) {
			SINGLETON.bundles.add(bundle);
		}
	}

	private final List<FormsBundle> bundles = new ArrayList<>();

	/**
	 * @throws PreservationException
	 */
	private FormBundleManager() throws PreservationException {
		discoverBundles();
		LOG.debug("FormBundleManager created");
	}

	/**
	 * @throws PreservationException
	 */
	private void discoverBundles() throws PreservationException {
		// Wipe any existing known bundles
		bundles.clear();

		// Get discovered processors
		final Map<String, PreservationDatasourceProcessor> processors = SourceProcessorManager.INSTANCE.getProcessors();

		// Add their respective bundles to the list of all known bundles
		for (final String processorName : processors.keySet()) {
			bundles.addAll(processors.get(processorName).discoverBundles());
		}
	}
}
