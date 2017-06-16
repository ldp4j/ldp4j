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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.2
 *   Bundle      : ldp4j-application-kernel-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.template;

import java.net.URI;
import java.util.Objects;

import org.ldp4j.application.ext.ContainerHandler;
import org.ldp4j.application.ext.annotations.MembershipRelation;
import org.ldp4j.application.kernel.template.MembershipAwareContainerTemplate;
import org.ldp4j.application.kernel.template.TemplateVisitor;
import org.ldp4j.application.vocabulary.LDP;

import com.google.common.base.MoreObjects.ToStringHelper;

class MutableMembershipAwareContainerTemplate extends MutableContainerTemplate implements MembershipAwareContainerTemplate {

	private static final MembershipRelation DEFAULT_MEMBERSHIP_RELATION = MembershipRelation.HAS_MEMBER;
	private static final URI DEFAULT_MEMBERSHIP_PREDICATE = LDP.MEMBER.as(URI.class);

	private URI membershipPredicate=DEFAULT_MEMBERSHIP_PREDICATE;
	private MembershipRelation membershipRelation=DEFAULT_MEMBERSHIP_RELATION;

	MutableMembershipAwareContainerTemplate(String id, Class<? extends ContainerHandler> handlerClass) {
		super(id, handlerClass);
	}

	void setMembershipPredicate(URI membershipPredicate) {
		if(membershipPredicate==null) {
			this.membershipPredicate=DEFAULT_MEMBERSHIP_PREDICATE;
		} else {
			this.membershipPredicate=membershipPredicate;
		}
	}

	void setMembershipRelation(MembershipRelation membershipRelation) {
		if(membershipRelation==null) {
			this.membershipRelation=DEFAULT_MEMBERSHIP_RELATION;
		} else {
			this.membershipRelation=membershipRelation;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(TemplateVisitor visitor) {
		visitor.visitMembershipAwareContainerTemplate(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI membershipPredicate() {
		return this.membershipPredicate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MembershipRelation membershipRelation() {
		return this.membershipRelation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return
			Objects.
				hash(super.hashCode(),this.membershipPredicate,this.membershipRelation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=super.equals(obj);
		if(result && this.getClass()==obj.getClass()) {
			MutableMembershipAwareContainerTemplate that = (MutableMembershipAwareContainerTemplate) obj;
			result=
				Objects.equals(this.membershipPredicate, that.membershipPredicate) &&
				Objects.equals(this.membershipRelation, that.membershipRelation);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ToStringHelper stringHelper() {
		return
			super.
				stringHelper().
					add("membershipPredicate",this.membershipPredicate).
					add("membershipRelation", this.membershipRelation);
	}

}
