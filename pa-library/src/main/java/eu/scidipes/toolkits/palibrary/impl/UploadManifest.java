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
package eu.scidipes.toolkits.palibrary.impl;

import static eu.scidipes.common.framework.utils.Misc.HASH_PRIME;
import static org.apache.commons.lang.Validate.notNull;
import info.digitalpreserve.interfaces.CurationPersistentIdentifier;
import info.digitalpreserve.interfaces.DigitalObjectLocation;
import info.digitalpreserve.interfaces.Identifiable;
import info.digitalpreserve.interfaces.Identifier;
import info.digitalpreserve.interfaces.Manifest;
import info.digitalpreserve.interfaces.RegistryObjectType;
import info.digitalpreserve.interfaces.RepInfoCategory;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.scidipes.common.framework.FrameworkWrapper;
import eu.scidipes.common.framework.core.RegistryStorageUtils;

/**
 * Basic Manifest implementation for use when uploading to a SCIDIP registry/repository
 * 
 * @author Simon Berriman
 * @author Tom Bunting
 */
public class UploadManifest implements Manifest {

	/** Logger */
	private static final Logger LOG = Logger.getLogger(UploadManifest.class);

	private Long version = Long.valueOf(1L);
	private final CurationPersistentIdentifier cpid;
	private final CurationPersistentIdentifier rilcpid;
	private final Set<RepInfoCategory> categories = new LinkedHashSet<>();
	private String resourceName = null;
	private URL location = null;

	/**
	 * Create a new instance with the provided ril CPID and a newly allocated manifest CPID obtained from the Framework
	 * 
	 * @param rilcpid
	 * @throws NullPointerException
	 *             if either argument is null
	 */
	public UploadManifest(final CurationPersistentIdentifier rilcpid) {
		this(rilcpid, FrameworkWrapper.allocateNewPID());
	}

	/**
	 * Create a new instance using the provided RIL CPID and Manifest CPID.
	 * 
	 * @param rilcpid
	 * @param manifestcpid
	 * @throws NullPointerException
	 *             if either argument is null
	 */
	public UploadManifest(final CurationPersistentIdentifier rilcpid, final CurationPersistentIdentifier manifestcpid) {
		notNull(rilcpid, "rilcpid must not be null");
		notNull(manifestcpid, "manifestcpid must not be null");

		this.cpid = manifestcpid;
		this.rilcpid = rilcpid;
		if (LOG.isDebugEnabled()) {
			LOG.debug("New Upload Manifest created for RIL '" + rilcpid + "' with CPID '" + cpid + "'");
		}
	}

	/**
	 * @return
	 * @see info.digitalpreserve.interfaces.RegistryObject#getRegistryObjectType()
	 */
	@Override
	public RegistryObjectType getRegistryObjectType() {
		return RegistryObjectType.MANIFEST;
	}

	/**
	 * @return
	 * @see info.digitalpreserve.interfaces.Identifiable#getCpid()
	 */
	@Override
	public Identifier<DigitalObjectLocation> getCpid() {
		return cpid;
	}

	/**
	 * @return
	 * @see info.digitalpreserve.interfaces.Manifest#getManifestCpid()
	 */
	@Override
	public CurationPersistentIdentifier getManifestCpid() {
		return cpid;
	}

	/**
	 * @return
	 * @see info.digitalpreserve.interfaces.Manifest#getRILCpid()
	 */
	@Override
	public CurationPersistentIdentifier getRILCpid() {
		return rilcpid;
	}

	/**
	 * @return
	 * @see info.digitalpreserve.interfaces.Manifest#getVersion()
	 */
	@Override
	public Long getVersion() {
		return version;
	}

	/**
	 * @param version
	 * @see info.digitalpreserve.interfaces.Manifest#setVersion(java.lang.Long)
	 */
	@Override
	public void setVersion(final Long version) {
		this.version = version;
	}

	/**
	 * @return
	 * @see info.digitalpreserve.interfaces.Manifest#getCategories()
	 */
	@Override
	public Set<RepInfoCategory> getCategories() {
		return categories;
	}

	/**
	 * @return <code>null</code>
	 * @see info.digitalpreserve.interfaces.Manifest#getPpid()
	 */
	@Override
	public <D extends DigitalObjectLocation> Identifier<D> getPpid() {
		return null;
	}

	/**
	 * @return
	 * @see info.digitalpreserve.interfaces.Manifest#getResourceName()
	 */
	@Override
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * @return
	 * @see info.digitalpreserve.interfaces.Manifest#getLocation()
	 */
	@Override
	public URL getLocation() {
		return location;
	}

	/**
	 * Sets the resourceName
	 * 
	 * @param resourceName
	 *            the resourceName to set
	 */
	public final void setResourceName(final String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * Sets the location
	 * 
	 * @param location
	 *            the location to set
	 */
	public final void setLocation(final URL location) {
		this.location = location;
	}

	/**
	 * @return
	 * @see info.digitalpreserve.interfaces.Manifest#serialiseToString()
	 */
	@Override
	public String serialiseToString() {
		return RegistryStorageUtils.manifestToXMLString(this);
	}

	@Override
	public String toString() {
		return "Manifest for CPID [" + getManifestCpid() + ']';
	}

	@Override
	public final boolean equals(final Object obj) {
		return obj == this || (obj instanceof Manifest && obj.hashCode() == this.hashCode());
	}

	@Override
	public final int hashCode() {
		return "Manifest".hashCode() * HASH_PRIME + getManifestCpid().hashCode();
	}

	@Override
	public final int compareTo(final Identifiable<DigitalObjectLocation> o) {
		return getManifestCpid().compareTo(o.getCpid());
	}
}
