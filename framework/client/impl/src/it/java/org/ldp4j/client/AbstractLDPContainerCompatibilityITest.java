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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.cxf.helpers.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.server.testing.TestingApplicationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
public abstract class AbstractLDPContainerCompatibilityITest {

	private static final String DEPLOYMENT = "ldp-server-container";
	private static final String CONTROL_PHRASE = "Hello from TomEE Plus 1.5.0 ("+DEPLOYMENT+")";
	private static final Logger LOGGER=LoggerFactory.getLogger(AbstractLDPContainerCompatibilityITest.class);

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

	protected abstract ILDPContainer createContainerClient(URL url) throws Exception;

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testDeployment(@ArquillianResource final URL url) throws IOException {
		InputStream is = url.openStream();
		try {
			String content = IOUtils.readStringFromStream(is);
			LOGGER.debug("\t* Content: " + content);
			assertThat(content,equalTo(CONTROL_PHRASE));
		} finally {
			is.close();
		}
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testWorkingContainer(@ArquillianResource final URL url) throws Exception {
		LOGGER.debug("* Create resource: " + url);
		ILDPContainer server = createContainerClient(new URL(url.toString().concat("ldp/containers/WorkingContainer")));
		URL resource = server.createResource(Content.newInstance("example"), Format.Turtle);
		assertThat(resource,notNullValue());
		LOGGER.debug("\t- Resource: "+resource);
		
		LOGGER.debug("* Get full description: " + url);
		IContent description = server.getDescription(Format.Turtle,false,false);
		assertThat(description,notNullValue());
		String content = description.serialize(String.class);
		LOGGER.debug("\t- Description: "+content);
		assertThat(content,containsString(resource.toString()));

		LOGGER.debug("* Get partial description: " + url);
		description = server.getDescription(Format.Turtle,false,true);
		assertThat(description,notNullValue());
		content = description.serialize(String.class);
		LOGGER.debug("\t- Description: "+content);
		assertThat(content,containsString(resource.toString()));

		LOGGER.debug("* Get raw description: " + url);
		description = server.getDescription(Format.Turtle,true,false);
		assertThat(description,notNullValue());
		content = description.serialize(String.class);
		LOGGER.debug("\t- Description: "+content);
		assertThat(content,not(containsString(resource.toString())));
	}

	@Test(expected=LDPContainerException.class)
	@OperateOnDeployment(DEPLOYMENT)
	public void testFailingContainer(@ArquillianResource final URL url) throws Exception {
		LOGGER.debug("* Create resource: " + url);
		ILDPContainer server = createContainerClient(new URL(url.toString().concat("ldp/containers/FailingContainer")));
		server.createResource(Content.newInstance("example"), Format.Turtle);
	}

}