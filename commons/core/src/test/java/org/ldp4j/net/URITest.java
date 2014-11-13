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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.net;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.commons.net.Handler;
import org.ldp4j.commons.net.ProtocolHandlerConfigurator;
import org.ldp4j.net.Examples.Custom.Resolution.Variant;
import org.ldp4j.net.URI.Path;

// TODO: Add normalization tests
// TODO: Add getter tests
// TODO: Add builder tests
public class URITest {

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
	private static int roundtripScenarioCounter=0;
	private static int relativizationScenarioCounter=0;

	private URI existing;
	private URI custom;

	@SuppressWarnings("unused")
	private void verifyRoundtripScenario(String base, String target) {
		verifyRoundtripScenario(URI.create(base), URI.create(target));
	}

	private void verifyRoundtripScenario(URI uBase, URI uTarget) {
		URI customRelative = uBase.relativize(uTarget);
		assertThat(customRelative,notNullValue());
		URI customAbsolute = uBase.resolve(customRelative);
		assertThat(customAbsolute,notNullValue());
		System.out.printf("Scenario %d:%n",++roundtripScenarioCounter);
		System.out.printf("\t- Base...........: %s%n",uBase);
		System.out.printf("\t- Target.........: %s%n",uTarget);
		System.out.printf("\t- Relativization%n");
		System.out.printf("\t  + Custom.......: <%s>%n",customRelative);
		System.out.printf("\t- Roundtrip%n");
		System.out.printf("\t  + Custom.......: %s%n",customAbsolute);
		assertThat(customAbsolute,equalTo(uTarget));
	}

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

		System.out.printf("Scenario %d:%n",++relativizationScenarioCounter);
		System.out.printf("\t- Base...........: %s%n",base);
		System.out.printf("\t- Target.........: %s%n",target);
		System.out.printf("\t- Relativization%n");
		System.out.printf("\t  + Custom.......: <%s>%n",customRelative);

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

	private void verifyResolutionScenarios(String rawBase, Map<String, String> scenarios) {
		URI base=URI.create(rawBase);
		int i=0;
		for(Entry<String,String> entry:scenarios.entrySet()) {
			URI relative = URI.create(entry.getKey());
			URI customResolved = base.resolve(relative);
			System.out.printf("Scenario %d:%n",++i);
			System.out.printf("\t- Base......: %s%n",base);
			System.out.printf("\t- Relative..: <%s>%n",relative);
			System.out.printf("\t- Resolved: %n");
			System.out.printf("\t  + Expected: %s%n", entry.getValue());
			System.out.printf("\t  + Result..: %s%n",customResolved);
			assertThat(customResolved,notNullValue());
			assertThat(relative.toString(),customResolved,equalTo(URI.create(entry.getValue())));
		}
	}

	private void show(int i, String title, String rawURI, boolean query, boolean fragment) {
		URI uri = URI.create(decorateURI(rawURI,query,fragment));
		System.out.printf("[%03d] %s%n",i,uri.prettyPrint());
	}

	@Before
	public void setUp() throws Exception {
		existing = URI.create("http://www.example.org");
		custom = URI.create("net://example.org");
		ProtocolHandlerConfigurator.reset();
	}

	@Test
	public void testToURL$existing() throws Exception {
		assertThat(existing.toURL(),notNullValue());
	}

	@Test(expected=MalformedURLException.class)
	public void testToURL$nonLoaded() throws Exception {
		org.junit.Assume.assumeFalse(ProtocolHandlerConfigurator.isSupported("net"));
		custom.toURL();
	}

	@Test
	public void testToURL$loaded() throws Exception {
		ProtocolHandlerConfigurator.addProvider(Handler.class);
		assertThat(custom.toURL(),notNullValue());
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
	public void testRoundtrip$rootFile$queryChange() {
		URI base   = testCase(AUTHORITY,ROOT_DIR,FILE,QUERY,null);
		URI target = testCase(AUTHORITY,ROOT_DIR,FILE,OTHER_QUERY,null);
		verifyRoundtripScenario(base, target);
	}

	@Test
	public void testRelativize$file$queryChange() {
		URI base   = testCase(AUTHORITY,DIR,FILE,QUERY,null);
		URI target = testCase(AUTHORITY,DIR,FILE,OTHER_QUERY,null);
		verifyRelativizationScenario(base, target);
	}

	@Test
	public void testRoundtrip$file$queryChange() {
		URI base   = testCase(AUTHORITY,DIR,FILE,QUERY,null);
		URI target = testCase(AUTHORITY,DIR,FILE,OTHER_QUERY,null);
		verifyRoundtripScenario(base, target);
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
				System.out.printf("<%s>.relativize(<%s>)=<%s>%n",base,target,base.relativize(target));
			}
		}
	}

	@Test
	public void testResolve$normalExamples() throws Exception {
		verifyResolutionScenarios(
			Examples.
				Normative.
					Resolution.base(),
			Examples.
				Normative.
					Resolution.
						regular());
	}

	@Test
	public void testResolve$abnormalExamples$extraAncestorSegments() throws Exception {
		verifyResolutionScenarios(
			Examples.
				Normative.
					Resolution.base(),
			Examples.
				Normative.
					Resolution.
						outOfScope());
	}

	@Test
	public void testResolve$abnormalExamples$removeDotSegmentsWhenNotPartOfComponent() throws Exception {
		verifyResolutionScenarios(
			Examples.
				Normative.
					Resolution.base(),
			Examples.
				Normative.
					Resolution.
						dotSegmentAsPartOfComponent());
	}

	@Test
	public void testResolve$abnormalExamples$ignoreUnnecessaryDotSegments() throws Exception {
		verifyResolutionScenarios(
			Examples.
				Normative.
					Resolution.base(),
			Examples.
				Normative.
					Resolution.
						unnecessaryDotSegments());
	}

	@Test
	public void testResolve$abnormalExamples$notIsolatedQueryAndOrFragment() throws Exception {
		verifyResolutionScenarios(
			Examples.
				Normative.
					Resolution.base(),
			Examples.
				Normative.
					Resolution.
						notIsolatedPath());
	}

	@Test
	public void testResolve$normalExamples$noAuthority() throws Exception {
		verifyResolutionScenarios(
			Examples.Custom.Resolution.base(Variant.NO_AUTHORITY),
			Examples.Custom.Resolution.scenarios(Variant.NO_AUTHORITY));
	}

	@Test
	public void testResolve$normalExamples$relativeBase() throws Exception {
		verifyResolutionScenarios(
			Examples.Custom.Resolution.base(Variant.RELATIVE),
			Examples.Custom.Resolution.scenarios(Variant.RELATIVE));
	}

	@Test
	public void testPathNormalize() {
		for(String scenario:Examples.Custom.paths()) {
			Path path=Path.create(scenario);
			System.out.println(path+" : "+path.normalize());
		}
	}

	@Test
	public void uriBreakDown() {
		boolean[] flags = new boolean[]{false,true};
		int exampleCase=0;
		for(String rawURI:Examples.Custom.uris()) {
			for(boolean query:flags) {
				for(boolean fragment:flags) {
					show(++exampleCase,"URI",rawURI, query, fragment);
				}
			}
		}
	}

}