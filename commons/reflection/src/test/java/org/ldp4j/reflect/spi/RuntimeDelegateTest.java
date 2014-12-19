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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-reflection:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-reflection-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.reflect.spi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RuntimeDelegateTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		RuntimeDelegate.setInstance(null);
		System.clearProperty(RuntimeDelegate.REFLECTION_SPI_PROPERTY);
	}

	@Test
	public void testLifecycle$defaultDelegate() {
		assertThat(RuntimeDelegate.getInstance(),notNullValue());
		assertThat(RuntimeDelegate.getInstance().getClass().getCanonicalName(),equalTo(RuntimeDelegate.class.getCanonicalName().concat(".DefaultRuntimeDelegate")));
	}

	@Test
	public void testLifecycle$systemPropertyInitialization() {
		System.setProperty(RuntimeDelegate.REFLECTION_SPI_PROPERTY,MockRuntimeDelegate.class.getCanonicalName());
		assertThat(RuntimeDelegate.getInstance(),instanceOf(MockRuntimeDelegate.class));
	}

	@Test
	public void testLifecycle$programmaticInitialization() {
		RuntimeDelegate delegate=new MockRuntimeDelegate();
		RuntimeDelegate.setInstance(delegate);
		assertThat(RuntimeDelegate.getInstance(),sameInstance(delegate));
	}

	@Test
	public void testLifecycle$overwrite() {
		RuntimeDelegate delegate=new MockRuntimeDelegate();
		RuntimeDelegate.setInstance(delegate);
		assertThat(RuntimeDelegate.getInstance(),sameInstance(delegate));
		RuntimeDelegate newDelegate=new MockRuntimeDelegate();
		RuntimeDelegate.setInstance(newDelegate);
		assertThat(RuntimeDelegate.getInstance(),sameInstance(newDelegate));
	}

}
