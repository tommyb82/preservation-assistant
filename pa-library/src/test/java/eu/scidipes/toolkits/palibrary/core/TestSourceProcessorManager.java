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

import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import eu.scidipes.toolkits.pa.test.AbstractTest;
import eu.scidipes.toolkits.palibrary.interfaces.PreservationDatasourceProcessor;

/**
 * @author Tom Bunting
 * 
 */
public class TestSourceProcessorManager extends AbstractTest {

	/** Logger */
	private static final Logger LOG = Logger.getLogger(TestSourceProcessorManager.class);

	private final SourceProcessorManager sourceManager = SourceProcessorManager.INSTANCE;

	/**
	 * Test method for {@link eu.scidipes.toolkits.palibrary.core.SourceProcessorManager#reload()}.
	 */
	@Test
	public final void testReload() {
		sourceManager.reload();
		final Map<String, PreservationDatasourceProcessor> processors = sourceManager.getProcessors();
		LOG.debug(processors);

		assertEquals(3, processors.size());
	}

	/**
	 * Test method for {@link eu.scidipes.toolkits.palibrary.core.SourceProcessorManager#getProcessors()}.
	 */
	@Test
	public final void testGetProcessors() {
		final Map<String, PreservationDatasourceProcessor> processors = sourceManager.getProcessors();
		assertEquals(DummyProcessor.class, processors.get("Dummy processor").getClass());
	}
}
