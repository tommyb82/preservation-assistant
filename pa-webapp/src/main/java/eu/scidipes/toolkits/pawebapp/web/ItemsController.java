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

import static eu.scidipes.toolkits.palibrary.interfaces.CoreFieldMetadata.FILE_MIMETYPE;
import static eu.scidipes.toolkits.palibrary.interfaces.CoreFieldMetadata.FILE_NAME;
import static eu.scidipes.toolkits.palibrary.interfaces.CoreFieldMetadata.FILE_SIZE;
import static eu.scidipes.toolkits.palibrary.interfaces.FormFieldType.BYTESTREAM;
import static eu.scidipes.toolkits.palibrary.interfaces.FormFieldType.URI;
import static eu.scidipes.toolkits.pawebapp.web.SaveAction.ADD_NEW;
import static eu.scidipes.toolkits.pawebapp.web.SaveAction.EDIT;
import info.digitalpreserve.interfaces.CurationPersistentIdentifier;
import info.digitalpreserve.interfaces.RepInfoGroup;
import info.digitalpreserve.interfaces.RepresentationInformation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.scidipes.toolkits.palibrary.exceptions.PreservationException;
import eu.scidipes.toolkits.palibrary.impl.DatasetRIL;
import eu.scidipes.toolkits.palibrary.impl.FormImpl;
import eu.scidipes.toolkits.palibrary.impl.FormsBundleImpl;
import eu.scidipes.toolkits.palibrary.impl.UploadRepInfoLabel.CoreRIType;
import eu.scidipes.toolkits.palibrary.interfaces.Form;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;
import eu.scidipes.toolkits.palibrary.interfaces.LibraryAPI;
import eu.scidipes.toolkits.pawebapp.repository.DataSetRepository;
import eu.scidipes.toolkits.pawebapp.util.Base64MultipartFileEditor;
import eu.scidipes.toolkits.pawebapp.web.validation.FormValidator;

/**
 * {@link Controller} encapsulating operations on data set items (instances of {@link Form})
 * 
 * @author Tom Bunting
 * 
 */
@Controller
@SessionAttributes({ "form", "saveAction" })
@RequestMapping("/datasets/{datasetName}/items")
public class ItemsController {

	private static final Logger LOG = LoggerFactory.getLogger(ItemsController.class);

	@Autowired
	private DataSetRepository datasetRepo;

	@Autowired
	private LibraryAPI libraryAPI;

	@Autowired
	private FormValidator formValidator;

	/**
	 * Configures the {@link WebDataBinder} instance provided by Spring
	 * 
	 * @param binder
	 */
	@InitBinder
	public void initBinder(final WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new Base64MultipartFileEditor());

