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
package eu.scidipes.toolkits.pawebapp.web.lifecycle;

import info.digitalpreserve.exceptions.RIException;
import info.digitalpreserve.interfaces.Registry;

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.scidipes.common.framework.FrameworkWrapper;

/**
 * Application Lifecycle Listener implementation class ContextListener
 * 
 */
public class ContextListener implements ServletContextListener {

	private static final Logger LOG = LoggerFactory.getLogger(ContextListener.class);

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	@Override
	public void contextInitialized(final ServletContextEvent sce) {

		/* Ensure the FW is up and running.. */
		FrameworkWrapper.load();

		/* Read-only auth against enabled registries: */
		for (final Registry reg : FrameworkWrapper.getEnabledRegistries()) {
			try {
				reg.authoriseForReadOnly();
			} catch (final RIException e) {
				reg.setEnabled(false);
				LOG.warn(e.getMessage(), e);
			}
		}

		String version = "Version number unavailable";

		try {
			final Properties mavenProps = new Properties();
			final InputStream is = sce.getServletContext().getResourceAsStream(
					"/META-INF/maven/eu.scidipes.toolkits/pa-webapp/pom.properties");
			if (is != null) {
				mavenProps.load(is);
				version = mavenProps.getProperty("version", "");
			}
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
		}
		sce.getServletContext().setAttribute("version", version);
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		FrameworkWrapper.shutdown();
	}

}
