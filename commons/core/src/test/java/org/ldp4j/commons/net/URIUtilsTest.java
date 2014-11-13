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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ldp4j.net.Examples;
import org.ldp4j.net.Examples.Custom.Resolution.Variant;
import org.ldp4j.util.ListBuilder;
import org.ldp4j.util.MapBuilder;


public class URIUtilsTest {

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

	@Test(expected=NullPointerException.class)
	public void testRelativize$nullBase() {
		URIUtils.relativize(null, URI.create(BASE));
	}

	@Test(expected=NullPointerException.class)
	public void testRelativize$nullTarget() {
		URIUtils.relativize(URI.create(BASE), null);
	}

	@Test
	public void testRelativize$opaqueBase() {
		URI target = URI.create(BASE);
		URI opaque = URI.create("urn:opaque");
		assertThat(URIUtils.relativize(opaque,target),equalTo(target));
	}

	@Test
	public void uriBreakDown() {
		Map<String,String> cases=
			MapBuilder.
				<String,String>builder().
					add("Hierarchical only host and protocol default port","http://www.example.org").
					add("Hierarchical only host and port","http://www.example.org:8080").
					add("Hierarchical only user info, host and port","http://user:password@www.example.org:8080").
					add("Hierarchical authority with root directory path","http://www.example.org/").
					add("Hierarchical authority with root file path","http://www.example.org/file").
					add("Hierarchical authority with deep directory path","http://www.example.org/directory/subdirectory/").
					add("Hierarchical authority with deep file path","http://www.example.org/directory/subdirectory/file").
					add("Hierarchical no-authority with root directory path","urn:/").
					add("Hierarchical no-authority with root file path","urn:/file").
					add("Hierarchical no-authority with deep directory path","urn:/directory/subdirectory/").
					add("Hierarchical no-authority with deep file path","urn:/directory/subdirectory/file").
					add("Hierarchical relative only host and protocol default port","//www.example.org").
					add("Hierarchical relative only host and port","//www.example.org:8080").
					add("Hierarchical relative only user info, host and port","//user:password@www.example.org:8080").
					add("Hierarchical relative authority with root directory path","//www.example.org/").
					add("Hierarchical relative authority with root file path","//www.example.org/file").
					add("Hierarchical relative authority with deep directory path","//www.example.org/directory/subdirectory/").
					add("Hierarchical relative authority with deep file path","//www.example.org/directory/subdirectory/file").
					add("Hierarchical relative no-authority with root directory path","/").
					add("Hierarchical relative no-authority with root file path","/file").
					add("Hierarchical relative no-authority with root deep directory path","/directory/subdirectory/").
					add("Hierarchical relative no-authority with root deep file path","/directory/subdirectory/file").
					add("Hierarchical relative no-authority with file path","file").
					add("Hierarchical relative no-authority with deep directory path","directory/subdirectory/").
					add("Hierarchical relative no-authority with deep file path","directory/subdirectory/file").
					add("Null","").
					add("Opaque","urn:opaque:ssp").
					build();
		boolean[] flags = new boolean[]{false,true};
		int exampleCase=0;
		for(Entry<String,String> entry:cases.entrySet()) {
			for(boolean query:flags) {
				for(boolean fragment:flags) {
					show(++exampleCase,entry.getKey(),entry.getValue(), query, fragment);
				}
			}
		}
	}

	@Test
	public void relativization() {
		List<String> cases=
			ListBuilder.
				<String>builder().
					add("http://www.example.org").
					add("http://www.example.org/").
					add("http://www.example.org/file").
					add("http://www.example.org/directory/subdirectory/").
					add("http://www.example.org/directory/subdirectory/file").
					add("//www.example.org").
					add("//www.example.org/").
					add("//www.example.org/file").
					add("//www.example.org/directory/subdirectory/").
					add("//www.example.org/directory/subdirectory/file").
					add("").
					add("/").
					add("/file").
					add("/directory/subdirectory/").
					add("/directory/subdirectory/file").
					add("file").
					add("directory/subdirectory/").
					add("directory/subdirectory/file").
					add("urn:opaque:path").
					build();
		for(String rawBase:cases) {
			URI base=URI.create(rawBase);
			for(String rawTarget:cases) {
				URI target=URI.create(rawTarget);
				System.out.printf("<%s>.relativize(<%s>)=%n\t- JDK: <%s>%n\t- Custom: <%s>%n",base,target,base.relativize(target),URIUtils.relativize(base, target));
			}
		}
	}


	public void show(int i, String title, String rawURI, boolean query, boolean fragment) {
		URI uri = URI.create(extendURI(rawURI,query,fragment));
		System.out.printf("[%03d] %s (%s) {%n",i,extendTitle(title,query,fragment),uri);
		System.out.printf("\t- Scheme..............: %s%n",uri.getScheme());
		System.out.printf("\t- Scheme specific part: %s%n",uri.getSchemeSpecificPart());
		System.out.printf("\t  + Authority.........: %s%n",uri.getAuthority());
		System.out.printf("\t    * User info.......: %s%n",uri.getUserInfo());
		System.out.printf("\t    * Host............: %s%n",uri.getHost());
		System.out.printf("\t    * Port............: %s%n",uri.getPort());
		System.out.printf("\t  + Path..............: %s%n",uri.getPath());
		System.out.printf("\t  + Query.............: %s%n",uri.getQuery());
		System.out.printf("\t- Fragment............: %s%n",uri.getFragment());
		System.out.printf("\t- Flags...............: %s%n",flags(uri));
		System.out.printf("}%n");
	}
	private String flags(URI uri) {
		return
			String.format(
				"[%s][%s]",
				uri.isOpaque()?
					"O":
					"H",
				uri.isOpaque()?
					"-":
					uri.isAbsolute()?
						"A":
						"R"
			);
	}

