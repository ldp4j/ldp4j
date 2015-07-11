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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Map;
import java.util.Set;

import org.ldp4j.application.engine.resource.Container;
import org.ldp4j.application.engine.resource.Member;
import org.ldp4j.application.engine.resource.Resource;
import org.ldp4j.application.engine.resource.ResourceId;
import org.ldp4j.application.engine.resource.ResourceVisitor;
import org.ldp4j.application.engine.resource.Slug;
import org.ldp4j.application.engine.template.ContainerTemplate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

final class JPAContainer extends JPAResource implements Container {

	/**
	 * Not final to enable its usage in JPA
	 */
	private Map<ResourceId,JPAMember> members;

	/**
	 * Not final to enable its usage in JPA
	 */
	private long memberCounter;

	/**
	 * Not final to enable its usage in JPA
	 */
	private Map<String,JPASlug> slugs;

	@SuppressWarnings("unused")
	private JPAContainer() { // NOSONAR
		super();
		// JPA Friendly
	}

	JPAContainer(ResourceId id, ResourceId parentId) {
		super(id,parentId);
		this.members=Maps.newLinkedHashMap();
		this.slugs=Maps.newLinkedHashMap();
		this.memberCounter=0;
	}

	JPAContainer(ResourceId id) {
		this(id,null);
	}

	private Member createMember(JPAResource newResource) {
		long memberIndex=this.memberCounter++;
		JPAMember member = new JPAMember(id(), newResource.id(), memberIndex);
		this.members.put(member.memberId(), member);
		return member;
	}

	private JPAResource createMemberResource(ResourceId resourceId) {
		checkNotNull(resourceId,"Member resource identifier cannot be null");
		checkState(!this.members.containsKey(resourceId),"A resource with id '%s' is already a member of the container",resourceId);
		JPAResource newResource=createChild(resourceId,template().memberTemplate());
		return newResource;
	}

	private ContainerTemplate template() {
		return (ContainerTemplate)super.getTemplate(id());
	}

	@Override
	public void accept(ResourceVisitor visitor) {
		visitor.visitContainer(this);
	}

	@Override
	public Resource addMember(ResourceId resourceId) {
		JPAResource newResource = createMemberResource(resourceId);
		createMember(newResource);
		return newResource;
	}

	@Override
	public boolean hasMember(ResourceId resource) {
		return this.members.containsKey(resource);
	}

	@Override
	public Set<Member> members() {
		return ImmutableSet.<Member>copyOf(members.values());
	}

	@Override
	public Member findMember(ResourceId resourceId) {
		return this.members.get(resourceId);
	}

	@Override
	public boolean removeMember(Member member) {
		checkNotNull(member,"Member cannot be null");
		return this.members.remove(member.memberId())!=null;
	}

	@Override
	public Set<Slug> slugs() {
		return ImmutableSet.<Slug>copyOf(this.slugs.values());
	}

	@Override
	public Slug findSlug(String preferredPath) {
		return this.slugs.get(preferredPath);
	}

	@Override
	public Slug addSlug(String preferredPath) {
		checkNotNull(preferredPath,"Preferred path cannot be null");

		JPASlug tmp = JPASlug.create(preferredPath,this);

		JPASlug slug=this.slugs.get(tmp.preferredPath());
		if(slug==null){
			this.slugs.put(tmp.preferredPath(), tmp);
			return tmp;
		}

		synchronized(slug) {
			if(slug.version()<tmp.version()) {
				slug.setVersion(tmp.version());
				return slug;
			} else if(slug.version()==tmp.version()) {
				return slug;
			}
		}

		return addSlug(preferredPath+"_1");
	}

	@Override
	public String toString() {
		return
			stringHelper().
				add("memberCounter",this.memberCounter).
				add("members",this.members).
				add("slugs",this.slugs).
				toString();
	}

}