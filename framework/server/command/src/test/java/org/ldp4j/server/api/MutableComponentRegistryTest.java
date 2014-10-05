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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MutableComponentRegistryTest {

	private static final Class<ResourceIndex> NULL_COMPONENT_CLASS=null;

	@Mock private ResourceIndex index, otherIndex;

	private MutableComponentRegistry sut;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		try {
			sut = new MutableComponentRegistry();
		} catch (Error e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test(expected=NullPointerException.class)
	public void testRegisterComponent_exceptionPath_nullClass() throws Exception {
		sut.registerComponent(NULL_COMPONENT_CLASS, index);
	}

	@Test(expected=NullPointerException.class)
	public void testRegisterComponent_exceptionPath_nullInstance() throws Exception {
		sut.registerComponent(ResourceIndex.class, null);
	}

	@Test(expected=NullPointerException.class)
	public void testGetComponent_exceptionPath_nullClass() throws Exception {
		sut.getComponent(NULL_COMPONENT_CLASS);
	}

	@Test
	public void testGetComponent_nonExisting() throws Exception {
		assertThat(sut.getComponent(ResourceIndex.class),is(nullValue()));
	}

	@Test
	public void testNormalUsage() throws Exception {
		sut.registerComponent(ResourceIndex.class, index);
		assertThat(sut.getComponent(ResourceIndex.class),is(sameInstance(index)));
	}

	@Test
	public void testNormalUsage_overwriting() throws Exception {
		sut.registerComponent(ResourceIndex.class, index);
		assertThat(sut.getComponent(ResourceIndex.class),is(sameInstance(index)));
		sut.registerComponent(ResourceIndex.class, otherIndex);
		assertThat(sut.getComponent(ResourceIndex.class),is(sameInstance(otherIndex)));
	}
	
}