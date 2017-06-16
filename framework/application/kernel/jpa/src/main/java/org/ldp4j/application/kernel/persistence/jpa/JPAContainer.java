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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Map;
import java.util.Set;

import org.ldp4j.application.kernel.resource.Container;
import org.ldp4j.application.kernel.resource.Member;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.resource.ResourceVisitor;
import org.ldp4j.application.kernel.resource.Slug;
import org.ldp4j.application.kernel.template.ContainerTemplate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

final class JPAContainer extends JPAResource implements Container {

	/**
	 * Not final to enable its usage in JPA
	 */
	private Map<Key,JPAMember> members;

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

	private Key key() {
		return Key.newInstance(id());
	}

	private void createMember(Key memberId) {
		long memberIndex=this.memberCounter++;
		JPAMember member = new JPAMember(key(), memberId, memberIndex);
		this.members.put(memberId, member);
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
		checkNotNull(resourceId,"Member resource identifier cannot be null");
		Key memberId = Key.newInstance(resourceId);
		checkState(!this.members.containsKey(memberId),"A resource with id '%s' is already a member of the container",resourceId);
		JPAResource newResource=createChild(resourceId,template().memberTemplate());
		createMember(memberId);
		return newResource;
	}

	@Override
	public boolean hasMember(ResourceId resource) {
		return this.members.containsKey(Key.newInstance(resource));
	}

	@Override
	public Set<Member> members() {
		return ImmutableSet.<Member>copyOf(this.members.values());
	}

	@Override
	public Member findMember(ResourceId resourceId) {
		return this.members.get(Key.newInstance(resourceId));
	}

	@Override
	public boolean removeMember(Member member) {
		checkNotNull(member,"Member cannot be null");
		return this.members.remove(Key.newInstance(member.memberId()))!=null;
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