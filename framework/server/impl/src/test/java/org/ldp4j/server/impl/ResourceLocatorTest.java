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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.impl;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.net.URI;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ldp4j.server.impl.ResourceLocator;

public class ResourceLocatorTest {

	
	
	private ResourceLocator sut;

	@Before
	public void setUp() {
		sut=new ResourceLocator();
	}
	
	@Ignore("Not implemented yet")
	@Test
	public void testCreateResourceLocationUriInfoStringString() {
	}

	@Ignore("Not implemented yet")
	@Test
	public void testCreateResourceLocationStringString() {
	}

	@Test
	public void testResolveContainerFromLocation() {
		URI location = URI.create("http://localhost/ldp/resources/c/r");
		String c = sut.resolveContainerFromLocation(location);
		assertThat(c,is("c"));
	}

	@Test
	public void testResolveResourceFromLocation() {
		URI location = URI.create("http://localhost/ldp/resources/c/r");
		String c = sut.resolveResourceFromLocation(location);
		assertThat(c,is("r"));
	}

}
