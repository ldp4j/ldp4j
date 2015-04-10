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
package org.ldp4j.application.persistence.domain;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

@Entity
public class Container extends RDFSource implements Serializable {

	private static final long serialVersionUID = 1L;

	private AtomicLong memberCount;

	private List<Slug> slugs;

	private List<ResourceMembership> memberships;

	public Container() {
		super();
		this.memberCount=new AtomicLong();
		this.slugs=Lists.newArrayList();
		this.memberships=Lists.newArrayList();
	}

	@Transient
	@Override
	public ContainerTemplate getDefinedBy() {
		return (ContainerTemplate)super.getDefinedBy();
	}

	@Override
	public void setDefinedBy(Template template) {
		checkArgument(template instanceof ContainerTemplate,"Invalid template");
		super.setDefinedBy(template);
	}

	@Transient
	public ContainerTemplate getContainerTemplate() {
		return getDefinedBy();
	}

	public long getMemberCount() {
		return this.memberCount.get();
	}

	public void setMemberCount(long counter) {
		this.memberCount.set(counter);;
	}

	@OneToMany(mappedBy="container")
	public List<ResourceMembership> getMemberships() {
		return this.memberships;
	}

	public void setMemberships(List<ResourceMembership> memberships) {
		this.memberships = memberships;
	}

	public long nextMemberOrder() {
		return this.memberCount.getAndIncrement();
	}

	public String nextMemberPath(ResourceMembership membership, Slug slug) {
		long memberId=membership.getOrder();
		StringBuilder builder=new StringBuilder();
		builder.append(getEndpoint().getPath());
		if(!getEndpoint().getPath().endsWith("/")) {
			builder.append("/");
		}
		if(slug!=null) {
			builder.append(slug.nextSlugPath());
		} else {
			builder.append(memberId);
		}
		builder.append("/");
		return builder.toString();
	}

	@Transient
	public List<Resource> getMembers() {
		Builder<Resource> members=ImmutableList.<Resource>builder();
		for(ResourceMembership rm:this.memberships) {
			members.add(rm.getMember());
		}
		return members.build();
	}

	@OneToMany(mappedBy="container")
	public List<Slug> getSlugs() {
		return this.slugs;
	}

	public void setSlugs(List<Slug> slugs) {
		this.slugs=slugs;
	}

	public void accept(ResourceVisitor visitor) {
		visitor.visitContainer(this);
	}

	protected void toString(ToStringHelper helper) {
		super.toString(helper);
		helper.
			add("memberCount",this.memberCount).
			add("memberships",DomainHelper.identifyEntities(this.memberships));
	}

}
