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

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.UUID;

import org.ldp4j.application.data.Name;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public final class Local<T> extends Identity {

	private final UUID dataSourceId;
	private final Name<T> name;

	private Local(URI identifier, UUID dataSourceId, Name<T> name) {
		super(identifier);
		this.dataSourceId = dataSourceId;
		this.name = name;
	}

	UUID dataSourceId() {
		return dataSourceId;
	}

	public Name<T> name() {
		return this.name;
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
		return super.hashCode()+Objects.hashCode(this.dataSourceId,this.name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=super.equals(obj);
		if(result && obj.getClass()==getClass()) {
			Local<?> that=(Local<?>)obj;
			result=
				Objects.equal(this.dataSourceId, that.dataSourceId) &&
				Objects.equal(this.name, that.name);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void toString(ToStringHelper helper) {
		helper.
			add("dataSourceId",this.dataSourceId).
			add("name",this.name);
	}

	static <T> Local<T> create(UUID dataSourceId, Name<T> name) {
		checkNotNull(dataSourceId,"Data source identifier cannot be null");
		checkNotNull(name,"Local identity name cannot be null");
		return new Local<T>(IdentifierUtil.createLocalIdentifier(name),dataSourceId,name);
	}

}