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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-bean:0.2.2
 *   Bundle      : rmf-bean-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.bean.impl;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.rdf.bean.impl.TypeSupport;

public class TypeSupportTest {

	private Class<?>[] primitives={
		Byte.TYPE,
		Character.TYPE,
		Short.TYPE,
		Integer.TYPE,
		Long.TYPE,
		Float.TYPE,
		Double.TYPE,
		Boolean.TYPE
	};
	
	private Class<?>[] wrappers={
		Byte.class,
		Character.class,
		Short.class,
		Integer.class,
		Long.class,
		Float.class,
		Double.class,
		Boolean.class
	};

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPrimitives() throws Exception {
		for(Class<?> clazz:primitives) {
			assertThat(TypeSupport.isPrimitive(clazz),equalTo(true));
			assertThat(TypeSupport.isLiteral(clazz),equalTo(true));
			assertThat(TypeSupport.isAggregation(clazz),equalTo(false));
			assertThat(TypeSupport.isRepeatable(clazz),equalTo(false));
		}
		for(Class<?> clazz:wrappers) {
			assertThat(TypeSupport.isPrimitive(clazz),equalTo(false));
		}
	}

	@Test
	public void testWrappers() throws Exception {
		for(Class<?> clazz:wrappers) {
			assertThat(TypeSupport.isWrapper(clazz),equalTo(true));
			assertThat(TypeSupport.isLiteral(clazz),equalTo(true));
			assertThat(TypeSupport.isAggregation(clazz),equalTo(false));
			assertThat(TypeSupport.isRepeatable(clazz),equalTo(false));
		}
		for(Class<?> clazz:primitives) {
			assertThat(TypeSupport.isWrapper(clazz),equalTo(false));
		}
	}

	@Test
	public void testStrings() throws Exception {
		assertThat(TypeSupport.isWrapper(CharSequence.class),equalTo(false));
		assertThat(TypeSupport.isLiteral(CharSequence.class),equalTo(true));
		assertThat(TypeSupport.isAggregation(CharSequence.class),equalTo(false));
		assertThat(TypeSupport.isRepeatable(CharSequence.class),equalTo(false));
		assertThat(TypeSupport.isWrapper(String.class),equalTo(false));
		assertThat(TypeSupport.isLiteral(String.class),equalTo(true));
		assertThat(TypeSupport.isAggregation(String.class),equalTo(false));
		assertThat(TypeSupport.isRepeatable(String.class),equalTo(false));
	}

	@Test
	public void testCollections$List() throws Exception {
		assertThat(TypeSupport.isWrapper(List.class),equalTo(false));
		assertThat(TypeSupport.isLiteral(List.class),equalTo(false));
		assertThat(TypeSupport.isAggregation(List.class),equalTo(true));
		assertThat(TypeSupport.isRepeatable(List.class),equalTo(true));
	}

	@Test
	public void testCollections$Set() throws Exception {
		assertThat(TypeSupport.isWrapper(Set.class),equalTo(false));
		assertThat(TypeSupport.isLiteral(Set.class),equalTo(false));
		assertThat(TypeSupport.isAggregation(Set.class),equalTo(true));
		assertThat(TypeSupport.isRepeatable(Set.class),equalTo(false));
	}
}
