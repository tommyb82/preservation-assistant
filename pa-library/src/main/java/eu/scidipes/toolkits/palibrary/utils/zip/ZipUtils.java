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
package eu.scidipes.toolkits.palibrary.utils.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * @author Tom Bunting
 * 
 */
public final class ZipUtils {

	public static class ByteArrayZipEntry {

		private final byte[] bytes;

		private final ZipEntry zipEntry;

		public ByteArrayZipEntry(final byte[] bytes, final ZipEntry zipEntry) {
			this.bytes = Arrays.copyOf(bytes, bytes.length);
			this.zipEntry = zipEntry;
		}

		/**
		 * @return the bytes
		 */
		private byte[] getBytes() {
			return bytes;
		}

		/**
		 * @return the zipEntry
		 */
		private ZipEntry getZipEntry() {
			return zipEntry;
		}

	}

	private ZipUtils() {
		/* Private constructor */
	}

	/**
	 * Zips one or more {@link ByteArrayZipEntry} objects together and returns the archive as a base64-encoded
	 * <code>String</code>
	 * 
	 * @param entries
	 * @return a base64-encoded <code>String</code> which is the zip archive of the passed entries
	 * 
	 * @throws IOException
	 *             in the event of an exception writing to the zip output stream
	 * @throws IllegalArgumentException
	 *             if passed entries is null or empty
	 */
	public static String byteArrayZipEntriesToBase64(final Set<? extends ByteArrayZipEntry> entries) throws IOException {
		if (entries == null || entries.isEmpty()) {
			throw new IllegalArgumentException("entries cannot be null or empty");
		}

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (final ZipOutputStream zos = new ZipOutputStream(bos)) {
			for (final ByteArrayZipEntry entry : entries) {
				zos.putNextEntry(entry.getZipEntry());
				IOUtils.write(entry.getBytes(), zos);
				zos.closeEntry();
			}
		}

		return Base64.encodeBase64String(bos.toByteArray());
	}

}
