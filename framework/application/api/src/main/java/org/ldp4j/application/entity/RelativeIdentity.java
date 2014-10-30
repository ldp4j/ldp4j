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

public final class RelativeIdentity<T> extends Identity {

	private ManagedIdentity<T> parent;
	private URI path;

	private RelativeIdentity(URI identifier, ManagedIdentity<T> parent, URI path) {
		super(identifier);
		this.parent = parent;
		this.path = path;
	}

	public ManagedIdentity<T> parent() {
		return this.parent;
	}

	public URI path() {
		return this.path;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(IdentityVisitor visitor) {
		visitor.visitRelative(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return super.hashCode()+Objects.hashCode(this.parent,this.path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=super.equals(obj);
		if(result && obj instanceof RelativeIdentity) {
			RelativeIdentity<?> that=(RelativeIdentity<?>) obj;
			result=
				Objects.equal(this.parent,that.parent) &&
				Objects.equal(this.path,that.path);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void toString(StringHelper helper) {
		helper.
			add("parent",this.parent).
			add("path",this.path);
	}

	static <T,V> RelativeIdentity<T> create(Class<T> owner, V nativeId, URI path) {
		return create(Key.create(owner, nativeId),path);
	}

	static <T> RelativeIdentity<T> create(Key<T> key, URI path) {
		URI identifier=
			IdentifierUtil.
				createRelativeIdentifier(key,path);
		return new RelativeIdentity<T>(identifier,ManagedIdentity.create(key),path);
	}

}
