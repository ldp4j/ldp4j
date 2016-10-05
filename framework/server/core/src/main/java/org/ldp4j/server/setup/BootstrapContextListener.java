/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014-2016 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.setup;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.annotation.WebListener;

import org.ldp4j.application.engine.ApplicationContextCreationException;
import org.ldp4j.application.engine.ApplicationContextTerminationException;
import org.ldp4j.application.engine.ApplicationEngine;
import org.ldp4j.application.engine.ApplicationEngineLifecycleException;
import org.ldp4j.application.engine.ApplicationEngineRuntimeException;
import org.ldp4j.application.engine.context.ApplicationContext;
import org.ldp4j.server.frontend.ServerFrontend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServletContextListener to control the life cycle of the LDP4j Server
 * @since 1.0.0
 * @version 1.0
 */
@WebListener
public final class BootstrapContextListener implements ServletContextListener {

	private static final class BootstrapServletContextAttributeListener implements ServletContextAttributeListener {

		@Override
		public void attributeAdded(ServletContextAttributeEvent scab) {
			LOGGER.info(String.format("Added attribute '%s' with value '%s'",scab.getName(),scab.getValue()));
		}

		@Override
		public void attributeRemoved(ServletContextAttributeEvent scab) {
			LOGGER.info(String.format("Deleted attribute '%s' with value '%s'",scab.getName(),scab.getValue()));
		}

		@Override
		public void attributeReplaced(ServletContextAttributeEvent scab) {
			LOGGER.info(String.format("Replaced attribute '%s' with value '%s'",scab.getName(),scab.getValue()));
		}

	}

	private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapContextListener.class);

	private static final String SERVER_SHUTDOWN_LOGGING       = "org.ldp4j.server.bootstrap.logging.shutdown";
	private static final String SERVER_UPDATE_LOGGING         = "org.ldp4j.server.bootstrap.logging.update";
	private static final String SERVER_INITIALIZATION_LOGGING = "org.ldp4j.server.bootstrap.logging.initialization";

	private static final String LDP4J_TARGET_APPLICATION  = "ldp4jTargetApplication";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext servletContext=sce.getServletContext();
		if(isEnabled(SERVER_UPDATE_LOGGING)) {
			servletContext.
				addListener(new BootstrapServletContextAttributeListener());
		}

		registerCXFServlet(servletContext);

		if(isEnabled(SERVER_INITIALIZATION_LOGGING)) {
			LOGGER.info(BootstrapUtil.dumpContext("Context initialization started",servletContext));
		}

		try {
			ApplicationEngine engine =
				ApplicationEngine.
					engine().
						withContextPath(servletContext.getContextPath()).
						withTemporalDirectory((File)servletContext.getAttribute(ServletContext.TEMPDIR));
			engine.start();
			LOGGER.info("Started LDP4j Application Engine in context {}. Using temporal directory {}",engine.contextPath(),engine.temporalDirectory());
			loadApplicationContext(servletContext, engine);
		} catch (ApplicationEngineRuntimeException e) {
			LOGGER.error("Could not configure LDP4j Server Frontend due to an unexpected LDP4j Application Engine failure. Full stacktrace follows:",e);
		} catch (ApplicationEngineLifecycleException e) {
			LOGGER.error("LDP4j Application Engine could not start. Full stacktrace follows:",e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ServletContext servletContext = sce.getServletContext();
		if(isEnabled(SERVER_SHUTDOWN_LOGGING)) {
			LOGGER.info(BootstrapUtil.dumpContext("Context shutdown started",servletContext));
		}
		try {
			ApplicationContext applicationContext = (ApplicationContext)servletContext.getAttribute(ServerFrontend.LDP4J_APPLICATION_CONTEXT);
			disposeApplicationContext(servletContext, applicationContext);
			ApplicationEngine.engine().shutdown();
		} catch (ApplicationEngineRuntimeException e) {
			LOGGER.error("Could not shutdown LDP4j Server Frontend due to an unexpected LDP4j Application Engine failure. Full stacktrace follows:",e);
		} catch (ApplicationEngineLifecycleException e) {
			LOGGER.error("LDP4j Application Engine could not shutdown. Full stacktrace follows:",e);
		}
	}

	private static void registerCXFServlet(ServletContext servletContext) {
		LOGGER.info("Registering CXF servlet...");
		Dynamic dynamic =
			servletContext.
				addServlet(
					"LDP4jFrontendServerServlet",
					"org.apache.cxf.transport.servlet.CXFServlet");
		dynamic.addMapping("/*");
		/** See https://issues.apache.org/jira/browse/CXF-5068 */
		dynamic.setInitParameter("disable-address-updates","true");
		/** Required for testing */
		dynamic.setInitParameter("static-welcome-file","/index.html");
		dynamic.setInitParameter("static-resources-list","/index.html");
		dynamic.setLoadOnStartup(1);
		LOGGER.info("CXF servlet registered.");
	}

	private static String getTargetApplicationClassName(ServletContext context) {
		return context.getInitParameter(LDP4J_TARGET_APPLICATION);
	}

	private static void loadApplicationContext(ServletContext servletContext, ApplicationEngine engine) {
		String targetApplicationClassName=getTargetApplicationClassName(servletContext);
		try {
			ApplicationContext applicationContext=engine.load(targetApplicationClassName);
			servletContext.setAttribute(ServerFrontend.LDP4J_APPLICATION_CONTEXT, applicationContext);
			LOGGER.info("LDP4j Application '{}' ({}) initialized.",applicationContext.applicationName(),applicationContext.applicationClassName());
		} catch (ApplicationContextCreationException e) {
			LOGGER.error("Could not configure LDP4j Application to be used within the LDP4j Server Frontend. Full stacktrace follows:",e);
		}
	}

	private static void disposeApplicationContext(ServletContext servletContext, ApplicationContext applicationContext) {
		if(applicationContext==null) {
			return;
		}
		servletContext.removeAttribute(ServerFrontend.LDP4J_APPLICATION_CONTEXT);
		try {
			ApplicationEngine.engine().dispose(applicationContext);
			LOGGER.info("LDP4j Application '{}' ({}) shutdown.",applicationContext.applicationName(),applicationContext.applicationClassName());
		} catch (ApplicationContextTerminationException e) {
			LOGGER.error(String.format("Could not shutdown LDP4j Application '%s' (%s) due to an unexpected context failure. Full stacktrace follows:",applicationContext.applicationName(),applicationContext.applicationClassName()),e);
		}
	}

	private static boolean isEnabled(String property) {
		return Boolean.parseBoolean(System.getProperty(property));
	}

}