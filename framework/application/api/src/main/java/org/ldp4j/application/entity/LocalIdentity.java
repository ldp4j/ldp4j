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

import java.net.URI;

import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.*;

public final class LocalIdentity<T> extends Identity {

	private final T localId;

	private LocalIdentity(URI identifier, T localId) {
		super(identifier);
		this.localId = localId;
	}

	public T localId() {
		return this.localId;
	}
	public <S> ObjectAdapter<S> localIdas(Class<? extends S> targetClass) {
		checkNotNull(targetClass,"Target class cannot be null");
		return ObjectAdapter.create(targetClass, this.localId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(IdentityVisitor visitor) {
		visitor.visitLocal(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(),this.localId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=super.equals(obj);
		if(result && obj.getClass()==getClass()) {
			LocalIdentity<?> that=(LocalIdentity<?>)obj;
			result=Objects.equal(this.localId(), that.localId());
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void toString(StringHelper helper) {
		helper.
			add("localId",this.localId());
	}

	public static <T> LocalIdentity<T> create(T localId) {
		URI uri=IdentifierUtil.createLocalIdentifier(localId);
		return new LocalIdentity<T>(uri,localId);
	}

}