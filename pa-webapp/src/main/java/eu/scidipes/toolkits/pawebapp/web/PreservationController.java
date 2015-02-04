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

import java.util.Set;

import info.digitalpreserve.interfaces.Registry;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.scidipes.common.framework.FrameworkWrapper;
import eu.scidipes.toolkits.palibrary.exceptions.PreservationException;
import eu.scidipes.toolkits.palibrary.impl.FormsBundleImpl;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationJob;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationManager;
import eu.scidipes.toolkits.pawebapp.model.PreservationJobDTO;
import eu.scidipes.toolkits.pawebapp.repository.DataSetRepository;

/**
 * {@link Controller} encapsulating preservation-related operations
 * 
 * @author Tom Bunting
 * 
 */
@Controller
@RequestMapping("/preservation")
public class PreservationController {

	private static final Logger LOG = LoggerFactory.getLogger(PreservationController.class);

	@Autowired
	private PreservationManager preservationManager;

	@Autowired
	private DataSetRepository datasetRepo;

	@ModelAttribute("datasets")
	public Set<FormsBundle> getAvailableDataSets() {
		return datasetRepo.findAllPreservable();
	}

	@ModelAttribute("enabledRegistries")
	public Set<Registry> getEnabledRegistries() {
		return FrameworkWrapper.getEnabledRegistries();
	}

	@RequestMapping
	public String home() {
		return "preservation/home";
	}

	@RequestMapping(value = "/{datasetName}", method = RequestMethod.GET)
	public String home(@PathVariable final String datasetName, final Model model, final RedirectAttributes redirectAttrs) {

		final FormsBundle dataset = datasetRepo.findOne(datasetName);
		if (dataset != null) {
			model.addAttribute("dataset", dataset);
			return "preservation/home";
		} else {
			redirectAttrs.addFlashAttribute("errorKey", "datasets.preserve.errors.notstarted");
			return "redirect:/datasets";
		}
	}

	@RequestMapping(value = "/preserve", method = RequestMethod.POST)
	public String preserve(@RequestParam final String datasetName, @RequestParam final String registry,
			final RedirectAttributes redirectAttrs, final Model model) {

		final FormsBundleImpl dataset = datasetRepo.findOne(datasetName);

		for (final Registry reg : FrameworkWrapper.getEnabledRegistries()) {

			if (registry.equals(reg.getLocationUID())) {
				try {
					if (!preservationManager.preserve(dataset, reg)) {
						throw new PreservationException("Preservation job could not be started");
					}
				} catch (final PreservationException e) {
					/* Exceptions encountered during the preservation prep phase can be fed back to user immediately */
					LOG.error(e.getMessage(), e);
					redirectAttrs.addFlashAttribute("errorKey", "datasets.preserve.errors.failure");
					return "redirect:/preservation/" + datasetName;
				}
			}

		}
		redirectAttrs.addFlashAttribute("msgKey", "datasets.preserve.messages.started");
		return "redirect:/preservation/" + datasetName;
	}

	@RequestMapping("/jobdetails")
	@ResponseBody
	public PreservationJob jobDetails(@RequestParam final String datasetName) {

		for (final PreservationJob job : preservationManager.getPreservationJobs()) {
			if (job.getDatasetName().equals(datasetName)) {
				return PreservationJobDTO.wrapJob(job);
			}
		}

		throw new IllegalArgumentException("No preservation job found for that dataset");
	}

	@RequestMapping("/jobs/{datasetName}/delete")
	public String deleteJob(@PathVariable final String datasetName, final RedirectAttributes redirectAttrs) {
		if (preservationManager.deleteJob(datasetName)) {
			redirectAttrs.addFlashAttribute("msgKey", "datasets.preserve.jobs.deletesuccess");
		} else {
			redirectAttrs.addFlashAttribute("errorKey", "datasets.preserve.jobs.deletefailed");
		}
		return "redirect:/preservation/";
	}

}
