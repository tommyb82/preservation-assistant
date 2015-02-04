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
package eu.scidipes.toolkits.pawebapp.preservation;

import static eu.scidipes.toolkits.palibrary.interfaces.CoreFieldMetadata.FILE_MIMETYPE;
import static eu.scidipes.toolkits.palibrary.interfaces.CoreFieldMetadata.FILE_NAME;
import static eu.scidipes.toolkits.palibrary.interfaces.FormFieldType.BYTESTREAM;
import static eu.scidipes.toolkits.palibrary.interfaces.FormFieldType.URI;
import info.digitalpreserve.interfaces.CurationPersistentIdentifier;
import info.digitalpreserve.interfaces.Manifest;
import info.digitalpreserve.interfaces.Registry;
import info.digitalpreserve.interfaces.RegistryObject;
import info.digitalpreserve.interfaces.Repository;

import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import eu.scidipes.common.framework.FrameworkWrapper;
import eu.scidipes.common.framework.core.impl.CoreCurationPersistentIdentifier;
import eu.scidipes.toolkits.palibrary.exceptions.PreservationStorageException;
import eu.scidipes.toolkits.palibrary.impl.FormImpl;
import eu.scidipes.toolkits.palibrary.impl.UploadManifest;
import eu.scidipes.toolkits.palibrary.interfaces.CoreFieldMetadata;
import eu.scidipes.toolkits.palibrary.interfaces.Form;
import eu.scidipes.toolkits.palibrary.interfaces.FormFieldType;
import eu.scidipes.toolkits.palibrary.interfaces.Preservable;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationJobItemResult;
import eu.scidipes.toolkits.palibrary.utils.XMLUtils;
import eu.scidipes.toolkits.palibrary.utils.zip.ZipUtils;
import eu.scidipes.toolkits.palibrary.utils.zip.ZipUtils.ByteArrayZipEntry;

/**
 * Callable task to preserve a {@link Form} instance as a {@link Manifest} on a specified {@link Registry} instance,
 * plus any enclosed digital object on the corresponding {@link Repository}.
 * 
 * @author Tom Bunting
 * 
 */
class PreserveFormManifestTask implements Callable<PreservationJobItemResult<Preservable, RegistryObject>> {

	private static final Logger LOG = LoggerFactory.getLogger(PreserveFormManifestTask.class);

	private final Form form;
	private final Registry registry;

	PreserveFormManifestTask(final Form form, final Registry registry) {
		this.form = form;
		this.registry = registry;
	}

	@SuppressWarnings("boxing")
	@Override
	public PreservationJobItemResult<Preservable, RegistryObject> call() throws PreservationStorageException {

		LOG.trace("New PreserveFormManifestTask started in thread [{}] at [{}]", Thread.currentThread().getId(),
				new Date().toString());
		try {
			final String riLocation;

			final CurationPersistentIdentifier rilCpidToPreserve;
			final FormFieldType dataType = form.getDataHolderType();

			if (dataType == BYTESTREAM) {

				LOG.trace("{} indicates a bytestream", form);

				final Node xml = XMLUtils.formFieldsToMetadata(form.getFormFields());

				final String b64BytesToPreserve;
				final String fileNameToPreserve;
				final String fileTypeToPreserve;

				if (xml != null) {
					LOG.debug("Preparing: {} for preservation - flex fields found", form);

					if (LOG.isTraceEnabled()) {
						LOG.trace(XMLUtils.nodeToString(xml, true, false));
					}

					final byte[] mainFileBytes = Base64.decodeBase64(form.getDataHolder());
					final byte[] metaFileBytes = XMLUtils.nodeToString(xml, true, false).getBytes();

					final String mainFileName = form.getDataHolderMetadata().get(CoreFieldMetadata.FILE_NAME);
					final String metaFileName = mainFileName + "_meta.xml";

					final ZipEntry zipMain = new ZipEntry(mainFileName);
					final ZipEntry zipMeta = new ZipEntry(metaFileName);

					final Set<ByteArrayZipEntry> entries = new HashSet<>();
					entries.add(new ByteArrayZipEntry(mainFileBytes, zipMain));
					entries.add(new ByteArrayZipEntry(metaFileBytes, zipMeta));

					/* Set the key preservation details from the zip: */
					b64BytesToPreserve = ZipUtils.byteArrayZipEntriesToBase64(entries);
					fileNameToPreserve = mainFileName + "_pa-packaged.zip";
					fileTypeToPreserve = "application/zip";
					rilCpidToPreserve = new CoreCurationPersistentIdentifier(form.getRILCPID().getUID() + "-Z");

				} else {
					/* Just set the key preservation details dreckly from the form: */
					b64BytesToPreserve = form.getDataHolder();
					fileNameToPreserve = form.getDataHolderMetadata().get(FILE_NAME);
					fileTypeToPreserve = form.getDataHolderMetadata().get(FILE_MIMETYPE);
					rilCpidToPreserve = form.getRILCPID();
				}

				final byte[] dataBytes = Base64.decodeBase64(b64BytesToPreserve);
				final String fileName = fileNameToPreserve;
				final String fileType = fileTypeToPreserve;

				final Repository uploadRepo = registry.getUploadRepository();
				riLocation = uploadRepo.storeRIObject(dataBytes, fileName, fileType);

			} else if (dataType == URI) {

				LOG.trace("{} indicates a URI", form);
				rilCpidToPreserve = form.getRILCPID();
				riLocation = form.getDataHolder();

			} else {
				throw new PreservationStorageException("Unhandled storage type [" + dataType + "] for RI", form);
			}

			final UploadManifest uploadManifest;

			/* Manifest CPID may have already been set on Form instance: */
			if (form.getManifestCPID() == null) {
				uploadManifest = new UploadManifest(rilCpidToPreserve);
			} else {
				uploadManifest = new UploadManifest(form.getRILCPID(), form.getManifestCPID());
			}

			final StringBuilder manifestName = new StringBuilder();
			manifestName.append(form.getParentBundle().getDatasetName());
			manifestName.append('-' + form.getName());

			if (!StringUtils.isBlank(form.getItemFileName())) {
				manifestName.append('-' + form.getItemFileName().trim());
			}

			LOG.debug("Creating manifest for '{}' with location '{}'", manifestName, riLocation);

			uploadManifest.setResourceName(manifestName.toString());
			uploadManifest.setLocation(new URL(riLocation));
			uploadManifest.getCategories().addAll(form.getCategories());

			LOG.debug("Storing new manifest '{}' to {}", uploadManifest.getManifestCpid(), registry);

			FrameworkWrapper.storeManifest(uploadManifest, registry);

			/* TODO: Revisit: pause should not be necessary but without it the retrieval fails intermittently */
			Thread.sleep(2000);

			final Manifest stored = FrameworkWrapper.getManifest(uploadManifest.getManifestCpid());

			/* Set the preserved CPID on the form instance and set preserved flag: */
			synchronized (form) {
				if (form.getManifestCPID() == null) {
					((FormImpl) form).setManifestCPID(stored.getManifestCpid());
				}
				((FormImpl) form).setPreserved(true);
			}

			return new PreservationJobItemResultImpl(form, stored);

		} catch (final Exception e) {
			throw new PreservationStorageException(e, form);
		} finally {
			LOG.trace("Upload callable finished in thread [{}] at [{}]", Thread.currentThread().getId(),
					new Date().toString());
		}
	}

}
