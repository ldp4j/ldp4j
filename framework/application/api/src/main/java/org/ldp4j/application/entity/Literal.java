/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.entity;

import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.*;

public final class Literal<T> implements Value {

	private final T value;

	private Literal(T value) {
		this.value = value;
	}

	public T value() {
		return this.value;
	}

	public <S> boolean isA(Class<? extends S> clazz) {
		if(clazz==null) {
			return false;
		}
		return clazz.isInstance(this.value);
	}

	public <S> ObjectAdapter<S> valueAs(Class<? extends S> targetClass) {
		return ObjectAdapter.create(targetClass,this.value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof Literal<?>) {
			Literal<?> that=(Literal<?>) obj;
			result=Objects.equal(this.value, that.value);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					add("value",value.toString()).
					toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(ValueVisitor visitor) {
		visitor.visitLiteral(this);
	}

	public static <T> Literal<T> create(T value) {
		checkNotNull(value,"Literal value cannot be null");
		return new Literal<T>(value);
	}

}