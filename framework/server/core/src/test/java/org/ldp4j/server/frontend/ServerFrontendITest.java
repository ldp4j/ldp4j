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
import java.util.Date;

import javax.ws.rs.core.Link;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.DateUtils;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.ldp4j.application.engine.context.CreationPreferences.InteractionModel;
import org.ldp4j.commons.testing.categories.DEBUG;
import org.ldp4j.commons.testing.categories.ExceptionPath;
import org.ldp4j.commons.testing.categories.HappyPath;
import org.ldp4j.commons.testing.categories.LDP;
import org.ldp4j.commons.testing.categories.Setup;
import org.ldp4j.example.MyApplication;
import org.ldp4j.server.testing.ServerFrontendTestHelper;
import org.ldp4j.server.testing.ServerFrontendWebAppBuilder;
import org.ldp4j.server.testing.ServerFrontendTestHelper.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
public class ServerFrontendITest {

	private static final String NL = System.getProperty("line.separator");

	private static ServerFrontendTestHelper HELPER;

	private static final String EXAMPLE_BODY =
		"@prefix sav : <http://test/vocab#> ."+NL+
		"<> a sav:Inference ;"+NL+
		"	sav:uses <dataSet1>, <vocabulary1> .";

	private static final String TEST_SUITE_BODY =
		"<> a <http://www.w3.org/ns/ldp#RDFSource> , <http://example.com/ns#Bug> ;"+NL+
		"\t<http://example.com/ns#refersTo> <relativeResource> ;"+NL+
		"\t<http://example.com/ns#severity> \"High\" ;"+NL+
		"\t<http://purl.org/dc/terms/description>  \"Issues that need to be fixed.\" ;"+NL+
		"\t<http://purl.org/dc/terms/title> \"Another bug to test.\" .";


	private static final String DEPLOYMENT = "ldp4j-server-core";
	private static final String CONTROL_PHRASE = "Hello from Tomcat 7.0.20 ("+DEPLOYMENT+")";
	private static final Logger LOGGER=LoggerFactory.getLogger(ServerFrontendITest.class);

