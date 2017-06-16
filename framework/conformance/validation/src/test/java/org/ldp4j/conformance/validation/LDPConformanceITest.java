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
 *   Artifact    : org.ldp4j.framework:ldp4j-conformance-validation:0.2.2
 *   Bundle      : ldp4j-conformance-validation-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.conformance.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.conformance.validation.SuiteResultCollector.SuiteResults;
import org.ldp4j.conformance.validation.SuiteResultCollector.SuiteResults.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.ldp.testsuite.LdpTestSuite;

import com.jayway.restassured.RestAssured;

/**
 * LDP Test Suite runner, see <a
 * href="https://github.com/w3c/ldp-testsuite">https
 * ://github.com/w3c/ldp-testsuite</a>.
 */
@RunWith(Arquillian.class)
public class LDPConformanceITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(LDPConformanceITest.class);

	private static final String TEXT_TURTLE = "text/turtle";
	private static final int    OK          = 200;

	/** @see org.testng.TestNG#HAS_FAILURE */
	private static final int TESTNG_STATUS_HAS_FAILURE = 1;

	/** @see org.testng.TestNG#HAS_SKIPPED */
	private static final int TESTNG_STATUS_HAS_SKIPPED = 2;

	/** @see org.testng.TestNG#HAS_NO_TEST */
	private static final int TESTNG_STATUS_HAS_NO_TEST = 8;

	@Deployment(name="default",testable=false)
	@TargetsContainer("local")
	public static WebArchive createDeployment() throws Exception {
		return AcceptanceTestDriver.applicationArchive("tckf.war");
	}

	@Test
	@OperateOnDeployment("default")
	public void testService(@ArquillianResource final URL contextURL) throws Exception {
		RestAssured.
			given().
				header("Accept",TEXT_TURTLE).
				baseUri(contextURL.toString()).
		expect().
			statusCode(OK).
			contentType(TEXT_TURTLE).
		when().
			get(LDPConformanceITest.resolve(contextURL, "ldp4j/api/resource/"));
	}

	@Test
	@OperateOnDeployment("default")
	public void testBasicContainerConformance(@ArquillianResource final URL contextURL) throws Exception {
		LOGGER.info("Running W3C official LDP Basic Container Test Suite against '{}' server",contextURL);
		final String reportPath = targetWorkingDir().getAbsolutePath();

		final Map<String, String> options = new HashMap<>();
		options.put("server", LDPConformanceITest.resolve(contextURL,"ldp4j/api/basic_container/"));
		options.put("basic", null);
		options.put("cont-res",LDPConformanceITest.resolve(contextURL, "ldp4j/api/resource/"));
		options.put("read-only-prop","http://www.example.org/vocab#creationDate");
		options.put("httpLogging", null);
		options.put("skipLogging", null);
		options.put("listeners",SuiteResultCollector.class.getCanonicalName());
		options.put("output", reportPath);

		LOGGER.debug("You can find LDP Basic Container Test Suite outputs at {}", reportPath);

		final LdpTestSuite testSuite = new LdpTestSuite(options);
		testSuite.run();

		final SuiteResults results = SuiteResultCollector.lastResults();

		LOGGER.info("LDP Basic Container Test Suite execution completed:", results);

		assertThat(results.numberOfTests(Result.values()), equalTo(90));
		assertThat(results.numberOfTests(Result.PASSED), equalTo(72));
		assertThat(results.numberOfTests(Result.FAILED), equalTo(2));
		assertThat(results.hasTestResult("testPatchMethod", Result.FAILED),equalTo(true));
		assertThat(results.hasTestResult("testPutToCreate", Result.FAILED),equalTo(true));
		assertThat(results.numberOfTests(Result.SKIPPED), equalTo(16));

		assertThat(
			"LDP Basic Container Test Suite finished without errors",
			testSuite.getStatus() & TESTNG_STATUS_HAS_FAILURE,
			equalTo(TESTNG_STATUS_HAS_FAILURE));
		assertThat(
			"LDP Basic Container Test Suite finished without skipped tests",
			testSuite.getStatus() & TESTNG_STATUS_HAS_SKIPPED,
			equalTo(TESTNG_STATUS_HAS_SKIPPED));
		assertThat(
			"LDP Basic Container Test Suite is empty - no test run",
			testSuite.getStatus() & TESTNG_STATUS_HAS_NO_TEST,
			equalTo(0));
	}

	@Test
	@OperateOnDeployment("default")
	public void testDirectContainerConformance(@ArquillianResource final URL contextURL) throws Exception {
		LOGGER.info("Running W3C official LDP Direct Container Test Suite against '{}' server",contextURL);
		final String reportPath = targetWorkingDir().getAbsolutePath();

		final Map<String, String> options = new HashMap<>();
		options.put("server", LDPConformanceITest.resolve(contextURL,"ldp4j/api/resource/direct_container/"));
		options.put("direct", null);
		options.put("cont-res",LDPConformanceITest.resolve(contextURL, "ldp4j/api/resource/"));
		options.put("read-only-prop","http://www.example.org/vocab#creationDate");
		options.put("httpLogging", null);
		options.put("skipLogging", null);
		options.put("listeners",SuiteResultCollector.class.getCanonicalName());
		options.put("output", reportPath);

		LOGGER.debug("You can find the LDP Direct Container Test Suite outputs at {}", reportPath);

		final LdpTestSuite testSuite = new LdpTestSuite(options);
		testSuite.run();

		final SuiteResults results = SuiteResultCollector.lastResults();
		LOGGER.info("LDP Direct Container Test Suite execution completed:", results);

		assertThat(results.numberOfTests(Result.values()), equalTo(97));
		assertThat(results.numberOfTests(Result.PASSED), equalTo(79));
		assertThat(results.numberOfTests(Result.FAILED), equalTo(2));
		assertThat(results.hasTestResult("testPatchMethod", Result.FAILED),equalTo(true));
		assertThat(results.hasTestResult("testPutToCreate", Result.FAILED),equalTo(true));
		assertThat(results.numberOfTests(Result.SKIPPED), equalTo(16));

		assertThat(
			"LDP Direct Container Test Suite finished without errors",
			testSuite.getStatus() & TESTNG_STATUS_HAS_FAILURE,
			equalTo(TESTNG_STATUS_HAS_FAILURE));
		assertThat(
			"LDP Direct Container Test Suite finished without skipped tests",
			testSuite.getStatus() & TESTNG_STATUS_HAS_SKIPPED,
			equalTo(TESTNG_STATUS_HAS_SKIPPED));
		assertThat(
			"LDP Direct Container Test Suite is empty - no test run",
			testSuite.getStatus() & TESTNG_STATUS_HAS_NO_TEST,
			equalTo(0));
	}

	@Test
	@OperateOnDeployment("default")
	public void testIndirectContainerConformance(@ArquillianResource final URL contextURL) throws Exception {
		LOGGER.info("Running W3C official LDP Indirect Container Test Suite against '{}' server",contextURL);
		final String reportPath = targetWorkingDir().getAbsolutePath();

		final Map<String, String> options = new HashMap<>();
		options.put("server", LDPConformanceITest.resolve(contextURL,"ldp4j/api/resource/indirect_container/"));
		options.put("indirect", null);
		options.put("cont-res",LDPConformanceITest.resolve(contextURL, "ldp4j/api/resource/"));
		options.put("read-only-prop","http://www.example.org/vocab#creationDate");
		options.put("httpLogging", null);
		options.put("skipLogging", null);
		options.put("listeners",SuiteResultCollector.class.getCanonicalName());
		options.put("output", reportPath);

		LOGGER.debug("You can find the LDP Indirect Container Test Suite outputs at {}", reportPath);

		final LdpTestSuite testSuite = new LdpTestSuite(options);
		testSuite.run();

		final SuiteResults results = SuiteResultCollector.lastResults();
		LOGGER.info("LDP Indirect Container Test Suite execution completed:", results);

		assertThat(results.numberOfTests(Result.values()), equalTo(90));
		assertThat(results.numberOfTests(Result.PASSED), equalTo(73));
		assertThat(results.numberOfTests(Result.FAILED), equalTo(2));
		assertThat(results.hasTestResult("testPatchMethod", Result.FAILED),equalTo(true));
		assertThat(results.hasTestResult("testPutToCreate", Result.FAILED),equalTo(true));
		assertThat(results.numberOfTests(Result.SKIPPED), equalTo(15));

		assertThat(
			"LDP Indirect Container Test Suite finished without errors",
			testSuite.getStatus() & TESTNG_STATUS_HAS_FAILURE,
			equalTo(TESTNG_STATUS_HAS_FAILURE));
		assertThat(
			"LDP Indirect Container Test Suite finished without skipped tests",
			testSuite.getStatus() & TESTNG_STATUS_HAS_SKIPPED,
			equalTo(TESTNG_STATUS_HAS_SKIPPED));
		assertThat(
			"LDP Indirect Container Test Suite is empty - no test run",
			testSuite.getStatus() & TESTNG_STATUS_HAS_NO_TEST,
			equalTo(0));
	}

	private ByteArrayOutputStream buffer;

	@Before
	public void setUp() {
		this.buffer=new ByteArrayOutputStream();
		System.setErr(new PrintStream(this.buffer));
	}

	@After
	public void tearDown() throws IOException {
		final List<String> lines = IOUtils.readLines(new StringReader(this.buffer.toString()));
		boolean failure=false;
		int count=0;
		for(final String line:lines) {
			if(failure) {
				if(count<5) {
					count++;
				} else {
					failure=false;
				}
			} else if(line.startsWith("[FAILURE]")) {
				failure=true;
				count=0;
			}
			if(failure && !line.trim().isEmpty()) {
				LOGGER.info(line);
			}
		}
	}

	private File targetWorkingDir() {
		final String relPath = System.getProperty("conformance.reports.path");
		final File targetDir = new File(relPath);
		if (!targetDir.exists()) {
			Assume.assumeTrue("Could not create report-directory",targetDir.mkdirs());
		}
		return targetDir;
	}

	private static String resolve(final URL base, final String path) {
		return base.toString()+path;
	}

}