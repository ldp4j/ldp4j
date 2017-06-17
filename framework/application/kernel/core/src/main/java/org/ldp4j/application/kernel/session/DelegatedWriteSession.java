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
package org.ldp4j.application.kernel.session;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ContainerHandler;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.session.UnitOfWork.EventHandler;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.TemplateIntrospector;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SessionTerminationException;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DelegatedWriteSession implements WriteSession {

	enum Status {
		ACTIVE,
		COMPLETED,
		ABORTED
	}

	private final class MembershipAwareContainerTargetCollector implements EventHandler {

		private final class TargetCollector implements DelegatedSnapshotVisitor {

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

		@Override
		public void notifyObjectCreation(DelegatedResourceSnapshot obj) {
			obj.accept(new TargetCollector());
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

	private static final Logger LOGGER=LoggerFactory.getLogger(DelegatedWriteSession.class);
	private static final String RESOURCE_NAME_CANNOT_BE_NULL = "Resource snapshot name cannot be null";
	private static final String RESOURCE_CANNOT_BE_NULL      = "Resource snapshot cannot be null";
	private static final String WRITE_SESSION_NOT_ACTIVE     = "Write session is not active (%s)";

	private final Map<ResourceId,DelegatedResourceSnapshot> resourceCache;

	private final WriteSessionConfiguration configuration;
	private final WriteSessionService writeSessionService;

	private final SnapshotFactory snapshotFactory;

	private volatile Status status;

	protected DelegatedWriteSession(WriteSessionConfiguration configuration, WriteSessionService writeSessionService) {
		this.configuration = configuration;
		this.writeSessionService = writeSessionService;
		UnitOfWork.
			getCurrent().
				setEventHandler(new MembershipAwareContainerTargetCollector());
		this.status=Status.ACTIVE;
		this.resourceCache=new LinkedHashMap<ResourceId,DelegatedResourceSnapshot>();
		this.snapshotFactory=SnapshotFactory.newInstance(this);
	}

	private DelegatedResourceSnapshot resolveResource(ResourceId resourceId, ResourceTemplate template) {
		DelegatedResourceSnapshot resource=this.resourceCache.get(resourceId);
		if(resource==null) {
			Resource delegate=loadResource(resourceId);
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
		ResourceTemplate template=loadTemplate(resourceId.templateId());
		checkArgument(template!=null,"Unknown template '%s' ",resourceId.templateId());
		checkArgument(Snapshots.areCompatible(snapshotClass,template),"Cannot wrap an object managed by '%s' with an snapshot of type '%s'",resourceId.templateId(),snapshotClass.getCanonicalName());
		DelegatedResourceSnapshot resource = resolveResource(resourceId, template);
		return snapshotClass.cast(resource);
	}

	/**
	 * @param individual
	 * @return
	 */
	private ResourceId getIdentifier(final Individual<?, ?> individual) {
		ResourceId result=null;
		Object rawId=individual.id();
		if(rawId instanceof ManagedIndividualId) {
			ManagedIndividualId id=(ManagedIndividualId)rawId;
			result=ResourceId.createId(id.name(),id.managerId());
		}
		return result;
	}

	private boolean isMainResource(DelegatedResourceSnapshot snapshot) {
		ResourceSnapshot targetSnapshot = getTargetSnapshot();
		if(!(targetSnapshot instanceof DelegatedContainerSnapshot)) {
			return false;
		}
		DelegatedContainerSnapshot targetContainerSnapshot=(DelegatedContainerSnapshot)targetSnapshot;
		List<DelegatedResourceSnapshot> newMembers=targetContainerSnapshot.newMembers();
		LOGGER.debug("New members: {}",newMembers);
		if(newMembers.isEmpty()) {
			return false;
		}
		return snapshot==newMembers.get(0);
	}

	private ResourceSnapshot getTargetSnapshot() {
		ResourceSnapshot targetSnapshot=null;
		Resource target=this.configuration.getTarget();
		if(target!=null) {
			targetSnapshot=find(ResourceSnapshot.class, target.id());
		}
		return targetSnapshot;
	}

	ResourceTemplate loadTemplate(String templateId) {
		return this.writeSessionService.templateManagementService().templateOfId(templateId);
	}

	Resource loadResource(ResourceId resourceId) {
		return this.writeSessionService.resourceOfId(resourceId);
	}

	DelegatedResourceSnapshot resolveResource(ResourceId resourceId) {
		return resolveResource(resourceId,loadTemplate(resourceId.templateId()));
	}

	String getDesiredPath(DelegatedResourceSnapshot snapshot) {
		String desiredPath=null;
		if(isMainResource(snapshot)) {
			desiredPath=this.configuration.getPath();
		}
		if(LOGGER.isDebugEnabled()) {
			if(desiredPath==null && this.configuration.getPath()!=null) {
				LOGGER.debug("Resource {} is not main resource",snapshot.resourceId());
			} else if(desiredPath!=null) {
				LOGGER.debug("Resource {} is the main resource and may be deployed at '{}'",snapshot.resourceId(),desiredPath);
			} else {
				LOGGER.debug("Resource {} is the main resource but does not have a deployment preference",snapshot.resourceId());
			}
		}
		return desiredPath;
	}

	URI getIndirectId(DelegatedResourceSnapshot snapshot) {
		URI indirectId=null;
		if(isMainResource(snapshot)) {
			indirectId=this.configuration.getIndirectId();
		}
		if(LOGGER.isDebugEnabled()) {
			if(indirectId==null && this.configuration.getIndirectId()!=null) {
				LOGGER.debug("Resource {} is not main resource",snapshot.resourceId());
			} else if(indirectId!=null) {
				LOGGER.debug("Resource {} is the main resource and may be alternative identified by '{}'",snapshot.resourceId(),indirectId);
			} else {
				LOGGER.debug("Resource {} is the main resource but does not have an alternative identifier",snapshot.resourceId());
			}
		}
		return indirectId;
	}

	Date getLastModified() {
		return this.configuration.getLastModified();
	}

	Status status() {
		return this.status;
	}

	<T extends DelegatedResourceSnapshot> T newTransient(ResourceId resourceId, DelegatedResourceSnapshot parent, Class<? extends T> clazz) {
		DelegatedResourceSnapshot newSnapshot = this.snapshotFactory.newTransient(resourceId, parent);
		checkState(clazz.isInstance(newSnapshot),"Incompatible types (%s cannot be assigned from %s)",clazz.getCanonicalName(),newSnapshot.getClass().getCanonicalName());
		checkState(this.resourceCache.put(resourceId, newSnapshot)==null,"Transient resource should be new");
		newSnapshot.setSession(this);
		return clazz.cast(newSnapshot);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends ResourceSnapshot> S find(
			Class<? extends S> snapshotClass,
			Name<?> name,
			Class<? extends ResourceHandler> handlerClass) {
		checkNotNull(snapshotClass,"Resource snapshot class cannot be null");
		checkNotNull(name,RESOURCE_NAME_CANNOT_BE_NULL);
		checkNotNull(handlerClass,"Resource handler class cannot be null");
		checkArgument(!ContainerSnapshot.class.isAssignableFrom(snapshotClass) || ContainerHandler.class.isAssignableFrom(handlerClass),"Incompatible snapshot and handler classes ('%s' instances are not handled by '%s')",snapshotClass.getCanonicalName(),handlerClass.getCanonicalName());
		ResourceTemplate template=this.writeSessionService.templateManagementService().templateOfHandler(handlerClass);
		checkArgument(template!=null,"Handler class '%s' is not associated to any existing template",handlerClass.getCanonicalName());
		checkArgument(Snapshots.areCompatible(snapshotClass,template),"Cannot wrap an object managed by '%s' with an snapshot of type '%s'",handlerClass.getCanonicalName(),snapshotClass.getCanonicalName());
		checkState(this.status.equals(Status.ACTIVE),WRITE_SESSION_NOT_ACTIVE,this.status);
		ResourceId id=ResourceId.createId(name,template);
		DelegatedResourceSnapshot resource = resolveResource(id, template);
		return snapshotClass.cast(resource);
	}

	/**
	 * {@inheritDoc}
	 */
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
	 * {@inheritDoc}
	 */
	@Override
	public void modify(ResourceSnapshot resource) {
		checkNotNull(resource,RESOURCE_CANNOT_BE_NULL);
		checkArgument(resource instanceof DelegatedResourceSnapshot,"Unknown resource '%s'",resource.name());
		checkState(this.status.equals(Status.ACTIVE),WRITE_SESSION_NOT_ACTIVE,status);
		DelegatedResourceSnapshot delegatedResource=(DelegatedResourceSnapshot)resource;
		delegatedResource.modify();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(ResourceSnapshot resource) {
		checkNotNull(resource,RESOURCE_CANNOT_BE_NULL);
		checkArgument(resource instanceof DelegatedResourceSnapshot,"Unknown resource '%s'",resource.name());
		checkState(this.status.equals(Status.ACTIVE),WRITE_SESSION_NOT_ACTIVE,status);
		DelegatedResourceSnapshot delegatedResource=(DelegatedResourceSnapshot)resource;
		delegatedResource.delete();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveChanges() throws WriteSessionException {
		checkState(this.status.equals(Status.ACTIVE),WRITE_SESSION_NOT_ACTIVE,this.status);
		this.status=Status.COMPLETED;
		for(Entry<ResourceId, DelegatedResourceSnapshot> entry:this.resourceCache.entrySet()) {
			DelegatedResourceSnapshot resource = entry.getValue();
			resource.saveChanges();
		}
		this.writeSessionService.commitSession(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void discardChanges() throws WriteSessionException {
		checkState(this.status.equals(Status.ACTIVE),WRITE_SESSION_NOT_ACTIVE,this.status);
		this.status=Status.ABORTED;
		this.writeSessionService.rollbackSession(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws SessionTerminationException {
		this.writeSessionService.terminateSession(this);
	}

}