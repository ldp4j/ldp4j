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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.vocabulary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.fail;

import java.net.URI;

import javax.xml.namespace.QName;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.commons.testing.Utils;

import com.google.common.base.Throwables;

@RunWith(JMockit.class)
public class TypeAdapterTest {

	private static final String SHOULD_BE_ACCESIBLE = "Chosen adapter method 'private static void org.ldp4j.application.vocabulary.External.privateMethod()' should be accesible";
	private static final String SHOULD_ACCEPT   = "Chosen adapter method 'public static void org.ldp4j.application.vocabulary.External.noParameterMethod()' should accept 'org.ldp4j.application.vocabulary.ImmutableTerm'";
	private static final String ADAPTER_FAILED  = "Chosen adapter method 'public static java.net.URI org.ldp4j.application.vocabulary.TypeAdapter$URIAdapter.adaptTo(org.ldp4j.application.vocabulary.Term)' failed while adapting value 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type'";
	private static final String COULD_NOT_ADAPT = "Chosen adapter method 'public static java.net.URI org.ldp4j.application.vocabulary.TypeAdapter$URIAdapter.adaptTo(org.ldp4j.application.vocabulary.Term)' could not adapt value 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	@Test
	public void verifyIsUtilityClass$QNameAdapter() {
		assertThat(Utils.isUtilityClass(TypeAdapter.QNameAdapter.class),equalTo(true));
	}

	@Test
	public void verifyIsUtilityClass$URIAdapter() {
		assertThat(Utils.isUtilityClass(TypeAdapter.URIAdapter.class),equalTo(true));
	}

	@Test
	public void testAdapt$URI() {
		assertThat(TypeAdapter.adapt(RDF.TYPE,URI.class),equalTo(URI.create(RDF.TYPE.qualifiedEntityName())));
	}

	@Test
	public void testAdapt$QName() {
		assertThat(TypeAdapter.adapt(RDF.TYPE,QName.class),equalTo(new QName(RDF.NAMESPACE,RDF.TYPE.entityName(),RDF.NS_PREFIX)));
	}

	@Test
	public void testAdapt$notFound() {
		try {
			TypeAdapter.adapt("uri",URI.class);
			fail("Should not adapt String to URI");
		} catch (CannotAdaptClassesException e) {
			assertThat(e.getMessage(),equalTo("Could not find adapter of adapting class 'java.lang.String' to 'java.net.URI'"));
		}
	}

	@Test
	public void testAdapt$nullResult() {
		new MockUp<TypeAdapter.URIAdapter>() {
			@Mock
			public URI adaptTo(Term term) {
				return null;
			}
		};
		try {
			TypeAdapter.adapt(RDF.TYPE,URI.class);
			fail("Should not adapt Term to URI if null is returned");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo(COULD_NOT_ADAPT));
		}
	}

	@Test
	public void testAdapt$illegalArgumentException() throws Exception {
		TypeAdapter<Term, URI> adapter = TypeAdapter.createAdapter(Term.class, URI.class);
		Deencapsulation.setField(adapter,"adapterMethod",External.class.getDeclaredMethod("noParameterMethod"));
		try {
			adapter.adapt(RDF.TYPE);
			fail("Should not adapt Term to URI if IllegalArgumentException is thrown");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo(SHOULD_ACCEPT));
		}
	}

	@Test
	public void testAdapt$illegalAccessException() throws Exception {
		TypeAdapter<Term, URI> adapter = TypeAdapter.createAdapter(Term.class, URI.class);
		Deencapsulation.setField(adapter,"adapterMethod",External.class.getDeclaredMethod("privateMethod"));
		try {
			adapter.adapt(RDF.TYPE);
			fail("Should not adapt Term to URI if IllegalAccessException is thrown");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo(SHOULD_BE_ACCESIBLE));
		}
	}

	@Test
	public void testAdapt$invocationTargetException() {
		new MockUp<TypeAdapter.URIAdapter>() {
			@Mock
			public URI adaptTo(Term term) {
				throw new IllegalArgumentException("FAILURE");
			}
		};
		try {
			TypeAdapter.adapt(RDF.TYPE,URI.class);
			fail("Should not adapt Term to URI if InvocationTargetException is thrown");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo(ADAPTER_FAILED));
			Throwable rootCause = Throwables.getRootCause(e);
			assertThat(rootCause,instanceOf(IllegalArgumentException.class));
			assertThat(rootCause.getMessage(),equalTo("FAILURE"));
		}
	}

}
