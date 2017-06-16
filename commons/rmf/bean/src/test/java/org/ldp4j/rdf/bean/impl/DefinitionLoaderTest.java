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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.rdf.bean.ComplexNamedFallbackType;
import org.ldp4j.rdf.bean.ComplexUnnamedFallbackType;
import org.ldp4j.rdf.bean.InvalidDefinitionException;
import org.ldp4j.rdf.bean.annotations.Type;
import org.ldp4j.rdf.bean.example.services.Control;
import org.ldp4j.rdf.bean.example.services.Service;

import fallback.SimpleNamedFallbackType;
import fallback.SimpleUnnamedFallbackType;

@Type(
	name=DefinitionLoaderTest.NAME,
	namespace=DefinitionLoaderTest.NAMESPACE
)
public class DefinitionLoaderTest {

	public static final String NAMESPACE = "http://www.example.org/vocab#";
	public static final String NAME = "MyType";

	private TypeManagerImpl manager;

	@Before
	public void setUp() {
		manager = new TypeManagerImpl(new TransactionalTypeRegistry());
	}
	
	@Test(expected=InvalidDefinitionException.class)
	public void testForClass$orphanClass$nullNamespace() throws Exception {
		Class<?> orphanClass = Class.forName("NullNamespaceOrphanType");
		DefinitionLoader.loadType(orphanClass,manager);
	}

	@Test(expected=InvalidDefinitionException.class)
	public void testForClass$orphanClass$emptyNamespace() throws Exception {
		Class<?> orphanClass = Class.forName("EmptyNamespaceOrphanType");
		DefinitionLoader.loadType(orphanClass,manager);
	}

	@Test
	public void testForClass$unnamed$fallback$simple() throws Exception {
		org.ldp4j.rdf.bean.Type sut = DefinitionLoader.loadType(SimpleUnnamedFallbackType.class,manager);
		assertThat(sut.getNamespace(),equalTo("http://fallback/"));
		assertThat(sut.getName(),equalTo(SimpleUnnamedFallbackType.class.getSimpleName()));
	}

	@Test
	public void testForClass$unnamed$fallback$complex() throws Exception {
		org.ldp4j.rdf.bean.Type sut = DefinitionLoader.loadType(ComplexUnnamedFallbackType.class,manager);
		assertThat(sut.getNamespace(),equalTo("http://ldp4j.org/rdf/bean/"));
		assertThat(sut.getName(),equalTo(ComplexUnnamedFallbackType.class.getSimpleName()));
	}

	@Test
	public void testForClass$named$fallback$simple() throws Exception {
		org.ldp4j.rdf.bean.Type sut = DefinitionLoader.loadType(SimpleNamedFallbackType.class,manager);
		assertThat(sut.getNamespace(),equalTo("http://fallback/"));
		assertThat(sut.getName(),equalTo(SimpleNamedFallbackType.NAME));
	}

	@Test
	public void testForClass$named$fallback$complex() throws Exception {
		org.ldp4j.rdf.bean.Type sut = DefinitionLoader.loadType(ComplexNamedFallbackType.class,manager);
		assertThat(sut.getNamespace(),equalTo("http://ldp4j.org/rdf/bean/"));
		assertThat(sut.getName(),equalTo(ComplexNamedFallbackType.NAME));
	}

	@Test
	public void testForClass$unnamed$vocabularyNamespace() throws Exception {
		org.ldp4j.rdf.bean.Type sut = DefinitionLoader.loadType(Service.class,manager);
		assertThat(sut.getNamespace(),equalTo("http://delicias.dia.fi.upm.es/alm-istack/transactions/services#"));
		assertThat(sut.getName(),equalTo(Service.class.getSimpleName()));
	}

	@Test
	public void testForClass$named$vocabularyNamespace() throws Exception {
		org.ldp4j.rdf.bean.Type sut = DefinitionLoader.loadType(Control.class,manager);
		assertThat(sut.getNamespace(),equalTo("http://delicias.dia.fi.upm.es/alm-istack/transactions/services#"));
		assertThat(sut.getName(),equalTo("HypermediaControl"));
	}

	@Test
	public void testForClass$defined() throws Exception {
		org.ldp4j.rdf.bean.Type sut = DefinitionLoader.loadType(DefinitionLoaderTest.class,manager);
		assertThat(sut.getNamespace(),equalTo(NAMESPACE));
		assertThat(sut.getName(),equalTo(NAME));
	}
	
}