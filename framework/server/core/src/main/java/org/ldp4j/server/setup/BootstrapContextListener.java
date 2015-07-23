/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.setup;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.annotation.WebListener;

import org.ldp4j.application.engine.ApplicationContextTerminationException;
import org.ldp4j.application.engine.ApplicationEngine;
import org.ldp4j.application.engine.ApplicationEngineLifecycleException;
import org.ldp4j.application.engine.ApplicationEngineRuntimeException;
import org.ldp4j.application.engine.ApplicationContextCreationException;
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

	private static final String SUB_SUB_VALUE_PREFIX = "\t\t\t\t* ";
	private static final String SUB_VALUE_PREFIX = "\t\t\t- ";
	private static final String VALUE_SEPARATOR = ": ";
	private static final String VALUE_PREFIX    = "\t\t+ ";
	private static final String SERVER_SHUTDOWN_LOGGING       = "org.ldp4j.server.bootstrap.logging.shutdown";
	private static final String SERVER_UPDATE_LOGGING         = "org.ldp4j.server.bootstrap.logging.update";
	private static final String SERVER_INITIALIZATION_LOGGING = "org.ldp4j.server.bootstrap.logging.initialization";

	private static final String LDP4J_TARGET_APPLICATION  = "ldp4jTargetApplication";

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

	private static final String NEW_LINE=System.getProperty("line.separator");

	private void addMessage(Map<String, Object> messages, String attributeName, Object object) {
		if(object!=null) {
			messages.put(attributeName,object);
		}
	}

	private String dumpContext(String event, ServletContext context) {
		Map<String,Object> messages=new TreeMap<String,Object>();

		addMessage(messages,"Context path", context.getContextPath());
		addMessage(messages,"Servlet context name", context.getServletContextName());

		addMessage(messages,"Server info",context.getServerInfo());
		addMessage(messages,"Major version",context.getMajorVersion());
		addMessage(messages,"Minor version",context.getMinorVersion());

		addMessage(messages,"Effective major version",context.getEffectiveMajorVersion());
		addMessage(messages,"Effective minor version",context.getEffectiveMinorVersion());

		Set<SessionTrackingMode> efectiveSessionTrackingModes=context.getEffectiveSessionTrackingModes();
		if(efectiveSessionTrackingModes!=null && !efectiveSessionTrackingModes.isEmpty()) {
			StringBuilder builder=new StringBuilder();
			for(SessionTrackingMode trackingMode:efectiveSessionTrackingModes) {
				builder.append(NEW_LINE).append(VALUE_PREFIX).append(trackingMode);
			}
			addMessage(messages,"Efective session tracking modes",builder.toString());
		}

		Enumeration<String> attributeNames = context.getAttributeNames();
		if(attributeNames!=null && attributeNames.hasMoreElements()) {
			StringBuilder builder=new StringBuilder();
			while(attributeNames.hasMoreElements()) {
				String name = attributeNames.nextElement();
				if("org.apache.tomcat.util.scan.MergedWebXml".equals(name)) {
					continue;
				}
				Object value = context.getAttribute(name);
				builder.append(NEW_LINE).append(VALUE_PREFIX).append(name).append(VALUE_SEPARATOR).append(value.toString()).append(" (").append(value.getClass().getCanonicalName()).append(")");
			}
			addMessage(messages,"Attributes",builder.toString());
		}

		Enumeration<String> initParameterNames = context.getInitParameterNames();
		if(initParameterNames!=null && initParameterNames.hasMoreElements()) {
			StringBuilder builder=new StringBuilder();
			while(initParameterNames.hasMoreElements()) {
				String name = initParameterNames.nextElement();
				String value = context.getInitParameter(name);
				builder.append(NEW_LINE).append(VALUE_PREFIX).append(name).append(VALUE_SEPARATOR).append(value);
			}
			addMessage(messages,"Init parameters",builder.toString());
		}

		SessionCookieConfig sessionCookieConfig = context.getSessionCookieConfig();
		if(sessionCookieConfig!=null) {
			StringBuilder builder=new StringBuilder();
			builder.append(NEW_LINE).append(VALUE_PREFIX).append("Name").append(VALUE_SEPARATOR).append(sessionCookieConfig.getName());
			builder.append(NEW_LINE).append(VALUE_PREFIX).append("Comment").append(VALUE_SEPARATOR).append(sessionCookieConfig.getComment());
			builder.append(NEW_LINE).append(VALUE_PREFIX).append("Domain").append(VALUE_SEPARATOR).append(sessionCookieConfig.getDomain());
			builder.append(NEW_LINE).append(VALUE_PREFIX).append("Path").append(VALUE_SEPARATOR).append(sessionCookieConfig.getPath());
			builder.append(NEW_LINE).append(VALUE_PREFIX).append("Max age").append(VALUE_SEPARATOR).append(sessionCookieConfig.getMaxAge());
			addMessage(messages,"Session cookie config",builder.toString());
		}

		Map<String, ? extends ServletRegistration> servletRegistrations = context.getServletRegistrations();
		if(servletRegistrations!=null && !servletRegistrations.isEmpty()) {
			StringBuilder builder=new StringBuilder();
			for(Entry<String, ? extends ServletRegistration> entry:servletRegistrations.entrySet()) {
				ServletRegistration registration = entry.getValue();
				builder.append(NEW_LINE).append(VALUE_PREFIX).append(entry.getKey()).append(VALUE_SEPARATOR);
				builder.append(NEW_LINE).append(SUB_VALUE_PREFIX).append("Name.......: ").append(registration.getName());
				builder.append(NEW_LINE).append(SUB_VALUE_PREFIX).append("Class name.: ").append(registration.getClassName());
				String runAsRole = registration.getRunAsRole();
				if(runAsRole!=null && !runAsRole.trim().isEmpty()) {
					builder.append(NEW_LINE).append(SUB_VALUE_PREFIX).append("Run as role: ").append(runAsRole.trim());
				}
				Map<String, String> initParameters = registration.getInitParameters();
				if(initParameters!=null && !initParameters.isEmpty()) {
					builder.append(NEW_LINE).append(SUB_VALUE_PREFIX).append("Init parameters:");
					for(Entry<String, String> ipEntry:initParameters.entrySet()) {
						builder.append(NEW_LINE).append(SUB_SUB_VALUE_PREFIX).append(ipEntry.getKey()).append(VALUE_SEPARATOR).append(ipEntry.getValue());
					}
				}
				Collection<String> mappings = registration.getMappings();
				if(mappings!=null && !mappings.isEmpty()) {
					builder.append(NEW_LINE).append(SUB_VALUE_PREFIX).append("Mappings:");
					for(String mapping:mappings) {
						builder.append(NEW_LINE).append(SUB_SUB_VALUE_PREFIX).append(mapping);
					}
				}
			}
			addMessage(messages,"Servlet registrations",builder.toString());
		}
		StringBuilder builder=new StringBuilder();
		builder.append(event).append(":");
		for(Entry<String, Object> entry:messages.entrySet()) {
			builder.append(NEW_LINE).append("\t- ").append(entry.getKey()).append(VALUE_SEPARATOR).append(entry.getValue());
		}

		return builder.toString();
	}

	private String getTargetApplicationClassName(ServletContextEvent sce) {
		return sce.getServletContext().getInitParameter(LDP4J_TARGET_APPLICATION);
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if(isEnabled(SERVER_UPDATE_LOGGING)) {
			sce.
				getServletContext().
					addListener(
						new BootstrapServletContextAttributeListener()
					);
		}

		LOGGER.info("Registering CXF servlet...");
		Dynamic dynamic =
			sce.
				getServletContext().
					addServlet("LDP4jFrontendServerServlet","org.apache.cxf.transport.servlet.CXFServlet");
		dynamic.addMapping("/*");
		/** See https://issues.apache.org/jira/browse/CXF-5068 */
		dynamic.setInitParameter("disable-address-updates","true");
		/** Required for testing */
		dynamic.setInitParameter("static-welcome-file","/index.html");
		dynamic.setInitParameter("static-resources-list","/index.html");
		dynamic.setLoadOnStartup(1);
		LOGGER.info("CXF servlet registered.");

		if(isEnabled(SERVER_INITIALIZATION_LOGGING)) {
			LOGGER.info(dumpContext("Context initialization started",sce.getServletContext()));
		}

		try {
			String targetApplicationClassName=getTargetApplicationClassName(sce);
			ApplicationEngine engine = ApplicationEngine.engine();
			engine.start();
			ApplicationContext applicationContext=engine.load(targetApplicationClassName);
			sce.getServletContext().setAttribute(ServerFrontend.LDP4J_APPLICATION_CONTEXT, applicationContext);
			LOGGER.info("LDP4j Application '{}' ({}) initialized.",applicationContext.applicationName(),applicationContext.applicationClassName());
		} catch (ApplicationContextCreationException e) {
			LOGGER.error("Could not configure LDP4j Application to be used within the LDP4j Server Frontend. Full stacktrace follows:",e);
		} catch (ApplicationEngineRuntimeException e) {
			LOGGER.error("Could not configure LDP4j Server Frontend due to an unexpected LDP4j Application Engine failure. Full stacktrace follows:",e);
		} catch (ApplicationEngineLifecycleException e) {
			LOGGER.error("LDP4j Application Engine could not start. Full stacktrace follows:",e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if(isEnabled(SERVER_SHUTDOWN_LOGGING)) {
			LOGGER.info(dumpContext("Context shutdown started",sce.getServletContext()));
		}
		try {
			ApplicationContext applicationContext = (ApplicationContext)sce.getServletContext().getAttribute(ServerFrontend.LDP4J_APPLICATION_CONTEXT);
			if(applicationContext!=null) {
				sce.getServletContext().removeAttribute(ServerFrontend.LDP4J_APPLICATION_CONTEXT);
				try {
					ApplicationEngine.engine().dispose(applicationContext);
					LOGGER.info("LDP4j Application '{}' ({}) shutdown.",applicationContext.applicationName(),applicationContext.applicationClassName());
				} catch (ApplicationContextTerminationException e) {
					LOGGER.error(String.format("Could not shutdown LDP4j Application '%s' (%s) due to an unexpected context failure. Full stacktrace follows:",applicationContext.applicationName(),applicationContext.applicationClassName()),e);
				}
			}
			ApplicationEngine.engine().shutdown();
		} catch (ApplicationEngineRuntimeException e) {
			LOGGER.error("Could not shutdown LDP4j Server Frontend due to an unexpected LDP4j Application Engine failure. Full stacktrace follows:",e);
		} catch (ApplicationEngineLifecycleException e) {
			LOGGER.error("LDP4j Application Engine could not shutdown. Full stacktrace follows:",e);
		}
	}

	/**
	 * @param property
	 * @return
	 */
	protected boolean isEnabled(String property) {
		return Boolean.parseBoolean(System.getProperty(property));
	}

}