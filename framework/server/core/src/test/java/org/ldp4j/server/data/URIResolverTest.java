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

public class URIResolverTest {

	private URI endpoint;
	private URI alternative;
	private String[] candidates;
	private URIResolver sut;
	private Map<String,URIDescriptor> candidateMap;

	@Before
	public void setUp() throws Exception {
		endpoint = URI.create("http://www.example.org/target/resource/");
		alternative = URI.create("http://www.ldp4j.org/target/resource/");
		candidates = new String[]{
			"",
			endpoint.toString(),
			"relative",
			endpoint.resolve("relative").toString(),
			"..",
			endpoint.resolve("..").toString(),
			alternative.toString(),
			"urn:example",
			"urn:/test/test2"
		};

		candidateMap = ImmutableMap.
			<String,URIDescriptor>builder().
				put("",URIDescriptor.newDescriptor(Scope.RESOURCE,Type.SELF)).
				put(endpoint.toString(),URIDescriptor.newDescriptor(Scope.ENDPOINT,Type.ABSOLUTE)).
				put("relative",URIDescriptor.newDescriptor(Scope.RESOURCE,Type.CHILD)).
				put(endpoint.resolve("relative").toString(),URIDescriptor.newDescriptor(Scope.ENDPOINT,Type.ABSOLUTE)).
				put("..",URIDescriptor.newDescriptor(Scope.RESOURCE,Type.ANCESTOR)).
				put(endpoint.resolve("..").toString(),URIDescriptor.newDescriptor(Scope.APPLICATION,Type.ABSOLUTE)).
				put(alternative.toString(),URIDescriptor.newDescriptor(Scope.EXTERNAL,Type.ABSOLUTE)).
				put("urn:example",URIDescriptor.newDescriptor(Scope.EXTERNAL,Type.OPAQUE)).
				put("urn:/test/test2",URIDescriptor.newDescriptor(Scope.EXTERNAL,Type.ABSOLUTE)).
				build();
		sut = URIResolver.newInstance(endpoint, alternative);
	}

	@Test
	public void testResolve() {
		for(int i=0;i<candidates.length;i++) {
			String candidate = candidates[i];
			URI erCandidate = endpoint.resolve(candidate);
			URI arCandidate = alternative.resolve(candidate);
			URI rCandidate = sut.resolve(erCandidate, arCandidate);
			System.out.printf("<%s> %s.resolve(<%s>,<%s>): <%s>%n",candidate,sut,erCandidate,arCandidate,rCandidate);
			assertThat("Resolution failed ["+i+"]",rCandidate.toString(),equalTo(candidate));
		}
	}

	@Test
	public void testEvaluate() {
		URI application = endpoint.resolve("..");
		URIDescriber resolver=
			URIDescriber.
				newInstance(
					application, endpoint
				);
		for(Entry<String, URIDescriptor> entry:candidateMap.entrySet()) {
			URI candidate = URI.create(entry.getKey());
			assertThat("Evaluation failed ["+candidate+"]",resolver.describe(candidate),equalTo(entry.getValue()));
		}
	}

	@Test(expected=NullPointerException.class)
	public void testNewInstance$nullEndpoint() throws Exception {
		URIResolver.newInstance(null, alternative);
	}

	@Test(expected=NullPointerException.class)
	public void testNewInstance$nullAlternative() throws Exception {
		URIResolver.newInstance(endpoint, null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewInstance$sameEndpointAsAlternative() throws Exception {
		URIResolver.newInstance(endpoint, endpoint);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewInstance$opaqueEndpoint() throws Exception {
		URIResolver.newInstance(URI.create("urn:opaque"), alternative);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewInstance$opaqueAlternative() throws Exception {
		URIResolver.newInstance(endpoint,URI.create("urn:opaque"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewInstance$relativeEndpoint() throws Exception {
		URIResolver.newInstance(URI.create("relative/path"), alternative);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewInstance$relativeAlternative() throws Exception {
		URIResolver.newInstance(endpoint,URI.create("relativePath"));
	}

}
