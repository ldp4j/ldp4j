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
 * W3C RDF Vocabulary Description Language 1.0 (RDF Schema) Vocabulary.
 * <p>
 * This vocabulary provides an informal representation of the terms as defined
 * in the RDFS SChema specification. Check the specification for normative
 * reference.
 * <p>
 *
 * <b>Namespace:</b> {@code http://www.w3.org/2000/01/rdf-schema#} <br>
 * <b>Prefix:</b> {@code rdfs}
 *
 * @version 1.0
 * @since 1.0.0
 * @author Miguel Esteban Guti&eacute;rrez
 * @see <a href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/">http://www.w3.org/TR/2004/REC-rdf-schema-20040210/</a>
 */
public final class RDFS extends AbstractImmutableVocabulary<ImmutableTerm> {

	private static final long serialVersionUID = 1083533867342570283L;

	/** The namespace of the vocabulary ({@code http://www.w3.org/2000/01/rdf-schema#}) **/
	public static final String NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";

	/** The preferred prefix of the vocabulary ({@code rdfs}) **/
	public static final String NS_PREFIX = "rdfs";

	/* ---------------------------------------------------------------------- */
	/* PROPERTIES                                                             */
	/* ---------------------------------------------------------------------- */

	/**
	 * TYPE
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#subClassOf}.
	 * <p>
	 * The subject is a subclass of a class.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_subclassof">rdfs:subClassOf</a>
	 */
	public static final Term SUB_CLASS_OF;

	/**
	 * FIRST
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#subPropertyOf}.
	 * <p>
	 * The subject is a subproperty of a property.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_subpropertyof">rdfs:subPropertyOf</a>
	 */
	public static final Term SUB_PROPERTY_OF;

	/**
	 * REST
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#domain}.
	 * <p>
	 * A domain of the subject property.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_domain">rdfs:domain</a>
	 */
	public static final Term DOMAIN;

	/**
	 * VALUE
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#range}.
	 * <p>
	 * A range of the subject property.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_range">rdfs:range</a>
	 */
	public static final Term RANGE;

	/**
	 * SUBJECT
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#label}.
	 * <p>
	 * A human-readable name for the subject.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_label">rdfs:label</a>
	 */
	public static final Term LABEL;

	/**
	 * PREDICATE
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#comment}.
	 * <p>
	 * A description of the subject resource.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_comment">rdfs:comment</a>
	 */
	public static final Term COMMENT;

	/**
	 * OBJECT
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#member}.
	 * <p>
	 * A member of the subject resource.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_member">rdfs:member</a>
	 */
	public static final Term MEMBER;

	/**
	 * SEE_ALSO
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#seeAlso}.
	 * <p>
	 * Further information about the subject resource.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_seealso">rdfs:seeAlso</a>
	 */
	public static final Term SEE_ALSO;

	/**
	 * IS_DEFINED_BY
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#isDefinedBy}.
	 * <p>
	 * The definition of the subject resource.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_isdefinedby">rdfs:isDefinedBy</a>
	 */
	public static final Term IS_DEFINED_BY;

	/* ---------------------------------------------------------------------- */
	/* CLASSES                                                                */
	/* ---------------------------------------------------------------------- */

	/**
	 * XML_LITERAL
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#Resource}.
	 * <p>
	 * The class resource, everything.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_resource">rdfs:Resource</a>
	 */
	public static final Term RESOURCE;

	/**
	 * PROPERTY
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#Literal}.
	 * <p>
	 * The class of literal values, e.g. textual strings and integers.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_literal">rdfs:Literal</a>
	 */
	public static final Term LITERAL;

	/**
	 * STATEMENT
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#Class}.
	 * <p>
	 * The class of classes.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_class">rdfs:Class</a>
	 */
	public static final Term CLASS;

	/**
	 * BAG
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#Datatype}.
	 * <p>
	 * The class of RDF datatypes.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_datatype">rdfs:Datatype</a>
	 */
	public static final Term DATATYPE;

	/**
	 * SEQ
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#Container}.
	 * <p>
	 * The class of RDF containers.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_container">rdfs:Container</a>
	 */
	public static final Term CONTAINER;

	/**
	 * ALT
	 * <p>
	 * {@code http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty}.
	 * <p>
	 * The class of container membership properties, {@code rdf:_1},
	 * {@code rdf:_2}, ..., all of which are sub-properties of
	 * {@code rdfs:member}.
	 *
	 * @see <a
	 *      href="http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_containermembershipproperty">rdfs:ContainerMembershipProperty</a>
	 */
	public static final Term CONTAINER_MEMBERSHIP_PROPERTY;

	/** The unique instance of the vocabulary **/
	private static final RDFS VOCABULARY=new RDFS();

	static {
		// Initialize properties
		SUB_CLASS_OF=term("subClassOf");
		SUB_PROPERTY_OF=term("subPropertyOf");
		DOMAIN=term("domain");
		RANGE=term("range");
		LABEL=term("label");
		COMMENT=term("comment");
		MEMBER=term("member");
		SEE_ALSO=term("seeAlso");
		IS_DEFINED_BY=term("isDefinedBy");
		// Initialize classes
		RESOURCE=term("Resource");
		LITERAL=term("Literal");
		CLASS=term("Class");
		DATATYPE=term("Datatype");
		CONTAINER=term("Container");
		CONTAINER_MEMBERSHIP_PROPERTY=term("ContainerMembershipProperty");
		VOCABULARY.initialize();
	}

	private RDFS() {
		super(ImmutableTerm.class,RDFS.NAMESPACE,RDFS.NS_PREFIX);
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
	public static RDFS getInstance() {
		return RDFS.VOCABULARY;
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