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
package eu.scidipes.toolkits.palibrary.impl.ontologies;

import info.digitalpreserve.exceptions.RIException;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import eu.scidipes.common.framework.core.impl.CoreCurationPersistentIdentifier;
import eu.scidipes.common.framework.core.impl.CoreRepInfoCategory;
import eu.scidipes.toolkits.palibrary.exceptions.PreservationInitialisationException;
import eu.scidipes.toolkits.palibrary.impl.FormFieldImpl;
import eu.scidipes.toolkits.palibrary.impl.FormImpl;
import eu.scidipes.toolkits.palibrary.impl.FormsBundleImpl;
import eu.scidipes.toolkits.palibrary.interfaces.Form;
import eu.scidipes.toolkits.palibrary.interfaces.FormField;
import eu.scidipes.toolkits.palibrary.interfaces.FormType;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationDatasourceProcessor;

/**
 * Implementation of {@link PreservationDatasourceProcessor} for the Earth Science Preserved Inventory Item ontology.
 * 
 * @author Tom Bunting
 * 
 * @see AbstractOntologyProcessor
 * 
 */
public final class ESOntologyProcessor extends AbstractOntologyProcessor {

	private static final Logger LOG = Logger.getLogger(ESOntologyProcessor.class);

	@Override
	public FormsBundle sourceToBundle(final Object source) throws PreservationInitialisationException {

		final OWLOntology owl = loadOntology(source);

		final OWLReasoner reasoner = reasonOverOntology(owl);

		// Process this particular OWL:
		final IRI ontologyIRI = owl.getOntologyID().getOntologyIRI();

		final String ontologyName = ontologyIRI.getFragment();
		final FormsBundleImpl esaBundle = new FormsBundleImpl(ontologyName, getName());
		esaBundle.setDisplayName(ontologyName);

		final OWLDataFactory fac = manager.getOWLDataFactory();

		final OWLClass campaignPhase = fac.getOWLClass(IRI.create(ontologyIRI + "#CAMPAIGN_PHASE"));
		final NodeSet<OWLClass> subClasses = reasoner.getSubClasses(campaignPhase, true);

		// Get the 'hasPhaseType' object property spec:
		final OWLObjectProperty hasPhaseType = fac.getOWLObjectProperty(IRI.create(ontologyIRI + "#hasPhaseType"));

		// Iterate over each subclass of CAMPAIGN_PHASE
		for (final OWLClass subClass : subClasses.getFlattened()) {
			final NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(subClass, true);

			// Iterate over the individuals of each subclass and create a Form item for each:
			for (final OWLNamedIndividual individual : individuals.getFlattened()) {
				final Set<OWLAnnotation> annotations = individual.getAnnotations(owl);

				String formName = "";
				String displayName = "";
				String introText = "";

				// Iterate over the annotations of each individual and pick out what we need:
				for (final OWLAnnotation annotation : annotations) {
					final OWLAnnotationValue value = annotation.getValue();
					final IRI propIRI = annotation.getProperty().getIRI();
					if (value instanceof OWLLiteral) {

						final OWLLiteral literalVal = (OWLLiteral) value;

						if (LOG.isTraceEnabled()) {
							LOG.trace("    " + propIRI.getFragment() + ": " + literalVal.getLiteral());
						}

						if (annotation.getProperty().isLabel()) {
							displayName = literalVal.getLiteral();
						} else if ("name".equals(propIRI.getFragment())) {
							/* Trim any trailing whitespace from name which may cause problems with queries: */
							formName = literalVal.getLiteral().trim();
						} else if ("description".equals(propIRI.getFragment())) {
							introText = literalVal.getLiteral();
						}
					}
				}
				final FormImpl form = new FormImpl(formName, esaBundle);
				form.setDisplayName(displayName);
				form.setIntroText(introText);
				final String group = subClass.getIRI().getFragment();
				form.setGroup(group);

				/*
				 * TODO: Update ontology to include annotation for group display ordering on classes
				 */
				switch (group) {
				case "CAMPAIGN_PREPARATION":
					form.setGroupOrder(1);
					break;
				case "CAMPAIGN_OPERATION":
					form.setGroupOrder(2);
					break;
				case "POST_CAMPAIGN":
					form.setGroupOrder(3);
					break;
				default:
					form.setGroupOrder(4);
				}

				final NodeSet<OWLNamedIndividual> phaseTypeNodeSet = reasoner.getObjectPropertyValues(individual,
						hasPhaseType);
				if (phaseTypeNodeSet.isSingleton()) {
					final OWLNamedIndividual phaseType = phaseTypeNodeSet.getFlattened().iterator().next();

					if (LOG.isTraceEnabled()) {
						LOG.trace("PHASE TYPE: " + phaseType.getIRI());
					}

					// The ES ontology contains classes and individuals with the same IRI and a lower or upper case
					// fragment (DOCUMENT, SOFTWARE, DATA)
					final String namespace = phaseType.getIRI().getNamespace();
					final String fragment = phaseType.getIRI().getFragment().toUpperCase();
					addFlexFields(owl, IRI.create(namespace, fragment), form);

					// Set RIL-CPID and Category defaults
					try {
						switch (fragment) {
						case "DOCUMENT":
							// Postscript (PDF)
							form.setType(FormType.DOCUMENT);
							form.setRILCPID(new CoreCurationPersistentIdentifier("64AD0E19-502C-4C2C-9F34-26C289C88E7D"));
							form.getCategories().add(new CoreRepInfoCategory("Semantic/Document"));
							break;
						case "DATA":
							// ZIP (TODO: update)
							form.setType(FormType.DATA);
							form.setRILCPID(new CoreCurationPersistentIdentifier("3A50B481-682A-48A0-9409-BA831EF887D6"));
							form.getCategories().add(new CoreRepInfoCategory("Semantic/Data"));
							break;
						case "SOFTWARE":
							// Java
							form.setType(FormType.SOFTWARE);
							form.setRILCPID(new CoreCurationPersistentIdentifier("F278753F-5F7D-4232-B428-81BB139B104A"));
							form.getCategories().add(new CoreRepInfoCategory("Other/Software"));
							break;
						default:
							form.setRILCPID(new CoreCurationPersistentIdentifier("MISSING"));
							form.getCategories().add(new CoreRepInfoCategory("Other"));
						}
					} catch (final RIException riEx) {
						LOG.error(riEx.getMessage(), riEx);
					}
				}

				esaBundle.addForm(form);
			}
		}
		return esaBundle;
	}

	private void addFlexFields(final OWLOntology owl, final IRI classIRI, final Form form)
			throws PreservationInitialisationException {
		final List<FormField> fields = form.getFormFields();

		final OWLDataFactory fac = manager.getOWLDataFactory();
		final OWLClass inventoryItemSubclass = fac.getOWLClass(classIRI);
		final Set<OWLAnnotation> annotations = inventoryItemSubclass.getAnnotations(owl);

		for (final OWLAnnotation annotation : annotations) {
			final OWLAnnotationValue value = annotation.getValue();
			final IRI propIRI = annotation.getProperty().getIRI();

			if (value instanceof OWLLiteral) {
				final OWLLiteral literalVal = (OWLLiteral) value;
				final FormFieldImpl field = new FormFieldImpl(propIRI.getFragment(), form);
				field.setDefaultValue(literalVal.getLiteral());

				if (LOG.isTraceEnabled()) {
					LOG.trace("    ADDED FLEX FIELD: " + propIRI.getFragment() + ": " + literalVal.getLiteral());
				}
				fields.add(field);
			}
		}
	}

	@Override
	public String getName() {
		return "ES-PDSC Ontology Processor";
	}

}
