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

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.scidipes.toolkits.palibrary.exceptions.PreservationException;
import eu.scidipes.toolkits.palibrary.impl.FormsBundleImpl;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;
import eu.scidipes.toolkits.palibrary.interfaces.LibraryAPI;
import eu.scidipes.toolkits.pawebapp.model.DataSetCompletedFormCount;
import eu.scidipes.toolkits.pawebapp.model.FormCountByItemName;
import eu.scidipes.toolkits.pawebapp.repository.DataSetRepository;

/**
 * {@link Controller} encapsulating operations on data sets (collections and individual instances of {@link FormsBundle}
 * )
 * 
 * @author Tom Bunting
 * 
 */
@Controller
@RequestMapping("/datasets")
public class DataSetController {

	private static final Logger LOG = LoggerFactory.getLogger(DataSetController.class);

	@Autowired
	private DataSetRepository datasetRepo;

	@Autowired
	private LibraryAPI libraryAPI;

	/**
	 * Combines the in-progress data sets preserved in the webapp database with any empty bundles <i>not</i> already
	 * saved from {@link LibraryAPI}
	 * 
	 * @return a set of data sets
	 */
	@ModelAttribute("dataSets")
	public Set<DataSetCompletedFormCount> getAvailableDataSets() {
		return datasetRepo.findAllWithCompleteFormsCount();
	}

	/**
	 * Maps requests to the data set home page
	 * 
	 * @param model
	 * @return the path the the dataset home template
	 */
	@RequestMapping
	public String home(final Model model) {
		model.addAttribute("availableBundles", libraryAPI.getAvailableFormsBundles());
		return "datasets/home";
	}

	/**
	 * Checks the existence of a dataset identified by the <code>datasetName</code> argument and deletes it if it
	 * exists. Adds a success or error message to <code>redirectAttrs</code> depending on the outcome.
	 * 
	 * @param datasetName
	 * @param redirectAttrs
	 * @return a redirect link to the dataset home page
	 */
	@RequestMapping("/{datasetName}/delete")
	public String deleteDataset(@PathVariable final String datasetName, final RedirectAttributes redirectAttrs) {
		if (datasetRepo.exists(datasetName)) {
			datasetRepo.delete(datasetName);

			LOG.info("Deleted dataset: [{}]", datasetName);

			redirectAttrs.addFlashAttribute("msgKey", "datasets.delete.succcess");
		} else {
			redirectAttrs.addFlashAttribute("errorKey", "datasets.delete.failure");
		}
		return "redirect:/datasets";
	}

	/**
	 * Creates a new {@link FormsBundle} instance using the <code>datasetName</code> and based on the bundle identified
	 * by <code>bundleName</code>
	 * 
	 * @param datasetName
	 * @param bundleName
	 * @param redirectAttrs
	 * @return a redirect link to the dataset home page
	 */
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public String createNew(@RequestParam final String datasetName, final String bundleName,
			final RedirectAttributes redirectAttrs) {

		if (!datasetRepo.exists(datasetName)) {
			try {
				final FormsBundleImpl newBundle = (FormsBundleImpl) libraryAPI.getEmptyStructureForBundle(bundleName);
				newBundle.setDatasetName(datasetName);
				newBundle.setDisplayName(datasetName);
				datasetRepo.save(newBundle);

				LOG.info("Created new dataset: [{}] using bundle: [{}]", datasetName, newBundle.getBundleName());

				redirectAttrs.addFlashAttribute("msgKey", "datasets.new.messages.sucess");
			} catch (final PreservationException e) {
				LOG.error(e.getMessage(), e);
				redirectAttrs.addFlashAttribute("errorKey", "datasets.new.errors.bundlenotfound");
			}
		} else {
			redirectAttrs.addFlashAttribute("errorKey", "datasets.new.errors.datasetexists");
		}
		return "redirect:/datasets";
	}

	@RequestMapping("/{datasetName}")
	public String showDataSet(@PathVariable final String datasetName, final Model model,
			final RedirectAttributes redirectAttrs) {
		final FormsBundle dataSet = datasetRepo.findOne(datasetName);

		if (dataSet == null) {
			redirectAttrs.addFlashAttribute("errorKey", "datasets.errors.notfound");
			return "redirect:/datasets";
		}

		final Set<FormCountByItemName> formCounts = datasetRepo.findDistinctFormsByBundle(datasetName);
		model.addAttribute("formCounts", formCounts);
		model.addAttribute("dataSet", dataSet);
		model.addAttribute("datasetPath", "/datasets/" + datasetName);
		return "datasets/details";
	}

}
