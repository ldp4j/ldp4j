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

import java.net.URI;

import javax.xml.namespace.QName;

/**
 * W3C Resource Description Framework (RDF) Vocabulary.
 * <p>
 * This vocabulary provides an informal representation of the terms as defined
 * in the RDF specification. Check the specification for normative
 * reference.
 * <p>
 *
 * <b>Namespace:</b> {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#} <br>
 * <b>Prefix:</b> {@code rdf}
 *
 * @version 1.0
 * @since 1.0.0
 * @author Miguel Esteban Guti&eacute;rrez
 * @see <a href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/">http://www.w3.org/TR/2004/REC-rdf-schema-20040210/</a>
 */
public final class RDF extends AbstractImmutableVocabulary<ImmutableTerm> {

	private static final long serialVersionUID = -1616871929205422837L;

	/** The namespace of the vocabulary ({@code http://www.w3.org/1999/02/22-rdf-syntax-ns#}) **/
	public static final String NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	/** The preferred prefix of the vocabulary ({@code rdf}) **/
	public static final String NS_PREFIX = "rdf";

	/* ---------------------------------------------------------------------- */
	/* PROPERTIES                                                             */
	/* ---------------------------------------------------------------------- */

	/**
	 * TYPE
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#type}.
	 * <p>
	 * The subject is an instance of a class.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_type">rdf:type</a>
	 */
	public static final Term TYPE;

	/**
	 * FIRST
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#first}.
	 * <p>
	 * The first item in the subject RDF list.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_first">rdf:first</a>
	 */
	public static final Term FIRST;

	/**
	 * REST
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#rest}.
	 * <p>
	 * The rest of the subject RDF list after the first item.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_rest">rdf:rest</a>
	 */
	public static final Term REST;

	/**
	 * VALUE
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#value}.
	 * <p>
	 * The definition of the subject resource.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_value">rdf:value</a>
	 */
	public static final Term VALUE;

	/**
	 * SUBJECT
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#subject}.
	 * <p>
	 * The subject of the subject RDF statement.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_subject">rdf:subject</a>
	 */
	public static final Term SUBJECT;

	/**
	 * PREDICATE
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate}.
	 * <p>
	 * The predicate of the subject RDF statement.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_predicate">rdf:predicate</a>
	 */
	public static final Term PREDICATE;

	/**
	 * OBJECT
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#object}.
	 * <p>
	 * The object of the subject RDF statement.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_object">rdf:object</a>
	 */
	public static final Term OBJECT;

	/* ---------------------------------------------------------------------- */
	/* CLASSES                                                                */
	/* ---------------------------------------------------------------------- */

	/**
	 * XML_LITERAL
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral}.
	 * <p>
	 * The class of XML literal values.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_xmlliteral">rdf:XMLLiteral</a>
	 */
	public static final Term XML_LITERAL;

	/**
	 * PROPERTY
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#Property}.
	 * <p>
	 * The class of RDF properties.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_property">rdf:Property</a>
	 */
	public static final Term PROPERTY;

	/**
	 * STATEMENT
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement}.
	 * <p>
	 * The class of RDF statements.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_statement">rdf:Statement</a>
	 */
	public static final Term STATEMENT;

	/**
	 * BAG
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag}.
	 * <p>
	 * The class of unordered containers.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_bag">rdf:Bag</a>
	 */
	public static final Term BAG;

	/**
	 * SEQ
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq}.
	 * <p>
	 * The class of ordered containers.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_seq">rdf:Seq</a>
	 */
	public static final Term SEQ;

	/**
	 * ALT
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt}.
	 * <p>
	 * The class of containers of alternatives.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_alt">rdf:Alt</a>
	 */
	public static final Term ALT;

	/**
	 * LIST
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#List}.
	 * <p>
	 * The class of RDF Lists.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_list">rdf:List</a>
	 */
	public static final Term LIST;

	/* ---------------------------------------------------------------------- */
	/* INDIVIDUALS                                                            */
	/* ---------------------------------------------------------------------- */

	/**
	 * NIL
	 * <p>
	 * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#nil}.
	 * <p>
	 * The class of RDF Lists.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_nil">rdf:nil</a>
	 */
	public static final Term NIL;

	/** The unique instance of the vocabulary **/
	private static final RDF VOCABULARY=new RDF();

	static {
		// Initialize properties
		TYPE=term("type");
		FIRST=term("first");
		REST=term("rest");
		VALUE=term("value");
		SUBJECT=term("subject");
		PREDICATE=term("predicate");
		OBJECT=term("object");
		// Initialize classes
		XML_LITERAL=term("XMLLiteral");
		PROPERTY=term("Property");
		STATEMENT=term("Statement");
		BAG=term("Bag");
		SEQ=term("Seq");
		ALT=term("Alt");
		LIST=term("List");
		// Initialize individuals
		NIL=term("nil");
		VOCABULARY.initialize();
	}

	private RDF() {
		super(ImmutableTerm.class,RDF.NAMESPACE,RDF.NS_PREFIX);
	}

	/**
	 * Create a term
	 *
	 * @param localPart
	 *            The local part of the term's URI
	 * @return A {@code LDPTerm} instance that represents the term.
	 */
	private static Term term(final String localPart) {
		return new ImmutableTerm(VOCABULARY,localPart);
	}

	/**
	 * Retrieve the LDP vocabulary instance.
	 * @return Return the unique instance of the vocabulary.
	 */
	public static RDF getInstance() {
		return RDF.VOCABULARY;
	}

	/**
	 * Get the terms of the vocabulary
	 *
	 * @return An array with all the terms of the vocabulary.
	 */
	public static Term[] values() {
		return getInstance().terms();
	}

	/**
	 * Find the term that matches the specified name.
	 *
	 * @param term
	 *            A {@code String}-based representation of the term's name.
	 * @return The {@code Term} that matches the specified name.
	 */
	public static Term valueOf(final String term) {
		return getInstance().fromName(term);
	}

	/**
	 * Find the term that matches the specified URI.
	 *
	 * @param term
	 *            A {@code QName}-based representation of the term's URI.
	 * @return The {@code Term} that matches the specified URI.
	 * @see javax.xml.namespace.QName
	 */
	public static Term valueOf(final QName term) {
		return getInstance().fromValue(term);
	}

	/**
	 * Find the term that matches the specified URI.
	 *
	 * @param term
	 *            A {@code URI}-based representation of the term's URI.
	 * @return The {@code LDPTerm} that matches the specified URI.
	 * @see java.net.URI
	 */
	public static Term valueOf(final URI term) {
		return getInstance().fromValue(term);
	}

}