	@Deployment(name=DEPLOYMENT, testable=false)
	@TargetsContainer("tomcat-7.0.20")
	public static WebArchive createLinkedDataPlatformServerWar() {
		return
			new ServerFrontendWebAppBuilder().
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

	@Rule
	public TestName testName=new TestName();

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
		LDP.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testEnhancedGet(@ArquillianResource final URL url) throws Exception {
		LOGGER.info("Started {}",testName.getMethodName());
		HELPER.base(url);
		HELPER.setLegacy(false);
		HELPER.httpRequest(HELPER.newRequest(MyApplication.ROOT_PERSON_RESOURCE_PATH,HttpGet.class));
		LOGGER.info("Completed {}",testName.getMethodName());
	}

	@Test
	@Category({
		LDP.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testEnhancedHead(@ArquillianResource final URL url) throws Exception {
		LOGGER.info("Started {}",testName.getMethodName());
		HELPER.base(url);
		HELPER.setLegacy(false);
		HELPER.httpRequest(HELPER.newRequest(MyApplication.ROOT_PERSON_RESOURCE_PATH,HttpHead.class));
		LOGGER.info("Completed {}",testName.getMethodName());
	}

	@Test
	@Category({
		LDP.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testEnhancedOptions(@ArquillianResource final URL url) throws Exception {
		LOGGER.info("Started {}",testName.getMethodName());
		HELPER.base(url);
		HELPER.setLegacy(false);
		HELPER.httpRequest(HELPER.newRequest(MyApplication.ROOT_PERSON_RESOURCE_PATH,HttpOptions.class));
		LOGGER.info("Completed {}",testName.getMethodName());
	}

	@Test
	@Category({
		LDP.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testEnhancedDelete(@ArquillianResource final URL url) throws Exception {
		LOGGER.info("Started {}",testName.getMethodName());
		HELPER.base(url);
		HELPER.setLegacy(false);
		HELPER.httpRequest(HELPER.newRequest(MyApplication.ROOT_PERSON_RESOURCE_PATH,HttpDelete.class));
		LOGGER.info("Completed {}",testName.getMethodName());
	}

	@Test
	@Category({
		LDP.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testEnhancedPut(@ArquillianResource final URL url) throws Exception {
		LOGGER.info("Started {}",testName.getMethodName());
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
		LOGGER.info("Completed {}",testName.getMethodName());
	}

	@Test
	@Category({
		LDP.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testClientResourceSimulation(@ArquillianResource final URL url) throws Exception {
		LOGGER.info("Started {}",testName.getMethodName());
		HELPER.base(url);
		HELPER.setLegacy(false);
		String relativeContainerPath = MyApplication.ROOT_PERSON_RESOURCE_PATH.concat("relatives/");

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
				TEST_SUITE_BODY,
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
		Metadata resourceGetResponse = HELPER.httpRequest(HELPER.newRequest(path,HttpGet.class));
		HttpPut resourcePut = HELPER.newRequest(path,HttpPut.class);
		resourcePut.setEntity(
			new StringEntity(
				TEST_SUITE_BODY,
				ContentType.create("text/turtle", "UTF-8"))
		);
		resourcePut.addHeader(HttpHeaders.IF_MATCH,resourceGetResponse.etag);
		HELPER.httpRequest(resourcePut);
		HELPER.httpRequest(HELPER.newRequest(path,HttpDelete.class));
		HELPER.httpRequest(HELPER.newRequest(path,HttpGet.class));
		HELPER.httpRequest(get);
		HELPER.httpRequest(rcGet);
		LOGGER.info("Completed {}",testName.getMethodName());
	}

	@Test
	@Category({
		LDP.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testClientContainerSimulation(@ArquillianResource final URL url) throws Exception {
		LOGGER.info("Started {}",testName.getMethodName());
		HELPER.base(url);
		HELPER.setLegacy(false);
		HttpGet get = HELPER.newRequest(MyApplication.ROOT_PERSON_CONTAINER_PATH,HttpGet.class);
		HttpPost post = HELPER.newRequest(MyApplication.ROOT_PERSON_CONTAINER_PATH,HttpPost.class);
		post.setEntity(
			new StringEntity(
					TEST_SUITE_BODY,
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
		LOGGER.info("Completed {}",testName.getMethodName());
	}

	@Test
	@Category({
		LDP.class,
		ExceptionPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testUnsupportedCreationPreferences(@ArquillianResource final URL url) throws Exception {
		LOGGER.info("Started {}",testName.getMethodName());
		HELPER.base(url);
		HELPER.setLegacy(false);
		HttpPost post = HELPER.newRequest(MyApplication.ROOT_PERSON_CONTAINER_PATH,HttpPost.class);
		post.setEntity(
			new StringEntity(
				TEST_SUITE_BODY,
				ContentType.create("text/turtle", "UTF-8"))
		);
		String interactionModel=
			Link.
				fromUri(InteractionModel.INDIRECT_CONTAINER.asURI()).
				rel("type").
				build().
				toString();
		post.setHeader("Link",interactionModel);
		Metadata response = HELPER.httpRequest(post);
		assertThat(response.status,equalTo(HttpStatus.SC_FORBIDDEN));
		LOGGER.info("Completed {}",testName.getMethodName());
	}

	/**
	 * Enforce http://tools.ietf.org/html/rfc7232#section-2.2:
	 * if the clock in the request is ahead of the clock of the origin
	 * server (e.g., I request from Spain the update of a resource held in USA)
	 * the last-modified data should be changed to that of the request and not
	 * a generated date from the origin server
	 */
	@Test
	@Category({
		DEBUG.class,
		HappyPath.class
	})
	@OperateOnDeployment(DEPLOYMENT)
	public void testProperLastModified(@ArquillianResource final URL url) throws Exception {
		LOGGER.info("Started {}",testName.getMethodName());
		HELPER.base(url);
		HELPER.setLegacy(false);

		long now = System.currentTimeMillis();
		Date clientPostDate=new Date(now-24*60*60*1000);
		Date clientPutDate=new Date(now+24*60*60*1000);

		HttpPost post = HELPER.newRequest(MyApplication.ROOT_PERSON_CONTAINER_PATH,HttpPost.class);
		post.setEntity(
			new StringEntity(
				TEST_SUITE_BODY,
				ContentType.create("text/turtle", "UTF-8"))
		);
		post.setHeader("Date",DateUtils.formatDate(clientPostDate));

		Metadata postResponse = HELPER.httpRequest(post);
		assertThat(postResponse.status,equalTo(HttpStatus.SC_CREATED));

		String path=HELPER.relativize(postResponse.location);
		HttpGet get=HELPER.newRequest(path,HttpGet.class);

		Metadata getResponse=HELPER.httpRequest(get);
		assertThat(DateUtils.parseDate(getResponse.lastModified).after(clientPostDate),equalTo(true));

		HttpPut put = HELPER.newRequest(path,HttpPut.class);
		put.setEntity(
			new StringEntity(
				getResponse.body,
				ContentType.create("text/turtle", "UTF-8"))
		);
		put.setHeader(HttpHeaders.IF_MATCH,getResponse.etag);
		put.setHeader("Date",DateUtils.formatDate(clientPutDate));

		Metadata putResponse=HELPER.httpRequest(put);
		Date lastModified = DateUtils.parseDate(putResponse.lastModified);
		assertThat(lastModified.getTime(),equalTo(trunk(clientPutDate.getTime())));

		LOGGER.info("Completed {}",testName.getMethodName());
	}

	private long trunk(long time) {
		long result = time/1000;
		result*=1000;
		return result;
	}
}