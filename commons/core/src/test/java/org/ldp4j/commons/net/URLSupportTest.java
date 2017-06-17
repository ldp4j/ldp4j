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


import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.commons.net.URLSupport;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class URLSupportTest {

	private static final String EXAMPLE_ANOTHER_PACKAGE = "org.another";
	private static final String EXAMPLE_PACKAGE = "org.test";

	@Before
	public void setUp() throws Exception {
		String object="";
		setValue(object);
	}
	
	@Test
	public void testGet$noPropertyDefined() {
		assertThat(URLSupport.getProtocolHandlerProviders(),empty());
	}

	@Test
	public void testGet$blankPropertyDefined() {
		setValue("   ");
		assertThat(URLSupport.getProtocolHandlerProviders(),empty());
	}

	@Test
	public void testGet$predefinedPropertyDefined() {
		setValue(EXAMPLE_PACKAGE);
		List<String> providers = URLSupport.getProtocolHandlerProviders();
		assertThat(providers,hasSize(1));
		assertThat(providers,contains(EXAMPLE_PACKAGE));
	}

	@Test
	public void testRoundtrip$simple() {
		URLSupport.setProtocolHandlerProviders(Arrays.asList(new String[]{EXAMPLE_PACKAGE}));
		List<String> providers = URLSupport.getProtocolHandlerProviders();
		assertThat(providers,hasSize(1));
		assertThat(providers,contains(EXAMPLE_PACKAGE));
	}

	@Test
	public void testRoundtrip$duplicated() {
		URLSupport.setProtocolHandlerProviders(Arrays.asList(new String[]{EXAMPLE_PACKAGE,EXAMPLE_PACKAGE}));
		List<String> providers = URLSupport.getProtocolHandlerProviders();
		assertThat(providers,hasSize(1));
		assertThat(providers,contains(EXAMPLE_PACKAGE));
	}

	@Test
	public void testRoundtrip$multiple() {
		URLSupport.setProtocolHandlerProviders(Arrays.asList(new String[]{EXAMPLE_PACKAGE,EXAMPLE_ANOTHER_PACKAGE}));
		List<String> providers = URLSupport.getProtocolHandlerProviders();
		assertThat(providers,hasSize(2));
		assertThat(providers,contains(EXAMPLE_PACKAGE,EXAMPLE_ANOTHER_PACKAGE));
	}

	private void setValue(String object) {
		System.setProperty("java.protocol.handler.pkgs", object);
	}

}
