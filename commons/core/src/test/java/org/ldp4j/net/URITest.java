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
import static org.hamcrest.Matchers.*;

import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.commons.net.Handler;
import org.ldp4j.commons.net.ProtocolHandlerConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Add normalization tests
// TODO: Add getter tests
// TODO: Add builder tests
public class URITest {

	private static final Logger LOGGER=LoggerFactory.getLogger(URITest.class);

	private static final String OTHER_QUERY = "otherParam=otherValue";
	private static final String QUERY = "param=value";
	private static final String AUTHORITY = "http://www.example.org";
	private static final String ROOT_DIR = "/";
	private static final String FILE = "file";
	private static final String DIR = "/directory/subdirectory/";

	private static int roundtripScenarioCounter=0;

	private URI existing;
	private URI custom;

	private void verifyRoundtripScenario(URI uBase, URI uTarget) {
		URI customRelative = uBase.relativize(uTarget);
		assertThat(customRelative,notNullValue());
		URI customAbsolute = uBase.resolve(customRelative);
		assertThat(customAbsolute,notNullValue());
		LOGGER.debug("Scenario {}:",++roundtripScenarioCounter);
		LOGGER.debug("\t- Base...........: {}",uBase);
		LOGGER.debug("\t- Target.........: {}",uTarget);
		LOGGER.debug("\t- Relativization");
		LOGGER.debug("\t  + Custom.......: <{}>",customRelative);
		LOGGER.debug("\t- Roundtrip");
		LOGGER.debug("\t  + Custom.......: {}",customAbsolute);
		assertThat(customAbsolute,equalTo(uTarget));
	}

	private URI testCase(String authority, String dir, String file, String query, String fragment) {
		String suffix = "";
		if(query!=null) {
			suffix+="?"+query;
		}
		if(fragment!=null) {
			suffix+="#"+fragment;
		}
		return URI.create(authority+dir+file+suffix);
	}

	private String decorateURI(String baseURI, boolean withQuery, boolean withFragment) {
		StringBuilder builder=new StringBuilder();
		builder.append(baseURI);
		if(withQuery) {
			builder.append("?query=value");
		}
		if(withFragment) {
			builder.append("#fragment");
		}
		return builder.toString();
	}

	private void show(int i, String title, String rawURI, boolean query, boolean fragment) {
		URI uri = URI.create(decorateURI(rawURI,query,fragment));
		LOGGER.debug(String.format("[%03d] %s%n",i,uri.prettyPrint()));
	}

	@Before
	public void setUp() throws Exception {
		existing = URI.create("http://www.example.org");
		custom = URI.create("net://example.org");
		ProtocolHandlerConfigurator.reset();
	}

	@Test
	public void testToURL$existing() throws Exception {
		assertThat(existing.toURL(),notNullValue());
	}

	@Test(expected=MalformedURLException.class)
	public void testToURL$nonLoaded() throws Exception {
		org.junit.Assume.assumeFalse(ProtocolHandlerConfigurator.isSupported("net"));
		custom.toURL();
	}

	@Test
	public void testToURL$loaded() throws Exception {
		ProtocolHandlerConfigurator.addProvider(Handler.class);
		assertThat(custom.toURL(),notNullValue());
	}

	@Test
	public void testRoundtrip$rootFile$queryChange() {
		URI base   = testCase(AUTHORITY,ROOT_DIR,FILE,QUERY,null);
		URI target = testCase(AUTHORITY,ROOT_DIR,FILE,OTHER_QUERY,null);
		verifyRoundtripScenario(base, target);
	}

	@Test
	public void testRoundtrip$file$queryChange() {
		URI base   = testCase(AUTHORITY,DIR,FILE,QUERY,null);
		URI target = testCase(AUTHORITY,DIR,FILE,OTHER_QUERY,null);
		verifyRoundtripScenario(base, target);
	}

	@Test
	public void uriBreakDown() {
		boolean[] flags = new boolean[]{false,true};
		int exampleCase=0;
		for(String rawURI:Examples.Custom.uris()) {
			for(boolean query:flags) {
				for(boolean fragment:flags) {
					show(++exampleCase,"URI",rawURI, query, fragment);
				}
			}
		}
	}

	@Test
	public void testIsRelative$hierarchical$relative() {
		assertThat(URI.create("a/b/c").isRelative(),equalTo(true));
	}

	@Test
	public void testIsRelative$hierarchical$absolute() {
		assertThat(existing.isRelative(),equalTo(false));
	}

	@Test
	public void testIsRelative$opaque() {
		assertThat(URI.create("scheme:a/b/c").isRelative(),equalTo(false));
	}

	@Test
	public void testIsAbsolute$hierarchical$relative() {
		assertThat(URI.create("a/b/c").isAbsolute(),equalTo(false));
	}

	@Test
	public void testIsAbsolute$hierarchical$absolute() {
		assertThat(existing.isAbsolute(),equalTo(true));
	}

	@Test
	public void testIsAbsolute$opaque() {
		assertThat(URI.create("scheme:a/b/c").isAbsolute(),equalTo(true));
	}

	@Test
	public void testIsHierarchical$hierarchical$relative() {
		assertThat(URI.create("a/b/c").isHierarchical(),equalTo(true));
	}

	@Test
	public void testIsHierarchical$hierarchical$absolute() {
		assertThat(existing.isHierarchical(),equalTo(true));
	}

	@Test
	public void testIsHierarchical$opaque() {
		assertThat(URI.create("scheme:a/b/c").isHierarchical(),equalTo(false));
	}

	@Test
	public void testIsOpaque$hierarchical$relative() {
		assertThat(URI.create("a/b/c").isOpaque(),equalTo(false));
	}

	@Test
	public void testIsOpaque$hierarchical$absolute() {
		assertThat(existing.isOpaque(),equalTo(false));
	}

	@Test
	public void testIsOpaquel$opaque() {
		assertThat(URI.create("scheme:a/b/c").isOpaque(),equalTo(true));
	}

	@Test
	public void testUnwrap() {
		assertThat(this.existing.unwrap(),notNullValue());
		assertThat(this.custom.unwrap(),notNullValue());
		assertThat(URI.create("").unwrap(),notNullValue());
	}

	@Test
	public void testHashCodeEqualsContract() {
		assertThat(this.existing,not(equalTo(this.custom)));
		assertThat(this.existing.hashCode(),not(equalTo(this.custom.hashCode())));
		assertThat(this.existing.hashCode(),equalTo(this.existing.unwrap().hashCode()));
	}

}