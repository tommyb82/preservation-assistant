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
package eu.scidipes.toolkits.palibrary.impl;

import static eu.scidipes.toolkits.palibrary.utils.MiscUtils.HASH_INIT;
import static eu.scidipes.toolkits.palibrary.utils.MiscUtils.HASH_PRIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.digitalpreserve.interfaces.CurationPersistentIdentifier;
import info.digitalpreserve.interfaces.Manifest;
import info.digitalpreserve.interfaces.RepInfoCategory;
import info.digitalpreserve.interfaces.RepInfoGroup;
import info.digitalpreserve.interfaces.RepresentationInformation;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.scidipes.common.framework.core.impl.CoreCurationPersistentIdentifier;
import eu.scidipes.common.framework.core.impl.CoreOtherRepInfo;
import eu.scidipes.common.framework.core.impl.CoreRepInfoCategory;
import eu.scidipes.common.framework.core.impl.CoreStructureRepInfo;
import eu.scidipes.toolkits.pa.test.AbstractTest;
import eu.scidipes.toolkits.palibrary.impl.UploadRepInfoLabel.CoreRIType;

/**
 * Unit tests for {@link UploadRepInfoLabel}
 * 
 * @author Tom Bunting
 * 
 */
public class TestUploadRepInfoLabel extends AbstractTest {

	private static final Logger LOG = Logger.getLogger(TestUploadRepInfoLabel.class);

	private UploadRepInfoLabel ril;

	private Manifest man1;

	private final CurationPersistentIdentifier cpid = new CoreCurationPersistentIdentifier("0000-0000-0123-5678");

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		ril = new UploadRepInfoLabel("Test RIL", "This is a test RIL", cpid);

		final Set<RepInfoCategory> categories = new HashSet<RepInfoCategory>();
		categories.add(new CoreRepInfoCategory("Semantic/Document"));

		man1 = mock(Manifest.class);
		when(man1.getManifestCpid()).thenReturn(new CoreCurationPersistentIdentifier("MAN1-0000-SOME-PDF"));
		when(man1.getRILCpid()).thenReturn(new CoreCurationPersistentIdentifier("RIL-PDF_1-4_in_English"));
		when(man1.getCategories()).thenReturn(categories);
	}

	/**
	 * Test method for {@link UploadRepInfoLabel#hashCode()}.
	 */
	@Test
	public final void testHashCode() {
		final int expected = HASH_INIT * HASH_PRIME + ril.getCpid().hashCode();
		assertEquals(expected, ril.hashCode());
	}

	/**
	 * Test method for {@link UploadRepInfoLabel#UploadRepInfoLabel(String)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public final void testUploadRepInfoLabelString() throws IOException {
		final String repInfoStr = IOUtils.toString(getClass().getResourceAsStream("TestRIL1.xml"));
		final UploadRepInfoLabel xmlDerivedRIL = new UploadRepInfoLabel(repInfoStr);

		assertEquals(3, xmlDerivedRIL.getRepresentationInformationChildren().length);
	}

	/**
	 * Test method for {@link UploadRepInfoLabel#UploadRepInfoLabel(String, String, CurationPersistentIdentifier)} .
	 */
	@Test
	public final void testUploadRepInfoLabelStringStringCurationPersistentIdentifier() {
		final UploadRepInfoLabel convenienceRIL = new UploadRepInfoLabel("Test RIL", "This is a test RIL", cpid);

		assertEquals(3, convenienceRIL.getRepresentationInformationChildren().length);
	}

	/**
	 * Test method for {@link UploadRepInfoLabel#addRepInfo(Manifest)} .
	 */
	@Test
	public final void testAddRepinfoManifest() {
		ril.addRepInfo(man1, UploadRepInfoLabel.CoreRIType.OTHER);
		LOG.debug("Added mock Manifest to Other repinfo in ril");

		final RepresentationInformation[] coreOtherRI = retrieveCoreOtherRIFromRIL();

		assertEquals(1, coreOtherRI.length);
		assertEquals(man1.getManifestCpid(), coreOtherRI[0].getCpid());
	}

	private RepresentationInformation[] retrieveCoreStructureRIFromRIL() {
		final RepresentationInformation[] repInfo = ril.getRepresentationInformationChildren();

		for (final RepresentationInformation coreRI : repInfo) {
			if (coreRI instanceof CoreStructureRepInfo) {
				final RepInfoGroup repInfoGroup = (RepInfoGroup) coreRI.getRepresentationInformation();

				return repInfoGroup.getRepresentationInformationChildren();
			}
		}
		throw new IllegalStateException("Unable to retrieve core structural RI from ril");
	}

	private RepresentationInformation[] retrieveCoreOtherRIFromRIL() {
		final RepresentationInformation[] repInfo = ril.getRepresentationInformationChildren();

		for (final RepresentationInformation coreRI : repInfo) {
			if (coreRI instanceof CoreOtherRepInfo) {
				final RepInfoGroup repInfoGroup = (RepInfoGroup) coreRI.getRepresentationInformation();

				return repInfoGroup.getRepresentationInformationChildren();
			}
		}
		throw new IllegalStateException("Unable to retrieve core other RI from ril");
	}

	/**
	 * Test method for {@link UploadRepInfoLabel#removeRepInfo(Manifest)} .
	 */
	@Test
	public final void testRemoveRepInfoManifest() {
		ril.addRepInfo(man1, CoreRIType.STRUCTURAL);
		ril.removeRepInfo(man1, CoreRIType.STRUCTURAL);

		LOG.trace(ril.serialiseToString());

		final RepresentationInformation[] coreStructuralRI = retrieveCoreStructureRIFromRIL();
		assertTrue(coreStructuralRI.length == 0);
	}

}
