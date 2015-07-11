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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-persistency:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-persistency-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.persistence.jpa;

import java.util.Objects;

import org.ldp4j.application.engine.resource.Attachment;
import org.ldp4j.application.engine.resource.ResourceId;

import com.google.common.base.MoreObjects;

final class JPAAttachment implements Attachment {

	/**
	 * Persistent key required by JPA
	 */
	private long primaryKey;

	/**
	 * Not final to enable its usage in JPA
	 */
	private AttachmentId attachmentId;

	/**
	 * Not final to enable its usage in JPA
	 */
	private long version;

	JPAAttachment() {
		// JPA-friendly
	}

	JPAAttachment(AttachmentId attachmentId, long version) {
		this();
		this.attachmentId = attachmentId;
		this.version = version;
	}

	AttachmentId attachmentId() {
		return this.attachmentId;
	}

	@Override
	public String id() {
		return this.attachmentId.id();
	}

	@Override
	public ResourceId resourceId() {
		return this.attachmentId.resourceId();
	}

	@Override
	public long version() {
		return this.version;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.primaryKey);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof JPAAttachment) {
			JPAAttachment that=(JPAAttachment)obj;
			result=this.primaryKey==that.primaryKey;
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("primaryKey",this.primaryKey).
					add("id", id()).
					add("resourceId", resourceId()).
					add("version", this.version).
					toString();
	}

}