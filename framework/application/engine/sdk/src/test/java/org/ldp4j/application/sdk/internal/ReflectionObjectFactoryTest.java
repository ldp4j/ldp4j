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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-sdk:0.2.2
 *   Bundle      : ldp4j-application-engine-sdk-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.sdk.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;



import java.lang.reflect.InvocationTargetException;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.application.sdk.spi.ObjectParseException;

public class ReflectionObjectFactoryTest {

	private ReflectionObjectFactory<ReflectionObjectFactoryTest> sut;

	public static void valueOf(String valueOf) {
		throw new IllegalStateException("failure");
	}

	@Before
	public void setUp() throws Exception {
		this.sut=new ReflectionObjectFactory<ReflectionObjectFactoryTest>(ReflectionObjectFactoryTest.class,getClass().getDeclaredMethod("valueOf",String.class));
	}

	@Test
	public void testTargetClass() throws Exception {
		assertThat((Object)sut.targetClass(),sameInstance((Object)ReflectionObjectFactoryTest.class));
	}

	@Test
	public void testFromString() throws Exception {
		try {
			this.sut.fromString("data");
			fail("Should fail to create object");
		} catch (ObjectParseException e) {
			assertThat((Object)e.getValueClass(),sameInstance((Object)ReflectionObjectFactoryTest.class));
			assertThat(e.getRawValue(),equalTo("data"));
			assertThat(e.getCause(),instanceOf(InvocationTargetException.class));
			assertThat(e.getCause().getCause(),instanceOf(IllegalStateException.class));
		}
	}

}
