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

import java.io.Serializable;
import java.net.URI;

import javax.persistence.*;

import org.ldp4j.application.ext.annotations.MembershipRelation;
import org.ldp4j.application.persistence.domain.ContainerTemplate;

import com.google.common.base.Objects.ToStringHelper;

@MappedSuperclass
public abstract class MembershipAwareContainerTemplate extends ContainerTemplate implements Serializable {

	private static final long serialVersionUID = 1L;

	private URI membershipPredicate;
	private MembershipRelation membershipRelation;

	public MembershipAwareContainerTemplate() {
		super();
	}

	@Column(nullable=false,updatable=false)
	public URI getMembershipPredicate() {
		return membershipPredicate;
	}

	public void setMembershipPredicate(URI membershipPredicate) {
		this.membershipPredicate = membershipPredicate;
	}

	@Column(nullable=false,updatable=false)
	@Enumerated(EnumType.STRING)
	public MembershipRelation getMembershipRelation() {
		return membershipRelation;
	}

	public void setMembershipRelation(MembershipRelation membershipRelation) {
		this.membershipRelation = membershipRelation;
	}

	protected void toString(ToStringHelper helper) {
		super.toString(helper);
		helper.
			add("membershipPredicate",this.membershipPredicate).
			add("membershipRelation",this.membershipRelation);
	}

}