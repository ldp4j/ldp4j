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

public abstract class Literal<T> extends Node {

	private final T value;

	Literal(T value) {
		if(value==null) {
			throw new IllegalArgumentException("Object 'value' cannot be null");
		}
		this.value = value;
	}
	
	private int compareLiterals(Literal<?> o1, Literal<?> o2) {
		boolean equals = o1.getValue().equals(o2.getValue());
		if(equals) {
			return 0;
		}
		return o1.getValue().toString().compareTo(o2.getValue().toString());
	}

	public T getValue() {
		return value;
	}

	@Override
	public int compareTo(Node o) {
		if(o==null) {
			return 1;
		}
		if(o==this) {
			return 0;
		}
		if(o instanceof URIRef || o instanceof BlankNode) {
			return 1;
		}
		return compareLiterals(this,(Literal<?>)o);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if(requiresQuotation()) {
			builder.append("\"").append(value).append("\"");
		} else {
			builder.append(value);
		}
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 2;
		result = prime * result +  value.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Literal)) {
			return false;
		}
		Literal<?> other = (Literal<?>) obj;
		return value.equals(other.value);
	}

	protected final boolean requiresQuotation() {
		return !((value instanceof Integer) || (value instanceof Double) || (value instanceof Boolean));
	}

}