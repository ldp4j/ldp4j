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

import java.util.Objects;


/**
 * A term implementation that enforces the creation of immutable terms defined
 * for an {@code AbstractImmutableVocabulary}, which behave as {@code Enum} constants.
 *
 * @version 1.0
 * @since 1.0.0
 * @author Miguel Esteban Guti&eacute;rrez
 * @see AbstractImmutableVocabulary
 */
public class ImmutableTerm implements Term {

	private static final long serialVersionUID = 6842492155071877731L;

	private static final int HASH_CODE_SALT = 19;
	private final int ordinal;
	private final String name;
	private final AbstractImmutableVocabulary<? extends ImmutableTerm> vocabulary;
	private final String entityName;

	/**
	 * Create a new term for a vocabulary and entity.
	 *
	 * @param vocabulary
	 *            the vocabulary to which the term will belong to.
	 * @param entityName
	 *            the entity name of the term.
	 * @throws NullPointerException
	 *             if any of the vocabulary is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the entity name is not valid.
	 */
	public ImmutableTerm(AbstractImmutableVocabulary<? extends ImmutableTerm> vocabulary, String entityName) {
		this.vocabulary = Objects.requireNonNull(vocabulary,"Vocabulary cannot be null");
		this.entityName = entityName;
		this.name = TermUtils.toTermName(entityName);
		this.ordinal = vocabulary.reserveTermName(this.name);
		vocabulary.registerTerm(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int ordinal() {
		return ordinal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String name() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String entityName() {
		return entityName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String qualifiedEntityName() {
		return vocabulary.getNamespace() + entityName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Vocabulary getDeclaringVocabulary() {
		return vocabulary;
	}

	/**
	 * {@inheritDoc} <br>
	 * This method may be overridden, though it typically isn't necessary or
	 * desirable. An term type should override this method when a more specific
	 * value types are supported.
	 */
	@Override
	public <V> V as(Class<? extends V> valueClass) {
		try {
			return TypeAdapter.adapt(this, valueClass);
		} catch (CannotAdaptClassesException e) {
			throw new UnsupportedOperationException("Class '"+getClass().getCanonicalName()+" cannot be transformed to '"+valueClass.getCanonicalName()+"'",e);
		}
	}

	/**
	 * Returns true if the specified object is equal to this term constant.
	 *
	 * @param other
	 *            the object to be compared for equality with this object.
	 * @return true if the specified object is equal to this term constant.
	 */
	@Override
	public final boolean equals(Object other) {
		return this == other;
	}

	/**
	 * Returns a hash code for this term constant.
	 *
	 * @return a hash code for this term constant.
	 */
	@Override
	public final int hashCode() {
		return super.hashCode()*HASH_CODE_SALT;
	}

	/**
	 * Throws CloneNotSupportedException. This guarantees that terms are never
	 * cloned, which is necessary to preserve their "singleton" status.
	 *
	 * @return (never returns)
	 */
	@Override
	protected final Object clone() throws CloneNotSupportedException { // NOSONAR
		throw new CloneNotSupportedException();
	}

	/**
	 * Returns the qualified entity name of this term constant.
	 * This method may be overridden, though it typically isn't necessary or
	 * desirable. An term type should override this method when a more
	 * "programmer-friendly" string form exists.
	 *
	 * @return the name of this term constant
	 */
	@Override
	public String toString() {
		return qualifiedEntityName();
	}

	/**
	 * Compares this term with the specified object for order. Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 *
	 * Term constants are only comparable to other term constants of the same
	 * vocabulary. The natural order implemented by this method is the order in
	 * which the constants are declared.
	 */
	@Override
	public int compareTo(Term other) {
		ImmutableTerm self = this;
		if(self.getDeclaringVocabulary() != other.getDeclaringVocabulary()) {
			throw new ClassCastException();
		}
		return self.ordinal - other.ordinal();
	}

}