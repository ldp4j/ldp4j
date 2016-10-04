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
package org.ldp4j.application.data;


import java.io.Serializable;

import com.google.common.base.Objects;

final class ImmutableName<T extends Serializable> implements Name<T> {

	/**
	 *
	 */
	private static final long serialVersionUID = -9131252943262008686L;

	private final T id;

	private ImmutableName(T id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T id() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(NameVisitor visitor) {
		visitor.visit(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return id().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(id());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof ImmutableName) {
			result=Objects.equal(id, ((ImmutableName<?>) obj).id);
		}
		return result;
	}

	public static <T extends Serializable> Name<T> newGlobalName(T id) {
		return new ImmutableName<T>(id);
	}

	public static <T extends Serializable> Name<T> newLocalName(T id) {
		return new ImmutableName<T>(id);
	}

}