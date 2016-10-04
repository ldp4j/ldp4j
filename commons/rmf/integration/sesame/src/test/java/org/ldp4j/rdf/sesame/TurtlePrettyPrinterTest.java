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
 *   Artifact    : org.ldp4j.commons.rmf:integration-sesame:0.2.2
 *   Bundle      : integration-sesame-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.sesame;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;

import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.rdf.sesame.SesameUtils;
import org.ldp4j.rdf.sesame.TurtlePrettyPrinter;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.sail.memory.model.MemValueFactory;

public class TurtlePrettyPrinterTest extends AbstractRDFTestCase {

	private static final String NO_NAMESPACE = "http://www.example.org/no";

	private static final String NO_PX = "np";

	private static final String COMMENT = "My comment";
	private static final String NS1_PX1="p1_1";
	private static final String NS1_PX2="p2_2";
	private static final String NS1_NAMESPACE="http://www.example.org/one#";
	private static final String NS2_PX="p2";
	private static final String NS2_NAMESPACE="http://www.example.org/other#";

	private static final String BN_FOLDED="bnFolded";
	@SuppressWarnings("unused")
	private static final String BN_UNFOLDED="bnUnfolded";

	private static final String URI1="uri1";
	private static final String URI2="uri2";
	@SuppressWarnings("unused")
	private static final String RELATIVE="rel";

	private static final String BASE="http://www.example.org/base/";
	private static final String BASE_DIRECTIVE = "@base <"+BASE+"> .";

	private TurtlePrettyPrinter noBase;
	private StringWriter outNoBase;

	private TurtlePrettyPrinter withBase;
	private StringWriter outWithBase;
	private ValueFactory vf;

	private URI uri(String value) {
		return vf.createURI(value);
	}

	private BNode blankNode(String value) {
		return vf.createBNode(value);
	}

	private Statement statement(Resource subject, URI predicate, Value object) {
		return vf.createStatement(subject, predicate, object);
	}

	private String comment(String comment) {
		return String.format("# %s",comment);
	}

	private String prefixDirective(String prefix, String namespace) {
		return String.format("@prefix %s: <%s> .",prefix,namespace);
	}

	private URI uriRef(String namespace, String localPart) {
		return uri(namespace+(namespace.endsWith("/")?"":namespace.endsWith("#")?"":"/")+localPart);
	}

	private String qname(String prefix, String localPart) {
		return prefix+":"+localPart;
	}

	@Before
	public void setUp() {
		vf = new MemValueFactory();
		outNoBase = new StringWriter();
		outWithBase = new StringWriter();
		noBase = new TurtlePrettyPrinter(outNoBase);
		withBase = new TurtlePrettyPrinter(vf.createURI(BASE),outWithBase);
	}

	@Test
	public void testNoBase$emptyDoc() throws Exception {
		noBase.startRDF();
		noBase.endRDF();
		assertThat(outNoBase.toString(),isEmptyString());
	}

	@Test
	public void testNoBase$emptyDoc$CommentsAreDiscarded() throws Exception {
		noBase.startRDF();
		noBase.handleComment(COMMENT);
		noBase.endRDF();
		assertThat(outNoBase.toString(),isEmptyString());
	}

	@Test
	public void testNobase$emptuDoc$NamespacesAreDiscarded() throws Exception {
		noBase.startRDF();
		noBase.handleNamespace(NS1_PX1, NS1_NAMESPACE);
		noBase.endRDF();
		assertThat(outNoBase.toString(),isEmptyString());
	}

	@Test
	public void testWithBase$emptyDoc() throws Exception {
		withBase.startRDF();
		withBase.endRDF();
		assertThat(outWithBase.toString(),containsString(BASE_DIRECTIVE));
	}

	@Test
	public void testWithBase$emptyDoc$CommentsAreDiscarded() throws Exception {
		withBase.startRDF();
		withBase.handleComment(COMMENT);
		withBase.endRDF();
		assertThat(outWithBase.toString(),containsString(BASE_DIRECTIVE));
		assertThat(outWithBase.toString(),not(containsString(comment(COMMENT))));
	}

