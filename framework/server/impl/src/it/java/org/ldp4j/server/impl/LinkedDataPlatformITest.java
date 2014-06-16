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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.server.Format;
import org.ldp4j.server.testing.TestingApplicationBuilder;
import org.ldp4j.server.testing.TestingUtil;
import org.ldp4j.server.testing.stubs.WorkingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
public class LinkedDataPlatformITest {

	private static final String DEPLOYMENT = "ldp-server";
	private static final String CONTROL_PHRASE = "Hello from TomEE Plus 1.5.0 ("+DEPLOYMENT+")";
	private static final Logger LOGGER=LoggerFactory.getLogger(LinkedDataPlatformITest.class);

	private static ResourceLocator locator;
	private ILinkedDataPlatformContainerManager containerManager;
	private ILinkedDataPlatformResourceManager resourceManager;
	private URI resourceLocation;
	private URL containerLocation;

	@Deployment(name=DEPLOYMENT, testable=false)
	@TargetsContainer("tomcat-7.0.20")
	public static WebArchive createLinkedDataPlatformServerWar() {
		return 
			new TestingApplicationBuilder().
				withControlPhrase(CONTROL_PHRASE).
				withDeployableName(DEPLOYMENT.concat(".war")).
				withStubs().
				build();
	}
	
	@BeforeClass
	public static void setUpBefore() {
		locator = new ResourceLocator();
	}

	private void prepareResource(URL url, String containerId, String entity) throws Exception {
		containerLocation=url;
		containerManager = TestingUtil.createServiceClient(url,ILinkedDataPlatformContainerManager.class);
		resourceManager = TestingUtil.createServiceClient(url,ILinkedDataPlatformResourceManager.class);
		resourceLocation = createResource(url, containerId, entity);
	}

	private URI createResource(URL url, String containerId, String entity) throws IOException {
		LOGGER.debug("* Create resource:");
		LOGGER.debug("\t- Target:");
		LOGGER.debug("\t\t+ Base.....: "+url);
		LOGGER.debug("\t\t+ Container: "+containerId);
		LOGGER.debug("\t- Entity.....: " + entity);
		Response response = containerManager.createResource(containerId, entity,Format.TURTLE.getMime());
		LOGGER.debug("\t- Response:");
		LOGGER.debug("\t\t+ Status.....: " + response.getStatus());
		LOGGER.debug("\t\t+ Body.......: " + IOUtils.toString((InputStream)response.getEntity()));
		List<Object> list = response.getMetadata().get("Location");
		LOGGER.debug("\t\t+ Location...: " + list);
	
		URI newResource = URI.create(list.get(0).toString());
		return newResource;
	}

	private Response deleteResource(URI location) throws IOException {
		String containerId=locator.resolveContainerFromLocation(location);
		String resourceId=locator.resolveResourceFromLocation(location);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("* Delete resource:");
			LOGGER.debug("\t- Target:");
			LOGGER.debug("\t\t+ Location.: " + location);
			LOGGER.debug("\t\t+ Container: " + containerId);
			LOGGER.debug("\t\t+ Resource.: " + resourceId);
		}
		Response response = resourceManager.deleteResource(containerId, resourceId);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("\t- Response:");
			LOGGER.debug("\t\t+ Status.......: " + response.getStatus());
			LOGGER.debug("\t\t+ ETag.........: " + response.getMetadata().get("ETag"));
			LOGGER.debug("\t\t+ Last-Modified: " + response.getMetadata().get("Last-Modified"));
			LOGGER.debug("\t\t+ Link.........: " + response.getMetadata().get("Link"));
			LOGGER.debug("\t\t+ Entity.......: " + IOUtils.toString((InputStream)response.getEntity()));
		}
		return response;
	}

	private Response searchContainer(String containerId) throws Exception {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("* Searching container:");
			LOGGER.debug("\t- Target:");
			LOGGER.debug("\t\t+ Location..: " + containerLocation);
			LOGGER.debug("\t\t+ Name: " + containerId);
		}
		Response response = containerManager.search(null, containerId, Format.TURTLE.getMime());
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("\t- Response:");
			LOGGER.debug("\t\t+ Status.......: " + response.getStatus());
		}
		if(response.getEntity()!=null) {
			String entity=IOUtils.toString((InputStream)response.getEntity());
			response=Response.status(response.getStatus()).entity(entity).build();
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("\t\t+ Entity.......: " +entity);
			}
		}
		return response;
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testDeployment(@ArquillianResource final URL url) throws IOException {
		LOGGER.debug(String.format("* Checking %s Deployment (%s)",DEPLOYMENT,url));
		InputStream is = url.openStream();
		try {
			String content = IOUtils.toString(is);
			LOGGER.debug("\t- Entity: " + content);
			assertThat(content,equalTo(CONTROL_PHRASE));
		} finally {
			is.close();
		}
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testSearchResource$workingContainer(@ArquillianResource final URL url) throws Exception {
		prepareResource(url,WorkingContainer.CONTAINER_ID,"search resource");
		
		Response response = searchContainer(WorkingContainer.CONTAINER_ID);
		assertThat(response.getStatus(),equalTo(Status.OK.getStatusCode()));
		assertThat(response.getEntity(),notNullValue());
		assertThat(response.getEntity().toString(),containsString(resourceLocation.toString()));
		
		response = deleteResource(resourceLocation);
		assertThat(response.getStatus(),equalTo(Status.OK.getStatusCode()));
		assertThat(response.getEntity(),notNullValue());
	
		response = searchContainer(WorkingContainer.CONTAINER_ID);
		assertThat(response.getStatus(),equalTo(Status.OK.getStatusCode()));
		assertThat(response.getEntity(),notNullValue());
		assertThat(response.getEntity().toString(),not(containsString(resourceLocation.toString())));
	}
	
}