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
package org.ldp4j.application.engine.session;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ldp4j.application.engine.resource.Container;
import org.ldp4j.application.engine.resource.Member;
import org.ldp4j.application.engine.resource.Resource;
import org.ldp4j.application.engine.resource.ResourceId;
import org.ldp4j.application.engine.resource.ResourceVisitor;
import org.ldp4j.application.session.ResourceSnapshot;

import com.google.common.base.MoreObjects;

final class MemberCollection {

	private final Map<ResourceId,DelegatedResourceSnapshot> members;
	private final List<DelegatedResourceSnapshot> newMembers;

	private MemberCollection() {
		this.members=new LinkedHashMap<ResourceId, DelegatedResourceSnapshot>();
		this.newMembers=new LinkedList<DelegatedResourceSnapshot>();
	}

	private void registerMember(DelegatedResourceSnapshot snapshot) {
		checkState(!members.containsKey(snapshot.resourceId()),"A resource with id '%s' is already a member of the container",snapshot.resourceId());
		this.members.put(snapshot.resourceId(),snapshot);
	}

	Set<DelegatedResourceSnapshot> members() {
		return Collections.unmodifiableSet(new LinkedHashSet<DelegatedResourceSnapshot>(this.members.values()));
	}

	boolean hasMember(ResourceSnapshot resource) {
		return members.containsValue(resource);
	}

	void addMember(DelegatedResourceSnapshot snapshot) {
		checkNotNull(snapshot,"Member cannot be null");
		registerMember(snapshot);
		this.newMembers.add(snapshot);
	}

	boolean removeMember(ResourceSnapshot member) {
		if(member==null) return false;
		boolean result = this.members.containsValue(member);
		if(result) {
			this.newMembers.remove(member);
			// TODO: Check that this works :-S
			this.members.remove(member.name());
		}
		return result;
	}

	List<DelegatedResourceSnapshot> newMembers() {
		return Collections.unmodifiableList(this.newMembers);
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("members",this.members.keySet()).
					toString();
	}

	static MemberCollection newInstance() {
		return new MemberCollection();
	}

	static MemberCollection createFromResource(Resource resource, final DelegatedWriteSession session) {
		final MemberCollection memberRepository = new MemberCollection();
		resource.accept(
			new ResourceVisitor() {
				@Override
				public void visitResource(Resource resource) {
					// Nothing to do
				}
				@Override
				public void visitContainer(Container resource) {
					for(Member member:resource.members()) {
						memberRepository.registerMember(session.resolveResource(member.memberId()));
					}
				}
			}
		);
		return memberRepository;
	}

}