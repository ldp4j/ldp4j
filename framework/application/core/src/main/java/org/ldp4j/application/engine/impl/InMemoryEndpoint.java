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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.impl;

import java.util.Date;

import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.engine.endpoint.Endpoint;
import org.ldp4j.application.engine.resource.ResourceId;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.*;

final class InMemoryEndpoint implements Endpoint {

	private final long id;
	private final String path;
	private final Date created;

	private ResourceId resourceId;
	private EntityTag entityTag;
	private Date lastModified;
	private Date deleted;

	InMemoryEndpoint(long id, String path, ResourceId resourceId, Date created, EntityTag entityTag) {
		this.id = id;
		this.path = path;
		this.resourceId = resourceId;
		this.created = created;
		this.entityTag = entityTag;
		this.lastModified = created;
	}

	@Override
	public long id() {
		return id;
	}

	@Override
	public String path() {
		return path;
	}

	@Override
	public Date created() {
		return this.created;
	}

	@Override
	public Date deleted() {
		return this.deleted;
	}

	@Override
	public EntityTag entityTag() {
		return this.entityTag;
	}

	@Override
	public Date lastModified() {
		return this.lastModified;
	}

	@Override
	public ResourceId resourceId() {
		return this.resourceId;
	}

	@Override
	public void modify(EntityTag newEntityTag, Date newLastModified) {
		checkState(this.deleted==null,"Endpoint is already deleted");
		this.entityTag=newEntityTag;
		this.lastModified=new Date(newLastModified.getTime());
	}

	void delete(Date deleted) {
		checkState(this.deleted==null,"Endpoint is already deleted");
		this.deleted=deleted;
		this.resourceId=null;
	}

	@Override
	public String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					omitNullValues().
					add("id",this.id).
					add("path",this.path).
					add("created",this.created).
					add("deleted",this.deleted).
					add("entityTag",this.entityTag).
					add("lastModified",this.lastModified.getTime()).
					add("resourceId",this.resourceId).
					toString();
	}

}