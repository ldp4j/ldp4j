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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.2.2
 *   Bundle      : ldp4j-commons-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.net;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.commons.net.ProtocolHandlerConfigurator;
import org.ldp4j.net.Examples.Custom.Resolution.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URIResolutionTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(URIResolutionTest.class);

	private void verifyResolutionScenarios(String rawBase, Map<String, String> scenarios) {
		URI base=URI.create(rawBase);
		int i=0;
		for(Entry<String,String> entry:scenarios.entrySet()) {
			URI relative = URI.create(entry.getKey());
			URI customResolved = base.resolve(relative);
			LOGGER.debug("Scenario {}",++i);
			LOGGER.debug("  - Base......: {}",base);
			LOGGER.debug("  - Relative..: <{}>",relative);
			LOGGER.debug("  - Resolved:");
			LOGGER.debug("    + Expected: {}", entry.getValue());
			LOGGER.debug("    + Result..: {}",customResolved);
			assertThat(customResolved,notNullValue());
			assertThat(relative.toString(),customResolved,equalTo(URI.create(entry.getValue())));
		}
	}

	@Before
	public void setUp() throws Exception {
		ProtocolHandlerConfigurator.reset();
	}

	@Test
	public void testResolve$normalExamples() throws Exception {
		verifyResolutionScenarios(
			Examples.
				Normative.
					Resolution.base(),
			Examples.
				Normative.
					Resolution.
						regular());
	}

	@Test
	public void testResolve$abnormalExamples$extraAncestorSegments() throws Exception {
		verifyResolutionScenarios(
			Examples.
				Normative.
					Resolution.base(),
			Examples.
				Normative.
					Resolution.
						outOfScope());
	}

	@Test
	public void testResolve$abnormalExamples$removeDotSegmentsWhenNotPartOfComponent() throws Exception {
		verifyResolutionScenarios(
			Examples.
				Normative.
					Resolution.base(),
			Examples.
				Normative.
					Resolution.
						dotSegmentAsPartOfComponent());
	}

	@Test
	public void testResolve$abnormalExamples$ignoreUnnecessaryDotSegments() throws Exception {
		verifyResolutionScenarios(
			Examples.
				Normative.
					Resolution.base(),
			Examples.
				Normative.
					Resolution.
						unnecessaryDotSegments());
	}

	@Test
	public void testResolve$abnormalExamples$notIsolatedQueryAndOrFragment() throws Exception {
		verifyResolutionScenarios(
			Examples.
				Normative.
					Resolution.base(),
			Examples.
				Normative.
					Resolution.
						notIsolatedPath());
	}

	@Test
	public void testResolve$normalExamples$noAuthority() throws Exception {
		verifyResolutionScenarios(
			Examples.Custom.Resolution.base(Variant.NO_AUTHORITY),
			Examples.Custom.Resolution.scenarios(Variant.NO_AUTHORITY));
	}

	@Test
	public void testResolve$normalExamples$relativeBase() throws Exception {
		verifyResolutionScenarios(
			Examples.Custom.Resolution.base(Variant.RELATIVE),
			Examples.Custom.Resolution.scenarios(Variant.RELATIVE));
	}

	@Test
	public void testResolve$cornerCase$noAuthority() throws Exception {
		URI base = URI.create("?query");
		URI target=URI.create("relative/uri/");
		URI resolve = base.resolve(target);
		LOGGER.debug("Corner case [no authority]: {}",resolve);
	}

	@Test
	public void testResolve$cornerCase$authority() throws Exception {
		URI base = URI.create("http://www.example.org?query");
		URI target=URI.create("relative/uri/");
		URI resolve = base.resolve(target);
		LOGGER.debug("Corner case [authority]: {}",resolve);
	}

}