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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-application:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-application-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.session;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.resource.ResourceIdHelper;
import org.ldp4j.application.session.UnitOfWork.EventHandler;
import org.ldp4j.application.spi.ResourceRepository;
import org.ldp4j.application.template.BasicContainerTemplate;
import org.ldp4j.application.template.ContainerTemplate;
import org.ldp4j.application.template.DirectContainerTemplate;
import org.ldp4j.application.template.IndirectContainerTemplate;
import org.ldp4j.application.template.MembershipAwareContainerTemplate;
import org.ldp4j.application.template.ResourceTemplate;
import org.ldp4j.application.template.TemplateIntrospector;
import org.ldp4j.application.template.TemplateManagementService;
import org.ldp4j.application.template.TemplateVisitor;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.IndividualVisitor;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ContainerHandler;
import org.ldp4j.application.ext.ResourceHandler;

final class DelegatedWriteSession implements WriteSession {

	enum Status {
		ACTIVE,
		COMPLETED,
		ABORTED
	}

	private final class MembershipAwareContainerTargetCollector implements EventHandler {
		@Override
		public void notifyObjectCreation(DelegatedResourceSnapshot obj) {
			obj.accept(
				new DelegatedSnapshotVisitor() {
					@Override
					public void visitDelegatedResourceSnapshot(final DelegatedResourceSnapshot child) {
						if(child.isRoot()) {
							return;
						}
						child.parent().accept(
							new DelegatedSnapshotVisitor() {
								@Override
								public void visitDelegatedResourceSnapshot(DelegatedResourceSnapshot resource) {
									// Nothing special to do
								}
								@Override
								public void visitDelegatedContainerSnapshot(DelegatedContainerSnapshot container) {
									if(!container.hasMember(child) || !TemplateIntrospector.newInstance(container.template()).isMembershipAwareContainer()) { 
										return;
									}
									if(!container.isRoot()) {
										UnitOfWork.getCurrent().registerDirty(container.parent());
									}
								}
							}
						);
					}
					@Override
					public void visitDelegatedContainerSnapshot(DelegatedContainerSnapshot resource) {
						visitDelegatedResourceSnapshot(resource);
					}
				}
			);
		}

		@Override
		public void notifyObjectUpdate(DelegatedResourceSnapshot obj) {
			// Not needed
		}

		@Override
		public void notifyObjectDeletion(DelegatedResourceSnapshot obj) {
			// Not needed
		}
	}

	private static final String RESOURCE_NAME_CANNOT_BE_NULL = "Resource snapshot name cannot be null";
	private static final String RESOURCE_CANNOT_BE_NULL      = "Resource snapshot cannot be null";
	private static final String WRITE_SESSION_NOT_ACTIVE     = "Write session is not active (%s)";

	private final Map<ResourceId,DelegatedResourceSnapshot> resourceCache;

	private final ResourceRepository resourceRepository;
	private final WriteSessionService writeSessionService;
	private final TemplateManagementService templateManagementService;

	private final SnapshotFactory snapshotFactory;

	private volatile Status status;
	
	protected DelegatedWriteSession(ResourceRepository resourceRepository, TemplateManagementService templateManagementService, WriteSessionService writeSessionService) {
		this.resourceRepository = resourceRepository;
		this.templateManagementService = templateManagementService;
		this.writeSessionService = writeSessionService;
		UnitOfWork.
			getCurrent().
				setEventHandler(new MembershipAwareContainerTargetCollector());
		this.status=Status.ACTIVE;
		this.resourceCache=new LinkedHashMap<ResourceId,DelegatedResourceSnapshot>();
		this.snapshotFactory=SnapshotFactory.newInstance(this.templateManagementService, this);
	}
		
	private DelegatedResourceSnapshot resolveResource(ResourceId resourceId, ResourceTemplate template) {
		DelegatedResourceSnapshot resource=this.resourceCache.get(resourceId);
		if(resource==null) {
			Resource delegate=this.resourceRepository.find(resourceId, Resource.class);
			if(delegate!=null) {
				resource=snapshotFactory.newPersistent(delegate,template);
				resource.setSession(this);
				this.resourceCache.put(resourceId, resource);
			}
		}
		return resource;
	}

	private <S extends ResourceSnapshot> S find(
			Class<? extends S> snapshotClass, 
			ResourceId resourceId) {
		ResourceTemplate template=DelegatedWriteSession.this.templateManagementService.findTemplateById(resourceId.templateId());
		checkArgument(template!=null,"Unknown template '%s' ",resourceId.templateId());
		checkArgument(areCompatible(snapshotClass,template),"Cannot wrap an object managed by '%s' with an snapshot of type '%s'",resourceId.templateId(),snapshotClass.getCanonicalName());
		DelegatedResourceSnapshot resource = resolveResource(resourceId, template);
		return snapshotClass.cast(resource);
	}

