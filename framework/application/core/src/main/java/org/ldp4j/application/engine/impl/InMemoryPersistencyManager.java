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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.engine.constraints.ConstraintReportRepository;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.engine.endpoint.Endpoint;
import org.ldp4j.application.engine.endpoint.EndpointRepository;
import org.ldp4j.application.engine.lifecycle.LifecycleException;
import org.ldp4j.application.engine.lifecycle.Managed;
import org.ldp4j.application.engine.resource.Resource;
import org.ldp4j.application.engine.resource.ResourceId;
import org.ldp4j.application.engine.resource.ResourceRepository;
import org.ldp4j.application.engine.spi.PersistencyManager;
import org.ldp4j.application.engine.template.BasicContainerTemplate;
import org.ldp4j.application.engine.template.ContainerTemplate;
import org.ldp4j.application.engine.template.DirectContainerTemplate;
import org.ldp4j.application.engine.template.IndirectContainerTemplate;
import org.ldp4j.application.engine.template.MembershipAwareContainerTemplate;
import org.ldp4j.application.engine.template.ResourceTemplate;
import org.ldp4j.application.engine.template.TemplateLibrary;
import org.ldp4j.application.engine.template.TemplateVisitor;
import org.ldp4j.application.engine.transaction.TransactionManager;

final class InMemoryPersistencyManager implements PersistencyManager, Managed {

	private final class RootResourceCreator implements TemplateVisitor {

		private final ResourceId id;
		private InMemoryResource resource;

		private RootResourceCreator(ResourceId id) {
			this.id = id;
		}

		private InMemoryResource createdResource() {
			return this.resource;
		}

		private void createResource(InMemoryResource resource) {
			this.resource=resource;
			this.resource.setTemplateLibrary(InMemoryPersistencyManager.this.templateLibrary);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visitResourceTemplate(ResourceTemplate template) {
			createResource(new InMemoryResource(id,null));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visitContainerTemplate(ContainerTemplate template) {
			createResource(new InMemoryContainer(id,null));
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

	private final InMemoryResourceRepository resourceRepository;
	private final InMemoryEndpointRepository endpointRepository;
	private final InMemoryConstraintReportRepository constraintReportRepository;
	private final InMemoryTransactionManager transactionManager;

	private TemplateLibrary templateLibrary;

	InMemoryPersistencyManager() {
		this.resourceRepository=new InMemoryResourceRepository();
		this.endpointRepository=new InMemoryEndpointRepository();
		this.constraintReportRepository=new InMemoryConstraintReportRepository();
		this.transactionManager = new InMemoryTransactionManager();
	}

	ResourceRepository resourceRepository() {
		return this.resourceRepository;
	}

	EndpointRepository endpointRepository() {
		return this.endpointRepository;
	}

	ConstraintReportRepository constraintReportRepository() {
		return this.constraintReportRepository;
	}

	TransactionManager transactionManager() {
		return this.transactionManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void useTemplates(TemplateLibrary library) {
		this.templateLibrary=library;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Endpoint createEndpoint(Resource resource, String path, EntityTag entityTag, Date creationDate) {
		checkNotNull(resource,"Endpoint's resource cannot be null");
		checkNotNull(entityTag,"Endpoint's entity tag cannot be null");
		checkNotNull(creationDate,"Endpoint's last modified data cannot be null");
		return new InMemoryEndpoint(path,resource.id(),creationDate,entityTag);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws LifecycleException {
		this.resourceRepository.init();
		this.constraintReportRepository.init();
		this.endpointRepository.init();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown() throws LifecycleException {
		this.endpointRepository.shutdown();
		this.constraintReportRepository.shutdown();
		this.resourceRepository.shutdown();
	}

}