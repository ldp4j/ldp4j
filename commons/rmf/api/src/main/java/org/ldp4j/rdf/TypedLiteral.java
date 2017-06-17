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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-api:0.2.2
 *   Bundle      : rmf-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf;



public final class TypedLiteral<T> extends Literal<T> {

	private final Datatype type;

	TypedLiteral(T value, Datatype type) {
		super(value);
		if(type==null) {
			throw new IllegalArgumentException("Object 'type' cannot be null");
		}
		this.type = type;
	}

	public Datatype getType() {
		return type;
	}

	@Override
	public String toString() {
		String literal=super.toString();
		if(requiresQuotation()) {
			literal=literal.concat(String.format("^^<%s>",type.toURI()));
		} else {
			literal=String.format("\"%s\"^^<%s>",literal,type.toURI());
		}
		return literal;
	}

	@Override
	public int hashCode() {
		final int prime = 19;
		int result = super.hashCode();
		result = prime * result + type.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof TypedLiteral)) {
			return false;
		}
		TypedLiteral<?> other = (TypedLiteral<?>) obj;
		return type.equals(other.type);
	}

	@Override
	public <S> S accept(NodeVisitor<S> visitor, S defaultValue) {
		return visitor.visitTypedLiteral(this, defaultValue);
	}
	
}