	private boolean areCompatible(final Class<?> clazz, ResourceTemplate template) {
		final AtomicReference<Boolean> result=new AtomicReference<Boolean>();
		template.accept(
			new TemplateVisitor() {
				@Override
				public void visitResourceTemplate(ResourceTemplate template) {
					result.set(clazz.isAssignableFrom(ResourceSnapshot.class));
				}
				@Override
				public void visitContainerTemplate(ContainerTemplate template) {
					result.set(clazz.isAssignableFrom(ContainerSnapshot.class));
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
		return result.get();
	}

	ResourceTemplate loadTemplate(String templateId) {
		return this.templateManagementService.findTemplateById(templateId);
	}

	Resource loadResource(ResourceId resourceId) {
		return this.resourceRepository.find(resourceId,Resource.class);
	}

	DelegatedResourceSnapshot resolveResource(ResourceId resourceId) {
		return resolveResource(resourceId,loadTemplate(resourceId.templateId()));
	}

	Status status() {
		return this.status;
	}

	Resource extractWrappedResource(ResourceSnapshot snapshot) {
		checkArgument(snapshot instanceof DelegatedResourceSnapshot,"Unknown resource '%s'",snapshot.name());
		DelegatedResourceSnapshot delegatedResource=(DelegatedResourceSnapshot)snapshot;
		return delegatedResource.delegate();
	}

	<T extends DelegatedResourceSnapshot> T newTransient(ResourceId resourceId, DelegatedResourceSnapshot parent, Class<? extends T> clazz) {
		DelegatedResourceSnapshot newSnapshot = this.snapshotFactory.newTransient(resourceId, parent);
		checkState(clazz.isInstance(newSnapshot),"Incompatible types (%s cannot be assigned from %s)",clazz.getCanonicalName(),newSnapshot.getClass().getCanonicalName());
		checkState(this.resourceCache.put(resourceId, newSnapshot)==null,"Transient resource should be new");
		newSnapshot.setSession(this);
		return clazz.cast(newSnapshot);
	}

	@Override
	public <S extends ResourceSnapshot> S find(
			Class<? extends S> snapshotClass, 
			Name<?> name,
			Class<? extends ResourceHandler> handlerClass) {
		checkNotNull(snapshotClass,"Resource snapshot class cannot be null");
		checkNotNull(name,RESOURCE_NAME_CANNOT_BE_NULL);
		checkNotNull(handlerClass,"Resource handler class cannot be null");
		checkArgument(!ContainerSnapshot.class.isAssignableFrom(snapshotClass) || ContainerHandler.class.isAssignableFrom(handlerClass),"Incompatible snapshot and handler classes ('%s' instances are not handled by '%s')",snapshotClass.getCanonicalName(),handlerClass.getCanonicalName());
		ResourceTemplate template=this.templateManagementService.findTemplateByHandler(handlerClass);
		checkArgument(template!=null,"Handler class '%s' is not associated to any existing template",handlerClass.getCanonicalName());
		checkArgument(areCompatible(snapshotClass,template),"Cannot wrap an object managed by '%s' with an snapshot of type '%s'",handlerClass.getCanonicalName(),snapshotClass.getCanonicalName());
		checkState(this.status.equals(Status.ACTIVE),WRITE_SESSION_NOT_ACTIVE,this.status);
		ResourceId id=ResourceIdHelper.createId(name,template);
		DelegatedResourceSnapshot resource = resolveResource(id, template);
		return snapshotClass.cast(resource);
	}

	@Override
	public <S extends ResourceSnapshot> S resolve(
			final Class<? extends S> snapshotClass, 
			final Individual<?,?> individual) {
		checkNotNull(snapshotClass,"Resource snapshot class cannot be null");
		checkNotNull(individual,"Individual cannot be null");
		checkState(this.status.equals(Status.ACTIVE),WRITE_SESSION_NOT_ACTIVE,this.status);
		S result=null;
		ResourceId id = getIdentifier(individual);
		if(id!=null) {
			result=find(snapshotClass,id); 
		}
		return result;
	}

	/**
	 * @param individual
	 * @return
	 */
	private ResourceId getIdentifier(final Individual<?, ?> individual) {
		final AtomicReference<ResourceId> resourceId=new AtomicReference<ResourceId>();
		individual.accept(
			new IndividualVisitor() {
				@Override
				public void visitManagedIndividual(ManagedIndividual individual) {
					resourceId.set(translateIdentifier(individual.id()));
				}
				@Override
				public void visitLocalIndividual(LocalIndividual individual) {
					resourceId.set(null);
				}
				@Override
				public void visitExternalIndividual(ExternalIndividual individual) {
					resourceId.set(null);
				}
				private ResourceId translateIdentifier(ManagedIndividualId id) {
					return ResourceId.createId(id.name(), id.managerId());
				}
			}
		);
		ResourceId id = resourceId.get();
		return id;
	}

	@Override
	public void modify(ResourceSnapshot resource) {
		checkNotNull(resource,RESOURCE_CANNOT_BE_NULL);
		checkArgument(resource instanceof DelegatedResourceSnapshot,"Unknown resource '%s'",resource.name());
		checkState(this.status.equals(Status.ACTIVE),WRITE_SESSION_NOT_ACTIVE,status);
		DelegatedResourceSnapshot delegatedResource=(DelegatedResourceSnapshot)resource;
		delegatedResource.modify();
	}

	@Override
	public void delete(ResourceSnapshot resource) {
		checkNotNull(resource,RESOURCE_CANNOT_BE_NULL);
		checkArgument(resource instanceof DelegatedResourceSnapshot,"Unknown resource '%s'",resource.name());
		checkState(this.status.equals(Status.ACTIVE),WRITE_SESSION_NOT_ACTIVE,status);
		DelegatedResourceSnapshot delegatedResource=(DelegatedResourceSnapshot)resource;
		delegatedResource.delete();
	}

	@Override
	public void saveChanges() throws WriteSessionException {
		checkState(this.status.equals(Status.ACTIVE),WRITE_SESSION_NOT_ACTIVE,this.status);
		this.status=Status.COMPLETED;
		for(Entry<ResourceId, DelegatedResourceSnapshot> entry:this.resourceCache.entrySet()) {
			DelegatedResourceSnapshot resource = entry.getValue();
			resource.saveChanges();
		}
		this.writeSessionService.commitSession();
	}

	@Override
	public void discardChanges() throws WriteSessionException {
		checkState(this.status.equals(Status.ACTIVE),WRITE_SESSION_NOT_ACTIVE,this.status);
		this.status=Status.ABORTED;
	}

}