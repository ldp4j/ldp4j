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
package org.ldp4j.application.ext.annotations;

import java.net.URI;

import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.application.vocabulary.Term;

/**
 * Used for defining the membership relation used by direct and indirect
 * container templates
 */
public enum MembershipRelation {

	/**
	 * Use the {@code http://www.w3.org/ns/ldp#hasMemberRelation} relation.
	 */
	HAS_MEMBER(LDP.HAS_MEMBER_RELATION),

	/**
	 * Use the {@code http://www.w3.org/ns/ldp#isMemberOfRelation} relation.
	 */
	IS_MEMBER_OF(LDP.IS_MEMBER_OF_RELATION),
	;

	private final Term term;

	private MembershipRelation(Term term) {
		this.term = term;
	}

	/**
	 * Return the {@code Term} from the LDP vocabulary that represents the
	 * membership relation.
	 *
	 * @return the {@code Term} from the LDP vocabulary that represents the
	 *         membership relation.
	 */
	public Term term() {
		return term;
	}

	/**
	 * Return the {@code URI} that represents the membership relation.
	 *
	 * @return the {@code URI} that represents the membership relation.
	 */
	public URI toURI() {
		return term.as(URI.class);
	}

}