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
package org.ldp4j.application.persistence;

import java.net.URI;
import java.util.Date;

import javax.persistence.EntityManager;

import org.ldp4j.application.ext.annotations.MembershipRelation;
import org.ldp4j.application.persistence.domain.Application;
import org.ldp4j.application.persistence.domain.BasicContainerTemplate;
import org.ldp4j.application.persistence.domain.ContainerTemplate;
import org.ldp4j.application.persistence.domain.DirectContainerTemplate;
import org.ldp4j.application.persistence.domain.IndirectContainerTemplate;
import org.ldp4j.application.persistence.domain.MembershipAwareContainerTemplate;
import org.ldp4j.application.persistence.domain.RDFSourceTemplate;
import org.ldp4j.application.persistence.domain.Template;
import org.ldp4j.application.persistence.domain.TemplateAttachment;

public final class TemplateManager extends BaseManager {

	private final Application application;

	public TemplateManager(EntityManager manager, Date timestamp, Application application) {
		super(manager,timestamp);
		this.application = application;
	}

	private void validateMemberTemplate(Application application, Template memberTemplate) {
		if(!memberTemplate.getApplication().equals(application)) {
			throw new IllegalArgumentException("Member template must belong to the application where the container template is to be created");
		}
	}

	private void initializeTemplate(Application application, Class<?> handlerClass, String name, String description, Template template) {
		template.setApplication(application);
		template.setHandlerClassName(handlerClass.getName());
		template.setName(name);
		template.setDescription(description);
		application.getTemplates().add(template);
	}

	private void initializeContainer(Application application, Class<?> handlerClass, String name, String description, Template memberTemplate, ContainerTemplate template) {
		validateMemberTemplate(application, memberTemplate);
		initializeTemplate(application, handlerClass, name, description, template);
		template.setMemberTemplate(memberTemplate);
	}

	private void initializeMembershipAwareContainer(Application application, Class<?> handlerClass, String name, String description, Template memberTemplate, URI membershipPredicate, MembershipRelation membershipRelation, MembershipAwareContainerTemplate template) {
		initializeContainer(application, handlerClass, name, description, memberTemplate, template);
		template.setMembershipPredicate(membershipPredicate);
		template.setMembershipRelation(membershipRelation);
	}

	// TODO: Add proper exception handling
	public RDFSourceTemplate createRDFSourceTemplate(Class<?> handlerClass, String name, String description) {
		RDFSourceTemplate template=new RDFSourceTemplate();
		initializeTemplate(this.application, handlerClass, name, description, template);
		getManager().persist(template);
		return template;
	}

	// TODO: Add proper exception handling
	public BasicContainerTemplate createBasicContainerTemplate(Class<?> handlerClass, String name, String description, Template memberTemplate) {
		BasicContainerTemplate template=new BasicContainerTemplate();
		initializeContainer(this.application, handlerClass, name, description, memberTemplate, template);
		getManager().persist(template);
		return template;
	}

	// TODO: Add proper exception handling
	public DirectContainerTemplate createDirectContainerTemplate(Class<?> handlerClass, String name, String description, Template memberTemplate, URI membershipPredicate, MembershipRelation membershipRelation) {
		DirectContainerTemplate template=new DirectContainerTemplate();
		initializeMembershipAwareContainer(this.application, handlerClass, name, description, memberTemplate, membershipPredicate, membershipRelation, template);
		getManager().persist(template);
		return template;
	}

	// TODO: Add proper exception handling
	public IndirectContainerTemplate createIndirectContainerTemplate(Class<?> handlerClass, String name, String description, Template memberTemplate, URI membershipPredicate, MembershipRelation membershipRelation, URI insertedContentRelation) {
		IndirectContainerTemplate template=new IndirectContainerTemplate();
		initializeMembershipAwareContainer(this.application, handlerClass, name, description, memberTemplate, membershipPredicate, membershipRelation, template);
		template.setInsertedContentRelation(insertedContentRelation);
		getManager().persist(template);
		return template;
	}

	// TODO: Add proper exception handling
	public void attachTemplate(Template ownerTemplate, Template attachedTemplate, String name, String path, String description) {
		if(!ownerTemplate.getApplication().equals(attachedTemplate.getApplication())) {
			throw new IllegalArgumentException("Owner and attached templates must belong to the same application");
		}
		TemplateAttachment attachment=new TemplateAttachment();
		attachment.setPath(path);
		attachment.setName(name);
		attachment.setDescription(description);
		attachment.setOwnerTemplate(ownerTemplate);
		attachment.setAttachedTemplate(attachedTemplate);
		ownerTemplate.getTemplateAttachments().add(attachment);
		getManager().persist(attachment);
	}

}
