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
package org.ldp4j.reflect;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.ldp4j.reflect.harness.Childclass;
import org.ldp4j.reflect.harness.Marker;
import org.ldp4j.reflect.harness.Parentinterface;
import org.ldp4j.reflect.harness.Superclass;
import org.ldp4j.reflect.harness.Superinterface;

public class MetaClassTest {

	private void showParameterArguments(Map<TypeVariable<?>, Type> parameterArguments) {
		System.out.println("Parameter arguments:");
		for(Entry<TypeVariable<?>,Type> entry:parameterArguments.entrySet()) {
			System.out.printf("- %s : %s%n",entry.getKey(),entry.getValue());
		}
	}

	@Test(expected=NullPointerException.class)
	public void testCreate$null() {
		MetaClass.create(null);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testCreate$notNull() {
		MetaClass metaClass = MetaClass.create(Childclass.class);
		assertThat(metaClass,notNullValue());
		assertThat((Class)metaClass.rawType(),sameInstance((Class)Childclass.class));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testSuperclass() {
		MetaClass metaClass = MetaClass.create(Childclass.class);
		assertThat(metaClass.superClass(),notNullValue());
		assertThat((Class)metaClass.superClass().rawType(),sameInstance((Class)Childclass.class.getSuperclass()));
	}

	@Test
	public void testInterfaces() {
		MetaClass metaClass = MetaClass.create(Childclass.class);
		assertThat(metaClass.interfaces(),notNullValue());
		List<Class<?>> interfaces = Arrays.asList(Childclass.class.getInterfaces());
		for(MetaClass metaInterface:metaClass.interfaces()) {
			assertThat(metaInterface.rawType(),isIn(interfaces));
		}
	}

	@Test
	public void testHierarchy$rootInterface() {
		MetaClass metaClass = MetaClass.create(Superinterface.class);
		List<MetaClass> hierarchy = metaClass.hierarchy();
		assertThat(hierarchy,contains(metaClass));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testHierarchy$rootClass() {
		MetaClass metaClass = MetaClass.create(MetaClass.class);
		List<MetaClass> hierarchy = metaClass.hierarchy();
		assertThat(hierarchy,hasSize(2));
		assertThat(hierarchy.get(0),equalTo(metaClass));
		assertThat((Class)hierarchy.get(1).rawType(),equalTo((Class)Object.class));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testHierarchy$leafInterface() {
		MetaClass metaClass = MetaClass.create(Parentinterface.class);
		List<MetaClass> hierarchy = metaClass.hierarchy();
		assertThat(hierarchy,hasSize(2));
		assertThat(hierarchy.get(0),equalTo(metaClass));
		assertThat((Class)hierarchy.get(1).rawType(),equalTo((Class)Superinterface.class));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testHierarchy$leafClass() {
		MetaClass metaClass = MetaClass.create(Childclass.class);
		List<MetaClass> hierarchy = metaClass.hierarchy();
		assertThat(hierarchy,hasSize(6));
		assertThat(hierarchy.get(0),equalTo(metaClass));
		assertThat((Class)hierarchy.get(1).rawType(),equalTo((Class)Superclass.class));
		assertThat((Class)hierarchy.get(2).rawType(),equalTo((Class)Parentinterface.class));
		assertThat((Class)hierarchy.get(3).rawType(),equalTo((Class)Object.class));
		assertThat((Class)hierarchy.get(4).rawType(),equalTo((Class)Marker.class));
		assertThat((Class)hierarchy.get(5).rawType(),equalTo((Class)Superinterface.class));
	}

	@Test
	public void testTypeParameters$notParameterizedType() {
		MetaClass metaClass = MetaClass.create(Childclass.class);
		assertThat(metaClass.typeParameters(),notNullValue());
		assertThat(metaClass.typeParameters().length,equalTo(0));
	}

	@Test
	public void testTypeParameters$parameterizedType() {
		MetaClass metaClass = MetaClass.create(Superclass.class);
		assertThat(metaClass.typeParameters(),notNullValue());
		assertThat(metaClass.typeParameters().length,equalTo(1));
		assertThat(metaClass.typeParameters()[0],equalTo((Type)Superclass.class.getTypeParameters()[0]));
	}

	@Test
	public void testTypeArguments$notParameterizedType() {
		MetaClass metaClass = MetaClass.create(Childclass.class);
		assertThat(metaClass.typeArguments(),notNullValue());
		assertThat(metaClass.typeArguments().length,equalTo(0));
	}

	@Test
	public void testTypeArguments$parameterizedType() {
		MetaClass metaClass = MetaClass.create(Superclass.class);
		TypeVariable<?>[] parameters = metaClass.typeParameters();
		assertThat(metaClass.typeArguments(),notNullValue());
		assertThat(metaClass.typeArguments().length,equalTo(parameters.length));
		for(int i=0;i<parameters.length;i++) {
			assertThat(metaClass.typeArguments()[i],equalTo((Type)parameters[i]));
		}
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testParameterArguments() {
		MetaClass metaClass = MetaClass.create(Childclass.class);
		Map<TypeVariable<?>, Type> parameterArguments = metaClass.parameterArguments();
		TypeVariable<?> E = Superclass.class.getTypeParameters()[0];
		TypeVariable<?> T = Marker.class.getTypeParameters()[0];
		Type EVal=String.class;
		Type TVal=Superclass.class;
		assertThat(parameterArguments,notNullValue());
		assertThat(parameterArguments.keySet(),hasSize(2));
		assertThat(parameterArguments.keySet(),hasItem(T));
		assertThat(parameterArguments.keySet(),hasItem(E));
		assertThat(parameterArguments.get(E),sameInstance(EVal));
		Type actual = parameterArguments.get(T);
		assertThat(actual,instanceOf(ParameterizedType.class));
		assertThat((Class)((ParameterizedType)actual).getRawType(),sameInstance((Class)TVal));
		showParameterArguments(parameterArguments);
	}

}
