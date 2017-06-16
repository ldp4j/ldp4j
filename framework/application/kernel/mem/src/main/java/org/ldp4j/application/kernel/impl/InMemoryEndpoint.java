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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-mem:0.2.2
 *   Bundle      : ldp4j-application-kernel-mem-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Date;

import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.resource.ResourceId;

import com.google.common.base.MoreObjects;

final class InMemoryEndpoint implements Endpoint {

	private final String path;
	private final Date created;

	private ResourceId resourceId;
	private EntityTag entityTag;
	private Date lastModified;
	private Date deleted;

	private InMemoryEndpoint(String path, ResourceId resourceId, Date created, EntityTag entityTag) {
		this.path = path;
		this.resourceId = resourceId;
		this.created = created;
		this.entityTag = entityTag;
		this.lastModified = created;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String path() {
		return this.path;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date created() {
		return this.created;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date deleted() {
		return this.deleted;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityTag entityTag() {
		return this.entityTag;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date lastModified() {
		return this.lastModified;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResourceId resourceId() {
		return this.resourceId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modify(EntityTag newEntityTag, Date newLastModified) {
		checkState(this.deleted==null,"Endpoint is already deleted");
		this.entityTag=newEntityTag;
		this.lastModified=new Date(newLastModified.getTime());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Date deleted) {
		checkState(this.deleted==null,"Endpoint is already deleted");
		this.deleted=deleted;
		this.resourceId=null;
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
					add("path",this.path).
					add("created",this.created).
					add("deleted",this.deleted).
					add("entityTag",this.entityTag).
					add("lastModified",this.lastModified.getTime()).
					add("resourceId",this.resourceId).
					toString();
	}

	static InMemoryEndpoint create(String path, ResourceId resourceId, Date created, EntityTag entityTag) {
		checkNotNull(path,"Endpoint path cannot be null");
		checkNotNull(resourceId,"Endpoint identifier cannot be null");
		checkNotNull(created,"Endpoint creation date cannot be null");
		checkNotNull(entityTag,"Endpoint entity tag cannot be null");
		return new InMemoryEndpoint(path, resourceId, created, entityTag);
	}

}
