package eu.scidipes.toolkits.palibrary.interfaces;

import java.io.Serializable;
import java.util.Set;

import eu.scidipes.toolkits.palibrary.impl.DatasetRIL;

/**
 * @author Simon Berriman
 */
public interface Bundle extends Comparable<Bundle>, Serializable {

	String getDatasetName();

	String getDisplayName();

	String getBundleName();

	String getProcessorName();

	String getTemplateSource();

	Set<DatasetRIL> getRils();
}
