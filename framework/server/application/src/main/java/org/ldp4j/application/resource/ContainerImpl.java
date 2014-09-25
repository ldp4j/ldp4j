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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-application:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-application-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.resource;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.ldp4j.application.template.ContainerTemplate;

final class ContainerImpl extends ResourceImpl implements Container {

	private final Set<ResourceId> members;

	protected ContainerImpl(ResourceId id, ResourceId parentId) {
		super(id,parentId);
		this.members=new LinkedHashSet<ResourceId>();
	}
	
	protected ContainerImpl(ResourceId id) {
		this(id,null);
	}

	@Override
	public void accept(ResourceVisitor visitor) {
		visitor.visitContainer(this);
	}

	ContainerTemplate template() {
		return (ContainerTemplate)super.getTemplate(id());
	}

	@Override
	public Set<ResourceId> memberIds() {
		return Collections.unmodifiableSet(new LinkedHashSet<ResourceId>(this.members));
	}

	@Override
	public Resource addMember(ResourceId resourceName) {
		checkNotNull(resourceName,"Member resource name cannot be null");
		checkState(!this.members.contains(resourceName),"A resource with id '%s' is already a member of the container",resourceName);
		ResourceImpl newResource=createChild(resourceName,template().memberTemplate());
		this.members.add(newResource.id());
		return newResource;
	}

	@Override
	public boolean hasMember(ResourceId resource) {
		return this.members.contains(resource);
	}

	@Override
	public boolean removeMember(ResourceId resourceId) {
		return this.members.remove(resourceId);
	}

	@Override
	public String toString() {
		return 
			stringHelper().
				add("members", memberIds()).
				toString();
	}

}