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
package org.ldp4j.server.data;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.server.data.URIDescriptor.Scope;
import org.ldp4j.server.data.URIDescriptor.Type;

import com.google.common.collect.ImmutableMap;

public class URIDescriberTest {

	private URI endpoint;
	private URI target;
	private Map<String,URIDescriptor> candidateMap;
	private URIDescriber sut;
	private URI application;

	@Before
	public void setUp() throws Exception {
		endpoint = URI.create("http://www.example.org/target/resource/");
		application = endpoint.resolve("..");
		target = URI.create("http://www.ldp4j.org/target/resource/");
		candidateMap = ImmutableMap.
			<String,URIDescriptor>builder().
				put("",URIDescriptor.newDescriptor(Scope.RESOURCE,Type.SELF)).
				put(endpoint.toString(),URIDescriptor.newDescriptor(Scope.ENDPOINT,Type.ABSOLUTE)).
				put("relative",URIDescriptor.newDescriptor(Scope.RESOURCE,Type.CHILD)).
				put(endpoint.resolve("relative").toString(),URIDescriptor.newDescriptor(Scope.ENDPOINT,Type.ABSOLUTE)).
				put(endpoint.resolve("relative/resource").toString(),URIDescriptor.newDescriptor(Scope.RESOURCE,Type.ABSOLUTE)).
				put("..",URIDescriptor.newDescriptor(Scope.RESOURCE,Type.ANCESTOR)).
				put(application.toString(),URIDescriptor.newDescriptor(Scope.APPLICATION,Type.ABSOLUTE)).
				put(target.toString(),URIDescriptor.newDescriptor(Scope.EXTERNAL,Type.ABSOLUTE)).
				put("urn:example",URIDescriptor.newDescriptor(Scope.EXTERNAL,Type.OPAQUE)).
				put("urn:/test/test2",URIDescriptor.newDescriptor(Scope.EXTERNAL,Type.ABSOLUTE)).
				build();
		sut = URIDescriber.newInstance(application, endpoint);
	}

	@Test
	public void testDescribe() {
		for(Entry<String, URIDescriptor> entry:candidateMap.entrySet()) {
			URI candidate = URI.create(entry.getKey());
			URIDescriptor result = sut.describe(candidate);
			System.out.printf("%s.describe(%s): %s%n",sut,candidate,result);
			assertThat("Evaluation failed ["+candidate+"]",result,equalTo(entry.getValue()));
		}
	}

	@Test(expected=NullPointerException.class)
	public void testNewInstance$nullBase() throws Exception {
		URIDescriber.newInstance(null, target);
	}

	@Test(expected=NullPointerException.class)
	public void testNewInstance$nullTarget() throws Exception {
		URIDescriber.newInstance(endpoint, null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewInstance$opaqueApplication() throws Exception {
		URIDescriber.newInstance(URI.create("urn:opaque"), endpoint);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewInstance$opaqueEndpoint() throws Exception {
		URIDescriber.newInstance(application,URI.create("urn:opaque"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewInstance$relativeApplication() throws Exception {
		URIDescriber.newInstance(URI.create("relative/path"), endpoint);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewInstance$relativeEndpoint() throws Exception {
		URIDescriber.newInstance(application,URI.create("relativePath"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewInstance$endpointNotRelativeFromApplication() throws Exception {
		URIDescriber.newInstance(application, target);
	}

}
