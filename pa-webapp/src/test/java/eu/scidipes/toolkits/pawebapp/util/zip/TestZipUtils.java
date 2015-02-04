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
package eu.scidipes.toolkits.pawebapp.util.zip;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.scidipes.toolkits.pa.test.AbstractTest;
import eu.scidipes.toolkits.palibrary.utils.zip.ZipUtils;
import eu.scidipes.toolkits.palibrary.utils.zip.ZipUtils.ByteArrayZipEntry;

/**
 * @author Tom Bunting
 * 
 */
public class TestZipUtils extends AbstractTest {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(TestZipUtils.class);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link eu.scidipes.toolkits.palibrary.utils.zip.ZipUtils#byteArrayZipEntriesToBase64(java.util.Set)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public final void testByteArrayZipEntriesToBase64() throws IOException {
		final String mainFileName = "SCIDIP-ES-Registry-Framework-Overview.pdf";
		final String metaFileName = "meta.xml";

		final InputStream mainFileStream = getClass().getResourceAsStream(mainFileName);
		final InputStream metaFileStream = getClass().getResourceAsStream(metaFileName);

		final byte[] mainFileBytes = IOUtils.toByteArray(mainFileStream);
		final byte[] metaFileBytes = IOUtils.toByteArray(metaFileStream);

		final ZipEntry zipMain = new ZipEntry(mainFileName);
		final ZipEntry zipMeta = new ZipEntry(metaFileName);

		final Set<ByteArrayZipEntry> entries = new HashSet<>();
		entries.add(new ByteArrayZipEntry(mainFileBytes, zipMain));
		entries.add(new ByteArrayZipEntry(metaFileBytes, zipMeta));

		final String zipResult = ZipUtils.byteArrayZipEntriesToBase64(entries);
		assertTrue(zipResult != null && zipResult.length() > 100);
	}

}
