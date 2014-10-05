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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
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
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.annotation.WebListener;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.lifecycle.ApplicationInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServletContextListener to control the life cycle of the LDP4j Server 
 * @since 1.0.0
 * @version 1.0
 */
@WebListener
public final class BootstrapContextListener implements ServletContextListener {
	
	private static final String SERVER_SHUTDOWN_LOGGING       = "org.ldp4j.server.bootstrap.logging.shutdown";
	private static final String SERVER_UPDATE_LOGGING         = "org.ldp4j.server.bootstrap.logging.update";
	private static final String SERVER_INITIALIZATION_LOGGING = "org.ldp4j.server.bootstrap.logging.initialization";

	private static final String LDP4J_TARGET_APPLICATION = "ldp4jTargetApplication";

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
				builder.append(NEW_LINE).append("\t\t+ ").append(trackingMode);
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
				builder.append(NEW_LINE).append("\t\t+ ").append(name).append(": ").append(value.toString()).append(" (").append(value.getClass().getCanonicalName()).append(")");
			}
			addMessage(messages,"Attributes",builder.toString());
		}
	
		Enumeration<String> initParameterNames = context.getInitParameterNames();
		if(initParameterNames!=null && initParameterNames.hasMoreElements()) {
			StringBuilder builder=new StringBuilder();
			while(initParameterNames.hasMoreElements()) {
				String name = initParameterNames.nextElement();
				String value = context.getInitParameter(name);
				builder.append(NEW_LINE).append("\t\t+ ").append(name).append(": ").append(value);
			}
			addMessage(messages,"Init parameters",builder.toString());
		}
	
		SessionCookieConfig sessionCookieConfig = context.getSessionCookieConfig();
		if(sessionCookieConfig!=null) {
			StringBuilder builder=new StringBuilder();
			builder.append(NEW_LINE).append("\t\t+ ").append("Name").append(": ").append(sessionCookieConfig.getName());
			builder.append(NEW_LINE).append("\t\t+ ").append("Comment").append(": ").append(sessionCookieConfig.getComment());
			builder.append(NEW_LINE).append("\t\t+ ").append("Domain").append(": ").append(sessionCookieConfig.getDomain());
			builder.append(NEW_LINE).append("\t\t+ ").append("Path").append(": ").append(sessionCookieConfig.getPath());
			builder.append(NEW_LINE).append("\t\t+ ").append("Max age").append(": ").append(sessionCookieConfig.getMaxAge());
			addMessage(messages,"Session cookie config",builder.toString());
		}
	
		Map<String, ? extends ServletRegistration> servletRegistrations = context.getServletRegistrations();
		if(servletRegistrations!=null && !servletRegistrations.isEmpty()) {
			StringBuilder builder=new StringBuilder();
			for(Entry<String, ? extends ServletRegistration> entry:servletRegistrations.entrySet()) {
				ServletRegistration registration = entry.getValue();
				builder.append(NEW_LINE).append("\t\t+ ").append(entry.getKey()).append(": ");
				builder.append(NEW_LINE).append("\t\t\t- ").append("Name.......: ").append(registration.getName());
				builder.append(NEW_LINE).append("\t\t\t- ").append("Class name.: ").append(registration.getClassName());
				String runAsRole = registration.getRunAsRole();
				if(runAsRole!=null && !runAsRole.trim().isEmpty()) {
					builder.append(NEW_LINE).append("\t\t\t- ").append("Run as role: ").append(runAsRole.trim());
				}
				Map<String, String> initParameters = registration.getInitParameters();
				if(initParameters!=null && !initParameters.isEmpty()) {
					builder.append(NEW_LINE).append("\t\t\t- ").append("Init parameters:");
					for(Entry<String, String> ipEntry:initParameters.entrySet()) {
						builder.append(NEW_LINE).append("\t\t\t\t* ").append(ipEntry.getKey()).append(": ").append(ipEntry.getValue());
					}
				}
				Collection<String> mappings = registration.getMappings();
				if(mappings!=null && !mappings.isEmpty()) {
					builder.append(NEW_LINE).append("\t\t\t- ").append("Mappings:");
					for(String mapping:mappings) {
						builder.append(NEW_LINE).append("\t\t\t\t* ").append(mapping);
					}
				}
			}
			addMessage(messages,"Servlet registrations",builder.toString());
		}
		StringBuilder builder=new StringBuilder();
		builder.append(event).append(":");
		for(Entry<String, Object> entry:messages.entrySet()) {
			builder.append(NEW_LINE).append("\t- ").append(entry.getKey()).append(": ").append(entry.getValue());
		}
		
		return builder.toString();
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

		if(isEnabled(SERVER_INITIALIZATION_LOGGING)) {
			LOGGER.info(dumpContext("Context initialization started",sce.getServletContext()));
		}

		String targetApplicationClassName = getTargetApplicationClassName(sce);
		try {
			ApplicationContext applicationContext = ApplicationContext.currentContext();
			applicationContext.initialize(targetApplicationClassName);
			LOGGER.info("LDP4j Application '{}' ({}) initialized",applicationContext.applicationName(),targetApplicationClassName);
		} catch (ApplicationInitializationException e) {
			LOGGER.error("Could not initialize LDP4j application '"+targetApplicationClassName+"'",e);
		}
	}

	private String getTargetApplicationClassName(ServletContextEvent sce) {
		return sce.getServletContext().getInitParameter(LDP4J_TARGET_APPLICATION);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if(isEnabled(SERVER_SHUTDOWN_LOGGING)) {
			LOGGER.info(dumpContext("Context shutdown started",sce.getServletContext()));
		}

		ApplicationContext applicationContext = ApplicationContext.currentContext();
		if(applicationContext.shutdown()) {
			String targetApplicationClassName = getTargetApplicationClassName(sce);
			LOGGER.info("LDP4j application "+applicationContext.applicationName()+" ("+targetApplicationClassName+") shutdown");
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