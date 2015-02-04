/**
 *
 */
package eu.scidipes.toolkits.palibrary.interfaces;

import java.util.Set;

import eu.scidipes.toolkits.palibrary.exceptions.PreservationException;
import eu.scidipes.toolkits.palibrary.exceptions.PreservationInitialisationException;

/**
 * Defines a library of operations on preservation datasets and source bundles
 * 
 * @author Simon Berriman
 * @author Tom Bunting
 */
public interface LibraryAPI {

	Set<? extends Bundle> getAvailableFormsBundles();

	FormsBundle getEmptyStructureForBundle(String bundleName) throws PreservationException;

	boolean deleteBundle(FormsBundle bundle) throws PreservationInitialisationException;
}
