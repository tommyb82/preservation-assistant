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
package eu.scidipes.toolkits.pawebapp.web;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import info.digitalpreserve.interfaces.CurationPersistentIdentifier;
import info.digitalpreserve.interfaces.RepInfoGroup;
import info.digitalpreserve.interfaces.RepresentationInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.scidipes.common.framework.FrameworkWrapper;
import eu.scidipes.common.framework.core.impl.BaseRepresentationInformation;
import eu.scidipes.toolkits.palibrary.impl.DatasetRIL;
import eu.scidipes.toolkits.palibrary.impl.FormImpl;
import eu.scidipes.toolkits.palibrary.impl.FormsBundleImpl;
import eu.scidipes.toolkits.palibrary.impl.UploadRepInfoLabel;
import eu.scidipes.toolkits.palibrary.impl.UploadRepInfoLabel.CoreRIType;
import eu.scidipes.toolkits.palibrary.interfaces.Form;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;
import eu.scidipes.toolkits.pawebapp.repository.DataSetRepository;

/**
 * {@link Controller} encapsulating operations on item groups (collections and individual instances of
 * {@link DatasetRIL} )
 * 
 * @author Tom Bunting
 * 
 */
@Controller
@RequestMapping("/datasets/{datasetName}/items/groups")
public class ItemGroupsController {

	private static final Logger LOG = LoggerFactory.getLogger(ItemGroupsController.class);

	@Autowired
	private DataSetRepository datasetRepo;

	/**
	 * Returns a {@link Set} of entry pairs representing the core RI members of a {@link DatasetRIL} instance.
	 * 
	 * @param datasetName
	 * @param rilCPID
	 * @return a set of key (core RI name) value (set of forms) pairs
	 */
	@ResponseBody
	@RequestMapping("/rilmemberitems")
	public Set<Entry<String, Set<Form>>> getRILMemberItems(@PathVariable final String datasetName, final String rilCPID) {

		LOG.debug("Fetching ril member items for dataset: [{}] -  ril [{}]", datasetName, rilCPID);

		final FormsBundle dataset = datasetRepo.findOne(datasetName);
		final DatasetRIL dsRIL = datasetRepo.findOneRIL(rilCPID);

		/*
		 * Build a map of forms by their manifest CPID, if assigned. Presence of a manifest CPID indicates the form has
		 * been added to a RIL and / or preserved:
		 */
		final Map<String, Form> dsFormsByManCPID = new HashMap<>();
		for (final Form form : dataset.getForms()) {
			if (form.getManifestCPID() != null) {
				LOG.trace("Adding form [{}] with manifest CPID [{}] to ril member items lookup table", form,
						form.getManifestCPID());
				dsFormsByManCPID.put(form.getManifestCPID().getUID(), form);
			}
		}

		final Map<String, Set<Form>> rilMemberItems = new HashMap<>();

		final RepresentationInformation[] repInfo = dsRIL.getRil().getRepresentationInformationChildren();

		for (final RepresentationInformation coreRI : repInfo) {

			if (coreRI.getRepresentationInformation() instanceof RepInfoGroup) {
				final RepInfoGroup repInfoGroup = (RepInfoGroup) coreRI.getRepresentationInformation();

				for (final RepresentationInformation ri : repInfoGroup.getRepresentationInformationChildren()) {

					final String coreRIType = CoreRIType.fromClass(coreRI.getClass()).toString();

					if (!rilMemberItems.containsKey(coreRIType)) {
						rilMemberItems.put(coreRIType, new HashSet<Form>());
					}
					rilMemberItems.get(coreRIType).add(dsFormsByManCPID.get(ri.getCpid().getUID()));
				}

			}
		}
		return rilMemberItems.entrySet();
	}

	/**
	 * Creates a new {@link DatasetRIL} using the <code>rilName</code> and <code>rilDesc</code> arguments and adds it to
	 * the dataset identified by <code>datasetName</code>.
	 * 
	 * @param datasetName
	 * @param formName
	 * @param formID
	 * @param rilName
	 * @param rilDesc
	 * @param model
	 * @param redirectAttrs
	 * @return a redirect link back to the form item edit page
	 */
	@RequestMapping("/create")
	public String createGroup(@PathVariable final String datasetName,
			@MatrixVariable(value = "fn", required = false, pathVar = "datasetName") final String formName,
			final Integer formID, final String rilName, final String rilDesc, final Model model,
			final RedirectAttributes redirectAttrs) {

		final FormsBundleImpl dataset = datasetRepo.findOne(datasetName);

		LOG.info("Creating new DatasetRIL group name='{}', description='{}' in dataset '{}'", rilName, rilDesc, dataset);

		final CurationPersistentIdentifier rilCPID = FrameworkWrapper.allocateNewPID();

		final UploadRepInfoLabel ril = new UploadRepInfoLabel(rilName, rilDesc, rilCPID);
		final DatasetRIL dsRIL = new DatasetRIL(rilCPID.getUID(), rilName, ril);

		dataset.addRil(dsRIL);
		datasetRepo.save(dataset);

		return buildRedirectLink(formName, datasetName, formID);
	}

