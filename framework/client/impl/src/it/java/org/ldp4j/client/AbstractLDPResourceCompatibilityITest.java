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
package org.ldp4j.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import javax.ws.rs.core.EntityTag;

import org.apache.cxf.helpers.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.client.impl.CoreLDPResourceITest;
import org.ldp4j.server.testing.TestingApplicationBuilder;
import org.ldp4j.server.testing.stubs.DeletableContainer;
import org.ldp4j.server.testing.stubs.WorkingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
public abstract class AbstractLDPResourceCompatibilityITest {

	private static final String EXAMPLE_SERIALIZATION = "example";

	private static final String DEPLOYMENT = "ldp-server-resource";
	private static final String CONTROL_PHRASE = "Hello from TomEE Plus 1.5.0 ("+DEPLOYMENT+")";
	private static final Logger LOGGER=LoggerFactory.getLogger(CoreLDPResourceITest.class);

	@Deployment(name=DEPLOYMENT, testable=false)
	@TargetsContainer("tomcat-7.0.20")
	public static WebArchive createLinkedDataPlatformServerWar() throws Exception {
		return 
			new TestingApplicationBuilder().
				withControlPhrase(CONTROL_PHRASE).
				withDeployableName(DEPLOYMENT.concat(".war")).
				withStubs().
				build();
	}

	private ILDPContainer containerProxy;

	protected abstract ILDPContainer createContainerClient(URL url) throws Exception;

	protected abstract ILDPResource createResourceClient(URL url) throws Exception;

	private ILDPResource prepareResource(URL url, String containerId, String entity) throws Exception {
		containerProxy=createContainerClient(new URL(url.toString().concat("ldp/containers/"+containerId)));
		URL resourceLocation = createResource(containerProxy,entity);
		return createResourceClient(resourceLocation);
	}
	
	private URL createResource(ILDPContainer containerProxy, String entity) throws Exception {
		LOGGER.debug("* Create resource:");
		LOGGER.debug("\t- Entity: "+entity);
		URL response = containerProxy.createResource(Content.newInstance(entity),Format.TURTLE);
		LOGGER.debug("\t- Response:");
		LOGGER.debug("\t\t+ Location...: " + response);
		return response;
	}

	private void deleteResource(ILDPResource resource, String body)
			throws LDPResourceException, IOException {
		LOGGER.debug("* Retrieving resource:");
		IRepresentation representation = resource.getContent(Format.TURTLE);
		assertThat(representation,notNullValue());
		IContent content = representation.getContent();
		assertThat(content,notNullValue());
		String serialize = content.serialize(String.class);
		LOGGER.debug("\t- Response:");
		LOGGER.debug("\t\t+ Entity: " + serialize);
		assertThat(serialize,equalTo(body));
	
		LOGGER.debug("* Deleting resource:");
		DeletionResult response = resource.delete();
		assertThat(response,notNullValue());
		LOGGER.debug("\t- Response:");
		LOGGER.debug("\t\t+ " + response);
	
		try {
			LOGGER.debug("* Retriving resource again:");
			resource.getContent(Format.TURTLE);
			fail("Resource should not be addressable after deletion");
		} catch (LDPResourceException e) {
			LOGGER.debug("\t- Response:",e);
		}
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public final void testDeployment(@ArquillianResource final URL url) throws IOException {
		InputStream is = url.openStream();
		try {
			String content = IOUtils.readStringFromStream(is);
			LOGGER.debug("\t* Entity: " + content);
			assertThat(content,equalTo(CONTROL_PHRASE));
		} finally {
			is.close();
		}
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public final void testWorkingResource(@ArquillianResource final URL url) throws Exception {
		Date before=new Date();
		LOGGER.debug("* Create resource: " + url);
		LOGGER.debug("\t- Date: "+before+" ("+before.getTime()+")");
		ILDPContainer server = createContainerClient(new URL(url.toString().concat("ldp/containers/WorkingContainer")));
		URL resource = server.createResource(Content.newInstance(EXAMPLE_SERIALIZATION), Format.TURTLE);
		LOGGER.debug("\t- Resource: "+resource);
		ILDPResource resourceProxy=createResourceClient(resource);
		LOGGER.debug("* Getting resource: " + resource);
		IRepresentation content = resourceProxy.getContent(Format.TURTLE);
		assertThat(content,notNullValue());
		String serialization = content.getContent().serialize(String.class);
		String entityTag = content.getEntityTag();
		Date lastModified = content.getLastModified();
		EntityTag etag = EntityTag.valueOf(entityTag);
		assertThat(serialization,equalTo(EXAMPLE_SERIALIZATION));
		assertThat(entityTag,notNullValue());
		assertThat(etag,notNullValue());
		assertThat((Boolean)etag.isWeak(),is(Boolean.TRUE));
		assertThat(lastModified,notNullValue());
		LOGGER.debug("\t- Serialization: "+serialization);
		LOGGER.debug("\t- Entity Tag...: "+entityTag);
		LOGGER.debug("\t- Last Modified: "+lastModified+" ("+lastModified.getTime()+")");

		LOGGER.debug("* Updating resource: " + resource);
		IRepresentation content2 = resourceProxy.updateContent(Content.newInstance("new data"), Format.RDFXML);
		String serialization2 = content2.getContent().serialize(String.class);
		String entityTag2 = content2.getEntityTag();
		Date lastModified2 = content2.getLastModified();
		EntityTag etag2 = EntityTag.valueOf(entityTag);
		assertThat(serialization2,equalTo(""));
		assertThat(entityTag2,notNullValue());
		assertThat(etag2,notNullValue());
		assertThat((Boolean)etag2.isWeak(),is(Boolean.TRUE));
		assertThat(lastModified2,notNullValue());
		LOGGER.debug("\t- Entity Tag...: "+entityTag2);
		LOGGER.debug("\t- Last Modified: "+lastModified2+" ("+lastModified2.getTime()+")");

		LOGGER.debug("* Getting resource: " + resource);
		IRepresentation content3 = resourceProxy.getContent(Format.TURTLE);
		String serialization3 = content3.getContent().serialize(String.class);
		String entityTag3 = content2.getEntityTag();
		Date lastModified3 = content2.getLastModified();
		EntityTag etag3 = EntityTag.valueOf(entityTag);
		assertThat(serialization3,equalTo("new data"));
		assertThat(entityTag3,notNullValue());
		assertThat(etag3,notNullValue());
		assertThat((Boolean)etag3.isWeak(),is(Boolean.TRUE));
		assertThat(lastModified3,notNullValue());
		LOGGER.debug("\t- Serialization: "+serialization3);
		LOGGER.debug("\t- Entity Tag...: "+entityTag3);
		LOGGER.debug("\t- Last Modified: "+lastModified3+" ("+lastModified3.getTime()+")");
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public final void testDeleteResource$deletableResource(@ArquillianResource final URL url) throws Exception {
		String body = "deletable resource";
		ILDPResource resource = prepareResource(url,DeletableContainer.CONTAINER_ID,body);
		
		deleteResource(resource, body);
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public final void testDeleteResource$annotatedResource(@ArquillianResource final URL url) throws Exception {
		String body = "deletable resource";
		ILDPResource resource = prepareResource(url,WorkingContainer.CONTAINER_ID,body);
		
		deleteResource(resource, body);
	}

}
