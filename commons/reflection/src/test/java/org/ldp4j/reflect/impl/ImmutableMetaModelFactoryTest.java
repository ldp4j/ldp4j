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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;

import org.junit.Test;
import org.ldp4j.reflect.harness.Childclass;
import org.ldp4j.reflect.meta.MetaClass;
import org.ldp4j.reflect.meta.MetaConstructor;
import org.ldp4j.reflect.meta.MetaField;
import org.ldp4j.reflect.meta.MetaMethod;

public class ImmutableMetaModelFactoryTest {

	@Test
	public void testGetInstance() {
		assertThat(ImmutableMetaModelFactory.getInstance(),instanceOf(ImmutableMetaModelFactory.class));
	}

	@Test
	public void testNewMetaClass() {
		MetaClass<String> metaClass = ImmutableMetaModelFactory.getInstance().newMetaClass(String.class, String.class);
		assertThat(metaClass,instanceOf(ImmutableMetaClass.class));
		assertThat(metaClass.get(),equalTo(String.class));
	}

	@Test
	public void testNewMetaConstructor() throws Exception {
		Constructor<URI> constructor = URI.class.getConstructor(String.class);
		MetaClass<URI> metaClass=ImmutableMetaModelFactory.getInstance().newMetaClass(URI.class,URI.class);
		MetaConstructor<URI> metaConstructor = ImmutableMetaModelFactory.getInstance().newMetaConstructor(metaClass,constructor);
		assertThat(metaConstructor,instanceOf(ImmutableMetaConstructor.class));
		assertThat(metaConstructor.get(),equalTo(constructor));
	}

	@Test
	public void testNewMetaMethod() throws Exception {
		Method method = String.class.getMethod("concat", String.class);
		MetaClass<String> metaClass=ImmutableMetaModelFactory.getInstance().newMetaClass(String.class,String.class);
		MetaMethod<String, String> metaMethod= ImmutableMetaModelFactory.getInstance().newMetaMethod(metaClass, method, metaClass);
		assertThat(metaMethod,instanceOf(ImmutableMetaMethod.class));
		assertThat(metaMethod.get(),equalTo(method));
	}

	@Test
	public void testNewMetaField() throws Exception {
		Field field=Childclass.class.getDeclaredField("known");
		MetaClass<Childclass> metaClass = ImmutableMetaModelFactory.getInstance().newMetaClass(Childclass.class, Childclass.class);
		MetaField<Childclass> metaField = ImmutableMetaModelFactory.getInstance().newMetaField(metaClass,field);
		assertThat(metaField,instanceOf(ImmutableMetaField.class));
		assertThat(metaField.get(),equalTo(field));
	}

}