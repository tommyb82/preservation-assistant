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

import java.io.File;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.MissingImportEvent;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.MissingImportListener;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import eu.scidipes.toolkits.palibrary.exceptions.PreservationInitialisationException;
import eu.scidipes.toolkits.palibrary.impl.AbstractFileSourceProcessor;

/**
 * Provides base OWL API operations common to all concrete {@link Ontology} processors
 * 
 * @author Tom Bunting
 * 
 * @see <a href="http://owlapi.sourceforge.net/">http://owlapi.sourceforge.net/</a>
 * @see <a href="http://sourceforge.net/projects/owlapi/">http://sourceforge.net/projects/owlapi/</a>
 * 
 */
public abstract class AbstractOntologyProcessor extends AbstractFileSourceProcessor {

	private static final Logger LOG = Logger.getLogger(AbstractOntologyProcessor.class);

	protected final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	/**
	 * Creates a configured {@link OWLReasoner} instance and uses it to reason over the give ontology. Returns the
	 * reasoner for further use.
	 * 
	 * @param owl
	 * @return the owl reasoner object
	 */
	protected OWLReasoner reasonOverOntology(final OWLOntology owl) {

		final OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		final ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();

		final OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);

		final OWLReasoner reasoner = reasonerFactory.createReasoner(owl, config);

		reasoner.precomputeInferences();

		if (reasoner.isConsistent()) {
			LOG.trace("Loaded consistent ontology: " + owl);
		} else {
			LOG.warn("Inconsistent ontology loaded, could be problematic: " + owl);
		}

		return reasoner;
	}

	protected OWLOntology loadOntology(final Object source) throws PreservationInitialisationException {

		if (!(source instanceof File)) {
			throw new PreservationInitialisationException("Ontology source object must be an instance of: "
					+ File.class.getName());
		}

		final FileDocumentSource owlDocSource = new FileDocumentSource((File) source);
		final OWLOntology owl;

		// Get hold of an ontology manager, and load the ontology file
		final OWLOntologyLoaderConfiguration owlLoaderConfig = new OWLOntologyLoaderConfiguration();

		// Loading stops completely if imports fail, so create a listener to log these events
		final MissingImportListener missingImportListener = new MissingImportListener() {
			@Override
			public void importMissing(final MissingImportEvent event) {
				LOG.warn("Import failed for URI: " + event.getImportedOntologyURI().toQuotedString(),
						event.getCreationException());
			}
		};

		manager.addMissingImportListener(missingImportListener);

		try {
			// Load the ontology with silent handling of missing imports
			owl = manager.loadOntologyFromOntologyDocument(owlDocSource,
					owlLoaderConfig.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT));
		} catch (final OWLOntologyCreationException e) {
			throw new PreservationInitialisationException(e.toString(), e);
		}

		return owl;
	}

}
