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
package org.ldp4j.application.entity.core;

import java.net.URI;

import org.ldp4j.application.entity.IdentityVisitor;
import org.ldp4j.application.entity.Key;
import org.ldp4j.application.entity.ManagedIdentity;

import com.google.common.base.Objects;

final class BaseManagedIdentity<T> extends BaseIdentity implements ManagedIdentity<T> {

	private Key<T> key;

	private BaseManagedIdentity(URI identifier, Key<T> key) {
		super(identifier);
		this.key = key;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Key<T> key() {
		return this.key;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(IdentityVisitor visitor) {
		visitor.visitManaged(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return super.hashCode()+Objects.hashCode(this.key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=super.equals(obj);
		if(result && obj instanceof BaseManagedIdentity) {
			BaseManagedIdentity<?> that=(BaseManagedIdentity<?>) obj;
			result=Objects.equal(this.key, that.key);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void toString(StringHelper helper) {
		helper.add("key",this.key);
	}

	static <T,V> ManagedIdentity<T> create(Class<T> owner, V nativeId) {
		return create(Key.create(owner, nativeId));
	}

	static <T> ManagedIdentity<T> create(Key<T> key) {
		URI identifier=
			IdentifierUtil.
				createManagedIdentifier(key);
		return new BaseManagedIdentity<T>(identifier,key);
	}

}
