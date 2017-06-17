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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.2.2
 *   Bundle      : ldp4j-commons-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.net;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.commons.net.ProtocolHandlerConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URIRelativizationTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(URIRelativizationTest.class);

	private static final String OTHER_FRAGMENT = "other";
	private static final String OTHER_QUERY = "otherParam=otherValue";
	private static final String QUERY = "param=value";
	private static final String AUTHORITY = "http://www.example.org";
	private static final String ROOT_DIR = "/";
	private static final String FILE = "file";
	private static final String FRAGMENT = "fragment";
	private static final String EMPTY_STRING = "";
	private static final String DIR = "/directory/subdirectory/";

	private static final String BASE = "http://www.megatwork.org/smart-aggregator/entity/component/";

	private static int relativizationScenarioCounter=0;

	private URI testCase(String authority, String dir, String file, String query, String fragment) {
		String suffix = "";
		if(query!=null) {
			suffix+="?"+query;
		}
		if(fragment!=null) {
			suffix+="#"+fragment;
		}
		return URI.create(authority+dir+file+suffix);
	}

	private String decorateURI(String baseURI, boolean withQuery, boolean withFragment) {
		StringBuilder builder=new StringBuilder();
		builder.append(baseURI);
		if(withQuery) {
			builder.append("?query=value");
		}
		if(withFragment) {
			builder.append("#fragment");
		}
		return builder.toString();
	}

	private URI verifyRelativizationScenario(URI base, URI target) {
		URI customRelative = base.relativize(target);
		assertThat(customRelative,notNullValue());

		LOGGER.debug("Scenario {}:",++relativizationScenarioCounter);
		LOGGER.debug("  - Base...........: {}",base);
		LOGGER.debug("  - Target.........: {}",target);
		LOGGER.debug("  - Relativization");
		LOGGER.debug("    + Custom.......: <{}>",customRelative);

		assertThat(String.format("<%s> --> <%s> : <%s>",base,target,customRelative),base.resolve(customRelative),equalTo(target));
		return customRelative;
	}

	private void verifyConfigurableRelativizationScenario(String base, String target, String resolution, boolean query, boolean fragment) {
		String extendedTarget = decorateURI(target,query,fragment);
		String extendedResolution = decorateURI(resolution,query,fragment);
		URI baseURI =URI.create(base);
		URI targetURI = URI.create(extendedTarget);
		URI resolutionURI = URI.create(extendedResolution);
		URI relative = verifyRelativizationScenario(baseURI, targetURI);
		assertThat("Invalid relativization {"+baseURI+", "+targetURI+"}",relative,equalTo(resolutionURI));
		assertThat(baseURI.resolve(resolutionURI),equalTo(targetURI));
	}

	@Before
	public void setUp() throws Exception {
		ProtocolHandlerConfigurator.reset();
	}

	@Test
	public void testRelativize$differentScheme() {
		URI base = URI.create(BASE);
		URI other = URI.create(BASE.replace("http","https"));
		assertThat(base.relativize(other),equalTo(other));
	}

	@Test
	public void testRelativize$differentAuthority() {
		URI base = URI.create(BASE);
		URI other = URI.create(BASE.replace(".org",".org:8080"));
		assertThat(base.relativize(other),equalTo(other));
	}

	@Test
	public void testRelativize$authority$fragmentChange() {
		URI base   = testCase(AUTHORITY,EMPTY_STRING,EMPTY_STRING,null,FRAGMENT);
		URI target = testCase(AUTHORITY,EMPTY_STRING,EMPTY_STRING,null,OTHER_FRAGMENT);
		verifyRelativizationScenario(base, target);
	}

	@Test
	public void testRelativize$rootDir$fragmentChange() {
		URI base   = testCase(AUTHORITY,ROOT_DIR,EMPTY_STRING,null,FRAGMENT);
		URI target = testCase(AUTHORITY,ROOT_DIR,EMPTY_STRING,null,OTHER_FRAGMENT);
		verifyRelativizationScenario(base, target);
	}

	@Test
	public void testRelativize$dir$fragmentChange() {
		URI base   = testCase(AUTHORITY,DIR,EMPTY_STRING,null,FRAGMENT);
		URI target = testCase(AUTHORITY,DIR,EMPTY_STRING,null,OTHER_FRAGMENT);
		verifyRelativizationScenario(base, target);
	}

	@Test
	public void testRelativize$rootFile$fragmentChange() {
		URI base   = testCase(AUTHORITY,ROOT_DIR,FILE,null,FRAGMENT);
		URI target = testCase(AUTHORITY,ROOT_DIR,FILE,null,OTHER_FRAGMENT);
		verifyRelativizationScenario(base, target);
	}

	@Test
	public void testRelativize$file$fragmentChange() {
		URI base   = testCase(AUTHORITY,DIR,FILE,null,FRAGMENT);
		URI target = testCase(AUTHORITY,DIR,FILE,null,OTHER_FRAGMENT);
		verifyRelativizationScenario(base, target);
	}

	@Test
	public void testRelativize$authority$queryChange() {
		URI base   = testCase(AUTHORITY,EMPTY_STRING,EMPTY_STRING,QUERY,null);
		URI target = testCase(AUTHORITY,EMPTY_STRING,EMPTY_STRING,OTHER_QUERY,null);
		verifyRelativizationScenario(base, target);
	}

	@Test
	public void testRelativize$rootDir$queryChange() {
		URI base   = testCase(AUTHORITY,ROOT_DIR,EMPTY_STRING,QUERY,null);
		URI target = testCase(AUTHORITY,ROOT_DIR,EMPTY_STRING,OTHER_QUERY,null);
		verifyRelativizationScenario(base, target);
	}

	@Test
	public void testRelativize$dir$queryChange() {
		URI base   = testCase(AUTHORITY,DIR,EMPTY_STRING,QUERY,null);
		URI target = testCase(AUTHORITY,DIR,EMPTY_STRING,OTHER_QUERY,null);
		verifyRelativizationScenario(base, target);
	}

	@Test
	public void testRelativize$rootFile$queryChange() {
		URI base   = testCase(AUTHORITY,ROOT_DIR,FILE,QUERY,null);
		URI target = testCase(AUTHORITY,ROOT_DIR,FILE,OTHER_QUERY,null);
		verifyRelativizationScenario(base, target);
	}

	@Test
	public void testRelativize$file$queryChange() {
		URI base   = testCase(AUTHORITY,DIR,FILE,QUERY,null);
		URI target = testCase(AUTHORITY,DIR,FILE,OTHER_QUERY,null);
		verifyRelativizationScenario(base, target);
	}

	@Test
	public void testRelativize$authorityToRootDir() {
		String base = "http://www.megatwork.org";
		String target = "http://www.megatwork.org/";
		String relative = "/";
		assertThat(URI.create(base).resolve(URI.create(relative)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, relative, false, false);
		verifyConfigurableRelativizationScenario(base, target, relative, false, true);
		verifyConfigurableRelativizationScenario(base, target, relative, true, false);
		verifyConfigurableRelativizationScenario(base, target, relative, true, true);
	}

	@Test
	public void testRelativize$authorityToRootDir$fragment() {
		String base = "http://www.megatwork.org#fragment";
		String target = "http://www.megatwork.org/";
		String relative = "/";
		assertThat(URI.create(base).resolve(URI.create(relative)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, relative, false, false);
		verifyConfigurableRelativizationScenario(base, target, relative, false, true);
		verifyConfigurableRelativizationScenario(base, target, relative, true, false);
		verifyConfigurableRelativizationScenario(base, target, relative, true, true);
	}

	@Test
	public void testRelativize$authorityToRootDir$query() {
		String base = "http://www.megatwork.org?param=value";
		String target = "http://www.megatwork.org/";
		String relative = "/";
		assertThat(URI.create(base).resolve(URI.create(relative)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, relative, false, false);
		verifyConfigurableRelativizationScenario(base, target, relative, false, true);
		verifyConfigurableRelativizationScenario(base, target, relative, true, false);
		verifyConfigurableRelativizationScenario(base, target, relative, true, true);
	}

	@Test
	public void testRelativize$authorityToRootDir$composite() {
		String base = "http://www.megatwork.org?param=value#fragment";
		String target = "http://www.megatwork.org/";
		String relative = "/";
		assertThat(URI.create(base).resolve(URI.create(relative)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, relative, false, false);
		verifyConfigurableRelativizationScenario(base, target, relative, false, true);
		verifyConfigurableRelativizationScenario(base, target, relative, true, false);
		verifyConfigurableRelativizationScenario(base, target, relative, true, true);
	}

	@Test
	public void testRelativize$rootDirToAuthority() {
		String base = "http://www.megatwork.org/";
		String target = "http://www.megatwork.org";
		String relative = "http://www.megatwork.org";
		assertThat(URI.create(base).resolve(URI.create(relative)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, relative, false, false);
		verifyConfigurableRelativizationScenario(base, target, relative, false, true);
		verifyConfigurableRelativizationScenario(base, target, relative, true, false);
		verifyConfigurableRelativizationScenario(base, target, relative, true, true);
	}

	@Test
	public void testRelativize$rootDirToAuthority$fragment() {
		String base = "http://www.megatwork.org/#fragment";
		String target = "http://www.megatwork.org";
		String relative = "http://www.megatwork.org";
		assertThat(URI.create(base).resolve(URI.create(relative)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, relative, false, false);
		verifyConfigurableRelativizationScenario(base, target, relative, false, true);
		verifyConfigurableRelativizationScenario(base, target, relative, true, false);
		verifyConfigurableRelativizationScenario(base, target, relative, true, true);
	}

	@Test
	public void testRelativize$rootDirToAuthority$query() {
		String base = "http://www.megatwork.org/?param=value";
		String target = "http://www.megatwork.org";
		String relative = "http://www.megatwork.org";
		assertThat(URI.create(base).resolve(URI.create(relative)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, relative, false, false);
		verifyConfigurableRelativizationScenario(base, target, relative, false, true);
		verifyConfigurableRelativizationScenario(base, target, relative, true, false);
		verifyConfigurableRelativizationScenario(base, target, relative, true, true);
	}

	@Test
	public void testRelativize$rootDirToAuthority$composite() {
		String base = "http://www.megatwork.org/?param=value#fragment";
		String target = "http://www.megatwork.org";
		String relative = "http://www.megatwork.org";
		assertThat(URI.create(base).resolve(URI.create(relative)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, relative, false, false);
		verifyConfigurableRelativizationScenario(base, target, relative, false, true);
		verifyConfigurableRelativizationScenario(base, target, relative, true, false);
		verifyConfigurableRelativizationScenario(base, target, relative, true, true);
	}

	@Test
	public void testRelativize$same() {
		URI base = URI.create(BASE);
		assertThat(base.relativize(base),equalTo(URI.create("")));
	}

	@Test
	public void testRelativize$child() {
		assertThat(URI.create(BASE).relativize(URI.create(BASE+"child")),equalTo(URI.create("child")));
	}

	@Test
	public void testRelativize$ancestor() {
		URI relativeAncestor = URI.create("../..");
		URI base = URI.create(BASE);
		assertThat(base.relativize(base.resolve(relativeAncestor)),equalTo(relativeAncestor));
	}

	@Test
	public void testRelativize$baseAsFile() {
		String base = BASE.substring(0,BASE.length()-1);
		verifyConfigurableRelativizationScenario(BASE, base, "../component", false, false);
	}

	@Test
		public void testRelativize$useCase() {
			URI base   = URI.create("http://www.megatwork.org/smart-aggregator/entity/component/file");
			URI target = URI.create("http://www.megatwork.org/app/ldp4j/api/file?param#test");
			verifyRelativizationScenario(base, target);
		}

	@Test
	public void testRelativize$siblings$baseBigger$dirToDir() {
		String base = "http://www.megatwork.org/smart-aggregator/common/segment1/segment2/base/";
		String target = "http://www.megatwork.org/smart-aggregator/common/segment3/target/";
		String resolution = "../../../segment3/target/";
		verifyConfigurableRelativizationScenario(base, target, resolution, false, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, false, true);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, true);
	}

	@Test
	public void testRelativize$siblings$baseBigger$dirToFile() {
		String base = "http://www.megatwork.org/smart-aggregator/common/segment1/segment2/base/";
		String target = "http://www.megatwork.org/smart-aggregator/common/segment3/target";
		String relative = "../../../segment3/target";
		verifyConfigurableRelativizationScenario(base, target, relative, false, false);
		verifyConfigurableRelativizationScenario(base, target, relative, false, true);
		verifyConfigurableRelativizationScenario(base, target, relative, true, false);
		verifyConfigurableRelativizationScenario(base, target, relative, true, true);
	}

	@Test
	public void testRelativize$siblings$baseBigger$fileToDir() {
		String base = "http://www.megatwork.org/smart-aggregator/common/segment1/segment2/base";
		String target = "http://www.megatwork.org/smart-aggregator/common/segment3/target/";
		String relative = "../../segment3/target/";
		verifyConfigurableRelativizationScenario(base, target, relative, false, false);
		verifyConfigurableRelativizationScenario(base, target, relative, false, true);
		verifyConfigurableRelativizationScenario(base, target, relative, true, false);
		verifyConfigurableRelativizationScenario(base, target, relative, true, true);
	}

	@Test
	public void testRelativize$siblings$baseBigger$fileToFile() {
		String base = "http://www.megatwork.org/smart-aggregator/common/segment1/segment2/base";
		String target = "http://www.megatwork.org/smart-aggregator/common/segment3/target";
		String relative = "../../segment3/target";
		verifyConfigurableRelativizationScenario(base, target, relative, false, false);
		verifyConfigurableRelativizationScenario(base, target, relative, false, true);
		verifyConfigurableRelativizationScenario(base, target, relative, true, false);
		verifyConfigurableRelativizationScenario(base, target, relative, true, true);
	}

	@Test
	public void testRelativize$siblings$targetBigger$dirToDir() {
		String base = "http://www.megatwork.org/smart-aggregator/common/segment1/base/";
		String target = "http://www.megatwork.org/smart-aggregator/common/segment2/segment3/target/";
		String relative = "../../segment2/segment3/target/";
		assertThat(URI.create(base).resolve(URI.create(relative)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, relative, false, false);
		verifyConfigurableRelativizationScenario(base, target, relative, false, true);
		verifyConfigurableRelativizationScenario(base, target, relative, true, false);
		verifyConfigurableRelativizationScenario(base, target, relative, true, true);
	}

	@Test
	public void testRelativize$siblings$targetBigger$dirToFile() {
		String base = "http://www.megatwork.org/smart-aggregator/common/segment1/base/";
		String target = "http://www.megatwork.org/smart-aggregator/common/segment2/segment3/target";
		String resolution = "../../segment2/segment3/target";
		assertThat(URI.create(base).resolve(URI.create(resolution)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, resolution, false, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, false, true);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, true);
	}

	@Test
	public void testRelativize$siblings$targetBigger$fileToDir() {
		String base = "http://www.megatwork.org/smart-aggregator/common/segment1/base";
		String target = "http://www.megatwork.org/smart-aggregator/common/segment2/segment3/target/";
		String resolution = "../segment2/segment3/target/";
		assertThat(URI.create(base).resolve(URI.create(resolution)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, resolution, false, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, false, true);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, true);
	}

	@Test
	public void testRelativize$siblings$targetBigger$fileToFile() {
		String base = "http://www.megatwork.org/smart-aggregator/common/segment1/base";
		String target = "http://www.megatwork.org/smart-aggregator/common/segment2/segment3/target";
		String resolution = "../segment2/segment3/target";
		assertThat(URI.create(base).resolve(URI.create(resolution)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, resolution, false, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, false, true);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, true);
	}

	@Test
	public void testRelativize$baseAuthority$toDeepDir() {
		String base = "http://www.megatwork.org";
		String target = "http://www.megatwork.org/smart-aggregator/common/segment2/segment3/target/";
		String resolution = "/smart-aggregator/common/segment2/segment3/target/";
		assertThat(URI.create(base).resolve(URI.create(resolution)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, resolution, false, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, false, true);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, true);
	}

	@Test
	public void testRelativize$baseAuthority$toDeepFile() {
		String base = "http://www.megatwork.org";
		String target = "http://www.megatwork.org/smart-aggregator/common/segment2/segment3/target";
		String resolution = "/smart-aggregator/common/segment2/segment3/target";
		assertThat(URI.create(base).resolve(URI.create(resolution)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, resolution, false, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, false, true);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, true);
	}

	@Test
	public void testRelativize$baseRootDir$toDeepDir() {
		String base = "http://www.megatwork.org/";
		String target = "http://www.megatwork.org/smart-aggregator/common/segment2/segment3/target/";
		String resolution = "smart-aggregator/common/segment2/segment3/target/";
		assertThat(URI.create(base).resolve(URI.create(resolution)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, resolution, false, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, false, true);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, true);
	}

	@Test
	public void testRelativize$baseRootDir$toDeepFile() {
		String base = "http://www.megatwork.org/";
		String target = "http://www.megatwork.org/smart-aggregator/common/segment2/segment3/target";
		String resolution = "smart-aggregator/common/segment2/segment3/target";
		assertThat(URI.create(base).resolve(URI.create(resolution)),equalTo(URI.create(target)));
		verifyConfigurableRelativizationScenario(base, target, resolution, false, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, false, true);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, false);
		verifyConfigurableRelativizationScenario(base, target, resolution, true, true);
	}

	@Test
	public void testRelativize$customCases() {
		for(String rawBase:Examples.Custom.uris()) {
			URI base=URI.create(rawBase);
			for(String rawTarget:Examples.Custom.uris()) {
				URI target=URI.create(rawTarget);
				LOGGER.debug("<{}>.relativize(<{}>)=<{}>",base,target,base.relativize(target));
			}
		}
	}

}