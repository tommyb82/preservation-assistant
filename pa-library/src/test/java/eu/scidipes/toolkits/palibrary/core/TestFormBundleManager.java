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
package eu.scidipes.toolkits.palibrary.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.scidipes.toolkits.pa.test.AbstractTest;
import eu.scidipes.toolkits.palibrary.interfaces.Bundle;

/**
 * @author Tom Bunting
 * 
 */
public class TestFormBundleManager extends AbstractTest {

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(TestFormBundleManager.class);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		System.setProperty("pa.ontology.path", "C:\\Temp\\ontologies\\ESA.owl");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link eu.scidipes.toolkits.palibrary.core#getDiscoveredBundles()}.
	 */
	@Test
	@Ignore
	public final void testGetDiscoveredBundles() {
		final Set<? extends Bundle> bundles = FormBundleManager.getDiscoveredBundles();
		assertEquals(1, bundles.size());
	}

	/**
	 * Test method for {@link eu.scidipes.toolkits.palibrary.core#getEmptyBundleFor(java.lang.String)} .
	 */
	@Test
	@Ignore
	public final void testGetEmptyBundleFor() {
		assertTrue(true);
	}
}
