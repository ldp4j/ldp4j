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
package org.ldp4j.server.controller;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.ContentPreferences.ContentPreferencesBuilder;
import org.ldp4j.application.engine.context.ContentPreferences.Preference;

import static com.google.common.base.Preconditions.*;

public class ContentPreferencesUtils {

	public static final String PREFERENCE_APPLIED_HEADER = "Preference-Applied";
	public static final String PREFER_HEADER = "Prefer";
	private static String PARAMETER="^\\s*(\\w*)\\s*=\\s*\"([^\"]+)\"\\s*$";

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
			String hint=matcher.group(1);
			boolean include=true;
			if(hint.equals("omit")) {
				include=false;
			} else if(hint.equals("include")) {
				include=true;
			} else {
				throw new InvalidPreferenceHeaderException("Invalid preference hint '"+hint+"'");
			}
			if(configured.contains(hint)) {
				throw new InvalidPreferenceHeaderException("Hint '"+hint+"' has already been configured");
			}
			configured.add(hint);
			for(String rawPreference:matcher.group(2).split("\\s")) {
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
	}

	private static void validatePrefix(String value, String prefix) {
		String[] items = prefix.split("=");
		if(items.length!=2) {
			throw new IllegalArgumentException("Could not parse preferences ("+value+"): could not find return representation");
		}
		if(!items[0].trim().equals("return")) {
			throw new IllegalArgumentException("Could not parse preferences ("+value+"): unexpected token '"+items[0].trim()+"'");
		}
		if(!items[1].trim().equals("representation")) {
			throw new IllegalArgumentException("Could not parse preferences ("+value+"): unexpected return type '"+items[1].trim()+"'");
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
		checkNotNull("Content preferences cannot be null");
		StringBuilder header=new StringBuilder();
		header.append("return=representation");
		return header.toString();
	}

}
