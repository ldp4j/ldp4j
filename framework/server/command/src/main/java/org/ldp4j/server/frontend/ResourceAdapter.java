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
package org.ldp4j.server.frontend;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.template.BasicContainerTemplate;
import org.ldp4j.application.template.ContainerTemplate;
import org.ldp4j.application.template.DirectContainerTemplate;
import org.ldp4j.application.template.IndirectContainerTemplate;
import org.ldp4j.application.template.MembershipAwareContainerTemplate;
import org.ldp4j.application.template.ResourceTemplate;
import org.ldp4j.application.template.TemplateVisitor;
import org.ldp4j.server.resources.MembershipRelation;
import org.ldp4j.server.resources.impl.ResourceBuilder;
import org.ldp4j.server.resources.impl.ResourceBuilder.ContainerBuilder;
import org.ldp4j.server.resources.impl.ResourceBuilder.SimpleResourceBuilder;

final class ResourceAdapter implements TemplateVisitor {
	private final Endpoint endpoint;
	private org.ldp4j.server.resources.Resource result;
	private final ApplicationContext applicationContext;
	private Resource resource;


	ResourceAdapter(ApplicationContext applicationContext, Endpoint endpoint) {
		this.applicationContext = applicationContext;
		this.endpoint = endpoint;
	}
	
	public org.ldp4j.server.resources.Resource getResource() {
		if(this.result==null) {
			this.resource=this.applicationContext.resolveResource(endpoint);
			this.applicationContext.resourceTemplate(this.resource).accept(this);
		}
		return result;
	}

	private SimpleResourceBuilder newResource() {
		SimpleResourceBuilder builder = ResourceBuilder.newResource(resource.id().name(), resource.id().templateId());
		populateEndpointMetadate(builder);
		return builder;
	}

	private ContainerBuilder newContainer() {
		ContainerBuilder builder = ResourceBuilder.newContainer(resource.id().name(), resource.id().templateId());
		populateEndpointMetadate(builder);
		return builder;
	}

	private void populateEndpointMetadate(ResourceBuilder<?> builder) {
		builder.
			withEntityTag(new javax.ws.rs.core.EntityTag(endpoint.entityTag().getValue())).
			withLastModified(endpoint.lastModified());
	}

	private MembershipRelation membershipRelation(org.ldp4j.application.ext.annotations.MembershipRelation membershipRelation) {
		switch(membershipRelation) {
			case HAS_MEMBER:
				return MembershipRelation.HAS_MEMBER;
			case IS_MEMBER_OF:
				return MembershipRelation.IS_MEMBER_OF;
			default:
				throw new AssertionError("Unexpected membership relation '"+membershipRelation+"'");
		}
	}

	@Override
	public void visitContainerTemplate(ContainerTemplate template) {
		throw new IllegalStateException("Raw container resources are not allowed");
	}

	@Override
	public void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
		throw new IllegalStateException("Raw membership aware container resources are not allowed");
	}

	@Override
	public void visitResourceTemplate(ResourceTemplate template) {
		this.result=newResource().build();
	}

	@Override
	public void visitBasicContainerTemplate(BasicContainerTemplate template) {
		result=newContainer().build();
	}

	@Override
	public void visitDirectContainerTemplate(DirectContainerTemplate template) {
		this.result=
			newContainer().
				withMembershipPredicate(template.membershipPredicate()).
				withMembershipRelation(membershipRelation(template.membershipRelation())).
				build();
	}

	@Override
	public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
		this.result=
			newContainer().
				withMembershipPredicate(template.membershipPredicate()).
				withMembershipRelation(membershipRelation(template.membershipRelation())).
				withInsertedContentRelation(template.insertedContentRelation()).
				build();
	}
}