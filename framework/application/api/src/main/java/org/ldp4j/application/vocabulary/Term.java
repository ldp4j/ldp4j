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

import java.io.Serializable;


/**
 * The class represents the terms of a vocabulary. The class includes different
 * mechanisms for identifying the term as well as utility methods for
 * transforming the term into different types.
 *
 * @version 1.0
 * @since 1.0.0
 * @author Miguel Esteban Guti&eacute;rrez
 * @see Vocabulary
 */
public interface Term extends Comparable<Term>, Serializable {

	/**
	 * Returns the ordinal of this term (its position in its vocabulary
	 * declaration, where the initial constant is assigned an ordinal of zero).
	 *
	 * @return the ordinal of this term
	 */
	int ordinal();

	/**
	 * Returns the name of this term constant, exactly as declared in its
	 * vocabulary declaration.
	 *
	 * <b>Most programmers should use the {@link Object#toString} method in preference
	 * to this one, as the toString method may return a more user-friendly
	 * name.</b> This method is designed primarily for use in specialized
	 * situations where correctness depends on getting the exact name, which
	 * will not vary from release to release.
	 *
	 * @return the name of this term
	 */
	String name();

	/**
	 * Get the simple name of the entity of the vocabulary identified by this
	 * term constant.
	 *
	 * @return the entity name
	 */
	String entityName();

	/**
	 * Get the qualified name of the entity of the vocabulary identified by
	 * this term constant.
	 *
	 * @return the qualified name
	 */
	String qualifiedEntityName();

	/**
	 * Returns the Vocabulary object corresponding to this term constant's
	 * vocabulary type. Two term constants e1 and e2 are of the same vocabulary
	 * type if and only if
	 * {@code e1.getDeclaringVocabulary() == e2.getDeclaringVocabulary()}.
	 *
	 * @return the Vocabulary object corresponding to this term constant's
	 *         vocabulary type
	 */
	Vocabulary getDeclaringVocabulary();

	/**
	 * Transform the term to an instance of the specified type.
	 *
	 * @param <T> the type of object to be returned
	 * @param type
	 *            The type to which the term is to be transformed to.
	 * @return An instance of the specified type that represents this term.
	 * @throws UnsupportedOperationException
	 *             if the term cannot be transformed to the specified type.
	 */
	<T> T as(Class<? extends T> type);

}