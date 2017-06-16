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


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.ContentPreferences.Preference;

import com.google.common.collect.ImmutableList;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ContentPreferencesUtilsTest {

	
	private static final String MIXED_PREFERENCES_INCLUDE_OMIT = "return=representation; include=\"http://www.w3.org/ns/ldp#PreferMinimalContainer\"; omit=\"http://www.w3.org/ns/ldp#PreferMembership http://www.w3.org/ns/ldp#PreferContainment\"";
	private static final String MIXED_PREFERENCES_OMIT_INCLUDE = "return=representation; omit=\"http://www.w3.org/ns/ldp#PreferMembership http://www.w3.org/ns/ldp#PreferContainment\"; include=\"http://www.w3.org/ns/ldp#PreferMinimalContainer\"";
	private static final String MULTIPLE_OMIT_PREFERENCES = "return=representation; omit=\"http://www.w3.org/ns/ldp#PreferMembership http://www.w3.org/ns/ldp#PreferContainment\"";
	private static final String SINGLE_INCLUDE_PREFERENCE = "return=representation; include=\"http://www.w3.org/ns/ldp#PreferMinimalContainer\"";
	private static final String MULTIPLE_INCLUDE_PREFERENCE = "return=representation; include=\"http://www.w3.org/ns/ldp#PreferContainment http://www.w3.org/ns/ldp#PreferMembership\"";

	private static final ContentPreferences COMPOSITE_PREFERENCES = 
			ContentPreferences.
				builder().
					withOmit(Preference.CONTAINMENT_TRIPLES).
					withOmit(Preference.MEMBERSHIP_TRIPLES).
					withInclude(Preference.MINIMAL_CONTAINER).	
					build();

	private static final ContentPreferences SINGLE_INCLUDE = 
			ContentPreferences.
				builder().
					withInclude(Preference.MINIMAL_CONTAINER).
					build();

	private static final ContentPreferences MULTIPLE_INCLUDES = 
			ContentPreferences.
				builder().
					withInclude(Preference.CONTAINMENT_TRIPLES).
					withInclude(Preference.MEMBERSHIP_TRIPLES).
					build();

	private static final ContentPreferences MULTIPLE_OMITS = 
			ContentPreferences.
				builder().
					withOmit(Preference.CONTAINMENT_TRIPLES).
					withOmit(Preference.MEMBERSHIP_TRIPLES).
					build();

	public static void main(String[] args) {
		List<String> examples=
			ImmutableList.<String>builder().
				add(SINGLE_INCLUDE_PREFERENCE).
				add(MULTIPLE_OMIT_PREFERENCES).
				add(MIXED_PREFERENCES_INCLUDE_OMIT).
				add(MIXED_PREFERENCES_OMIT_INCLUDE).
				build();
		for(String example:examples) {
			System.out.println(ContentPreferencesUtils.fromPreferenceHeader(example));
		}
		

	}
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testFromPreferenceHeader$validValues() throws Exception {
		assertThat(ContentPreferencesUtils.fromPreferenceHeader(MIXED_PREFERENCES_INCLUDE_OMIT),equalTo(COMPOSITE_PREFERENCES));
		assertThat(ContentPreferencesUtils.fromPreferenceHeader(MIXED_PREFERENCES_OMIT_INCLUDE),equalTo(COMPOSITE_PREFERENCES));
		assertThat(ContentPreferencesUtils.fromPreferenceHeader(SINGLE_INCLUDE_PREFERENCE),equalTo(SINGLE_INCLUDE));
		assertThat(ContentPreferencesUtils.fromPreferenceHeader(MULTIPLE_INCLUDE_PREFERENCE),equalTo(MULTIPLE_INCLUDES));
		assertThat(ContentPreferencesUtils.fromPreferenceHeader(MULTIPLE_OMIT_PREFERENCES),equalTo(MULTIPLE_OMITS));
	}

	@Test(expected=InvalidPreferenceHeaderException.class)
	public void testFromPreferenceHeader$invalidValues$no_parameter() throws Exception {
		ContentPreferencesUtils.fromPreferenceHeader("return=representation");
	}

	@Test(expected=InvalidPreferenceHeaderException.class)
	public void testFromPreferenceHeader$invalidValues$wrong_parameter() throws Exception {
		ContentPreferencesUtils.fromPreferenceHeader("return=representation; invalidParameter");
	}

	@Test(expected=InvalidPreferenceHeaderException.class)
	public void testFromPreferenceHeader$invalidValues$more_than_two_parameters() throws Exception {
		ContentPreferencesUtils.fromPreferenceHeader("return=representation; invalidParameter; another; third");
	}

	@Test(expected=InvalidPreferenceHeaderException.class)
	public void testFromPreferenceHeader$invalidValues$repeated_include_hints() throws Exception {
		ContentPreferencesUtils.fromPreferenceHeader("return=representation; include=\"http://www.w3.org/ns/ldp#PreferMinimalContainer\"; include=\"http://www.w3.org/ns/ldp#PreferMinimalContainer\"");
	}

	@Test(expected=InvalidPreferenceHeaderException.class)
	public void testFromPreferenceHeader$invalidValues$repeated_omit_hints() throws Exception {
		ContentPreferencesUtils.fromPreferenceHeader("return=representation; omit=\"http://www.w3.org/ns/ldp#PreferMinimalContainer\"; omit=\"http://www.w3.org/ns/ldp#PreferMinimalContainer\"");
	}

	@Test(expected=InvalidPreferenceHeaderException.class)
	public void testFromPreferenceHeader$invalidValues$repeated_unknown_hint_preference$1() throws Exception {
		ContentPreferencesUtils.fromPreferenceHeader("return=representation; omit=\"http://www.w3.org/ns/ldp#Unknown\"");
	}

	@Test(expected=InvalidPreferenceHeaderException.class)
	public void testFromPreferenceHeader$invalidValues$repeated_unknown_hint_preference$2() throws Exception {
		ContentPreferencesUtils.fromPreferenceHeader("return=representation; omit=\"http://www.w3.org/ns/ldp#PreferMinimalContainer http://www.w3.org/ns/ldp#Unknown\"");
	}

	@Test
	public void testRoundtrip() throws Exception {
		assertThat(ContentPreferencesUtils.fromPreferenceHeader(ContentPreferencesUtils.asPreferenceHeader(COMPOSITE_PREFERENCES)),equalTo(COMPOSITE_PREFERENCES));
		assertThat(ContentPreferencesUtils.fromPreferenceHeader(ContentPreferencesUtils.asPreferenceHeader(MULTIPLE_INCLUDES)),equalTo(MULTIPLE_INCLUDES));
		assertThat(ContentPreferencesUtils.fromPreferenceHeader(ContentPreferencesUtils.asPreferenceHeader(MULTIPLE_OMITS)),equalTo(MULTIPLE_OMITS));
	}

}
