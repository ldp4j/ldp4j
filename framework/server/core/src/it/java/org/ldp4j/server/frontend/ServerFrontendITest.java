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
package org.ldp4j.server.frontend;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.ldp4j.commons.testing.categories.DEBUG;
import org.ldp4j.commons.testing.categories.HappyPath;
import org.ldp4j.commons.testing.categories.LDP;
import org.ldp4j.commons.testing.categories.Setup;
import org.ldp4j.example.MyApplication;
import org.ldp4j.server.ServerFrontendTestHelper;
import org.ldp4j.server.ServerFrontendTestHelper.Metadata;
import org.ldp4j.server.testing.TestingApplicationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
public class ServerFrontendITest {

	private static final String NL = System.getProperty("line.separator");

	private static ServerFrontendTestHelper HELPER;

	private static final String EXAMPLE_BODY = 
			"@prefix sav : <http://test/vocab#> ."+NL+
			"@base <http://www.example.org/entities/> ." +NL+
			"_:inference a sav:Inference ;"+NL+
			"	sav:uses <dataSet1>, <vocabulary1> .";

	private static final String DEPLOYMENT = "ldp4j-server-core";
	private static final String CONTROL_PHRASE = "Hello from Tomcat 7.0.20 ("+DEPLOYMENT+")";
	private static final Logger LOGGER=LoggerFactory.getLogger(ServerFrontendITest.class);

	@Deployment(name=DEPLOYMENT, testable=false)
	@TargetsContainer("tomcat-7.0.20")
	public static WebArchive createLinkedDataPlatformServerWar() {
		return 
			new TestingApplicationBuilder().
				excludeMiddleware().
				withControlPhrase(CONTROL_PHRASE).
				withDeployableName(DEPLOYMENT.concat(".war")).
				withWebXml("web.xml").
				withPom("pom.xml").
				build(ServerFrontendTestHelper.getServerArchive());
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		HELPER=new ServerFrontendTestHelper(LOGGER);
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		if(HELPER!=null) {
			HELPER.shutdown();
		}
	}

	@Before
	public void setUp() {
		HELPER.setLegacy(true);
	}
	
	// -------------------------------------------------------------------------
	// Deployment verification
	// -------------------------------------------------------------------------

	@Test
	@Category(Setup.class)
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

	// -------------------------------------------------------------------------
	// LDP compliance tests
	// -------------------------------------------------------------------------

