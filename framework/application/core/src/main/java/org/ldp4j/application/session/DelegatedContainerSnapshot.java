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
package org.ldp4j.application.session;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;

import org.ldp4j.application.resource.Container;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.template.ContainerTemplate;
import org.ldp4j.application.data.Name;


final class DelegatedContainerSnapshot extends DelegatedResourceSnapshot implements ContainerSnapshot {

	protected DelegatedContainerSnapshot(ResourceId resourceId) {
		super(resourceId);
	}
	
	@Override
	public void accept(SnapshotVisitor visitor) {
		visitor.visitContainerSnapshot(this);
	}
	
	void accept(DelegatedSnapshotVisitor visitor) {
		visitor.visitDelegatedContainerSnapshot(this);
	}

	Container delegate() {
		return (Container)super.delegate();
	}

	ContainerTemplate template() {
		return (ContainerTemplate)super.template();
	}

	@Override
	public Set<? extends DelegatedResourceSnapshot> members() {
		return super.persistencyState().members(this);
	}

	@Override
	public boolean hasMember(ResourceSnapshot resource) {
		checkArgument(resource instanceof DelegatedResourceSnapshot,"Unsupported snapshot type");
		return super.persistencyState().hasMember((DelegatedResourceSnapshot)resource,this);
	}

	@Override
	public ResourceSnapshot addMember(final Name<?> resourceName) {
		return super.persistencyState().addMember(resourceName,this);
	}

	@Override
	public boolean removeMember(ResourceSnapshot member) {
		checkArgument(member instanceof DelegatedResourceSnapshot,"Unsupported snapshot type");
		return super.persistencyState().removeMember((DelegatedResourceSnapshot)member,this);
	}
	
	boolean softRemoveMember(ResourceSnapshot member) {
		checkArgument(member instanceof DelegatedResourceSnapshot,"Unsupported snapshot type");
		return super.persistencyState().softRemoveMember((DelegatedResourceSnapshot)member,this);
	}

	@Override
	public String toString() {
		return 
			stringHelper().
				add("members", members()).
				toString();
	}

}
