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
package eu.scidipes.toolkits.pawebapp.web;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.scidipes.toolkits.palibrary.core.FormBundleManager;
import eu.scidipes.toolkits.palibrary.core.SourceProcessorManager;
import eu.scidipes.toolkits.palibrary.exceptions.PreservationException;
import eu.scidipes.toolkits.palibrary.impl.FormsBundleImpl;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;
import eu.scidipes.toolkits.palibrary.interfaces.LibraryAPI;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationDatasourceProcessor;
import eu.scidipes.toolkits.pawebapp.repository.DataSetRepository;

/**
 * Controller class for all admin functions
 * 
 * @author Tom Bunting
 * 
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

	/* Message and error keys */
	private static final String SAVE_FAIL = "templates.edit.errors.savefailed";
	private static final String SAVE_FAIL_FILE_EXISTS = "templates.edit.errors.savefailed.fileexists";
	private static final String SAVE_SUCCESS = "templates.edit.messages.savesuccess";
	private static final String DELETE_SUCCESS = "templates.edit.messages.deletesuccess";
	private static final String DELETE_FAIL = "templates.edit.messages.deletefail";
	private static final String DELETE_FAIL_IN_USE = "templates.edit.messages.deletefailinuse";

	private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

	private static final String SOURCE_ROOT_PATH = System.getProperty("pa.sources.path");

	@Autowired
	private DataSetRepository datasetRepo;

	@Autowired
	private LibraryAPI libraryAPI;

	@RequestMapping
	public String home() {
		return "/admin/home";
	}

	@RequestMapping("/templates/deletebundle")
	public String deleteTemplateBundle(final String bundleName, final RedirectAttributes redirectAttrs) {

		final Long datasetCount = datasetRepo.countDatasetsByBundle(bundleName);

		if (datasetCount.longValue() > 0) {
			LOG.info("Cannot delete bundle '{}' in use by {} datasets", bundleName, datasetCount.toString());
			redirectAttrs.addFlashAttribute("errorKey", DELETE_FAIL_IN_USE);
			return "redirect:/admin/templates/";
		}

		boolean success = false;

		try {
			final FormsBundle bundleToDelete = libraryAPI.getEmptyStructureForBundle(bundleName);

			/* Attempt to delete the template source file first: */
			final String processorName = bundleToDelete.getProcessorName();

			final PreservationDatasourceProcessor processor = SourceProcessorManager.INSTANCE.getProcessors().get(
					processorName);

			final StringBuilder sourcePath = new StringBuilder();
			sourcePath.append(SOURCE_ROOT_PATH + File.separatorChar);
			sourcePath.append(processor.getClass().getSimpleName() + File.separatorChar);
			sourcePath.append(bundleToDelete.getTemplateSource());

			final File source = new File(sourcePath.toString());

			if (source.delete()) {
				LOG.info("Successfully deleted source file: {}", source);
				success = libraryAPI.deleteBundle(bundleToDelete);
			}

		} catch (final PreservationException e) {
			LOG.error(e.toString(), e);
		}

		if (success) {
			LOG.info("Successfully deleted bundle: {}", bundleName);
			redirectAttrs.addFlashAttribute("msgKey", DELETE_SUCCESS);
			return "redirect:/admin/templates/";
		} else {
			redirectAttrs.addFlashAttribute("errorKey", DELETE_FAIL);
			return "redirect:/admin/templates/";
		}

	}

	@RequestMapping(value = "/templates", method = RequestMethod.GET)
	public String templates(final Model model) {
		model.addAttribute("processorMap", SourceProcessorManager.INSTANCE.getProcessors());
		model.addAttribute("bundles", FormBundleManager.getDiscoveredBundles());
		return "/admin/templates";
	}

	@RequestMapping(value = "/templates", method = RequestMethod.POST)
	public String saveTemplate(final RedirectAttributes redirectAttrs, final String processorName,
			@RequestPart(value = "source") final MultipartFile sourceFile) {

		final PreservationDatasourceProcessor processor = SourceProcessorManager.INSTANCE.getProcessors().get(
				processorName);

		final StringBuilder destinationPath = new StringBuilder();
		destinationPath.append(SOURCE_ROOT_PATH + File.separatorChar);
		destinationPath.append(processor.getClass().getSimpleName() + File.separatorChar);
		destinationPath.append(sourceFile.getOriginalFilename());

		final File destination = new File(destinationPath.toString());

		if (destination.exists()) {
			redirectAttrs.addFlashAttribute("errorKey", SAVE_FAIL_FILE_EXISTS);
			return "redirect:/admin/templates/";
		}

		try {
			sourceFile.transferTo(destination);
			final FormsBundle bundle = processor.sourceToBundle(destination);
			((FormsBundleImpl) bundle).setTemplateSource(destination.getName());
			FormBundleManager.addBundle(bundle);

			LOG.info("Created new template bundle for processor: {}", processor.getName());

		} catch (final IOException | PreservationException e) {

			LOG.warn("Exception encountered creating bundle from: {}, deleting file: {}.", destination,
					Boolean.valueOf(destination.delete()));

			LOG.error(e.toString(), e);
			redirectAttrs.addFlashAttribute("errorKey", SAVE_FAIL);
			return "redirect:/admin/templates/";
		}

		redirectAttrs.addFlashAttribute("msgKey", SAVE_SUCCESS);
		return "redirect:/admin/templates/";
	}
}
