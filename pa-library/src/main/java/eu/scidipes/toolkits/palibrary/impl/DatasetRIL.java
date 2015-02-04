/*
 * Copyright (c) 2011-2013 Alliance for Permanent Access (APA) and its
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
package eu.scidipes.toolkits.palibrary.impl;

import static eu.scidipes.toolkits.palibrary.utils.MiscUtils.HASH_INIT;
import static eu.scidipes.toolkits.palibrary.utils.MiscUtils.HASH_PRIME;
import static java.lang.String.format;
import info.digitalpreserve.interfaces.RepInfoLabel;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import eu.scidipes.toolkits.palibrary.impl.jpa.RepInfoLabelConverter;
import eu.scidipes.toolkits.palibrary.interfaces.Preservable;

/**
 * Basic entity to act as a container for a serializable {@link RepInfoLabel} instance attached to a dataset
 * 
 * @author Tom Bunting
 * 
 */
@Entity
@Table(name = "dataset_ril")
public class DatasetRIL implements Preservable, Serializable {

	private static final long serialVersionUID = 1L;

	@Convert(converter = RepInfoLabelConverter.class)
	private UploadRepInfoLabel ril;

	@Id
	@Column(name = "ril_cpid")
	private String rilCPID;

	@Column(name = "ril_name")
	private String rilName;

	@Basic
	private boolean preserved = false;

	/**
	 * Indicates if the rep info label represented by this instance has been preserved to a SCIDIP registry
	 * 
	 * @return the preserved
	 */
	public final boolean isPreserved() {
		return preserved;
	}

	/**
	 * Set the preserved flag indicating the rep info label has been preserved to a registry
	 * 
	 * @param preserved
	 *            the preserved to set
	 */
	public final void setPreserved(final boolean preserved) {
		this.preserved = preserved;
	}

	/**
	 * Default zero-args constructor required by JPA
	 */
	protected DatasetRIL() {
		/* No action */
	}

	/**
	 * Convenience constructor for new instances. Requires all arguments be non-blank and non-null.
	 * 
	 * @param rilCPID
	 * @param rilName
	 * @param ril
	 * @throws IllegalArgumentException
	 *             if rilCPID or rilName are null, empty or whitespace
	 * @throws NullPointerException
	 *             of ril is null
	 */
	public DatasetRIL(final String rilCPID, final String rilName, final UploadRepInfoLabel ril) {
		if (StringUtils.isBlank(rilCPID)) {
			throw new IllegalArgumentException("rilCPID must not be null, empty or whitespace");
		}
		if (StringUtils.isBlank(rilName)) {
			throw new IllegalArgumentException("rilName must not be null, empty or whitespace");
		}

		Validate.notNull(ril, "ril must not be null");

		this.rilCPID = rilCPID;
		this.rilName = rilName;
		this.ril = ril;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof DatasetRIL)) {
			return false;
		}
		final DatasetRIL otherRIL = (DatasetRIL) o;
		return rilCPID.equals(otherRIL.getRilCPID());
	}

	/**
	 * Returns the rep info label represented by this instance
	 * 
	 * @return the ril
	 */
	public final RepInfoLabel getRil() {
		return ril;
	}

	/**
	 * Returns the RILs CPID string, if one has been assigned
	 * 
	 * @return the rilCPID
	 */
	public final String getRilCPID() {
		return rilCPID;
	}

	/**
	 * Returns the RILs name
	 * 
	 * @return the rilName
	 */
	public final String getRilName() {
		return rilName;
	}

	@Override
	public int hashCode() {
		return HASH_INIT * HASH_PRIME + rilCPID.hashCode();
	}

	@Override
	public String toString() {
		return format("DatasetRIL '%s' [%s]", rilName, rilCPID);
	}

	@Override
	public String getPreservableTypeID() {
		return "RIL";
	}

}
