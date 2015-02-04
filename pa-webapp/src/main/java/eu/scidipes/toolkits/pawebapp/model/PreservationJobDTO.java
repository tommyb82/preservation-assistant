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
package eu.scidipes.toolkits.pawebapp.model;

import static java.lang.Boolean.FALSE;
import info.digitalpreserve.interfaces.CurationPersistentIdentifier;
import info.digitalpreserve.interfaces.DigitalObject;
import info.digitalpreserve.interfaces.DigitalObjectLocation;
import info.digitalpreserve.interfaces.Identifiable;
import info.digitalpreserve.interfaces.Identifier;
import info.digitalpreserve.interfaces.Manifest;
import info.digitalpreserve.interfaces.RegistryObject;
import info.digitalpreserve.interfaces.RegistryObjectType;
import info.digitalpreserve.interfaces.RepInfoCategory;
import info.digitalpreserve.interfaces.RepInfoLabel;
import info.digitalpreserve.interfaces.RepInfoStatus;
import info.digitalpreserve.interfaces.RepresentationInformation;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.scidipes.common.framework.core.impl.CoreCurationPersistentIdentifier;
import eu.scidipes.toolkits.palibrary.interfaces.Preservable;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationJob;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationJobItemResult;

/**
 * DTO for use when serialising a {@link PreservationJob} instance as JSON. The main function of this class is to hide
 * the sensitive details available through the object map of each preserved object item when the type is
 * {@link Manifest}. Without this class and its use of {@link JsonIgnore} on certain fields and manipulation of other
 * returned values, the entire object map is serialised, potentially exposing sensitive information to clients.
 * 
 * @author Tom Bunting
 * 
 */
public class PreservationJobDTO implements PreservationJob {

	private static final Logger LOG = LoggerFactory.getLogger(PreservationJobDTO.class);

	private final PreservationJob job;

	private PreservationJobDTO(final PreservationJob job) {
		this.job = job;
	}

