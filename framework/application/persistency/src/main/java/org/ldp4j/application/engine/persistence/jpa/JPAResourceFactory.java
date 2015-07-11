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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.engine.resource.Resource;
import org.ldp4j.application.engine.resource.ResourceFactory;
import org.ldp4j.application.engine.resource.ResourceId;
import org.ldp4j.application.engine.template.BasicContainerTemplate;
import org.ldp4j.application.engine.template.ContainerTemplate;
import org.ldp4j.application.engine.template.DirectContainerTemplate;
import org.ldp4j.application.engine.template.IndirectContainerTemplate;
import org.ldp4j.application.engine.template.MembershipAwareContainerTemplate;
import org.ldp4j.application.engine.template.ResourceTemplate;
import org.ldp4j.application.engine.template.TemplateLibrary;
import org.ldp4j.application.engine.template.TemplateVisitor;

final class JPAResourceFactory implements ResourceFactory {

	private final class RootResourceCreator implements TemplateVisitor {

		private final ResourceId id;
		private JPAResource resource;

		private RootResourceCreator(ResourceId id) {
			this.id = id;
		}

		private JPAResource createdResource() {
			return this.resource;
		}

		private void createResource(JPAResource resource) {
			this.resource=resource;
			this.resource.setTemplateLibrary(JPAResourceFactory.this.templateLibrary);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visitResourceTemplate(ResourceTemplate template) {
			createResource(new JPAResource(id,null));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visitContainerTemplate(ContainerTemplate template) {
			createResource(new JPAContainer(id,null));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visitBasicContainerTemplate(BasicContainerTemplate template) {
			visitContainerTemplate(template);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
			visitContainerTemplate(template);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visitDirectContainerTemplate(DirectContainerTemplate template) {
			visitContainerTemplate(template);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
			visitContainerTemplate(template);
		}

	}

	private final JPAResourceRepository resourceRepository;

	private TemplateLibrary templateLibrary;

	JPAResourceFactory(JPAResourceRepository resourceRepository) {
		this.resourceRepository = resourceRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void useTemplates(TemplateLibrary templateLibrary) {
		this.templateLibrary=templateLibrary;
		this.resourceRepository.setTemplateLibrary(templateLibrary);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Resource createResource(ResourceTemplate template, Name<?> name) {
		checkNotNull(name,"Resource name cannot be null");
		checkArgument(this.templateLibrary.contains(template),"Unknown template "+template);
		RootResourceCreator creator=
			new RootResourceCreator(
				ResourceId.createId(name,template));
		template.accept(creator);
		return creator.createdResource();
	}

}