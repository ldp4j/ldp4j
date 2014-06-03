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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;
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
import org.ldp4j.server.testing.stubs.DeletableContainer;
import org.ldp4j.server.testing.stubs.WorkingContainer;
import org.ldp4j.server.testing.stubs.WorkingResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
public class LinkedDataPlatformResourceManagerITest {

	private static final String DEPLOYMENT = "ldp-server-resource";
	private static final String CONTROL_PHRASE = "Hello from TomEE Plus 1.5.0 ("+DEPLOYMENT+")";
	private static final Logger LOGGER=LoggerFactory.getLogger(LinkedDataPlatformResourceManagerITest.class);

	private static ResourceLocator locator;
	private ILinkedDataPlatformContainerManager containerManager;
	private ILinkedDataPlatformResourceManager resourceManager;
	private URI resourceLocation;

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

	private void logRequest(Object location, String containerId, String resourceId, String entity, String action) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("* "+action+":");
			LOGGER.debug("\t- Target:");
			logValue("Location.",location);
			logValue("Container",containerId);
			logValue("Resource.",resourceId);
			logEntity("Entity...",entity);
		}
	}

	private void logResponse(Response response) throws IOException {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("\t- Response:");
			logValue("Status.......",response.getStatus());
			logValue("ETag.........",response.getMetadata().get("ETag"));
			logValue("Last-Modified",response.getMetadata().get("Last-Modified"));
			logValue("Link.........",response.getMetadata().get("Link"));
			logValue("Location.....",response.getMetadata().get("Location"));
			String entity = IOUtils.toString((InputStream)response.getEntity());
			logEntity("Entity.......",entity);
		}
	}

	private void logValue(String field, Object value) {
		if(value!=null) {
			LOGGER.debug("\t\t+ "+field+": " + value);
		}
	}
	private void logEntity(String field, Object value) {
		if(value!=null) {
			LOGGER.debug("\t\t+ "+field+":\n" + value);
		}
	}
	private void verifyRequiredHeaders(Response response, boolean present) {
		Matcher<Object> filter=notNullValue();
		if(!present) {
			filter=nullValue();
		}
		assertThat(response.getMetadata().get("ETag"),filter);
		assertThat(response.getMetadata().get("Last-Modified"),filter);
		assertThat(response.getMetadata().get("Link"),filter);
	}

	private void prepareResource(URL url, String containerId, String entity) throws Exception {
		containerManager = TestingUtil.createServiceClient(url,ILinkedDataPlatformContainerManager.class);
		resourceManager = TestingUtil.createServiceClient(url,ILinkedDataPlatformResourceManager.class);
		resourceLocation = createResource(url, containerId, entity);
	}

	private URI createResource(URL url, String containerId, String entity) throws IOException {
		logRequest(url,containerId,null,entity,"Create resource");
		Response response = containerManager.createResource(containerId, entity,Format.Turtle.getMime());
		logResponse(response);
		List<Object> list = response.getMetadata().get("Location");
		URI newResource = URI.create(list.get(0).toString());
		return newResource;
	}

	private Response getResource(
				ILinkedDataPlatformResourceManager resourceManager, URI location)
				throws IOException {
			String containerId=locator.resolveContainerFromLocation(location);
			String resourceId=locator.resolveResourceFromLocation(location);
			logRequest(location,containerId,resourceId,null,"Retrieve resource");
			Response response = resourceManager.getResource(containerId,resourceId,Format.Turtle.getMime());
			logResponse(response);
			return response;
		}

	private Response updateResource(
			ILinkedDataPlatformResourceManager resourceManager, URI location, String entity)
			throws IOException {
		String containerId=locator.resolveContainerFromLocation(location);
		String resourceId=locator.resolveResourceFromLocation(location);
		logRequest(location, containerId, resourceId, entity, "Update resource");
		Response response = resourceManager.updateResource(containerId, resourceId, entity, Format.Turtle.getMime());
		logResponse(response);
		return response;
	}

	private Response deleteResource(URI location) throws IOException {
		String containerId=locator.resolveContainerFromLocation(location);
		String resourceId=locator.resolveResourceFromLocation(location);
		logRequest(location,containerId,resourceId,null,"Delete resource");
		Response response = resourceManager.deleteResource(containerId, resourceId);
		logResponse(response);
		return response;
	}
	
	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testDeployment(@ArquillianResource final URL url) throws IOException {
		LOGGER.debug(String.format("* Checking %s Deployment (%s)",DEPLOYMENT,url));
		InputStream is = url.openStream();
		try {
			String content = IOUtils.toString(is);
			LOGGER.debug("\t- Content: " + content);
			assertThat(content,equalTo(CONTROL_PHRASE));
		} finally {
			is.close();
		}
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testGetResource$workingContainer(@ArquillianResource final URL url) throws Exception {
		prepareResource(url,WorkingContainer.CONTAINER_ID,"get resource");

		Response response=getResource(resourceManager, resourceLocation);
		verifyRequiredHeaders(response,true);
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testUpdateResource$workingResource(@ArquillianResource final URL url) throws Exception {
		prepareResource(url,WorkingContainer.CONTAINER_ID,"update resource");
		
		Response response=updateResource(resourceManager,resourceLocation,"new data");
		assertThat(response.getStatus(),equalTo(Status.NO_CONTENT.getStatusCode()));
		verifyRequiredHeaders(response,true);

		response=getResource(resourceManager, resourceLocation);
		verifyRequiredHeaders(response,true);
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testUpdateResource$invalidContent(@ArquillianResource final URL url) throws Exception {
		prepareResource(url,WorkingContainer.CONTAINER_ID,"update resource (invalid)");
		
		Response response=updateResource(resourceManager,resourceLocation,WorkingResource.INVALID_CONTENT);
		assertThat(response.getStatus(),equalTo(Status.CONFLICT.getStatusCode()));
		verifyRequiredHeaders(response,true);

		response=getResource(resourceManager, resourceLocation);
		verifyRequiredHeaders(response,true);
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testDeleteResource$deletableResource(@ArquillianResource final URL url) throws Exception {
		prepareResource(url,DeletableContainer.CONTAINER_ID,"deletable resource");
		
		Response response=getResource(resourceManager, resourceLocation);
		assertThat(response.getStatus(),equalTo(Status.OK.getStatusCode()));
		verifyRequiredHeaders(response,true);

		response = deleteResource(resourceLocation);
		assertThat(response.getStatus(),equalTo(Status.OK.getStatusCode()));
		assertThat(response.getEntity(),notNullValue());
		verifyRequiredHeaders(response,false);
	
		response=getResource(resourceManager, resourceLocation);
		assertThat(response.getStatus(),equalTo(Status.GONE.getStatusCode()));
		verifyRequiredHeaders(response,false);
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testDeleteResource$annotatedResource(@ArquillianResource final URL url) throws Exception {
		prepareResource(url,WorkingContainer.CONTAINER_ID,"annotated resource");
		
		Response response=getResource(resourceManager, resourceLocation);
		assertThat(response.getStatus(),equalTo(Status.OK.getStatusCode()));
		verifyRequiredHeaders(response,true);

		response = deleteResource(resourceLocation);
		assertThat(response.getStatus(),equalTo(Status.OK.getStatusCode()));
		assertThat(response.getEntity(),notNullValue());
		verifyRequiredHeaders(response,false);
	
		response=getResource(resourceManager, resourceLocation);
		assertThat(response.getStatus(),equalTo(Status.GONE.getStatusCode()));
		verifyRequiredHeaders(response,false);
	}

}