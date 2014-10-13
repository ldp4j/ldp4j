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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ldp4j.application.ContentPreferences;
import org.ldp4j.application.ContentPreferences.ContentPreferencesBuilder;
import org.ldp4j.application.ContentPreferences.Preference;

import com.google.common.collect.ImmutableList;

public class PreferenceHeaderUtil {

	public static ContentPreferences fromHeader(String value) {
		String[] split = value.split(";");
		validatePrefix(value, split[0]);
		ContentPreferencesBuilder builder = ContentPreferences.builder();
		if(split.length>2) {
			throw new IllegalArgumentException("Could not parse preferences ("+value+"): invalid return representation preference configuration");
		} else if(split.length==2){
			String[] refinements = split[1].split(",");
			populatePreferences(builder,refinements);
			
		}
		return builder.build();
	}

	private static String option="\\s*(\\w*)\\s*=\\s*\"\\s*([^\\s\"]+\\s*)+\"\\s*";

	private static void populatePreferences(ContentPreferencesBuilder builder, String[] refinements) {
		Pattern pattern = Pattern.compile(option);
		for(String refinement:refinements) {
			Matcher matcher = pattern.matcher(refinement);
			if(!matcher.matches()) {
				throw new IllegalArgumentException("Invalid preference refinement '"+refinement+"'");
			}
			int groupCount = matcher.groupCount();
			for(int i=1;i<1+groupCount;i++) {
				System.out.println(i+".- '"+matcher.group(i)+"'");
			}
		}
	}

	protected static void validatePrefix(String value, String prefix) {
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
	
	public static String toPreferenceHeader(ContentPreferences contentPreferences) {
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
				if(!hasInclude) {
					header.append("; omit=\"");
				} else {
					header.append(", omit=\"");
				}
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
	
	public static void main(String[] args) {
		List<String> examples=
			ImmutableList.<String>builder().
				add("return=representation; include=\"http://www.w3.org/ns/ldp#PreferMinimalContainer\"").
				build();
		for(String example:examples) {
			System.out.println(fromHeader(example));
		}
		
		System.out.println(
			toPreferenceHeader(
				ContentPreferences.
					builder().
						withInclude(Preference.CONTAINMENT_TRIPLES).
						withInclude(Preference.MEMBERSHIP_TRIPLES).
						withOmit(Preference.MINIMAL_CONTAINER).	
						build()
			)
		);
		
		System.out.println(
				toPreferenceHeader(
					ContentPreferences.
						builder().
							withInclude(Preference.CONTAINMENT_TRIPLES).
							withInclude(Preference.MEMBERSHIP_TRIPLES).
							build()
				)
			);

		System.out.println(
				toPreferenceHeader(
					ContentPreferences.
						builder().
							withOmit(Preference.CONTAINMENT_TRIPLES).
							withOmit(Preference.MEMBERSHIP_TRIPLES).
							build()
				)
			);

	}
	
}
