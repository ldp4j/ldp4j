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
package org.ldp4j.application.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.application.spi.Service;
import org.ldp4j.application.spi.ServiceBuilder;
import org.ldp4j.application.template.BasicContainerTemplate;
import org.ldp4j.application.template.ContainerTemplate;
import org.ldp4j.application.template.DirectContainerTemplate;
import org.ldp4j.application.template.IndirectContainerTemplate;
import org.ldp4j.application.template.MembershipAwareContainerTemplate;
import org.ldp4j.application.template.ResourceTemplate;
import org.ldp4j.application.template.TemplateManagementService;
import org.ldp4j.application.template.TemplateVisitor;
import org.ldp4j.application.data.Name;

public final class ResourceFactoryService implements Service {

	private static final class ResourceFactoryServiceBuilder extends ServiceBuilder<ResourceFactoryService> {

		private ResourceFactoryServiceBuilder() {
			super(ResourceFactoryService.class);
		}
		
		@Override
		public ResourceFactoryService build() {
			return new ResourceFactoryService(service(TemplateManagementService.class));
		}

	}

	private final TemplateManagementService templateManagerService;

	private ResourceFactoryService(TemplateManagementService templateManagementService) {
		this.templateManagerService = templateManagementService;
	}

	private ResourceTemplate findTemplate(String id) {
		checkNotNull(id,"TemplateId identifier cannot be null");
		ResourceTemplate result = this.templateManagerService.findTemplateById(id);
		if(result==null) {
			throw new IllegalStateException("Could not find template '"+id+"'");
		}
		return result;
	}

	private <T extends Resource> ResourceTemplate findInstantiableTemplate(String templateId, boolean requireContainer) {
		ResourceTemplate template = findTemplate(templateId);
		if(!(template instanceof ContainerTemplate) && requireContainer) {
			throw new IllegalStateException("Cannot create a container from non-container template '"+template.id()+"'");
		}
		return template;
	}

	private ResourceImpl createResource(ResourceTemplate template, Name<?> resourceId, Resource parent) {
		checkNotNull(resourceId,"ResourceSnapshot identifier cannot be null");
		final ResourceId parentId=parent!=null?parent.id():null;
		final ResourceId id=ResourceIdHelper.createId(resourceId, template);
		final AtomicReference<ResourceImpl> result=new AtomicReference<ResourceImpl>();
		template.
			accept(
				new TemplateVisitor() {
					@Override
					public void visitResourceTemplate(ResourceTemplate template) {
						result.set(new ResourceImpl(id,parentId));
					}
					@Override
					public void visitContainerTemplate(ContainerTemplate template) {
						result.set(new ContainerImpl(id,parentId));
					}
					@Override
					public void visitBasicContainerTemplate(BasicContainerTemplate template) {
						visitContainerTemplate(template);
					}
					@Override
					public void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
						visitContainerTemplate(template);
					}
					@Override
					public void visitDirectContainerTemplate(DirectContainerTemplate template) {
						visitContainerTemplate(template);
					}
					@Override
					public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
						visitContainerTemplate(template);
					}
				}
			);
		ResourceImpl resource = result.get();
		resource.setResourceFactoryService(this);
		resource.setTemplateManagementService(templateManagerService);
		return resource;
	}

	public Resource createResource(String templateId, Name<?> resourceId, Resource parent) {
		return createResource(findTemplate(templateId),resourceId,parent);
	}

	public <T extends Resource> T createResource(String templateId, Name<?> resourceId, Resource parent, Class<? extends T> expectedResourceClass) {
		ResourceTemplate template=
			findInstantiableTemplate(
				templateId,
				Container.class.isAssignableFrom(expectedResourceClass));
		Resource newResource = createResource(template,resourceId,parent);
		return expectedResourceClass.cast(newResource);
	}

	public static ServiceBuilder<ResourceFactoryService> serviceBuilder() {
		return new ResourceFactoryServiceBuilder();
	}
	
	public static ResourceFactoryService defaultFactory() {
		return serviceBuilder().build();
	}

}