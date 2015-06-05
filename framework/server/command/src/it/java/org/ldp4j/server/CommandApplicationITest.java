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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.server.commands.xml.CreateEndpoint;
import org.ldp4j.server.commands.xml.DeleteEndpoint;
import org.ldp4j.server.testing.ServerFrontendTestHelper;
import org.ldp4j.server.testing.TestingApplicationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
public class CommandApplicationITest {

	private static final String DEPLOYMENT = "ldp4j-server-command";
	private static final String CONTROL_PHRASE = "Hello from Tomcat 7.0.20 ("+DEPLOYMENT+")";
	private static final Logger LOGGER=LoggerFactory.getLogger(CommandApplicationITest.class);

	private static IntegrationTestHelper HELPER;

	@Deployment(name=DEPLOYMENT, testable=false)
	@TargetsContainer("tomcat-7.0.20")
	public static WebArchive createLinkedDataPlatformServerWar() {
		return 
			new TestingApplicationBuilder().
				excludeMiddleware().
				withControlPhrase(CONTROL_PHRASE).
				withDeployableName(DEPLOYMENT.concat(".war")).
				withWebXml("web.xml").
				build(
					ServerFrontendTestHelper.getServerArchive(),
					ServerFrontendTestHelper.getServerArchive()
				);
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		HELPER=new IntegrationTestHelper(LOGGER);
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		if(HELPER!=null) {
			HELPER.shutdown();
		}
	}
		
	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testDeployment(@ArquillianResource final URL url) throws IOException {
		LOGGER.debug(String.format("* Checking %s Deployment (%s)",DEPLOYMENT,url));
		InputStream is = url.openStream();
		try {
			String content = IOUtils.toString(is);
			LOGGER.debug("\t- DelegatedSnapshot: " + content);
			assertThat(content,equalTo(CONTROL_PHRASE));
		} finally {
			is.close();
		}
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testProcessCommand$createNewResourceEndpoint(@ArquillianResource final URL url) throws Exception {
		CreateEndpoint command=
			new CreateEndpoint().
				withPath("resource/create/resource").
				withTemplateId("templateId");
		HELPER.base(url);
		HELPER.executeCommand(command);
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testProcessCommand$createBasicContainerEndpoint(@ArquillianResource final URL url) throws Exception {
		CreateEndpoint command=
			new CreateEndpoint().
				withPath("resource/create/basicContainer").
				withTemplateId("templateId");
		HELPER.base(url);
		HELPER.executeCommand(command);
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testProcessCommand$createDirectContainerEndpoint(@ArquillianResource final URL url) throws Exception {
		CreateEndpoint command=
			new CreateEndpoint().
				withPath("resource/create/directContainer").
				withTemplateId("templateId");
		HELPER.base(url);
		HELPER.executeCommand(command);
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testProcessCommand$createIndirectContainerEndpoint(@ArquillianResource final URL url) throws Exception {
		CreateEndpoint command=
			new CreateEndpoint().
				withPath("resource/create/directContainer").
				withTemplateId("templateId");
		HELPER.base(url);
		HELPER.executeCommand(command);
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testProcessCommand$createRepeated(@ArquillianResource final URL url) throws Exception {
		CreateEndpoint command=
			new CreateEndpoint().
				withPath("resource/create/repeated").
				withTemplateId("templateId");
		HELPER.base(url);
		HELPER.executeCommand(command);
		HELPER.executeCommand(command);
	}

	@Test
	@OperateOnDeployment(DEPLOYMENT)
	public void testProcessCommand$deleteExisting(@ArquillianResource final URL url) throws Exception {
		String path = "resource/delete/existing";
		CreateEndpoint createCommand=
			new CreateEndpoint().
				withPath(path).
				withTemplateId("templateId");
		DeleteEndpoint deleteCommand=new DeleteEndpoint(path);
		HELPER.base(url);
		HELPER.executeCommand(createCommand);
		HELPER.executeCommand(deleteCommand);
	}

}