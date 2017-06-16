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
 * A class for representing a vocabularies. The interface includes facilities
 * for discovering the terms defined in the vocabulary. methods for retrieving
 * the representation of the particular term in different formats.
 *
 * @version 1.0
 * @since 1.0.0
 * @author Miguel Esteban Guti&eacute;rrez
 * @see Term
 */
public interface Vocabulary extends Iterable<Term>, Serializable {

	/**
	 * Get the namespace of the vocabulary.
	 *
	 * @return the namespace of the vocabulary. It should never be {@code null}
	 *         nor empty.
	 */
	String getNamespace();

	/**
	 * Get the preferred prefix for the vocabulary namespace.
	 *
	 * @return the preferred prefix for the namespace. It should never be
	 *         {@code null} nor empty.
	 */
	String getPreferredPrefix();

	/**
	 * Get the number of terms defined in the vocabulary.
	 *
	 * @return the number of terms defined or {@code 0} if no term is defined.
	 */
	int size();

	/**
	 * Get the terms defined in the vocabulary.
	 *
	 * @return the terms defined for the vocabulary or an empty array if no
	 *         terms are defined.
	 */
	Term[] terms();

	/**
	 * Returns the vocabulary term with the specified name. The name must match
	 * exactly an identifier used to declare the term in this vocabulary.
	 *
	 * @param name
	 *            the name of the term to return
	 * @return the term of the vocabulary type with the specified name
	 * @throws IllegalArgumentException
	 *             if {@code name} is null.
	 */
	Term fromName(String name);

	/**
	 * Returns the vocabulary term specified in a given position.
	 *
	 * @param ordinal
	 *            the position in which the desired term was declared.
	 * @return the term that was declared in the specified position.
	 * @throws IndexOutOfBoundsException
	 *             if the specified ordinal is lower than {@code 0} or greater
	 *             or equal than {@code #size()}.
	 */
	Term fromOrdinal(int ordinal);

	/**
	 * Returns the vocabulary term that matches the specified value.
	 *
	 * @param <V> the type of value to be processed
	 * @param value
	 *            a value to determine the desired term.
	 * @return the term that upon transformation matches the specified
	 *         {@code value}.
	 * @throws UnsupportedOperationException
	 *             if the terms of the vocabulary cannot be transformed to
	 *             instances a compatible class.
	 * @see Term#as(Class)
	 */
	<V> Term fromValue(V value);

}
