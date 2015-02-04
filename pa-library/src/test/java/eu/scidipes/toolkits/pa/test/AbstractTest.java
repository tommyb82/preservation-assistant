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
package eu.scidipes.toolkits.pa.test;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abtract test class to be extended by PA test cases. Defines common logging etc
 * 
 * @author Tom Bunting
 * 
 */
public abstract class AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class);

	/**
	 * Adds log lines containing method names to separate test results
	 * 
	 * @return A {@link TestWatcher} that adds log lines containing method names
	 */
	@Rule
	public TestRule testWatcher() {
		return new TestWatcher() {

			@Override
			protected void finished(final Description description) {
				LOG.info("End of test {}.{}", description.getTestClass().getSimpleName(), description.getMethodName());
			}

			@Override
			protected void starting(final Description description) {
				LOG.info("Starting test {}.{}", description.getTestClass().getSimpleName(), description.getMethodName());
			}
		};
	}

}
