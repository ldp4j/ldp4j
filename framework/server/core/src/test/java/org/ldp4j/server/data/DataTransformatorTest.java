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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetHelper;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.DataSets;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.data.RelativeIndividualId;
import org.ldp4j.application.vocabulary.RDFS;

public class DataTransformatorTest {

	private static final URI APPLICATION_BASE = uri("http://localhost:8080/ldp4j/");
	private static final URI NANDANA_ENDPOINT = uri("api/nandana/");
	private static final URI NANDANA_LOCATION = APPLICATION_BASE.resolve(NANDANA_ENDPOINT);

	private static final ManagedIndividualId NANDANA_ID = ManagedIndividualId.createId(NamingScheme.getDefault().name("Nandana"), "MyHandler");
	private static final RelativeIndividualId NANDANA_ME_ID = RelativeIndividualId.createId(NANDANA_ID,uri("#me"));

	private DataTransformator sut;

	private static URI uri(String uri) {
		return URI.create(uri);
	}

	private String loadResource(String resourceName) {
		try {
			return IOUtils.toString(getClass().getResourceAsStream(resourceName), Charset.forName("UTF-8"));
		} catch (IOException e) {
			throw new AssertionError("Could not load resource '"+resourceName+"'");
		}
	}

	@Before
	public void setUp() throws Exception {
		sut =
			DataTransformator.
				create(APPLICATION_BASE).
				mediaType(new MediaType("text","turtle")).
				permanentEndpoint(NANDANA_ENDPOINT).
				enableResolution(
					new ResourceResolver() {
						@Override
						public URI resolveResource(ManagedIndividualId id) {
							if(id.equals(NANDANA_ID)) {
								return NANDANA_LOCATION;
							}
							return null;
						}
						@Override
						public ManagedIndividualId resolveLocation(URI path) {
							if(path.equals(NANDANA_LOCATION)) {
								return NANDANA_ID;
							}
							return null;
						}
					}
				);
	}

	@Test
	public void testUnmarshall() throws Exception {
		DataSet dataSet = sut.unmarshall(loadResource("/data/relative-managed-individuals.ttl"));
		assertThat(dataSet.numberOfIndividuals(),greaterThan(2));
		assertThat(dataSet.individualOfId(NANDANA_ID),notNullValue());
		assertThat(dataSet.individualOfId(NANDANA_ME_ID),notNullValue());
		String data=sut.marshall(dataSet);
		assertThat(data,notNullValue());
	}

	@Test
	public void testCornerCase() throws Exception {
		DataTransformator sut =
			DataTransformator.
				create(URI.create("http://localhost:8080/ldp4j-server-tckf/ldp4j/")).
				surrogateEndpoint(URI.create("api/basic_container/")).
				enableResolution(
					new ResourceResolver() {
						@Override
						public URI resolveResource(ManagedIndividualId id) {
							return null;
						}
						@Override
						public ManagedIndividualId resolveLocation(URI path) {
							return null;
						}
					}
				).
				mediaType(new MediaType("text","turtle"));

		sut.unmarshall(loadResource("/data/public-uri-clash.ttl"));
	}

	@Test
	public void testMarshallOmmitsUnknownManagedIndividuals() throws Exception {
		DataTransformator sut =
			DataTransformator.
				create(URI.create("http://localhost:8080/ldp4j-server-tckf/ldp4j/")).
				surrogateEndpoint(URI.create("api/basic_container/")).
				enableResolution(
					new ResourceResolver() {
						@Override
						public URI resolveResource(ManagedIndividualId id) {
							return null;
						}
						@Override
						public ManagedIndividualId resolveLocation(URI path) {
							return null;
						}
					}
				).
				mediaType(new MediaType("text","turtle"));
		DataSet dataSet = DataSets.createDataSet(name("test"));
		DataSetHelper helper = DataSetUtils.newHelper(dataSet);
		helper.
			managedIndividual(name("unknown"),"managerId").
				property(RDFS.LABEL).
					withLiteral("label").
				property("http://www.example.org/vocab#targetManagedResource").
					withIndividual(name("missing"),"missingManagerId").
				property("http://www.example.org/vocab#targetBNode").
					withIndividual("unknown");
		helper.
			localIndividual(name("unknown")).
				property(RDFS.LABEL).
					withLiteral("label").
				property("http://www.example.org/vocab#targetManagedResource").
					withIndividual(name("unknown"),"managerId");

		String marshall = sut.marshall(dataSet);
		System.out.println(marshall);
	}

	private Name<String> name(String id) {
		return NamingScheme.getDefault().name(id);
	}

	@Test
	public void testJSON_LD() throws Exception {
		DataSet dataSet = sut.unmarshall(loadResource("/data/relative-managed-individuals.ttl"));
		DataTransformator marshaller=sut.mediaType(new MediaType("application","ld+json"));
		String data=marshaller.marshall(dataSet);
		assertThat(data,notNullValue());
	}

}
