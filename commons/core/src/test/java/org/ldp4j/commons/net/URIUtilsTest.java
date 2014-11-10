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
package org.ldp4j.commons.net;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class URIUtilsTest {

	private static class MapBuilder<K,V> {

		private LinkedHashMap<K, V> map;

		public MapBuilder(LinkedHashMap<K, V> map) {
			this.map = map;
		}
		private MapBuilder<K,V> add(K key, V value) {
			this.map.put(key, value);
			return this;
		}

		private Map<K,V> build() {
			return this.map;
		}

		private static <K,V> MapBuilder<K,V> builder() {
			return new MapBuilder<K,V>(new LinkedHashMap<K,V>());
		}

	}

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
	private URI existing;
	private URI custom;

	@Before
	public void setUp() throws Exception {
		existing = URI.create("http://www.example.org");
		custom = URI.create("net://example.org");
		ProtocolHandlerConfigurator.reset();
	}

	@Test
	public void testToURL$existing() throws Exception {
		assertThat(URIUtils.toURL(existing),notNullValue());
	}

	@Test(expected=MalformedURLException.class)
	public void testToURL$nonLoaded() throws Exception {
		org.junit.Assume.assumeFalse(ProtocolHandlerConfigurator.isSupported("net"));
		URIUtils.toURL(custom);
	}

	@Test
	public void testToURL$loaded() throws Exception {
		ProtocolHandlerConfigurator.addProvider(Handler.class);
		assertThat(URIUtils.toURL(custom),notNullValue());
	}

	@Test
	public void testRelativize$differentScheme() {
		URI base = URI.create(BASE);
		URI other = URI.create(BASE.replace("http","https"));
		assertThat(URIUtils.relativize(base, other),equalTo(other));
	}

	@Test
	public void testRelativize$differentAuthority() {
		URI base = URI.create(BASE);
		URI other = URI.create(BASE.replace(".org",".org:8080"));
		assertThat(URIUtils.relativize(base, other),equalTo(other));
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

	@Ignore("JDK Failure")
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

	@Ignore("JDK Failure")
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
		assertThat(URIUtils.relativize(base, base),equalTo(URI.create("")));
	}

	@Test
	public void testRelativize$child() {
		assertThat(URIUtils.relativize(URI.create(BASE), URI.create(BASE+"child")),equalTo(URI.create("child")));
	}

	@Test
	public void testRelativize$ancestor() {
		URI relativeAncestor = URI.create("../..");
		URI base = URI.create(BASE);
		assertThat(URIUtils.relativize(base, base.resolve(relativeAncestor)),equalTo(relativeAncestor));
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
	public void testResolve$normalExamples() throws Exception {
		Map<String,String> examples=
			MapBuilder.
				<String,String>builder().
				// We do not deal with opaques...
				// add("g:h"           ,  "g:h")
				add("g"             ,  "http://a/b/c/g").
				add("./g"           ,  "http://a/b/c/g").
				add("g/"            ,  "http://a/b/c/g/").
				add("/g"            ,  "http://a/g").
				add("//g"           ,  "http://g").
				add("?y"            ,  "http://a/b/c/d;p?y").
				add("g?y"           ,  "http://a/b/c/g?y").
				add("#s"            ,  "http://a/b/c/d;p?q#s").
				add("g#s"           ,  "http://a/b/c/g#s").
				add("g?y#s"         ,  "http://a/b/c/g?y#s").
				add(";x"            ,  "http://a/b/c/;x").
				add("g;x"           ,  "http://a/b/c/g;x").
				add("g;x?y#s"       ,  "http://a/b/c/g;x?y#s").
				add(""              ,  "http://a/b/c/d;p?q").
				add("."             ,  "http://a/b/c/").
				add("./"            ,  "http://a/b/c/").
				add(".."            ,  "http://a/b/").
				add("../"           ,  "http://a/b/").
				add("../g"          ,  "http://a/b/g").
				add("../.."         ,  "http://a/").
				add("../../"        ,  "http://a/").
				add("../../g"       ,  "http://a/g").
				build();
		verifyResolutionScenarios("http://a/b/c/d;p?q",examples);
	}

	@Test
	public void testResolve$abnormalExamples$extraAncestorSegments() throws Exception {
		Map<String,String> examples=
			MapBuilder.
				<String,String>builder().
				add("../../../g"     ,  "http://a/g").
				add("../../../../g"  ,  "http://a/g").
				build();
		verifyResolutionScenarios("http://a/b/c/d;p?q",examples);
	}

	@Test
	public void testResolve$abnormalExamples$removeDotSegmentsWhenNotPartOfComponent() throws Exception {
		Map<String,String> examples=
			MapBuilder.
				<String,String>builder().
				add("/./g"   ,  "http://a/g"      ).
				add("/../g"  ,  "http://a/g"      ).
				add("g."     ,  "http://a/b/c/g." ).
				add(".g"     ,  "http://a/b/c/.g" ).
				add("g.."    ,  "http://a/b/c/g..").
				add("..g"    ,  "http://a/b/c/..g").
				build();
		verifyResolutionScenarios("http://a/b/c/d;p?q",examples);
	}

	@Test
	public void testResolve$abnormalExamples$ignoreUnnecessaryDotSegments() throws Exception {
		Map<String,String> examples=
			MapBuilder.
				<String,String>builder().
				add("./../g"        ,  "http://a/b/g"        ).
				add("./g/."         ,  "http://a/b/c/g/"     ).
				add("g/./h"         ,  "http://a/b/c/g/h"    ).
				add("g/../h"        ,  "http://a/b/c/h"      ).
				add("g;x=1/./y"     ,  "http://a/b/c/g;x=1/y").
				add("g;x=1/../y"    ,  "http://a/b/c/y"      ).
				build();
		verifyResolutionScenarios("http://a/b/c/d;p?q",examples);
	}

	@Test
	public void testResolve$abnormalExamples$notIsolatedQueryAndOrFragment() throws Exception {
		Map<String,String> examples=
			MapBuilder.
				<String,String>builder().
				add("g?y/./x"  , "http://a/b/c/g?y/./x" ).
				add("g?y/../x" , "http://a/b/c/g?y/../x").
				add("g#s/./x"  , "http://a/b/c/g#s/./x" ).
				add("g#s/../x" , "http://a/b/c/g#s/../x").
				build();
		verifyResolutionScenarios("http://a/b/c/d;p?q",examples);
	}

	private static int roundtripScenarioCounter=0;

	private static int relativizationScenarioCounter=0;

	@SuppressWarnings("unused")
	private void verifyRoundtripScenario(String base, String target) {
		verifyRoundtripScenario(URI.create(base), URI.create(target));
	}

	private void verifyRoundtripScenario(URI uBase, URI uTarget) {
		URI customRelative = URIUtils.relativize(uBase, uTarget);
		assertThat(customRelative,notNullValue());
		URI customAbsolute = URIUtils.resolve(uBase, customRelative);
		System.out.printf("Scenario %d:%n",++roundtripScenarioCounter);
		System.out.printf("\t- Base...........: %s%n",uBase);
		System.out.printf("\t- Target.........: %s%n",uTarget);
		System.out.printf("\t- Relativization%n");
		URI jdkRelative = uBase.relativize(uTarget);
		URI jdkAbsolute = uBase.resolve(jdkRelative);
		System.out.printf(
			"\t  + JDK%2$s: <%1$s>%n",
			jdkRelative,
			jdkAbsolute.equals(uTarget)?
				"..........":
				" [INVALID]");
		System.out.printf("\t  + Custom.......: <%s>%n",customRelative);
		System.out.printf("\t- Roundtrip%n");
		System.out.printf(
				"\t  + JDK%2$s: %1$s%n",
				uBase.resolve(customRelative),
				uBase.resolve(customRelative).equals(uTarget)?
					"..........":
					" [INVALID]");
		System.out.printf("\t  + Custom.......: %s%n",customAbsolute);
		assertThat(customAbsolute,notNullValue());
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

	private String extend(String base, boolean query, boolean fragment) {
		StringBuilder builder=new StringBuilder();
		builder.append(base);
		if(query) {
			builder.append("?query=value");
		}
		if(fragment) {
			builder.append("#fragment");
		}
		return builder.toString();
	}

	private URI verifyRelativizationScenario(URI base, URI target) {
		URI jdkRelative = base.relativize(target);
		URI customRelative = URIUtils.relativize(base, target);
		assertThat(customRelative,notNullValue());

		URI jdkResolution = base.resolve(jdkRelative);

		System.out.printf("Scenario %d:%n",++relativizationScenarioCounter);
		System.out.printf("\t- Base...........: %s%n",base);
		System.out.printf("\t- Target.........: %s%n",target);
		System.out.printf("\t- Relativization%n");
		System.out.printf(
			"\t  + JDK%2$s: <%1$s>%n",
			jdkRelative,
			jdkResolution.equals(target)?
				"..........":
				" [INVALID]");
		System.out.printf("\t  + Custom.......: <%s>%n",customRelative);

		assertThat(String.format("<%s> --> <%s> : <%s>",base,target,customRelative),base.resolve(customRelative),equalTo(target));
		return customRelative;
	}

	private void verifyConfigurableRelativizationScenario(String base, String target, String resolution, boolean query, boolean fragment) {
		String extendedTarget = extend(target,query,fragment);
		String extendedResolution = extend(resolution,query,fragment);
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
			URI jdkResolved=base.resolve(relative);
			URI customResolved = URIUtils.resolve(base,relative);
			System.out.printf("Scenario %d:%n",++i);
			System.out.printf("\t- Base....: %s %n",base);
			System.out.printf("\t- Relative: <%s> %n",relative);
			System.out.printf("\t- Resolved%n");
			System.out.printf("\t  + JDK...: %s %n",jdkResolved);
			System.out.printf("\t  + Custom: %s %n",customResolved);
			assertThat(customResolved,notNullValue());
			assertThat(relative.toString(),customResolved,equalTo(URI.create(entry.getValue())));
//			assertThat(jdkResolved,equalTo(URI.create(entry.getValue())));
		}
	}

}
