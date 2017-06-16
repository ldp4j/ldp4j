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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.spi.ModelFactory;
import org.ldp4j.application.kernel.template.ContainerTemplate;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.SimpleTemplateVisitor;
import org.ldp4j.application.kernel.template.TemplateLibrary;

final class JPAModelFactory implements ModelFactory {

	private final class RootResourceCreator extends SimpleTemplateVisitor {

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
			this.resource.setTemplateLibrary(JPAModelFactory.this.templateLibrary);
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

	}

	private final JPAResourceRepository resourceRepository;

	private TemplateLibrary templateLibrary;

	JPAModelFactory(JPAResourceRepository resourceRepository) {
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

	@Override
	public Endpoint createEndpoint(String path, Resource resource, Date created, EntityTag entityTag) {
		checkNotNull(resource,"Resource cannot be null");
		return JPAEndpoint.create(path, resource.id(), created, entityTag);
	}

}