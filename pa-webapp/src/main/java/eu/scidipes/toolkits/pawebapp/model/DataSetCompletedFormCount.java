/*
 * Copyright (c) 2011-2014 Alliance for Permanent Access (APA) and its
 * contributors. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * The APA licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.scidipes.toolkits.pawebapp.model;

import static java.lang.String.format;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;

/**
 * Simple wrapper class around a @{link FormsBundle} instance and a count of its completed forms.
 * 
 * @author Tom Bunting
 * 
 */
public final class DataSetCompletedFormCount {

	private final FormsBundle dataSet;

	private final long completedFormsCount;

	/**
	 * Sole constructor taking a {@link FormsBundle} instance and a <code>Long</code> completedFormsCount
	 * 
	 * @param dataSet
	 * @param completedFormsCount
	 */
	public DataSetCompletedFormCount(final FormsBundle dataSet, final Long completedFormsCount) {
		this.dataSet = dataSet;
		this.completedFormsCount = completedFormsCount.longValue();
	}

	/**
	 * Return the wrapped {@link FormsBundle} instance
	 * 
	 * @return
	 */
	public FormsBundle getDataSet() {
		return dataSet;
	}

	/**
	 * Return the completed forms count for this dataset
	 * 
	 * @return
	 */
	public long getCompletedFormsCount() {
		return completedFormsCount;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof DataSetCompletedFormCount)) {
			return false;
		}
		final DataSetCompletedFormCount other = (DataSetCompletedFormCount) o;
		return dataSet.equals(other.getDataSet());
	}

	@Override
	public int hashCode() {
		return getClass().getSimpleName().hashCode() * 31 * dataSet.hashCode();
	}

	@Override
	public String toString() {
		return format("%s: %s (%d)", getClass().getSimpleName(), dataSet.getDatasetName(),
				Long.valueOf(completedFormsCount));
	}
}
