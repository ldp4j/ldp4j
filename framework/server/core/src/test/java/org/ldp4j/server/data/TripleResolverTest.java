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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.ldp4j.server.data.spi.ContentTransformationException;

public class TripleResolverTest {

	private MediaType mediaType;

	@Before
	public void setUp() {
		this.mediaType = new MediaType("text", "turtle");
	}

	@Test
	public void testRoundtrip() throws ContentTransformationException {
		URI application = URI.create("http://www.example.org/target/");
		URI endpoint = application.resolve("resource/");
		URI alternative = URI.create("http://www.ldp4j.org/target/resource/");

		String rawEntity = loadResource("/data/example_without_base.ttl");
		TripleResolver safeResolver=
			TripleResolver.
				builder().
					withApplication(application).
					withEndpoint(endpoint).
					withAlternative(alternative).
					withEntity(rawEntity,this.mediaType).
					build();
		int count=0;
		for(TripleResolution resolution:safeResolver.tripleResolutions()) {
			System.out.printf(
				"[%03d] %s%n  - Subject: %s%n  - Object.: %s%n",
				++count,
				resolution.triple(),
				resolution.subjectResolution(),
				resolution.objectResolution());
		}
	}

	@Test
	public void testCornerCase() throws ContentTransformationException {
		URI application = URI.create("http://localhost:8080/ldp4j-server-tckf/ldp4j/");
		URI endpoint = application.resolve("api/basic_container/");
		URI alternative = URI.create("http://www.ldp4j.org/ldp4j-server-tckf/ldp4j/api/basic_container/");

		String rawEntity = loadResource("/data/public-uri-clash.ttl");
		TripleResolver safeResolver=
			TripleResolver.
				builder().
					withApplication(application).
					withEndpoint(endpoint).
					withAlternative(alternative).
					withEntity(rawEntity,this.mediaType).
					build();
		int count=0;
		for(TripleResolution resolution:safeResolver.tripleResolutions()) {
			System.out.printf(
				"[%03d] %s%n  - Subject: %s%n  - Object.: %s%n",
				++count,
				resolution.triple(),
				resolution.subjectResolution(),
				resolution.objectResolution());
		}
	}

	private String loadResource(String resourceName) {
		try {
			return IOUtils.toString(getClass().getResourceAsStream(resourceName), Charset.forName("UTF-8"));
		} catch (IOException e) {
			throw new AssertionError("Could not load resource '"+resourceName+"'");
		}
	}

}
