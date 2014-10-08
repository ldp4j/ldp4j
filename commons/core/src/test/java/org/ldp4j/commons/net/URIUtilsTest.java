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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.commons.net;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.net.MalformedURLException;
import java.net.URI;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ldp4j.commons.net.ProtocolHandlerConfigurator;
import org.ldp4j.commons.net.URIUtils;

@Ignore
public class URIUtilsTest {

	private static final String BASE = "http://www.megatwork.org/smart-aggregator/entity/component/";
	private URI existing;
	private URI custom;

	@Before
	public void setUp() throws Exception {
		existing = URI.create("http://www.example.org");
		custom = URI.create("net://example.org");
		ProtocolHandlerConfigurator.reset();
	}
	
	@Test
	public void testToURL$existing() throws Exception {
		assertThat(URIUtils.toURL(existing),notNullValue());
	}

	@Test(expected=MalformedURLException.class)
	public void testToURL$nonLoaded() throws Exception {
		org.junit.Assume.assumeFalse(ProtocolHandlerConfigurator.isSupported("net"));
		URIUtils.toURL(custom);
	}

	@Test
	public void testToURL$loaded() throws Exception {
		ProtocolHandlerConfigurator.addProvider(Handler.class);
		assertThat(URIUtils.toURL(custom),notNullValue());
	}

	@Test
	public void testResolve$child() {
		assertThat(URIUtils.resolve(URI.create(BASE), URI.create(BASE+"child")),equalTo(URI.create("child")));
	}

	@Test
	public void testResolve$ancestor() {
		URI relativeAncestor = URI.create("../..");
		URI base = URI.create(BASE);
		assertThat(URIUtils.resolve(base, base.resolve(relativeAncestor)),equalTo(relativeAncestor));
	}

	@Test
	public void testResolve$same() {
		URI base = URI.create(BASE);
		assertThat(URIUtils.resolve(base, base),equalTo(URI.create("")));
	}

	@Test
	public void testResolve$differentScheme() {
		URI base = URI.create(BASE);
		URI other = URI.create(BASE.replace("http","https"));
		assertThat(URIUtils.resolve(base, other),equalTo(other));
	}

	@Test
	public void testResolve$differentAuthority() {
		URI base = URI.create(BASE);
		URI other = URI.create(BASE.replace(".org",".org:8080"));
		assertThat(URIUtils.resolve(base, other),equalTo(other));
	}

	@Test
	public void testResolve$cornerCase() {
		String base = BASE.substring(0,BASE.length()-1);
		verifyResolution(BASE, base, "../component", false, false);
	}

	@Test
	public void testResolve$siblings$baseBigger() {
		String base = "http://www.megatwork.org/smart-aggregator/common/segment1/segment2/base/";
		String target = "http://www.megatwork.org/smart-aggregator/common/segment3/target";
		String resolution = "../../../segment3/target";
		verifyResolution(base, target, resolution, false, false);
		verifyResolution(base, target, resolution, false, true);
		verifyResolution(base, target, resolution, true, false);
		verifyResolution(base, target, resolution, true, true);
	}

	@Test
	public void testResolve$siblings$targetBigger() {
		String base = "http://www.megatwork.org/smart-aggregator/common/segment1/base/";
		String target = "http://www.megatwork.org/smart-aggregator/common/segment2/segment3/target";
		String resolution = "../../segment2/segment3/target";
		assertThat(URI.create(base).resolve(URI.create(resolution)),equalTo(URI.create(target)));
		verifyResolution(base, target, resolution, false, false);
		verifyResolution(base, target, resolution, false, true);
		verifyResolution(base, target, resolution, true, false);
		verifyResolution(base, target, resolution, true, true);
	}

	private String extend(String base, boolean query, boolean fragment) {
		StringBuilder builder=new StringBuilder();
		builder.append(base);
		if(fragment) {
			builder.append("#fragment");
		}
		if(query) {
			builder.append("?query=value");
		}
		return builder.toString();
	}
	
	private void verifyResolution(String base, String target, String resolution, boolean query, boolean fragment) {
		assertThat(URI.create(base).resolve(URI.create(resolution)),equalTo(URI.create(target)));
		String extendedTarget = extend(target,query,fragment);
		String extendedResolution = extend(resolution,query,fragment);
		URI baseURI =URI.create(base);
		URI targetURI = URI.create(extendedTarget);
		URI resolutionURI = URI.create(extendedResolution);
		URI relative = URIUtils.resolve(baseURI, targetURI);
		assertThat(relative,equalTo(resolutionURI));
		assertThat(baseURI.resolve(resolutionURI),equalTo(targetURI));
		assertThat(baseURI.resolve(relative),equalTo(targetURI));
	}

}
