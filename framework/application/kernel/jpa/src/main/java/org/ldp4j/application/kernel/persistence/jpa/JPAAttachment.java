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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-jpa:0.2.2
 *   Bundle      : ldp4j-application-kernel-jpa-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.persistence.jpa;

import java.util.Objects;

import org.ldp4j.application.kernel.resource.Attachment;
import org.ldp4j.application.kernel.resource.ResourceId;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

final class JPAAttachment implements Attachment {

	/**
	 * Persistent key required by JPA
	 */
	private long primaryKey;

	/**
	 * Not final to enable its usage in JPA
	 */
	private long version;

	/**
	 * Not final to enable its usage in JPA
	 */
	private String id;

	/**
	 * Surrogate object to guarantee DB portability
	 */
	private Key resourceId;

	JPAAttachment() {
		// JPA Friendly
	}

	JPAAttachment(String id) {
		this();
		this.id=id;
		this.version=0;
	}

	private Key key() {
		return Optional.fromNullable(this.resourceId).or(Key.NULL);
	}

	void bind(ResourceId resourceId) {
		this.resourceId=Key.newInstance(resourceId);
	}

	void unbind() {
		this.resourceId=null;
		this.version++;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String id() {
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResourceId resourceId() {
		return key().resourceId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long version() {
		return this.version;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.primaryKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof JPAAttachment) {
			JPAAttachment that=(JPAAttachment)obj;
			result=this.primaryKey==that.primaryKey;
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("primaryKey",this.primaryKey).
					add("id", this.id).
					add("resourceId", this.resourceId).
					add("version", this.version).
					toString();
	}

}