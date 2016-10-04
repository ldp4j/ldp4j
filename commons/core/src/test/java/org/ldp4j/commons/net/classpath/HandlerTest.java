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
package org.ldp4j.commons.net.classpath;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class HandlerTest {

	private static final String INVALID_PROTOCOL = "class";
	private static final String RESOURCE = "dummy.cfg";
	private static final String EXISTENT_RESOURCE = "classpath://"+RESOURCE;

	private static final String NF_RESOURCE = "not_found.cfg";
	private static final String NOT_FOUND_RESOURCE = "classpath://"+NF_RESOURCE;

	private static final String INVALID_RESOURCE = INVALID_PROTOCOL+"://"+RESOURCE;

	private static final class MyClassLoader extends ClassLoader {

		@Override
		public URL getResource(String name) {
			if(name.equals(RESOURCE)) {
				return null;
			}
			return super.getResource(name);
		}

		@Override
		public InputStream getResourceAsStream(String name) {
			if(name.equals(RESOURCE)) {
				return null;
			}
			return super.getResourceAsStream(name);
		}

		public String toString() {
			return EXISTENT_RESOURCE;
		}
	}

	@Before
	public void setUp() throws Exception {
		System.setProperty("java.protocol.handler.pkgs", "org.ldp4j.commons.net");
	}

	@Test
	public void testIntegration$manual$existent() throws IOException {
		URL url = new URL(null,EXISTENT_RESOURCE,new Handler());
		assertThat(Resources.toString(url,Charsets.UTF_8),endsWith(RESOURCE));
	}

	@Test
	public void testIntegration$manual$nonExistent() throws IOException {
		URL url = new URL(null,NOT_FOUND_RESOURCE,new Handler());
		try {
			url.openStream();
			fail("Should not read a missing resource");
		} catch (IOException e) {
			assertThat(e.getMessage(),containsString(NF_RESOURCE));
		}
	}

	@Test
	public void testIntegration$manual$existent$otherClassLoader() throws IOException {
		URL url = new URL(null,EXISTENT_RESOURCE,new Handler(new MyClassLoader()));
		try {
			url.openStream();
			fail("Should not read a filtered resource");
		} catch (IOException e) {
			assertThat(e.getMessage(),containsString(RESOURCE));
		}
	}

	@Test
	public void testIntegration$manual$invalidProtocol() throws IOException {
		URL url = new URL(null,INVALID_RESOURCE,new Handler());
		try {
			url.openStream();
			fail("Should not process an invalid protocol");
		} catch (IOException e) {
			assertThat(e,instanceOf(MalformedURLException.class));
			assertThat(e.getMessage(),containsString(INVALID_PROTOCOL));
		}
	}


	@Test
	public void testIntegration$protocol$existent() throws IOException {
		URL url = new URL(EXISTENT_RESOURCE);
		assertThat(Resources.toString(url,Charsets.UTF_8),endsWith(RESOURCE));
	}

	@Test
	public void testIntegration$protocol$nonExistent() throws IOException {
		URL url = new URL(NOT_FOUND_RESOURCE);
		try {
			url.openStream();
			fail("Should not read a missing resource");
		} catch (IOException e) {
			assertThat(e.getMessage(),containsString(NF_RESOURCE));
		}
	}

}
