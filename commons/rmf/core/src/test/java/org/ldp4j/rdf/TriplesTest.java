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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-core:0.2.2
 *   Bundle      : rmf-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.rdf.BlankNode;
import org.ldp4j.rdf.Format;
import org.ldp4j.rdf.Literal;
import org.ldp4j.rdf.Namespaces;
import org.ldp4j.rdf.Node;
import org.ldp4j.rdf.RDFContext;
import org.ldp4j.rdf.URIRef;
import org.ldp4j.rdf.util.InmutableTripleSet;
import org.ldp4j.rdf.util.TripleSet;

import static org.ldp4j.rdf.util.RDFModelDSL.*;
import static org.ldp4j.rdf.util.RDFOperations.*;
public class TriplesTest {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static final String XSD = "http://www.w3.org/2001/XMLSchema#";
	private static final String VOCABULARY = "http://www.example.org/vocabulary#";
	private static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String DATA = "http://www.example.org/data/";
	
	private final BlankNode i1=blankNode();
	private final BlankNode i2=blankNode();
	private final Literal<String> v1 = literal("v1");
	private final Literal<String> v2 = literal("v2");
	private final Literal<String> v3 = literal("v3");
	private final URIRef dp1 = uriRef(VOCABULARY+"dp1");
	private final URIRef dp2 = uriRef(VOCABULARY+"dp2");
	private final URIRef dp3 = uriRef(VOCABULARY+"dp3");
	private final URIRef op1 = uriRef(VOCABULARY+"op1");
	private final URIRef rdfType=uriRef(RDF+"type");
	private final URIRef C1 = uriRef(VOCABULARY+"C1");
	private final URIRef C2 = uriRef(VOCABULARY+"C2");
	private final TripleSet originalTripleSet = 
			tripleSet(
				triple(i1,rdfType,C1),
				triple(i1,dp1,v1),
				triple(i1,op1,i2),
				triple(i2,rdfType,C2),
				triple(i2,dp2,v2),
				triple(i2,dp3,v3)
			);
	private final TripleSet complexLiterals=
			tripleSet(
				triple(i1,uriRef(VOCABULARY+"hasIntegerValue"),literal(10)),
				triple(i1,uriRef(VOCABULARY+"hasDoubleValue"),literal(1.0d)),
				triple(i1,uriRef(VOCABULARY+"hasBooleanValue"),literal(false)),
				triple(i1,uriRef(VOCABULARY+"hasTypedValue"),typedLiteral("sts","http://www.w3.org/2001/XMLSchema#token")),
				triple(i1,uriRef(VOCABULARY+"hasI18NValue"),literal("sts","es"))
			);
	private InmutableTripleSet sut;

	@Before
	public void setUp() throws Exception {
		sut = new InmutableTripleSet(originalTripleSet);
		System.out.println("Original:");
		System.out.println(sut);
	}

	@Test
	public void testGet() throws Exception {
		System.out.println("After get ("+i1+",null,null):");
		System.out.println(sut.get(i1,null,null));
	}

	@Test
	public void testAdd() throws Exception {
		System.out.println("After adding {\n"+complexLiterals+"}:");
		System.out.println(sut.add(complexLiterals));
	}
	
	@Test
	public void testReplace() throws Exception {
		Map<Node,Node> replacements=new HashMap<Node, Node>();
		replacements.put(i1, blankNode());
		replacements.put(i2, uriRef(DATA+"ind2"));
		System.out.println("After replacing ("+replacements+"):");
		System.out.println(replace(sut,replacements));
	}
	@Test
	public void testSerialize() throws Exception {
		InmutableTripleSet toSerialize=replace(sut,i1,uriRef("resources/defects/1")).add(complexLiterals);
		Namespaces namespaces = 
			new Namespaces().
				addPrefix("ex", VOCABULARY).
				addPrefix("rdf", RDF).
				addPrefix("xsd", XSD);
		URI baseURI = URI.create(DATA);
		Format format = Format.TURTLE;
		String serialization = serialize(toSerialize, baseURI, namespaces, format);
		System.out.println("Serialization :");
		System.out.println(serialization);
		InmutableTripleSet parse = deserialize(serialization,baseURI.resolve("new"),format);
		System.out.println("Deserialization:");
		System.out.println(parse);
	}

	private String serialize(InmutableTripleSet triples, URI baseURI, Namespaces namespaces, Format format) throws IOException {
		System.out.println(
			String.format(
				"Serializing {\n%s,\n%s,\n%s,\n%s\n}",
				indent("Triples...: {"+LINE_SEPARATOR+indent(triples)+LINE_SEPARATOR+"}"),
				indent("BaseURI...: "+baseURI),
				indent("Format....: "+format),
				indent("Namespaces: "+namespaces)
			)
		);
		if(triples==null) {
			throw new IllegalArgumentException("Object 'triples' cannot be null");
		}
		if(baseURI==null) {
			throw new IllegalArgumentException("Object 'baseURI' cannot be null");
		}
		if(namespaces==null) {
			throw new IllegalArgumentException("Object 'namespaces' cannot be null");
		}
		if(format==null) {
			throw new IllegalArgumentException("Object 'format' cannot be null");
		}
		RDFContext context = RDFContext.createContext(baseURI);
		context.setNamespaces(namespaces);
		StringWriter writer = new StringWriter();
		context.serialize(triples, format, writer);
		String serialization = writer.toString();
		return serialization;
	}

	private InmutableTripleSet deserialize(String serialization, URI baseURI, Format format) throws IOException {
		System.out.println(
			String.format(
				"Deserializing {\n%s,\n%s,\n%s,\n}",
				indent("Serialization...: {"+LINE_SEPARATOR+indent(serialization)+LINE_SEPARATOR+"}"),
				indent("BaseURI...: "+baseURI),
				indent("Format....: "+format)
			)
		);
		InmutableTripleSet result = new InmutableTripleSet(RDFContext.createContext(baseURI).deserialize(serialization, format));
		return result;
	}

	private String indent(Object object) {
		return "\t"+object.toString().replaceAll(LINE_SEPARATOR,LINE_SEPARATOR.concat("\t"));
	}

}