	@Test
	@Category({
//		DEBUG.class,
		LDP.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testEnhancedGet(@ArquillianResource final URL url) throws Exception {
		HELPER.base(url);
		HELPER.setLegacy(false);
		HELPER.httpRequest(HELPER.newRequest(MyApplication.ROOT_PERSON_RESOURCE_PATH,HttpGet.class));
	}
	@Test
	@Category({
//		DEBUG.class,
		LDP.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testEnhancedHead(@ArquillianResource final URL url) throws Exception {
		HELPER.base(url);
		HELPER.setLegacy(false);
		HELPER.httpRequest(HELPER.newRequest(MyApplication.ROOT_PERSON_RESOURCE_PATH,HttpHead.class));
	}
	@Test
	@Category({
		LDP.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testEnhancedOptions(@ArquillianResource final URL url) throws Exception {
		HELPER.base(url);
		HELPER.setLegacy(false);
		HELPER.httpRequest(HELPER.newRequest(MyApplication.ROOT_PERSON_RESOURCE_PATH,HttpOptions.class));
	}

	@Test
	@Category({
//		DEBUG.class,
		LDP.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testEnhancedDelete(@ArquillianResource final URL url) throws Exception {
		HELPER.base(url);
		HELPER.setLegacy(false);
		HELPER.httpRequest(HELPER.newRequest(MyApplication.ROOT_PERSON_RESOURCE_PATH,HttpDelete.class));
	}

	@Test
	@Category({
		LDP.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testEnhancedPut(@ArquillianResource final URL url) throws Exception {
		HELPER.base(url);
		HELPER.setLegacy(false);
		HttpGet get = HELPER.newRequest(MyApplication.ROOT_PERSON_RESOURCE_PATH,HttpGet.class);
		HttpPut put = HELPER.newRequest(MyApplication.ROOT_PERSON_RESOURCE_PATH,HttpPut.class);
		put.setEntity(
			new StringEntity(
				EXAMPLE_BODY,
				ContentType.create("text/turtle", "UTF-8"))
		);
		Metadata getResponse=HELPER.httpRequest(get);
		put.addHeader(HttpHeaders.IF_MATCH,getResponse.etag);
		HELPER.httpRequest(put);
	}

	@Test
	@Category({
		DEBUG.class,
		LDP.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testClientResourceSimulation(@ArquillianResource final URL url) throws Exception {
		HELPER.base(url);
		HELPER.setLegacy(false);
		String relativeContainerPath = MyApplication.ROOT_PERSON_RESOURCE_PATH.concat("/relatives");

		HttpGet get = HELPER.newRequest(MyApplication.ROOT_PERSON_RESOURCE_PATH,HttpGet.class);
		HttpPut put = HELPER.newRequest(MyApplication.ROOT_PERSON_RESOURCE_PATH,HttpPut.class);
		put.setEntity(
			new StringEntity(
				EXAMPLE_BODY,
				ContentType.create("text/turtle", "UTF-8"))
		);
		HttpGet rcGet = HELPER.newRequest(relativeContainerPath,HttpGet.class);
		HttpPost rcPost = HELPER.newRequest(relativeContainerPath,HttpPost.class);
		rcPost.setEntity(
			new StringEntity(
				EXAMPLE_BODY,
				ContentType.create("text/turtle", "UTF-8"))
		);

		HELPER.httpRequest(HELPER.newRequest(relativeContainerPath,HttpOptions.class));
		Metadata getResponse=HELPER.httpRequest(get);
		put.addHeader(HttpHeaders.IF_MATCH,getResponse.etag);
		HELPER.httpRequest(put);
		HELPER.httpRequest(get);

		HELPER.httpRequest(rcGet);
		String location = HELPER.httpRequest(rcPost).location;
		HELPER.httpRequest(get);
		HELPER.httpRequest(rcGet);
		String path=HELPER.relativize(location);
		HELPER.httpRequest(HELPER.newRequest(path,HttpOptions.class));
		HELPER.httpRequest(HELPER.newRequest(path,HttpGet.class));
		HELPER.httpRequest(HELPER.newRequest(path,HttpDelete.class));
		HELPER.httpRequest(HELPER.newRequest(path,HttpGet.class));
		HELPER.httpRequest(get);
		HELPER.httpRequest(rcGet);
	}

	@Test
	@Category({
//		DEBUG.class,
		LDP.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testClientContainerSimulation(@ArquillianResource final URL url) throws Exception {
		HELPER.base(url);
		HELPER.setLegacy(false);
		HttpGet get = HELPER.newRequest(MyApplication.ROOT_PERSON_CONTAINER_PATH,HttpGet.class);
		HttpPost post = HELPER.newRequest(MyApplication.ROOT_PERSON_CONTAINER_PATH,HttpPost.class);
		post.setEntity(
			new StringEntity(
				EXAMPLE_BODY,
				ContentType.create("text/turtle", "UTF-8"))
		);
		HELPER.httpRequest(get);
		String location = HELPER.httpRequest(post).location;
		HELPER.httpRequest(get);
		String path=HELPER.relativize(location);
		HELPER.httpRequest(HELPER.newRequest(path,HttpOptions.class));
		HELPER.httpRequest(HELPER.newRequest(path,HttpGet.class));
		HELPER.httpRequest(HELPER.newRequest(path,HttpDelete.class));
		HELPER.httpRequest(HELPER.newRequest(path,HttpGet.class));
		HELPER.httpRequest(get);
	}
	
}