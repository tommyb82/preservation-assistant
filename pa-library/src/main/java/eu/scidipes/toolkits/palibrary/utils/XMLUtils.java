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
package eu.scidipes.toolkits.palibrary.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.scidipes.toolkits.palibrary.exceptions.PARuntimeException;
import eu.scidipes.toolkits.palibrary.interfaces.FormField;

/**
 * @author Tom Bunting
 * 
 */
public final class XMLUtils {

	private static final Logger LOG = LoggerFactory.getLogger(XMLUtils.class);

	private static TransformerFactory transformerFactory;
	private static DocumentBuilderFactory documentBuilderFactory;

	static {
		transformerFactory = TransformerFactory.newInstance();
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
	}

	private XMLUtils() {
		/* Private constructor */
	}

	/**
	 * Transforms a collection of {@link FormField} objects into a simple XML document
	 * 
	 * @param fields
	 *            a collection of form fields
	 * @return an xml document representing the form fields, or null if the passed collection is null or empty, or all
	 *         form fields have an empty value
	 */
	public static Node formFieldsToMetadata(final Collection<? extends FormField> fields) {
		if (fields != null && !fields.isEmpty()) {
			try {
				final Document xml;

				synchronized (documentBuilderFactory) {
					xml = documentBuilderFactory.newDocumentBuilder().newDocument();
				}

				xml.setXmlStandalone(true);
				final Element metaData = xml.createElement("metadata");
				int completedFieldCount = 0;

				for (final FormField field : fields) {
					if (!StringUtils.isEmpty(field.getValue())) {
						completedFieldCount++;
						final Element metaDataEntry = xml.createElement("metadataentry");
						final Element entryName = xml.createElement("entryname");
						final Element entryValue = xml.createElement("entryvalue");

						entryName.setTextContent(field.getDisplayName());
						entryValue.setTextContent(field.getValue());

						metaDataEntry.appendChild(entryName);
						metaDataEntry.appendChild(entryValue);
						metaData.appendChild(metaDataEntry);
					}
				}

				if (completedFieldCount > 0) {
					xml.appendChild(metaData);
					return xml;
				} else {
					return null;
				}

			} catch (final ParserConfigurationException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return null;
	}

	/**
	 * Delegates to {@link DocumentBuilder#parse(java.io.InputStream)} to convert XML {@link File} contents into a
	 * {@link Node} instance
	 * 
	 * @param file
	 * @return the document node
	 * @throws IllegalStateException
	 *             containing details of any exceptions encountered during processing
	 */
	public static Node fileToDoc(final File file) {
		try {
			final DocumentBuilder dBuilder = documentBuilderFactory.newDocumentBuilder();
			return dBuilder.parse(new FileInputStream(file));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new PARuntimeException(e.toString(), e);
		}
	}

	/**
	 * Delegates to {@link DocumentBuilder#parse(String)} to convert XML string contents into a {@link Node} instance
	 * 
	 * @param source
	 * @return the document node
	 * @throws IllegalStateException
	 *             containing details of any exceptions encountered during processing
	 */
	public static Node stringToDoc(final String source) {
		try {
			final DocumentBuilder dBuilder = documentBuilderFactory.newDocumentBuilder();
			return dBuilder.parse(source);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new PARuntimeException(e.toString(), e);
		}
	}

	/**
	 * Converts a {@Node} instance into a readable <code>String</code> with optional formatting and XML
	 * declaration
	 * 
	 * @param doc
	 * @param indentAndFormat
	 * @param omitXmlDeclaration
	 * @return the node contents as a String
	 */
	public static String nodeToString(final Node doc, final boolean indentAndFormat, final boolean omitXmlDeclaration) {
		final Properties outputProperties = new Properties();
		outputProperties.put(OutputKeys.INDENT, indentAndFormat ? "yes" : "no");
		outputProperties.put(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration ? "yes" : "no");
		outputProperties.put("{http://xml.apache.org/xslt}indent-amount", indentAndFormat ? "2" : "0");
		return nodeToString(doc, outputProperties);
	}

	private static String nodeToString(final Node doc, final Properties outputProperties) {
		try {
			final Transformer transformer = transformerFactory.newTransformer();
			outputProperties.put(OutputKeys.METHOD, "xml");
			outputProperties.put(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperties(outputProperties);
			final StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			return writer.getBuffer().toString();
		} catch (final TransformerException e) {
			LOG.error(e.getMessage(), e);
		}
		return "";
	}

}
