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
package org.ldp4j.commons.net;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.net.URI;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URIDescriptorTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(URIDescriptorTest.class);

	private static final String QUERY = "param=value";
	private static final String AUTHORITY = "http://www.example.org";
	private static final String ROOT_DIR = "/";
	private static final String FILE = "file";
	private static final String FRAGMENT = "fragment";
	private static final String EMPTY_STRING = "";
	private static final String DIR = "/directory/subdirectory/";

	private URI uri(String rawURI) {
		return URI.create(rawURI);
	}

	@Test(expected=NullPointerException.class)
	public void testCreate$null() throws Exception {
		URIDescriptor.create(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreate$opaque() throws Exception {
		URIDescriptor.create(uri("urn:opaque:uri"));
	}

	@Test
	public void testCreate$relative() throws Exception {
		testCase(null,DIR,EMPTY_STRING,null,null);
	}

	@Test
	public void testCreate$onlyAuthority() throws Exception {
		testCase(AUTHORITY, EMPTY_STRING, EMPTY_STRING, null, null);
	}

	@Test
	public void testCreate$onlyAuthority$fragment() throws Exception {
		testCase(AUTHORITY, EMPTY_STRING, EMPTY_STRING, null, FRAGMENT);
	}

	@Test
	public void testCreate$onlyAuthority$query() throws Exception {
		testCase(AUTHORITY, EMPTY_STRING, EMPTY_STRING, QUERY, null);
	}

	@Test
	public void testCreate$onlyAuthority$composite() throws Exception {
		testCase(AUTHORITY, EMPTY_STRING, EMPTY_STRING, QUERY, FRAGMENT);
	}

	@Test
	public void testCreate$rootDirectory() throws Exception {
		testCase(AUTHORITY, ROOT_DIR, EMPTY_STRING, null, null);
	}

	@Test
	public void testCreate$rootDirectory$fragment() throws Exception {
		testCase(AUTHORITY, ROOT_DIR, EMPTY_STRING, null, FRAGMENT);
	}

	@Test
	public void testCreate$rootDirectory$query() throws Exception {
		testCase(AUTHORITY, ROOT_DIR, EMPTY_STRING, QUERY, null);
	}

	@Test
	public void testCreate$rootDirectory$composite() throws Exception {
		testCase(AUTHORITY, ROOT_DIR, EMPTY_STRING, QUERY, FRAGMENT);
	}

	@Test
	public void testCreate$rootFile() throws Exception {
		testCase(AUTHORITY, ROOT_DIR, FILE, null, null);
	}

	@Test
	public void testCreate$rootFile$fragment() throws Exception {
		testCase(AUTHORITY, ROOT_DIR, FILE, null, FRAGMENT);
	}

	@Test
	public void testCreate$rootFile$query() throws Exception {
		testCase(AUTHORITY, ROOT_DIR, FILE, QUERY, null);
	}

	@Test
	public void testCreate$rootFile$composite() throws Exception {
		testCase(AUTHORITY, ROOT_DIR, FILE, QUERY, FRAGMENT);
	}

	@Test
	public void testCreate$directory() throws Exception {
		testCase(AUTHORITY, DIR, EMPTY_STRING, null, null);
	}

	@Test
	public void testCreate$directory$fragment() throws Exception {
		testCase(AUTHORITY, DIR, EMPTY_STRING, null, FRAGMENT);
	}

	@Test
	public void testCreate$directory$query() throws Exception {
		testCase(AUTHORITY, DIR, EMPTY_STRING, QUERY, null);
	}

	@Test
	public void testCreate$directory$composite() throws Exception {
		testCase(AUTHORITY, DIR, EMPTY_STRING, QUERY, FRAGMENT);
	}

	@Test
	public void testCreate$file() throws Exception {
		testCase(AUTHORITY, DIR, FILE, null, null);
	}

	@Test
	public void testCreate$file$fragment() throws Exception {
		testCase(AUTHORITY, DIR, FILE, null, FRAGMENT);
	}

	@Test
	public void testCreate$file$query() throws Exception {
		testCase(AUTHORITY, DIR, FILE, QUERY, null);
	}

	@Test
	public void testCreate$file$composite() throws Exception {
		testCase(AUTHORITY, DIR, FILE, QUERY, FRAGMENT);
	}

	private void testCase(String authority, String dir, String file, String query, String fragment) {
		String suffix = "";
		if(query!=null) {
			suffix+="?"+query;
		}
		if(fragment!=null) {
			suffix+="#"+fragment;
		}
		verifyCase(uri(nullable(authority)+dir+file+suffix), dir, file, query, fragment);
	}

	private static String nullable(String string) {
		return string==null?"":string;
	}

	private void verifyCase(URI target, String dir, String file, String query, String fragment) {
		URIDescriptor descriptor = URIDescriptor.create(target);
		LOGGER.debug("Descriptor for {} [dir={}, file={}, query={}, fragment={}]\n{}",target,dir,file,query,fragment,descriptor);
		assertThat("No descriptor created",descriptor,notNullValue());
		assertThat("Invalid uri",descriptor.getUri(),equalTo(target));
		assertThat("Invalid directory",descriptor.getDir(),equalTo(dir));
		assertThat("Invalid file",descriptor.getFile(),equalTo(file));
		assertThat("Invalid query",descriptor.getQuery(),equalTo(query));
		assertThat("Invalid fragment",descriptor.getFragment(),equalTo(fragment));
	}

}
