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

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;

final class BootstrapUtil {

	private static final String NEW_LINE=System.lineSeparator();
	private static final String SUB_SUB_VALUE_PREFIX = "\t\t\t\t* ";
	private static final String SUB_VALUE_PREFIX = "\t\t\t- ";
	private static final String VALUE_SEPARATOR = ": ";
	private static final String VALUE_PREFIX    = "\t\t+ ";

	private BootstrapUtil() {
	}

	static String dumpContext(String event, ServletContext context) {
		Map<String,Object> messages=new TreeMap<String,Object>();
		addMetadataMessages(context, messages);
		addEfectiveSessionTrackingModeMessages(messages, context.getEffectiveSessionTrackingModes());
		addAttributeMessages(context, messages);
		addInitParameterMessages(context, messages);
		addSessionCookieConfigMessages(messages, context.getSessionCookieConfig());
		addServletRegistrationMessages(messages, context.getServletRegistrations());
		return assembleMessages(event, messages);
	}

	private static void addMessage(Map<String, Object> messages, String attributeName, Object object) {
		if(object!=null) {
			messages.put(attributeName,object);
		}
	}

	private static String assembleMessages(String event, Map<String, Object> messages) {
		StringBuilder builder=new StringBuilder();
		builder.append(event).append(":");
		for(Entry<String, Object> entry:messages.entrySet()) {
			builder.append(NEW_LINE).append("\t- ").append(entry.getKey()).append(VALUE_SEPARATOR).append(entry.getValue());
		}
		return builder.toString();
	}

	private static void addMetadataMessages(ServletContext context, Map<String, Object> messages) {
		addMessage(messages,"Context path", context.getContextPath());
		addMessage(messages,"Servlet context name", context.getServletContextName());
		addMessage(messages,"Server info",context.getServerInfo());
		addMessage(messages,"Major version",context.getMajorVersion());
		addMessage(messages,"Minor version",context.getMinorVersion());
		addMessage(messages,"Effective major version",context.getEffectiveMajorVersion());
		addMessage(messages,"Effective minor version",context.getEffectiveMinorVersion());
	}

	private static void addServletRegistrationMessages(Map<String, Object> messages, Map<String, ? extends ServletRegistration> registrations) {
		if(registrations==null || !registrations.isEmpty()) {
			return;
		}
		StringBuilder builder=new StringBuilder();
		for(Entry<String, ? extends ServletRegistration> entry:registrations.entrySet()) {
			buildServletRegistrationMessage(builder, entry.getKey(), entry.getValue());
		}
		addMessage(messages,"Servlet registrations",builder.toString());
	}

	private static void buildServletRegistrationMessage(StringBuilder builder, String servletId, ServletRegistration registration) {
		builder.append(NEW_LINE).append(VALUE_PREFIX).append(servletId).append(VALUE_SEPARATOR);
		builder.append(NEW_LINE).append(SUB_VALUE_PREFIX).append("Name.......: ").append(registration.getName());
		builder.append(NEW_LINE).append(SUB_VALUE_PREFIX).append("Class name.: ").append(registration.getClassName());
		addRunAsRole(builder, registration.getRunAsRole());
		addInitParameters(builder, registration.getInitParameters());
		addMappings(builder, registration.getMappings());
	}

	private static void addMappings(StringBuilder builder, Collection<String> mappings) {
		if(mappings==null || mappings.isEmpty()) {
			return;
		}
		builder.append(NEW_LINE).append(SUB_VALUE_PREFIX).append("Mappings:");
		for(String mapping:mappings) {
			builder.append(NEW_LINE).append(SUB_SUB_VALUE_PREFIX).append(mapping);
		}
	}

	private static void addInitParameters(StringBuilder builder, Map<String, String> initParameters) {
		if(initParameters==null || initParameters.isEmpty()) {
			return;
		}
		builder.append(NEW_LINE).append(SUB_VALUE_PREFIX).append("Init parameters:");
		for(Entry<String, String> ipEntry:initParameters.entrySet()) {
			builder.append(NEW_LINE).append(SUB_SUB_VALUE_PREFIX).append(ipEntry.getKey()).append(VALUE_SEPARATOR).append(ipEntry.getValue());
		}
	}

	private static void addRunAsRole(StringBuilder builder, String runAsRole) {
		if(runAsRole!=null && !runAsRole.trim().isEmpty()) {
			builder.append(NEW_LINE).append(SUB_VALUE_PREFIX).append("Run as role: ").append(runAsRole.trim());
		}
	}

	private static void addSessionCookieConfigMessages(Map<String, Object> messages, SessionCookieConfig sessionCookieConfig) {
		if(sessionCookieConfig==null) {
			return;
		}
		StringBuilder builder=new StringBuilder();
		builder.append(NEW_LINE).append(VALUE_PREFIX).append("Name").append(VALUE_SEPARATOR).append(sessionCookieConfig.getName());
		builder.append(NEW_LINE).append(VALUE_PREFIX).append("Comment").append(VALUE_SEPARATOR).append(sessionCookieConfig.getComment());
		builder.append(NEW_LINE).append(VALUE_PREFIX).append("Domain").append(VALUE_SEPARATOR).append(sessionCookieConfig.getDomain());
		builder.append(NEW_LINE).append(VALUE_PREFIX).append("Path").append(VALUE_SEPARATOR).append(sessionCookieConfig.getPath());
		builder.append(NEW_LINE).append(VALUE_PREFIX).append("Max age").append(VALUE_SEPARATOR).append(sessionCookieConfig.getMaxAge());
		addMessage(messages,"Session cookie config",builder.toString());
	}

	private static void addInitParameterMessages(ServletContext context, Map<String, Object> messages) {
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
	}

	private static void addAttributeMessages(ServletContext context,Map<String, Object> messages) {
		Enumeration<String> attributeNames = context.getAttributeNames();
		if(attributeNames==null || !attributeNames.hasMoreElements()) {
			return;
		}
		StringBuilder builder=new StringBuilder();
		while(attributeNames.hasMoreElements()) {
			String name = attributeNames.nextElement();
			buildAttributeMessage(builder,name,context.getAttribute(name));
		}
		addMessage(messages,"Attributes",builder.toString());
	}

	private static void buildAttributeMessage(StringBuilder builder, String name, Object value) {
		if("org.apache.tomcat.util.scan.MergedWebXml".equals(name)) {
			return;
		}
		builder.
			append(NEW_LINE).
			append(VALUE_PREFIX).
			append(name).
			append(VALUE_SEPARATOR).
			append(value.toString()).
			append(" (").
			append(value.getClass().getCanonicalName()).
			append(")");
	}

	private static void addEfectiveSessionTrackingModeMessages(Map<String, Object> messages, Set<SessionTrackingMode> efectiveSessionTrackingModes) {
		if(efectiveSessionTrackingModes==null || efectiveSessionTrackingModes.isEmpty()) {
			return;
		}
		StringBuilder builder=new StringBuilder();
		for(SessionTrackingMode trackingMode:efectiveSessionTrackingModes) {
			builder.append(NEW_LINE).append(VALUE_PREFIX).append(trackingMode);
		}
		addMessage(messages,"Efective session tracking modes",builder.toString());
	}

}