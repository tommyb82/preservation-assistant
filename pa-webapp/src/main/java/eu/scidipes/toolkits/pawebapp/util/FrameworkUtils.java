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
package eu.scidipes.toolkits.pawebapp.util;

import info.digitalpreserve.exceptions.RIException;
import info.digitalpreserve.interfaces.CurationPersistentIdentifier;
import info.digitalpreserve.interfaces.RepInfoCategory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.commons.collections4.bidimap.DualTreeBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.scidipes.common.framework.FrameworkWrapper;
import eu.scidipes.common.framework.core.impl.CoreCurationPersistentIdentifier;
import eu.scidipes.toolkits.palibrary.interfaces.FormType;

/**
 * Utility methods where necessary in front of the {@link FrameworkWrapper} API
 * 
 * @author Tom Bunting
 * 
 */
public class FrameworkUtils {

	private static final Logger LOG = LoggerFactory.getLogger(FrameworkUtils.class);

	private FrameworkUtils() {
		/* No instantiation */
	}

	/**
	 * Retrieves a <code>List</code> of {@link RepInfoCategory}s via the Framework which is sorted by ascending
	 * alphabetical order based on the category's name.
	 * 
	 * @return a list of categories sorted by name
	 */
	public static List<RepInfoCategory> getSortedCategories() {
		final Comparator<RepInfoCategory> ricComparator = new Comparator<RepInfoCategory>() {
			@Override
			public int compare(final RepInfoCategory cat1, final RepInfoCategory cat2) {
				return cat1.getName().compareTo(cat2.getName());
			}
		};

		final List<RepInfoCategory> categories = new ArrayList<>(FrameworkWrapper.getAllPredefinedCategories());
		Collections.sort(categories, ricComparator);
		return categories;
	}

	/**
	 * Returns a map that holds an inner map per FormType (e.g. Document, Software etc) of user friendly type-names
	 * (e.g. 'PDF') to actual RILs
	 * 
	 * @return a map of type 'groups' to 'friendly' type names to RILs
	 * @throws IOException
	 */
	public static Map<FormType, Map<String, CurationPersistentIdentifier>> getRILsByType() throws IOException {

		/* The return Map, e.g. "DOC" -> "PDF" - RIL1 */
		final Map<FormType, Map<String, CurationPersistentIdentifier>> rilMap = new HashMap<>();

		final Properties props = new Properties();
		props.load(FrameworkUtils.class.getResourceAsStream("/rin/doc_type_rilcpids.properties"));
		populateMapFromRILProps(props, rilMap, FormType.DOCUMENT);

		props.clear();
		props.load(FrameworkUtils.class.getResourceAsStream("/rin/software_langs_rilcpids.properties"));
		populateMapFromRILProps(props, rilMap, FormType.SOFTWARE);

		props.clear();
		props.load(FrameworkUtils.class.getResourceAsStream("/rin/data_type_rilcpids.properties"));
		populateMapFromRILProps(props, rilMap, FormType.DATA);

		if (LOG.isTraceEnabled()) {
			for (final Entry<FormType, Map<String, CurationPersistentIdentifier>> outerMap : rilMap.entrySet()) {
				for (final Entry<String, CurationPersistentIdentifier> innerMap : outerMap.getValue().entrySet()) {
					LOG.trace(innerMap.getKey() + ": " + innerMap.getValue());
				}
			}
		}
		return rilMap;
	}

	private static void populateMapFromRILProps(final Properties props,
			final Map<FormType, Map<String, CurationPersistentIdentifier>> rilMap, final FormType key) {
		final Map<String, CurationPersistentIdentifier> innerMap = new DualTreeBidiMap<>();

		for (final Object o : props.keySet()) {
			final String innerKey = (String) o;
			try {
				final CurationPersistentIdentifier cpid = new CoreCurationPersistentIdentifier(
						props.getProperty(innerKey));

				/* If corresponding RIL does not exist on an enabled registry, the CPID won't be added */
				innerMap.put(innerKey, FrameworkWrapper.getRepInfoLabel(cpid).getCpid());
			} catch (final RIException e) {
				LOG.warn(e.getMessage());
			}
		}

		rilMap.put(key, innerMap);
	}

}
