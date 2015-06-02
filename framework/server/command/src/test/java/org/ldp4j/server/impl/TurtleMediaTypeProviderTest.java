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
package org.ldp4j.server.impl;


import static org.ldp4j.rdf.util.RDFModelDSL.uriRef;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import javax.xml.namespace.QName;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.data.DataDSL;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.IndividualReferenceBuilder;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NameVisitor;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.application.vocabulary.RDF;
import org.ldp4j.application.vocabulary.Term;
import org.ldp4j.rdf.URIRef;
import org.ldp4j.server.ResourceIndex;
import org.ldp4j.server.data.ImmutableContext;
import org.ldp4j.server.impl.TurtleMediaTypeProvider;

@RunWith(JMockit.class)
public class TurtleMediaTypeProviderTest {

	private static final String NL = System.getProperty("line.separator");
	private static final String EXAMPLE_BODY =
			"@prefix sav : <http://test/vocab#> ."+NL+
			"@base <http://www.example.org/entities/> ." +NL+
			"_:inference a sav:Inference ;"+NL+
			"	sav:uses <dataSet1>, <vocabulary1> .";

	private static final URI DATA_SET_PATH=URI.create("http://www.example.org/entities/dataSet1");
	private static final ResourceId DATA_SET_NAME = ResourceId.createId(NamingScheme.getDefault().name(DATA_SET_PATH),"templateId");
	private static final URI VOCABULARY_PATH=URI.create("http://www.example.org/entities/vocabulary1");
	private static final ResourceId VOCABULARY_NAME = ResourceId.createId(NamingScheme.getDefault().name(VOCABULARY_PATH),"templateId");
	private TurtleMediaTypeProvider sut;
	private URI BASE;

	@Before
	public void setUp() throws Exception {
		sut=new TurtleMediaTypeProvider();
		BASE=URI.create("http://www.example.org/");
	}

	@Test
	public void testUnmarshall(@Mocked final ResourceIndex mock) throws Exception {
		new NonStrictExpectations() {{
			mock.resolveLocation(DATA_SET_PATH);result=DATA_SET_NAME;minTimes=1;
			mock.resolveLocation(VOCABULARY_PATH);result=VOCABULARY_NAME;minTimes=1;
		}};
		DataSet result =
			sut.newUnmarshaller(ImmutableContext.newInstance(BASE, mock)).
				unmarshall(EXAMPLE_BODY, sut.getSupportedMediaTypes().iterator().next());
		System.out.println(result.toString());
	}
	private ManagedIndividualId managedIndividualId(ResourceId id) {
		return ManagedIndividualId.createId(id.name(),id.templateId());
	}

	@Ignore("Not ready for production")
	@Test
	public void testMarshall(@Mocked final ResourceIndex mock) throws Exception {
		new NonStrictExpectations() {{
			mock.resolveResource(managedIndividualId(DATA_SET_NAME));result=DATA_SET_PATH;minTimes=1;
			mock.resolveResource(managedIndividualId(VOCABULARY_NAME));result=VOCABULARY_PATH;minTimes=1;
		}};

		DataSet dataSet =
			DataDSL.
				dataSet().
					individual(IndividualReferenceBuilder.newReference().toLocalIndividual().named("inference")).
						hasLink(RDF.TYPE.qualifiedEntityName()).
							referringTo(IndividualReferenceBuilder.newReference().toLocalIndividual().named("http://test/vocab#Inference")).
						hasLink("http://test/vocab#uses").
							referringTo(IndividualReferenceBuilder.newReference().toLocalIndividual().named(VOCABULARY_PATH)).
							referringTo(IndividualReferenceBuilder.newReference().toLocalIndividual().named(DATA_SET_PATH)).
						build();

		String result=
			sut.newMarshaller(ImmutableContext.newInstance(BASE, mock)).
				marshall(dataSet, sut.getSupportedMediaTypes().iterator().next());
		System.out.println(dataSet);
		System.out.println(result);
	}

	@Test
	public void testNameVisitor() {

		URI create = URI.create("http://www.example.org/");

		GlobalNameVisitor sut=new GlobalNameVisitor(create);
		String namespaceURI = "http://example.org/scheme#";
		test(sut, NamingScheme.getDefault().name(new QName(namespaceURI,"QName")));
		test(sut, NamingScheme.getDefault().name(URI.create(namespaceURI+"URI")));
		test(sut, NamingScheme.getDefault().name(LDP.RESOURCE));
		test(sut, NamingScheme.getDefault().name("example string"));
		test(sut, NamingScheme.getDefault().name(19));
		test(sut, NamingScheme.getDefault().name(Object.class,"first"));
	}

	void test(GlobalNameVisitor sut, Name<?> name) {
		URIRef result=sut.getResource(name);
		System.out.println(name + " --> " + result);
	}

	private static final class GlobalNameVisitor extends NameVisitor {

		private final URI base;

		private URIRef resource=null;

		private GlobalNameVisitor(URI base) {
			this.base = base;
		}

		public URIRef getResource(Name<?> name) {
			name.accept(this);
			return resource;
		}

		private URIRef encode(Object id) {
			try {
				return uriRef(base.resolve(URLEncoder.encode(id.toString(), "UTF-8")));
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException("UTF-8 charset should be supported",e);
			}
		}

		@Override
		public void visitURI(URI id) {
			resource=uriRef(id);
		}

		@Override
		public void visitQName(QName id) {
			resource=uriRef(id);
		}

		@Override
		public void visitTerm(Term id) {
			resource=uriRef(id.as(URI.class));
		}

		@Override
		public void visitString(String id) {
			resource=encode(id);
		}

		@Override
		public void visitNumber(Number id) {
			resource=encode(id);
		}

		@Override
		public void visitObject(Object id) {
			resource=encode(id);
		}

	}

}