	@Test
	public void testWithbase$emptyDoc$NamespacesAreDiscarded() throws Exception {
		withBase.startRDF();
		withBase.handleNamespace(NS1_PX1, NS1_NAMESPACE);
		withBase.endRDF();
		assertThat(outWithBase.toString(),containsString(BASE_DIRECTIVE));
		assertThat(outWithBase.toString(),not(containsString(prefixDirective(NS1_PX1, NS1_NAMESPACE))));
	}

	@Test
	public void testWithbase$simpleDoc$UnusedNamespacesAreDiscarded() throws Exception {
		withBase.startRDF();
		withBase.handleNamespace(NS1_PX1, NS1_NAMESPACE);
		withBase.handleNamespace(NS2_PX, NS2_NAMESPACE);
		withBase.handleNamespace(NO_PX,NO_NAMESPACE);
		withBase.handleStatement(statement(uriRef(NS1_NAMESPACE,URI1),uriRef(NS2_NAMESPACE,URI2),blankNode(BN_FOLDED)));
		withBase.endRDF();
		assertThat(outWithBase.toString(),containsString(BASE_DIRECTIVE));
		assertThat(outWithBase.toString(),containsString(prefixDirective(NS1_PX1, NS1_NAMESPACE)));
		assertThat(outWithBase.toString(),containsString(prefixDirective(NS2_PX, NS2_NAMESPACE)));
		assertThat(outWithBase.toString(),containsString(qname(NS1_PX1,URI1)));
		assertThat(outWithBase.toString(),containsString(qname(NS2_PX,URI2)));
		assertThat(outWithBase.toString(),containsString(blankNode(BN_FOLDED).toString()));

		assertThat(outWithBase.toString(),not(containsString(prefixDirective(NO_PX, NO_NAMESPACE))));
		assertThat(outWithBase.toString(),not(containsString(uriRef(NS1_NAMESPACE,URI1).toString())));
		assertThat(outWithBase.toString(),not(containsString(uriRef(NS2_NAMESPACE,URI2).toString())));
	}

	@Test
	public void testWithbase$simpleDoc$NamespacePrefixesOverlap() throws Exception {
		withBase.startRDF();
		withBase.handleNamespace(NS1_PX1, NS1_NAMESPACE);
		withBase.handleNamespace(NS2_PX, NS2_NAMESPACE);
		withBase.handleNamespace(NS1_PX2,NS1_NAMESPACE);
		withBase.handleStatement(statement(uriRef(NS1_NAMESPACE,URI1),uriRef(NS2_NAMESPACE,URI2),blankNode(BN_FOLDED)));
		withBase.endRDF();
		assertThat(outWithBase.toString(),containsString(BASE_DIRECTIVE));
		assertThat(outWithBase.toString(),containsString(prefixDirective(NS1_PX2, NS1_NAMESPACE)));
		assertThat(outWithBase.toString(),containsString(prefixDirective(NS2_PX, NS2_NAMESPACE)));
		assertThat(outWithBase.toString(),containsString(qname(NS1_PX2,URI1)));
		assertThat(outWithBase.toString(),containsString(qname(NS2_PX,URI2)));
		assertThat(outWithBase.toString(),containsString(blankNode(BN_FOLDED).toString()));

		assertThat(outWithBase.toString(),not(containsString(prefixDirective(NS1_PX1, NS1_NAMESPACE))));
		assertThat(outWithBase.toString(),not(containsString(qname(NS1_PX1,URI1))));
		assertThat(outWithBase.toString(),not(containsString(uriRef(NS1_NAMESPACE,URI1).toString())));
		assertThat(outWithBase.toString(),not(containsString(uriRef(NS2_NAMESPACE,URI2).toString())));
	}

	protected URL getExamplesResource() {
		return ClassLoader.getSystemResource("corner_cases.ttl");
	}

	@Test
	public void testCornerCases() throws Exception {
		RepositoryConnection connection=getConnection();
		URI ctx=uri(NS1_NAMESPACE);
		URI ctx_res=uri(NS2_NAMESPACE);
		SesameUtils.load(connection, getExamplesResource(), ctx);
		List<URI> ctxs=new ArrayList<URI>();
		ctxs.add(ctx);
		String result = SesameUtils.prettyPrint(connection, ctxs);
		dumpTurtle(result);
		SesameUtils.load(connection, result, ctx_res);
	}
}