	private String extendTitle(String title, boolean query, boolean fragment) {
		StringBuilder builder=new StringBuilder();
		builder.append(title);
		if(query) {
			builder.append(" with query");
		}
		if(fragment) {
			builder.append(" with fragment");
		}
		return builder.toString();
	}

	@Test
	public void testRelativize$opaqueTarget() {
		URI base = URI.create(BASE);
		URI opaque = URI.create("urn:opaque");
		assertThat(URIUtils.relativize(base,opaque),equalTo(opaque));
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
	public void testResolve$extraExamples() throws Exception {
		Map<String,String> examples=
			MapBuilder.
				<String,String>builder().
				add("http://a/b/./c/g"        ,  "http://a/b/c/g").
				add("http://a/b/../b/c/g"     ,  "http://a/b/c/g").
				add("http://a/b/./c/g/."      ,  "http://a/b/c/g/").
				add("http://a/b/../b/c/g/.."  ,  "http://a/b/c/").
				build();
		verifyResolutionScenarios("http://a/b/c/d;p?q",examples);
	}

	@Test
	public void testResolve$extraExamples$noAuthority() throws Exception {
		Map<String,String> examples=
			MapBuilder.
				<String,String>builder().
				add("urn:/a/b/./c/g"        ,  "urn:/a/b/c/g").
				add("urn:/a/b/../b/c/g"     ,  "urn:/a/b/c/g").
				add("urn:/a/b/./c/g/."      ,  "urn:/a/b/c/g/").
				add("urn:/a/b/../b/c/g/.."  ,  "urn:/a/b/c/").
				build();
		verifyResolutionScenarios("urn:/a/b/c/d;p?q",examples);
	}

	@Test
	public void testResolve$non_normalized_base() {
		verifyResolutionScenario("http://a/./b/c/d;p?q","g","http://a/b/c/g");
		verifyResolutionScenario("http://a/././b/c/d;p?q","g","http://a/b/c/g");
		verifyResolutionScenario("http://a/b/../c/d;p?q","g","http://a/c/g");
		verifyResolutionScenario("http://a/b/../c/../d;p?q","g","http://a/g");
	}

	@Test
	public void testResolve$non_normalized_base$noAuthority() {
		verifyResolutionScenario("urn:/a/./b/c/d;p?q","g","urn:/a/b/c/g");
		verifyResolutionScenario("urn:/a/././b/c/d;p?q","g","urn:/a/b/c/g");
		verifyResolutionScenario("urn:/a/b/../c/d;p?q","g","urn:/a/c/g");
		verifyResolutionScenario("urn:/a/b/../c/../d;p?q","g","urn:/a/g");
	}

	@Test
	public void testResolve$non_normalized_base$relative() {
		verifyResolutionScenario("/a/./b/c/d;p?q","g","/a/b/c/g");
		verifyResolutionScenario("/a/././b/c/d;p?q","g","/a/b/c/g");
		verifyResolutionScenario("/a/b/../c/d;p?q","g","/a/c/g");
		verifyResolutionScenario("/a/b/../c/../d;p?q","g","/a/g");
	}

	private static int roundtripScenarioCounter=0;

	private static int relativizationScenarioCounter=0;

	private static int resolutionScenarioCounter=0;

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

	private String extendURI(String base, boolean query, boolean fragment) {
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

		System.out.printf("Relativization scenario %d:%n",++relativizationScenarioCounter);
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
		String extendedTarget = extendURI(target,query,fragment);
		String extendedResolution = extendURI(resolution,query,fragment);
		URI baseURI =URI.create(base);
		URI targetURI = URI.create(extendedTarget);
		URI resolutionURI = URI.create(extendedResolution);
		URI relative = verifyRelativizationScenario(baseURI, targetURI);
		assertThat("Invalid relativization {"+baseURI+", "+targetURI+"}",relative,equalTo(resolutionURI));
		assertThat(baseURI.resolve(resolutionURI),equalTo(targetURI));
	}

	private void verifyResolutionScenarios(String rawBase, Map<String, String> scenarios) {
		for(Entry<String,String> entry:scenarios.entrySet()) {
			verifyResolutionScenario(rawBase, entry.getKey(), entry.getValue());
		}
	}

	private void verifyResolutionScenario(String rawBase, String rawTarget,
			String rawResolution) {
		URI base=URI.create(rawBase);
		URI relative = URI.create(rawTarget);
		URI jdkResolved=base.resolve(relative);
		URI customResolved = URIUtils.resolve(base,relative);
		System.out.printf("Resolution scenario %d:%n",++resolutionScenarioCounter);
		System.out.printf("\t- Base....: %s %n",base);
		System.out.printf("\t- Relative: <%s> %n",relative);
		System.out.printf("\t- Resolved%n");
		System.out.printf("\t  + JDK...: %s %n",jdkResolved);
		System.out.printf("\t  + Custom: %s %n",customResolved);
		assertThat(customResolved,notNullValue());
		assertThat(relative.toString(),customResolved,equalTo(URI.create(rawResolution)));
//			assertThat(jdkResolved,equalTo(URI.create(entry.getValue())));
	}

}