	public static final PreservationJobDTO wrapJob(final PreservationJob job) {
		return new PreservationJobDTO(job);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.scidipes.toolkits.palibrary.interfaces.PreservationJob#getDatasetName()
	 */
	@Override
	public String getDatasetName() {
		return job.getDatasetName();
	}

	@JsonProperty("data")
	public List<PreservationJobItemResult<Preservable, ?>> getJobItems() {

		final List<PreservationJobItemResult<Preservable, ?>> jobItems = new ArrayList<>();

		final Map<Preservable, PreservationJobItemResult<Preservable, ?>> jobItemMap = job.getPreservationJobItemMap();

		synchronized (jobItemMap) {
			for (final Map.Entry<Preservable, PreservationJobItemResult<Preservable, ?>> entry : jobItemMap.entrySet()) {
				final PreservationJobItemResult<Preservable, ?> jobItem = entry.getValue();

				/*
				 * Create sanitised job item and manifest instances that will hide sensitive data from clients but pass
				 * through calls to all other properties
				 */
				jobItems.add(new PreservationJobItemResult<Preservable, RegistryObject>() {

					@Override
					public Preservable getPreservable() {
						return entry.getKey();
					}

					@Override
					public RegistryObject getPreservedObject() {

						LOG.trace("Fetching preservedObject for jobItem: {}", jobItem);

						if (jobItem != null && jobItem.getPreservedObject() instanceof Manifest) {
							return new SanitizedManifest((Manifest) jobItem.getPreservedObject());
						} else if (jobItem != null && jobItem.getPreservedObject() instanceof RepInfoLabel) {
							return new SanitizedRIL((RepInfoLabel) jobItem.getPreservedObject());
						} else {
							return null;
						}

					}

					@Override
					public Throwable getThrown() {
						return jobItem != null ? jobItem.getThrown() : null;
					}

					@Override
					public Boolean isSucceeded() {
						return jobItem != null ? jobItem.isSucceeded() : FALSE;
					}
				});
			}
		}
		return Collections.unmodifiableList(jobItems);
	}

	@Override
	@JsonIgnore
	public Map<Preservable, PreservationJobItemResult<Preservable, ?>> getPreservationJobItemMap() {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public int getCompleted() {
		return job.getCompleted();
	}

	@Override
	public String getRegistryUID() {
		return job.getRegistryUID();
	}

}

final class SanitizedManifest implements Manifest {

	private final Manifest manifest;

	SanitizedManifest(final Manifest manifest) {
		this.manifest = manifest;
	}

	@Override
	public int compareTo(final Identifiable<DigitalObjectLocation> o) {
		return manifest.compareTo(o);
	}

	@Override
	public Identifier<DigitalObjectLocation> getCpid() {
		return new CoreCurationPersistentIdentifier(manifest.getCpid().getUID());
	}

	@Override
	public RegistryObjectType getRegistryObjectType() {
		return manifest.getRegistryObjectType();
	}

	@Override
	public void setVersion(final Long version) {
		manifest.setVersion(version);
	}

	@Override
	public String serialiseToString() {
		return manifest.serialiseToString();
	}

	@Override
	public Long getVersion() {
		return manifest.getVersion();
	}

	@Override
	public String getResourceName() {
		return manifest.getResourceName();
	}

	@Override
	public CurationPersistentIdentifier getRILCpid() {
		return new CoreCurationPersistentIdentifier(manifest.getRILCpid().getUID());
	}

	@JsonIgnore
	@Override
	public <D extends DigitalObjectLocation> Identifier<D> getPpid() {
		return manifest.getPpid();
	}

	@Override
	public CurationPersistentIdentifier getManifestCpid() {
		/* Create new CPID instance with no location data that publishes creds to clients */
		return new CoreCurationPersistentIdentifier(manifest.getManifestCpid().getUID());
	}

	@Override
	public URL getLocation() {
		return manifest.getLocation();
	}

	@JsonIgnore
	@Override
	public Set<RepInfoCategory> getCategories() {
		return manifest.getCategories();
	}

}

final class SanitizedRIL implements RepInfoLabel {

	private final RepInfoLabel ril;

	SanitizedRIL(final RepInfoLabel ril) {
		this.ril = ril;
	}

	@JsonIgnore
	@Override
	public RepresentationInformation[] getRepresentationInformationChildren() {
		return ril.getRepresentationInformationChildren();
	}

	@Override
	public void setRepresentationInformationChildren(final RepresentationInformation[] ri) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public boolean isGroupEmpty() {
		return ril.isGroupEmpty();
	}

	@Override
	public CurationPersistentIdentifier getTypeCpid() {
		return new CoreCurationPersistentIdentifier(ril.getTypeCpid().getUID());
	}

	@JsonIgnore
	@Override
	public Set<RepInfoCategory> getCategories() {
		return ril.getCategories();
	}

	@Override
	public RepInfoStatus getStatus() {
		return ril.getStatus();
	}

	@Override
	public void setStatus(final RepInfoStatus status) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void setTypeCpid(final CurationPersistentIdentifier typeCpid) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public CurationPersistentIdentifier getCpid() {
		return new CoreCurationPersistentIdentifier(ril.getCpid().getUID());
	}

	@JsonIgnore
	@Override
	public DigitalObject getDigitalObject() {
		return ril.getDigitalObject();
	}

	@Override
	public void setCpid(final CurationPersistentIdentifier cpid) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void setDigitalObject(final DigitalObject dataob) {
		throw new UnsupportedOperationException("not supported");
	}

	@JsonIgnore
	@Override
	public RepresentationInformation getRepresentationInformation() {
		return ril.getRepresentationInformation();
	}

	@Override
	public void setRepresentationInformation(final RepresentationInformation ri) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public int compareTo(final Identifiable<DigitalObjectLocation> o) {
		return ril.compareTo(o);
	}

	@Override
	public RegistryObjectType getRegistryObjectType() {
		return ril.getRegistryObjectType();
	}

	@Override
	public Long getVersion() {
		return ril.getVersion();
	}

	@Override
	public String getDisplayname() {
		return ril.getDisplayname();
	}

	@Override
	public String getDescription() {
		return ril.getDescription();
	}

	@Override
	public Calendar getLastModified() {
		return ril.getLastModified();
	}

	@Override
	public void setVersion(final Long version) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void setDisplayname(final String displayname) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void setDescription(final String description) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void setLastModified(final Calendar lastModified) {
		throw new UnsupportedOperationException("not supported");
	}

	@JsonIgnore
	@Override
	public String serialiseToString() {
		return ril.serialiseToString();
	}

}