		/* Prevent malicious attempts to bind certain fields */
		binder.setDisallowedFields("formID", "name", "displayName", "introText", "group", "groupOrder");
	}

	@ModelAttribute("datasetPath")
	public String getDatasetPath(@PathVariable final String datasetName) {
		return "/datasets/" + datasetName;
	}

	@ModelAttribute("dsRILs")
	public Set<DatasetRIL> getDatasetRILs(@PathVariable final String datasetName) {
		final FormsBundle dataset = datasetRepo.findOne(datasetName);
		return dataset.getRils();
	}

	@RequestMapping("/{formID}/delete")
	public String deleteItem(@PathVariable final String datasetName, @PathVariable final Integer formID,
			final RedirectAttributes redirectAttrs) {

		final FormsBundleImpl dataset = datasetRepo.findOne(datasetName);
		final FormImpl form = datasetRepo.findOneForm(formID);
		final Form currentForm = dataset.getForms().get(dataset.getForms().indexOf(form));

		final List<Form> formsByName = datasetRepo.findAllByName(datasetName, form.getName());

		/* Reset rather than delete if this is the only form item with this name */
		if (formsByName.size() > 1) {
			LOG.debug("Deletion of form: {} was {}", form, dataset.deleteForm(currentForm) ? "successful"
					: "unsuccessful");
		} else {
			LOG.debug("Resetting form: {}", form);
			((FormImpl) currentForm).reset();
			dataset.setForm(currentForm);
		}

		LOG.debug("Saving dataset: {}", dataset);
		datasetRepo.save(dataset);

		redirectAttrs.addFlashAttribute("msgKey", "items.edit.messages.deleted");
		return "redirect:/datasets/" + datasetName;
	}

	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public String addNew(@PathVariable final String datasetName, @MatrixVariable("fn") final String formName,
			final Model model, final RedirectAttributes redirectAttrs) {

		final FormsBundle dataset = datasetRepo.findOne(datasetName);

		/* Get a new 'blank' template from the LibraryAPI */
		try {
			final FormsBundle emptyBundle = libraryAPI.getEmptyStructureForBundle(dataset.getBundleName());

			for (final Form templateForm : emptyBundle.getForms()) {

				if (formName.equals(templateForm.getName())) {
					final Form newForm = FormImpl.copy((FormImpl) templateForm, dataset);

					model.addAttribute("form", newForm);
					model.addAttribute("saveAction", ADD_NEW);
					return "datasets/items/edit";
				}

			}
		} catch (final PreservationException e) {
			LOG.error(e.getMessage(), e);
		}
		/* TODO: add error message */
		return "redirect:/datasets/" + datasetName;
	}

	/**
	 * Edit the first (initially only) item found by formName. Next/previous list IS filtered by formName supplied via
	 * matrix var on datasetName.
	 * 
	 * @param datasetName
	 * @param formName
	 * @param model
	 * @param redirectAttrs
	 * @return
	 */
	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public String editItemsByName(@PathVariable final String datasetName, @MatrixVariable("fn") final String formName,
			final Model model, final RedirectAttributes redirectAttrs) {
		final List<Form> forms = datasetRepo.findAllByName(datasetName, formName);
		model.addAttribute("formName", formName);
		return editItemHelper(forms, forms.get(0), model, redirectAttrs);
	}

	/**
	 * Edit the item identified by the formID. Next/previous MAY be filtered by formName supplied via matrix var on
	 * datasetName
	 * 
	 * @param datasetName
	 * @param formID
	 * @param formName
	 * @param model
	 * @param redirectAttrs
	 * @return
	 */
	@RequestMapping(value = "/{formID}/edit", method = RequestMethod.GET)
	public String editItem(@PathVariable final String datasetName, @PathVariable final int formID,
			@MatrixVariable(value = "fn", required = false, pathVar = "datasetName") final String formName,
			final Model model, final RedirectAttributes redirectAttrs) {

		final Form form = datasetRepo.findOneForm(Integer.valueOf(formID));
		final List<Form> formSubset;

		if (StringUtils.isEmpty(formName)) {
			/* No filtering of list by formName */
			formSubset = datasetRepo.findAllOrderByGrpItemName(datasetName);
		} else {
			model.addAttribute("formName", formName);
			formSubset = datasetRepo.findAllByName(datasetName, formName);
		}

		return editItemHelper(formSubset, form, model, redirectAttrs);
	}

	public String editItemHelper(final List<Form> formSubset, final Form form, final Model model,
			final RedirectAttributes redirectAttrs) {
		final ListIterator<Form> formIterator = formSubset.listIterator();

		while (formIterator.hasNext()) {
			final Form theForm = formIterator.next();
			if (theForm.equals(form)) {
				/* Jump back: */
				formIterator.previous();
				if (formIterator.hasPrevious()) {
					model.addAttribute("previous",
							Integer.valueOf(formSubset.get(formIterator.previousIndex()).getFormID().intValue()));
				}
				/* Jump forward: */
				formIterator.next();
				if (formIterator.hasNext()) {
					model.addAttribute("next",
							Integer.valueOf(formSubset.get(formIterator.nextIndex()).getFormID().intValue()));
				}
			}
		}
		model.addAttribute("saveAction", EDIT);

		final CurationPersistentIdentifier manifestCPID = form.getManifestCPID();
		final Map<DatasetRIL, Set<CoreRIType>> rilMembership = new HashMap<>();

		/* Fetch the current RIL membership for this form instance: */
		for (final DatasetRIL dsRIL : form.getParentBundle().getRils()) {

			final RepresentationInformation[] repInfo = dsRIL.getRil().getRepresentationInformationChildren();

			for (final RepresentationInformation coreRI : repInfo) {

				if (coreRI.getRepresentationInformation() instanceof RepInfoGroup) {
					final RepInfoGroup repInfoGroup = (RepInfoGroup) coreRI.getRepresentationInformation();

					for (final RepresentationInformation ri : repInfoGroup.getRepresentationInformationChildren()) {

						if (ri.getCpid().equals(manifestCPID)) {

							if (!rilMembership.containsKey(dsRIL)) {
								rilMembership.put(dsRIL, new HashSet<CoreRIType>());
							}
							rilMembership.get(dsRIL).add(CoreRIType.fromClass(coreRI.getClass()));
						}

					}

				}
			}
		}
		model.addAttribute("rilMembership", rilMembership);

		model.addAttribute("form", form);
		return "datasets/items/edit";
	}

	@RequestMapping(value = { "/edit", "/{formID}/edit", "/new" }, method = RequestMethod.POST)
	public String saveItem(@ModelAttribute final SaveAction saveAction, @ModelAttribute final Form form,
			final BindingResult result, final SessionStatus status, final RedirectAttributes redirectAttrs,
			@RequestPart(value = "dataHolder", required = false) final MultipartFile dataFile,
			@MatrixVariable(value = "fn", required = false, pathVar = "datasetName") final String formName) {

		formValidator.validate(form, result);
		if (result.hasErrors()) {
			boolean fixErrors = true;

			final List<FieldError> dataHolderErrors = result.getFieldErrors("dataHolder");

			/*
			 * Only ignore errors under very specific conditions, i.e. when there is a single no-file-supplied error and
			 * a current file exists
			 */
			if (result.getErrorCount() == 1 && dataHolderErrors != null && dataHolderErrors.size() == 1) {

				final FieldError dataHolderError = dataHolderErrors.get(0);
				if (FormValidator.FILE_REQ_ERR_CODE.equals(dataHolderError.getCode())
						&& !form.getDataHolderMetadata().isEmpty() && form.getDataHolderType() == BYTESTREAM) {
					fixErrors = false;
				}

			}

			if (fixErrors) {
				return "datasets/items/edit";
			}
		}

		/* Ensure that the dataHolder field is not overwritten when its an empty upload (i.e. re-save) */
		if (form.getDataHolderType() == BYTESTREAM && dataFile != null && !dataFile.isEmpty()) {
			LOG.debug("incoming dataFile: {}", dataFile);
			form.getDataHolderMetadata().put(FILE_NAME, dataFile.getOriginalFilename());
			form.getDataHolderMetadata().put(FILE_SIZE, String.valueOf(dataFile.getSize()));
			form.getDataHolderMetadata().put(FILE_MIMETYPE, dataFile.getContentType());
		} else if (form.getDataHolderType() == URI && !StringUtils.isEmpty(form.getDataHolder())) {
			LOG.debug("incoming dataHolder: {}", form.getDataHolder());
			form.getDataHolderMetadata().clear();
		}

		final FormsBundleImpl dataset = datasetRepo.findOne(form.getParentBundle().getDatasetName());

		if (saveAction == ADD_NEW) {
			dataset.addForm(form);
		} else {
			dataset.setForm(form);
		}

		final FormsBundle savedDataset = datasetRepo.save(dataset);
		final Form savedForm = savedDataset.getForms().get(savedDataset.getForms().indexOf(form));

		status.setComplete();
		redirectAttrs.addFlashAttribute("msgKey", "items.edit.messages.savesuccess");

		final String formNameMatrixVar = StringUtils.isEmpty(formName) ? "" : ";fn=" + formName;
		return "redirect:/datasets/" + savedDataset.getDatasetName() + formNameMatrixVar + "/items/"
				+ savedForm.getFormID() + "/edit";
	}
}
