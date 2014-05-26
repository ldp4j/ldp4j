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
 *   Artifact    : org.ldp4j.framework:ldp4j-client-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-client-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.client.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.client.Content;
import org.ldp4j.client.Format;
import org.ldp4j.client.impl.CoreLDPContainer;
import org.ldp4j.client.impl.spi.IRemoteLDPContainer;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CoreLDPContainerTest {

	private class MockRemoteLDPContainer implements IRemoteLDPContainer {
		private final URL target;

		public MockRemoteLDPContainer(URL target) {
			this.target = target;
		}

		@Override
		public URL getTarget() {
			return this.target;
		}

		@Override
		public Response createResourceFromTurtle(String content) {
			return null;
		}

		@Override
		public Response createResourceFromRDFXML(String content) {
			return null;
		}

		@Override
		public Response getResource(String format) {
			return null;
		}

		@Override
		public Response getResource(String format, boolean includeMembers, boolean includeSummary) {
			return null;
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testLDPContainer$nullGateway() {
		new CoreLDPContainer(null);
	}

	@Test
	public void testLDPContainer$nonNullGateway() throws Exception {
		new CoreLDPContainer(gateway);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreateResource$nullContent() throws Exception {
		CoreLDPContainer sut=new CoreLDPContainer(gateway);
		sut.createResource(null, Format.Turtle);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreateResource$nullFormat() throws Exception {
		CoreLDPContainer sut=new CoreLDPContainer(gateway);
		sut.createResource(Content.newInstance("test"), null);
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		target = new URL("http://localhost");
		gateway = new MockRemoteLDPContainer(target);
	}

	@Mock private IRemoteLDPContainer gatewayMock;
	@Mock private Response response;
	@Mock private MultivaluedMap<String, Object> headers;

	private URL target;
	private MockRemoteLDPContainer gateway;

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateResource$TurtleFormat() throws Exception {
		Mockito.
			when(gatewayMock.getTarget()).
			thenReturn(target,target,target);
		
		Mockito.
			when(gatewayMock.createResourceFromTurtle("test")).
			thenReturn(response);

		Mockito.
			when(response.getStatus()).
			thenReturn(Status.CREATED.getStatusCode());

		Mockito.
			when(response.getEntity()).
			thenReturn(target.toString(),target.toString(),target.toString());
		
		Mockito.
			when(response.getMetadata()).
			thenReturn(headers,headers,headers);
		
		List<Object> locations=new ArrayList<Object>();
		locations.add("htp:fail");
		locations.add(target);
		
		Mockito.
			when(headers.get("Location")).
			thenReturn(locations,locations,locations);

		CoreLDPContainer sut=new CoreLDPContainer(gatewayMock);
		URL resource = sut.createResource(Content.newInstance("test"), Format.Turtle);
		assertThat(resource,notNullValue());
		assertThat(resource,equalTo(target));
	}
}