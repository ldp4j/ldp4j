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
package org.ldp4j.application.impl;

import java.util.Date;

import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.resource.ResourceId;

import com.google.common.base.Objects;

final class InMemoryEndpoint implements Endpoint {

	private final long id;
	private final ResourceId resourceId;
	private final String path;


	private EntityTag entityTag;
	private Date lastModified;

	InMemoryEndpoint(long id, String path, ResourceId resourceId, EntityTag entityTag, Date lastModified) {
		this.id = id;
		this.path = path;
		this.resourceId = resourceId;
		this.entityTag = entityTag;
		this.lastModified = lastModified;
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
	public EntityTag entityTag() {
		return entityTag;
	}

	@Override
	public Date lastModified() {
		return lastModified;
	}

	@Override
	public ResourceId resourceId() {
		return resourceId;
	}

	@Override
	public void modify(EntityTag newEntityTag, Date newLastModified) {
		this.entityTag=newEntityTag;
		this.lastModified=new Date(newLastModified.getTime());
	}

	@Override
	public String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					omitNullValues().
					add("id",this.id).
					add("path",this.path).
					add("resourceId",this.resourceId).
					add("entityTag",this.entityTag).
					add("lastModified",this.lastModified.getTime()).
					toString();
	}

}