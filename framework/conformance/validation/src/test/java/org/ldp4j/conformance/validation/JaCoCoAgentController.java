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
 *   Artifact    : org.ldp4j.framework:ldp4j-conformance-validation:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-conformance-validation-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.conformance.validation;

import java.lang.reflect.Method;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServletContextListener enable dumping the data collected by the JaCoCo Agent properly
 * @see http://askstop.com/questions/2974363/arquillian-jboss-as7-managed-and-jacoco-jacoco-exec-file-is-empty
 */
@WebListener
public final class JaCoCoAgentController implements ServletContextListener {

	private static final String SERVER_SHUTDOWN_LOGGING="org.ldp4j.server.bootstrap.logging.shutdown";
	private static final String SERVER_UPDATE_LOGGING  ="org.ldp4j.server.bootstrap.logging.update";

	private static final class AttributeLogger implements ServletContextAttributeListener {
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

	private static final Logger LOGGER = LoggerFactory.getLogger(JaCoCoAgentController.class);

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
						new AttributeLogger()
					);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if(isEnabled(SERVER_SHUTDOWN_LOGGING)) {
			LOGGER.info(dumpContext("Context shutdown started",sce.getServletContext()));
		}
		try {
			Class<?> rtClass = ClassLoader.getSystemClassLoader().loadClass("org.jacoco.agent.rt.RT");
			Object jacocoAgent = rtClass.getMethod("getAgent").invoke(null);
			Method dumpMethod = jacocoAgent.getClass().getMethod("dump",boolean.class);
			dumpMethod.invoke(jacocoAgent, false);
		} catch (ClassNotFoundException e) {
			LOGGER.debug("no jacoco agent attached to this jvm");
		} catch (Exception e) {
			LOGGER.error("while trying to dump jacoco data", e);
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