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
 * W3C Linked Data Platform (LDP) Vocabulary.
 * <p>
 * This vocabulary provides an informal representation of the terms as defined
 * in the LDP specification. Check the LDP specification for normative
 * reference.
 * <p>
 *
 * <b>Namespace:</b> {@code <http://www.w3.org/ns/ldp#>} <br>
 * <b>Prefix:</b> {@code ldp}
 *
 * @version 1.0
 * @since 1.0.0
 * @author Miguel Esteban Guti&eacute;rrez
 *
 * @see <a href="http://www.w3.org/TR/ldp/">http://www.w3.org/TR/ldp/</a>
 */
public final class LDP extends AbstractImmutableVocabulary<ImmutableTerm> {

	private static final long serialVersionUID = -7172036547604166277L;

	/** The namespace of the vocabulary ({@code http://www.w3.org/ns/ldp#}) **/
	public static final String NAMESPACE = "http://www.w3.org/ns/ldp#";

	/** The preferred prefix of the vocabulary ({@code ldp}) **/
	public static final String NS_PREFIX = "ldp";

	/* ---------------------------------------------------------------------- */
	/* PROPERTIES                                                             */
	/* ---------------------------------------------------------------------- */

	/**
	 * CONTAINS
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#contains}.
	 * <p>
	 * Links a container with resources created through the container.
	 *
	 * @see <a href="http://www.w3.org/ns/ldp#contains">ldp:contains</a>
	 */
	public static final Term CONTAINS;

	/**
	 * MEMBER
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#member}.
	 * <p>
	 * LDP servers should use this predicate as the membership predicate if
	 * there is no obvious predicate from an application vocabulary to use.
	 *
	 * @see <a href="http://www.w3.org/ns/ldp#member">ldp:member</a>
	 */
	public static final Term MEMBER;

	/**
	 * MEMBERSHIP_RESOURCE
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#membershipResource}.
	 * <p>
	 * Indicates the membership-constant-URI in a membership triple. Depending
	 * upon the membership triple pattern a container uses, as indicated by the
	 * presence of {@code ldp:hasMemberRelation} or
	 * {@code ldp:isMemberOfRelation}, the membership-constant-URI might occupy
	 * either the subject or object position in membership triples.
	 *
	 * @see <a
	 *      href="http://www.w3.org/ns/ldp#membershipResource">ldp:membershipResource</a>
	 */
	public static final Term MEMBERSHIP_RESOURCE;

	/**
	 * HAS_MEMBER_RELATION
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#hasMemberRelation}.
	 * <p>
	 * Indicates which predicate is used in membership triples, and that the
	 * membership triple pattern is {@code <membership-constant-URI,
	 * object-of-hasMemberRelation, member-URI>}.
	 *
	 * @see <a
	 *      href="http://www.w3.org/ns/ldp#hasMemberRelation">ldp:hasMemberRelation</a>
	 */
	public static final Term HAS_MEMBER_RELATION;

	/**
	 * IS_MEMBER_OF_RELATION
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#isMemberOfRelation}.
	 * <p>
	 * Indicates which predicate is used in membership triples, and that the
	 * membership triple pattern is
	 * {@code <member-URI, object-of-isMemberOfRelation,
	 * membership-constant-URI>}.
	 *
	 * @see <a
	 *      href="http://www.w3.org/ns/ldp#isMemberOfRelation">ldp:isMemberOfRelation</a>
	 */
	public static final Term IS_MEMBER_OF_RELATION;

	/**
	 * INSERTED_CONTENT_RELATION
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#insertedContentRelation}.
	 * <p>
	 * Indicates which triple in a creation request should be used as the
	 * member-URI value in the membership triple added when the creation request
	 * is successful.
	 *
	 * @see <a
	 *      href="http://www.w3.org/ns/ldp#insertedContentRelation">ldp:insertedContentRelation</a>
	 */
	public static final Term INSERTED_CONTENT_RELATION;

	/**
	 * CONSTRAINED_BY
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#constrainedBy}.
	 * <p>
	 * Indicates that the resource has publishing constraints. The object of the
	 * triple should be the URL that provides the constraints that apply.
	 *
	 * @see <a
	 *      href="http://www.w3.org/ns/ldp#constrainedBy">ldp:constrainedBy</a>
	 */
	public static final Term CONSTRAINED_BY;

	/* ---------------------------------------------------------------------- */
	/* CLASSES                                                                */
	/* ---------------------------------------------------------------------- */

	/**
	 * XML_LITERAL
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#Resource}.
	 * <p>
	 * A HTTP-addressable resource whose lifecycle is managed by a LDP server.
	 *
	 * @see <a href="http://www.w3.org/ns/ldp#Resource">ldp:Resource</a>
	 */
	public static final Term RESOURCE;

	/**
	 * NON_RDF_SOURCE
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#NonRDFSource}.
	 * <p>
	 * A Linked Data Platform Resource (LDPR) whose state is NOT represented as
	 * RDFS.
	 *
	 * @see <a href="http://www.w3.org/ns/ldp#NonRDFSource">ldp:NonRDFSource</a>
	 */
    public static final Term NON_RDF_SOURCE;

	/**
	 * RDF_SOURCE
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#RDFSource}.
	 * <p>
	 * A Linked Data Platform Resource (LDPR) whose state is represented as RDFS.
	 *
	 * @see <a href="http://www.w3.org/ns/ldp#RDFSource">ldp:RDFSource</a>
	 */
    public static final Term RDF_SOURCE;

    /**
	 * SEQ
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#Container}.
	 * <p>
	 * A Linked Data Platform RDFS Source (LDP-RS) that also conforms to
	 * additional patterns and conventions for managing membership. Readers
	 * should refer to the specification defining this ontology for the list of
	 * behaviors associated with it.
	 *
	 * @see <a href="http://www.w3.org/ns/ldp#Container">ldp:Container</a>
	 */
	public static final Term CONTAINER;

	/**
	 * BASIC_CONTAINER
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#BasicContainer}.
	 * <p>
	 * An LDPC that uses a predefined predicate to simply link to its contained
	 * resources.
	 *
	 * @see <a href="http://www.w3.org/ns/ldp#BasicContainer">ldp:BasicContainer</a>
	 */
	public static final Term BASIC_CONTAINER;

	/**
	 * DIRECT_CONTAINER
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#DirectContainer}.
	 * <p>
	 * An LDPC that is similar to a LDP-DC but it allows an indirection with the
	 * ability to list as member a resource, such as a URI representing a
	 * real-world object, that is different from the resource that is created.
	 *
	 * @see <a
	 *      href="http://www.w3.org/ns/ldp#DirectContainer">ldp:DirectContainer</a>
	 */
	public static final Term DIRECT_CONTAINER;

	/**
	 * INDIRECT_CONTAINER
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#IndirectContainer}.
	 * <p>
	 * An LDPC that has the flexibility of choosing what form the membership
	 * triples take.
	 *
	 * @see <a
	 *      href="http://www.w3.org/ns/ldp#IndirectContainer">ldp:IndirectContainer</a>
	 */
	public static final Term INDIRECT_CONTAINER;

	/* ---------------------------------------------------------------------- */
	/* INDIVIDUALS                                                            */
	/* ---------------------------------------------------------------------- */

	/**
	 * MEMBER_SUBJECT
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#MemberSubject}.
	 * <p>
	 * Used to indicate default and typical behavior for
	 * {@code ldp:insertedContentRelation}, where the member-URI value in the membership
	 * triple added when a creation request is successful is the URI assigned to
	 * the newly created resource.
	 *
	 * @see <a href="http://www.w3.org/ns/ldp#MemberSubject">ldp:MemberSubject</a>
	 */
	public static final Term MEMBER_SUBJECT;

	/**
	 * PREFER_CONTAINMENT
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#PreferContainment}.
	 * <p>
	 * LDPTerm identifying a LDPC's containment triples, for example to allow
	 * clients to express interest in receiving them.
	 *
	 * @see <a
	 *      href="http://www.w3.org/ns/ldp#PreferContainment">ldp:PreferContainment</a>
	 */
	public static final Term PREFER_CONTAINMENT;

	/**
	 * PREFER_EMPTY_CONTAINER
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#PreferEmptyContainer}.
	 * <p>
	 * LDPTerm identifying the subset of a LDPC's triples present in an empty LDPC,
	 * for example to allow clients to express interest in receiving them.
	 * Currently this excludes containment and membership triples, but in the
	 * future other exclusions might be added. This definition is written to
	 * automatically exclude those new classes of triples.
	 *
	 * @see <a
	 *      href="http://www.w3.org/ns/ldp#PreferEmptyContainer">ldp:PreferEmptyContainer</a>
	 */
	public static final Term PREFER_EMPTY_CONTAINER;

	/**
	 * PREFER_MINIMAL_CONTAINER
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#PreferMinimalContainer}.
	 * <p>
	 * LDPTerm identifying the subset of a LDPC's triples present in an empty LDPC,
	 * for example to allow clients to express interest in receiving them.
	 * Currently this excludes containment and membership triples, but in the
	 * future other exclusions might be added. This definition is written to
	 * automatically exclude those new classes of triples.
	 *
	 * @see <a
	 *      href="http://www.w3.org/ns/ldp#PreferMinimalContainer">ldp:PreferMinimalContainer</a>
	 */
	public static final Term PREFER_MINIMAL_CONTAINER;

	/**
	 * PREFER_MEMBERSHIP
	 * <p>
	 * {@code http://www.w3.org/ns/ldp#PreferMembership}.
	 * <p>
	 * LDPTerm identifying a LDPC's membership triples, for example to allow
	 * clients to express interest in receiving them.
	 *
	 * @see <a
	 *      href="http://www.w3.org/ns/ldp#PreferMembership">ldp:PreferMembership</a>
	 */
	public static final Term PREFER_MEMBERSHIP;

	/** The unique instance of the vocabulary **/
	private static final LDP VOCABULARY=new LDP();

	static {
		// Initialize properties
		CONTAINS=term("contains");
		MEMBER=term("member");
		MEMBERSHIP_RESOURCE=term("membershipResource");
		HAS_MEMBER_RELATION=term("hasMemberRelation");
		IS_MEMBER_OF_RELATION=term("isMemberOfRelation");
		INSERTED_CONTENT_RELATION=term("insertedContentRelation");
		CONSTRAINED_BY=term("constrainedBy");
		// Initialize classes
		RESOURCE=term("Resource");
		NON_RDF_SOURCE=term("NonRDFSource");
		RDF_SOURCE=term("RDFSource");
		CONTAINER=term("Container");
		BASIC_CONTAINER=term("BasicContainer");
		DIRECT_CONTAINER=term("DirectContainer");
		INDIRECT_CONTAINER=term("IndirectContainer");
		// Initialize individuals
		MEMBER_SUBJECT=term("MemberSubject");
		PREFER_CONTAINMENT=term("PreferContainment");
		PREFER_EMPTY_CONTAINER=term("PreferEmptyContainer");
		PREFER_MINIMAL_CONTAINER=term("PreferMinimalContainer");
		PREFER_MEMBERSHIP=term("PreferMembership");
		VOCABULARY.initialize();
	}

	private LDP() {
		super(ImmutableTerm.class,LDP.NAMESPACE,LDP.NS_PREFIX);
	}

	/**
	 * Create a term
	 *
	 * @param localPart
	 *            The local part of the term's URI
	 * @return A {@code LDPTerm} instance that represents the term.
	 */
	private static Term term(String localPart) {
		return new ImmutableTerm(VOCABULARY,localPart);
	}

	/**
	 * Retrieve the LDP vocabulary instance.
	 * @return Return the unique instance of the vocabulary.
	 */
	public static LDP getInstance() {
		return LDP.VOCABULARY;
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
	public static Term valueOf(String term) {
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
	public static Term valueOf(QName term) {
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
	public static Term valueOf(URI term) {
		return getInstance().fromValue(term);
	}

}