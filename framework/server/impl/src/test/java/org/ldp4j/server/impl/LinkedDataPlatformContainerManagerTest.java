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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URL;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ldp4j.server.Format;
import org.ldp4j.server.LinkedDataPlatformException;
import org.ldp4j.server.impl.bootstrap.TestHelper;
import org.ldp4j.server.impl.LinkedDataPlatformContainerManager;
import org.ldp4j.server.impl.ResourceLocator;
import org.ldp4j.server.testing.stubs.FailingContainer;
import org.ldp4j.server.testing.stubs.WorkingContainer;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LinkedDataPlatformContainerManagerTest {

	private LinkedDataPlatformContainerManager sut;
	private final static String BODY="test";

	@BeforeClass
	public static void setUpBefore() {
		helper = new TestHelper();
	}
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		helper.initialize(new URL("http://localhost:9090/ldp"));
		sut=new LinkedDataPlatformContainerManager();
		sut.setUriInfo(info);
		sut.setResourceLocator(new ResourceLocator());
	}
	
	@After
	public void tearDown() {
		helper.shutdown();
	}
	
	@Test
	public void testUnknownContainer() {
		Response response = sut.createResource(BODY, BODY,Format.Turtle.getMime());
		assertThat(response,notNullValue());
		assertThat(response.getStatus(),equalTo(Status.NOT_FOUND.getStatusCode()));
		assertThat(response.getEntity(),equalTo((Object)String.format("Container '%s' does not exist.",BODY)));
	}

	@Mock private UriInfo info;
	@Mock private UriBuilder uriBuilder;
	private static TestHelper helper;
	
	@Test
	public void testSuccessfulResourceCreation() {
		String relativePath = "resources/".concat(WorkingContainer.CONTAINER_ID).concat("/").concat(Integer.toHexString(BODY.hashCode()));
		String absolutePath = "http://"+relativePath;

		when(info.getBaseUriBuilder()).thenReturn(uriBuilder);
		when(uriBuilder.path(relativePath)).thenReturn(uriBuilder);
		when(uriBuilder.build()).thenReturn(URI.create(absolutePath));
		
		Response response = sut.createResource(WorkingContainer.CONTAINER_ID, BODY, Format.Turtle.getMime());
		assertThat(response,notNullValue());
		assertThat(response.getStatus(),equalTo(Status.CREATED.getStatusCode()));
		assertThat(response.getEntity().toString(),equalTo(absolutePath));
		assertThat(response.getMetadata().get("Location"),hasItem((Object)relativePath));
		
		verify(info).getBaseUriBuilder();
		verify(uriBuilder).path(relativePath);
		verify(uriBuilder).build();
	}

	@Test
	public void testFailedResourceCreation() {
		Response response = sut.createResource(FailingContainer.CONTAINER_ID, BODY,Format.Turtle.getMime());
		assertThat(response,notNullValue());
		assertThat(response.getStatus(),equalTo(Status.INTERNAL_SERVER_ERROR.getStatusCode()));
		assertThat(response.getEntity().toString(),startsWith(LinkedDataPlatformException.class.getCanonicalName()));
	}
}
