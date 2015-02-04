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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.scidipes.toolkits.pa.test.AbstractTest;

/**
 * Unit tests for {@link DatasetRIL}
 * 
 * @author Tom Bunting
 * 
 */
public class TestDatasetRIL extends AbstractTest {

	private static final Logger LOG = Logger.getLogger(TestDatasetRIL.class);

	private DatasetRIL ril1;
	private DatasetRIL ril2;
	private DatasetRIL ril3;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		final UploadRepInfoLabel dummyRIL = new UploadRepInfoLabel(IOUtils.toString(getClass().getResourceAsStream(
				"EmptyRIL.xml")));

		ril1 = new DatasetRIL("0000-0000-0123-5678", "Empty RIL1", dummyRIL);
		ril2 = new DatasetRIL("0000-0000-0123-5679", "Empty RIL2", dummyRIL);
		ril3 = new DatasetRIL("0000-0000-0123-5678", "Empty RIL3", dummyRIL);
	}

	/**
	 * Test method for {@link eu.scidipes.toolkits.palibrary.impl.DatasetRIL#hashCode()}.
	 */
	@Test
	public final void testHashCode() {

		final int ril1Hash = ril1.hashCode();
		final int ril2Hash = ril2.hashCode();
		final int ril3Hash = ril3.hashCode();

		LOG.debug("ril1Hash: " + ril1Hash + ", ril2Hash: " + ril2Hash + ", ril3Hash: " + ril3Hash);

		assertEquals(ril1.hashCode(), ril3.hashCode());
		assertNotEquals(ril1.hashCode(), ril2.hashCode());
	}

	/**
	 * Test method for {@link eu.scidipes.toolkits.palibrary.impl.DatasetRIL#getRilCPID()}.
	 */
	@Test
	public final void testGetRilCPID() {
		assertNotNull(ril1.getRilCPID());
	}

	/**
	 * Test method for {@link eu.scidipes.toolkits.palibrary.impl.DatasetRIL#getRilName()}.
	 */
	@Test
	public final void testGetRilName() {
		assertNotNull(ril1.getRilName());
	}

	/**
	 * Test method for {@link eu.scidipes.toolkits.palibrary.impl.DatasetRIL#getRil()}.
	 */
	@Test
	public final void testGetRil() {
		assertNotNull(ril1.getRil());
	}

	/**
	 * Test method for {@link eu.scidipes.toolkits.palibrary.impl.DatasetRIL#equals(java.lang.Object)}.
	 */
	@Test
	public final void testEqualsObject() {
		assertEquals(ril1, ril3);
		assertNotEquals(ril1, ril2);
	}

	/**
	 * Test method for {@link eu.scidipes.toolkits.palibrary.impl.DatasetRIL#toString()}.
	 */
	@Test
	public final void testToString() {
		assertEquals("DatasetRIL 'Empty RIL1' [0000-0000-0123-5678]", ril1.toString());
	}

}
