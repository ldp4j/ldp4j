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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.resources.impl;

import java.net.URI;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.domain.LDP;
import org.ldp4j.application.domain.RDF;
import org.ldp4j.server.resources.MembershipAwareContainer;
import org.ldp4j.server.resources.MembershipRelation;
import org.ldp4j.server.resources.ResourceType;


public abstract class MembershipAwareContainerImpl extends ContainerImpl implements MembershipAwareContainer {
	
	private Name<?> membershipTarget;
	private URI membershipPredicate=LDP.MEMBER.as(URI.class);
	private MembershipRelation membershipRelation=MembershipRelation.HAS_MEMBER;
	
	protected MembershipAwareContainerImpl(ResourceType type) {
		super(type);
	}

	protected void fillInMetadata(Individual<?, ?> individual, Context ctx) {
		super.fillInMetadata(individual, ctx);
		individual.
			addValue(
				ctx.property(RDF.TYPE), 
				ctx.reference(type().term()));
		individual.
			addValue(
				membershipRelation.toURI(), 
				ctx.value(membershipPredicate));
	}

	@Override
	public URI membershipPredicate() {
		return membershipPredicate;
	}

	void setMembershipPredicate(URI predicate) {
		if(predicate!=null) {
			this.membershipPredicate = predicate;
		} else {
			this.membershipPredicate = LDP.MEMBER.as(URI.class);
		}
	}
	
	@Override
	public MembershipRelation membershipRelation() {
		return membershipRelation;
	}

	void setMembershipRelation(MembershipRelation relation) {
		if(relation!=null) {
			this.membershipRelation= relation;
		} else {
			this.membershipRelation = MembershipRelation.HAS_MEMBER;
		}
	}


	@Override
	public Name<?> membershipTarget() {
		return membershipTarget;
	}

	void setMembershipTarget(Name<?> membershipTarget) {
		this.membershipTarget = membershipTarget;
	}
	

}