	/**
	 * Adds the form identified by the <code>formID</code> argument to the {@link DatasetRIL} instance identified by the
	 * <code>rilCPID</code> argument. The form instance is allocated a new manifest CPID if necessary and this CPID is
	 * used to add new RI to the RIL.
	 * 
	 * @param datasetName
	 * @param formName
	 * @param formID
	 * @param coreRIType
	 * @param rilCPID
	 * @param model
	 * @param redirectAttrs
	 * @return a redirect link back to the form item edit page
	 */
	@RequestMapping("/additem")
	public String addItemToGroup(@PathVariable final String datasetName,
			@MatrixVariable(value = "fn", required = false, pathVar = "datasetName") final String formName,
			final Integer formID, final CoreRIType coreRIType, final String rilCPID, final Model model,
			final RedirectAttributes redirectAttrs) {

		final FormImpl form = datasetRepo.findOneForm(formID);
		final FormsBundleImpl dataset = (FormsBundleImpl) form.getParentBundle();

		final DatasetRIL dsRIL = dataset.getDatasetRIL(rilCPID);

		final UploadRepInfoLabel ril = (UploadRepInfoLabel) dsRIL.getRil();

		if (form.getManifestCPID() == null) {
			form.setManifestCPID(FrameworkWrapper.allocateNewPID());
			dataset.setForm(form);
			LOG.debug("Allocated new manifest CPID in: {}", form);
		}

		/* Create a new piece of core RI representing the form and add it to the RIL: */
		final BaseRepresentationInformation ri = coreRIType.newInstance();
		ri.setCpid(form.getManifestCPID());
		ri.setTypeCpid(form.getRILCPID());
		ri.getCategories().addAll(form.getCategories());

		ril.addRepInfo(ri, coreRIType);

		LOG.info("Added RI: {} to RIL: {} in dataset: {}", ri, ril, dataset);

		/* Entity manager does not detect changes to ril property, so update manually: */
		datasetRepo.updateDatasetRIL(ril, rilCPID);
		datasetRepo.save(dataset);

		LOG.debug("Saved dataset: {}", dataset);

		return buildRedirectLink(formName, datasetName, formID);
	}

	/**
	 * Removes the form identified by the given formID from the core RI section in the {@link DatasetRIL} instance
	 * identified by the <code>coreRIType</code> and <code>rilCPID</code> arguments.
	 * 
	 * @param datasetName
	 * @param formName
	 * @param formID
	 * @param coreRIType
	 * @param rilCPID
	 * @param model
	 * @param redirectAttrs
	 * @return a redirect link back to the form item edit page
	 */
	@RequestMapping("/removeitem")
	public String removeItemFromGroup(@PathVariable final String datasetName,
			@MatrixVariable(value = "fn", required = false, pathVar = "datasetName") final String formName,
			@RequestParam("f") final Integer formID, @RequestParam("ritype") final CoreRIType coreRIType,
			@RequestParam("rcpid") final String rilCPID, final Model model, final RedirectAttributes redirectAttrs) {

		final FormImpl form = datasetRepo.findOneForm(formID);
		final FormsBundleImpl dataset = (FormsBundleImpl) form.getParentBundle();
		final UploadRepInfoLabel ril = (UploadRepInfoLabel) dataset.getDatasetRIL(rilCPID).getRil();

		final BaseRepresentationInformation ri = coreRIType.newInstance();
		ri.setCpid(form.getManifestCPID());

		ril.removeRepInfo(ri, coreRIType);
		LOG.info("Removed RI: {} from RIL: {} in dataset: {}", ri, ril, dataset);

		datasetRepo.updateDatasetRIL(ril, rilCPID);
		datasetRepo.save(dataset);

		return buildRedirectLink(formName, datasetName, formID);
	}

	private String buildRedirectLink(final String formName, final String datasetName, final Integer formID) {
		final String formNameMatrixVar = isBlank(formName) ? "" : ";fn=" + formName;
		return "redirect:/datasets/" + datasetName + formNameMatrixVar + "/items/" + formID + "/edit";
	}

}
