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
package org.ldp4j.reflect.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;

import org.junit.Test;
import org.ldp4j.reflect.model.Invocable;
import org.ldp4j.reflect.model.Parameter;

public class ImmutableModelFactoryTest {

	public static interface Checker {

		public String array(String[] array);

	}


	@Test
	public void testNewInvocable$method() throws Exception {
		Method method = String.class.getMethod("concat", String.class);
		Invocable invocable = ImmutableModelFactory.getInstance().newInvocable(method);
		assertThat(invocable, notNullValue());
		assertThat(invocable.as(Method.class), equalTo(method));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testNewInvocable$constructor() throws Exception {
		Constructor<?> constructor = URI.class.getConstructor(String.class);
		Invocable invocable = ImmutableModelFactory.getInstance().newInvocable(constructor);
		assertThat(invocable, notNullValue());
		assertThat((Constructor)invocable.as(Constructor.class), equalTo((Constructor)constructor));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testNewParameter$fromConstructor() throws Exception {
		Constructor<?> constructor = URI.class.getConstructor(String.class);
		Parameter<?> invocable = ImmutableModelFactory.getInstance().newParameter(constructor,0,String.class,String.class);
		assertThat(invocable, notNullValue());
		assertThat((Class)invocable.getRawType(), equalTo((Class)String.class));
		assertThat(invocable.getType(), equalTo((Type)String.class));
		assertThat(invocable.getPosition(), equalTo(0));
		assertThat(invocable.getAnnotations(),notNullValue());
		assertThat(invocable.getAnnotations().length,equalTo(0));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewParameter$invalidPosition() throws Exception {
		Constructor<?> constructor = URI.class.getConstructor(String.class);
		ImmutableModelFactory.getInstance().newParameter(constructor,2,String.class,String.class);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewParameter$invalidRawType() throws Exception {
		Constructor<?> constructor = URI.class.getConstructor(String.class);
		ImmutableModelFactory.getInstance().newParameter(constructor,0,Long.class,Long.class);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewParameter$incompatibleType$class() throws Exception {
		Constructor<?> constructor = URI.class.getConstructor(String.class);
		ImmutableModelFactory.getInstance().newParameter(constructor,0,String.class,Long.class);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewParameter$incompatibleType$array() throws Exception {
		Method method = Checker.class.getMethod("array",String[].class);
		ImmutableModelFactory.getInstance().newParameter(method,0,String[].class,String.class);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNewParameter$incompatibleType$array2() throws Exception {
		Method method = Checker.class.getMethod("array",String[].class);
		ImmutableModelFactory.getInstance().newParameter(method,0,String[].class,Long[].class);
	}

}
