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

import javax.persistence.*;

import org.ldp4j.application.persistence.domain.Container;

import com.google.common.base.Objects.ToStringHelper;

/**
 * Entity implementation class for Entity: MembershipAwareContainer
 *
 */
@Entity
public class MembershipAwareContainer extends Container implements Serializable {

	private static final long serialVersionUID = 1L;

	private Resource targetResource;

	public MembershipAwareContainer() {
		super();
	}

	@Transient
	@Override
	public MembershipAwareContainerTemplate getContainerTemplate() {
		return (MembershipAwareContainerTemplate)super.getDefinedBy();
	}

	@Override
	public void setDefinedBy(Template template) {
		checkArgument(template instanceof MembershipAwareContainerTemplate,"Invalid template");
		super.setDefinedBy(template);
	}

	@ManyToOne
	@JoinColumn(name="target_resource_id")
	public Resource getTargetResource() {
		return targetResource;
	}

	public void setTargetResource(Resource targetResource) {
		this.targetResource = targetResource;
	}

	public void accept(ResourceVisitor visitor) {
		visitor.visitMembershipAwareContainer(this);
	}

	protected void toString(ToStringHelper helper) {
		super.toString(helper);
		helper.
			add("targetResource",DomainHelper.identifyEntity(this.targetResource));
	}

}
