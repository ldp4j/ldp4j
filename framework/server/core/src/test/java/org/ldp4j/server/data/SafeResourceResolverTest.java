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
package org.ldp4j.server.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.server.spi.ContentTransformationException;
import org.ldp4j.server.spi.IMediaTypeProvider;
import org.ldp4j.server.spi.IMediaTypeProvider.Unmarshaller;
import org.ldp4j.server.spi.RuntimeInstance;

import com.google.common.collect.ImmutableMap;

public class SafeResourceResolverTest {

	private MediaType mediaType;

	private IMediaTypeProvider provider;

	@Before
	public void setUp() {
		this.mediaType = new MediaType("text", "turtle");
		this.provider = RuntimeInstance.getInstance().getMediaTypeProvider(mediaType);
	}

	@Test
	public void testRoundtrip() throws ContentTransformationException {
		URI application = URI.create("http://www.example.org/target/");
		URI endpoint = application.resolve("resource/");
		URI alternative = URI.create("http://www.ldp4j.org/target/resource/");

		URI r1 = application.resolve("other/");
		URI r2 = endpoint.resolve("other/");
		URI r3 = endpoint.resolve("other/another/");

		ManagedIndividualId id1 = resourceId(1, "example");
		ManagedIndividualId id2 = resourceId(2, "example");
		ManagedIndividualId id3 = resourceId(3, "example");
		ManagedIndividualId id4 = resourceId(4, "example");
		ManagedIndividualId id5 = resourceId(5, "example");
		final Map<URI,ManagedIndividualId> resolutions=
			ImmutableMap.
				<URI,ManagedIndividualId>builder().
					put(application,id1).
					put(endpoint,id2).
					put(r1,id3).
					put(r2,id4).
					put(r3,id5).
					build();
		final Set<URI> checked=new HashSet<URI>();
		ResourceResolver resolver=new ResourceResolver() {
			@Override
			public URI resolveResource(ManagedIndividualId id) {
				return null;
			}

			@Override
			public ManagedIndividualId resolveLocation(URI path) {
				checked.add(path);
				return resolutions.get(path);
			}
		};

		String rawEntity = loadResource("/data/example_without_base.ttl");
		SafeResourceResolver safeResolver=
			SafeResourceResolver.
				builder().
					withApplication(application).
					withEndpoint(endpoint).
					withAlternative(alternative).
					withEntity(rawEntity,this.mediaType).
					build();
		safeResolver=safeResolver.setResourceResolver(resolver);
		DataSet dataSet=unmarshall(rawEntity,endpoint,safeResolver);
		System.out.println(dataSet);
		assertThat(checked,hasItems(application,endpoint,r1,r2,r3));
		assertThat(dataSet.individualOfId(id1),notNullValue());
		assertThat(dataSet.individualOfId(id2),notNullValue());
		assertThat(dataSet.individualOfId(id3),notNullValue());
		assertThat(dataSet.individualOfId(id4),notNullValue());
		assertThat(dataSet.individualOfId(id5),notNullValue());
	}

	private ManagedIndividualId resourceId(int id, String templateId) {
		return ManagedIndividualId.createId(NamingScheme.getDefault().name(id), templateId);
	}

	private DataSet unmarshall(String entity, URI base, ResourceResolver resolver) throws ContentTransformationException {
		return newUnmarshaller(base,resolver).unmarshall(entity, this.mediaType);
	}

	private Unmarshaller newUnmarshaller(URI base, ResourceResolver resolver) {
		return this.provider.newUnmarshaller(ImmutableContext.newInstance(base,resolver));
	}

	private String loadResource(String resourceName) {
		try {
			return IOUtils.toString(getClass().getResourceAsStream(resourceName), Charset.forName("UTF-8"));
		} catch (IOException e) {
			throw new AssertionError("Could not load resource '"+resourceName+"'");
		}
	}

}
