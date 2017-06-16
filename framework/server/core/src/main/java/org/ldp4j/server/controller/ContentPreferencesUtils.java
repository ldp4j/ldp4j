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
package org.ldp4j.server.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.ContentPreferences.ContentPreferencesBuilder;
import org.ldp4j.application.engine.context.ContentPreferences.Preference;

final class ContentPreferencesUtils {

	public static final String PREFERENCE_APPLIED_HEADER = "Preference-Applied";
	public static final String PREFER_HEADER = "Prefer";

	private static String PARAMETER="^\\s*(\\w*)\\s*=\\s*\"([^\"]+)\"\\s*$";

	private ContentPreferencesUtils() {
	}

	public static ContentPreferences fromPreferenceHeader(String header) {
		checkNotNull("Preference header cannot be null");
		String[] preferenceParts = header.split(";");
		validatePrefix(header, preferenceParts[0]);
		ContentPreferencesBuilder builder = ContentPreferences.builder();
		if(preferenceParts.length==1) {
			throw new InvalidPreferenceHeaderException("Could not parse preference ("+header+"): invalid return representation preference configuration");
		}
		populatePreferences(builder,Arrays.copyOfRange(preferenceParts,1,preferenceParts.length,String[].class));
		return builder.build();
	}

	private static void populatePreferences(ContentPreferencesBuilder builder, String[] parameters) {
		Pattern pattern = Pattern.compile(PARAMETER);
		Set<String> configured=new TreeSet<String>();
		for(String refinement:parameters) {
			Matcher matcher = pattern.matcher(refinement);
			if(!matcher.matches()) {
				throw new InvalidPreferenceHeaderException("Invalid preference refinement '"+refinement+"'");
			}
			boolean include = processHint(configured, matcher.group(1));
			processPreferences(builder,include,matcher.group(2).split("\\s"));
		}
	}

	private static void processPreferences(ContentPreferencesBuilder builder, boolean include, String[] rawPreferences) {
		for(String rawPreference:rawPreferences) {
			Preference preference = Preference.fromString(rawPreference.trim());
			if(preference==null) {
				throw new InvalidPreferenceHeaderException("Unknown preference '"+rawPreference+"'");
			}
			if(include) {
				builder.withInclude(preference);
			} else {
				builder.withOmit(preference);
			}
		}
	}

	private static boolean processHint(Set<String> configured, String hint) {
		boolean include=true;
		if("omit".equals(hint)) {
			include=false;
		} else if("include".equals(hint)) {
			include=true;
		} else {
			throw new InvalidPreferenceHeaderException("Invalid preference hint '"+hint+"'");
		}
		if(configured.contains(hint)) {
			throw new InvalidPreferenceHeaderException("Hint '"+hint+"' has already been configured");
		}
		configured.add(hint);
		return include;
	}

	private static void validatePrefix(String value, String prefix) {
		String[] items = prefix.split("=");
		if(items.length!=2) {
			throw new IllegalArgumentException(String.format("Could not parse preferences (%s): could not find return representation",value));
		}
		if(!"return".equals(items[0].trim())) {
			throw new IllegalArgumentException(String.format("Could not parse preferences (%s): unexpected token '%s'",value,items[0].trim()));
		}
		if(!"representation".equals(items[1].trim())) {
			throw new IllegalArgumentException(String.format("Could not parse preferences (%s): unexpected return type '%s'",value,items[1].trim()));
		}
	}

	public static String asPreferenceHeader(ContentPreferences contentPreferences) {
		checkNotNull("Content preferences cannot be null");
		StringBuilder header=new StringBuilder();
		header.append("return=representation");
		boolean hasInclude=false;
		for(Iterator<Preference> it=contentPreferences.includes().iterator();it.hasNext();) {
			if(!hasInclude) {
				hasInclude=true;
				header.append("; include=\"");
			}
			header.append(it.next().toURI());
			if(it.hasNext()) {
				header.append(" ");
			}
		}
		if(hasInclude) {
			header.append("\"");
		}
		boolean hasOmit=false;
		for(Iterator<Preference> it=contentPreferences.omits().iterator();it.hasNext();) {
			if(!hasOmit) {
				hasOmit=true;
				header.append("; omit=\"");
			}
			header.append(it.next().toURI());
			if(it.hasNext()) {
				header.append(" ");
			}
		}
		if(hasOmit) {
			header.append("\"");
		}
		return header.toString();
	}

	public static String asPreferenceAppliedHeader(ContentPreferences contentPreferences) {
		checkNotNull(contentPreferences,"Content preferences cannot be null");
		StringBuilder header=new StringBuilder();
		header.append("return=representation");
		return header.toString();
	}

}
