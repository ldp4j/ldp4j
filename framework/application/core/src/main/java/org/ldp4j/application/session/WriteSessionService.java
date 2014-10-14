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
package org.ldp4j.application.session;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Date;
import java.util.UUID;

import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.endpoint.EndpointCreationException;
import org.ldp4j.application.endpoint.EndpointManagementService;
import org.ldp4j.application.endpoint.EndpointNotFoundException;
import org.ldp4j.application.endpoint.EntityTag;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.spi.ResourceRepository;
import org.ldp4j.application.spi.Service;
import org.ldp4j.application.spi.ServiceBuilder;
import org.ldp4j.application.template.TemplateManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WriteSessionService implements Service {

	private static class WriteSessionServiceBuilder extends ServiceBuilder<WriteSessionService> {
		
		private WriteSessionServiceBuilder() {
			super(WriteSessionService.class);
		}

		public WriteSessionService build() {
			return 
				new WriteSessionService(
					resourceRepository(),
					service(EndpointManagementService.class),
					service(TemplateManagementService.class));
		}
		
	}

	private final class ResourceProcessor implements UnitOfWork.Visitor {
	
		private final Date lastModified;
	
		private ResourceProcessor(Date lastModified) {
			this.lastModified = lastModified;
		}
	
		@Override
		public void visitNew(DelegatedResourceSnapshot obj) {
			createResource(obj.delegate(),lastModified);
		}
	
		@Override
		public void visitDirty(DelegatedResourceSnapshot obj) {
			modifyResource(obj.delegate(),lastModified);
		}
	
		@Override
		public void visitDeleted(DelegatedResourceSnapshot obj) {
			deleteResource(obj.delegate(),lastModified);
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(WriteSessionService.class);

	private final ResourceRepository resourceRepository;
	private final EndpointManagementService endpointManagementService;
	private final TemplateManagementService templateManagementService;

	private WriteSessionService(ResourceRepository resourceRepository, EndpointManagementService endointManagementService, TemplateManagementService templateManagementService) {
		this.resourceRepository = resourceRepository;
		this.templateManagementService=templateManagementService;
		this.endpointManagementService = endointManagementService;
	}

	public WriteSession createSession() {
		UnitOfWork.newCurrent();
		logLifecycleMessage("Created write session...");
		return new DelegatedWriteSession(this.resourceRepository,this.templateManagementService,this);
	}

	public void terminateSession(WriteSession writeSession) {
		checkArgument(writeSession instanceof DelegatedWriteSession);
		DelegatedWriteSession session=(DelegatedWriteSession)writeSession;
		try {
			switch(session.status()) {
				case ACTIVE:
					logLifecycleMessage("Force termination of active session...");
					session.discardChanges();
					break;
				case ABORTED:
					logLifecycleMessage("Force termination of aborted session...");
					session.discardChanges();
					break;
				case COMPLETED:
					// Nothing to do
					break;
				
			}
		} catch (WriteSessionException e) {
			// TODO: Log this failure
		} finally {
			UnitOfWork.setCurrent(null);
		}
	}
	
	public ResourceSnapshot attach(WriteSession writeSession, Resource resource, Class<? extends ResourceHandler> handlerClass) {
		return writeSession.find(ResourceSnapshot.class, resource.id().name(), handlerClass);
	}

	public Resource detach(WriteSession writeSession, ResourceSnapshot snapshot) {
		checkArgument(writeSession instanceof DelegatedWriteSession);
		DelegatedWriteSession session=(DelegatedWriteSession)writeSession;
		return session.extractWrappedResource(snapshot);
	}
	
	void commitSession() {
		logLifecycleMessage("Commiting session...");
		UnitOfWork.getCurrent().accept(new ResourceProcessor(new Date()));
	}

	private void logLifecycleMessage(String msg) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(msg);
		}
	}

	private EntityTag generateEntityTag(Resource resource) {
		return new EntityTag(UUID.randomUUID().toString());
	}

	private void createResource(Resource resource, Date lastModified) {
		try {
			resourceRepository.add(resource);
			Endpoint newEndpoint=
				endpointManagementService.
					createEndpointForResource(
						resource, 
						generateEntityTag(resource), 
						lastModified);
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Created "+resource);
				LOGGER.trace("Created "+newEndpoint);
			}
		} catch (EndpointCreationException e) {
			throw new IllegalStateException(e);
		}
	}

	private void modifyResource(Resource resource, Date lastModified) {
		try {
			resourceRepository.add(resource);
			Endpoint endpoint = 
				endpointManagementService.modifyResourceEndpoint(
					resource,
					generateEntityTag(resource),
					lastModified);
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Modified "+resource);
				LOGGER.trace("Modified "+endpoint);
			}
		} catch (EndpointNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	private void deleteResource(Resource resource, Date lastModified) {
		try {
			resourceRepository.remove(resource);
			Endpoint endpoint = endpointManagementService.deleteResourceEndpoint(resource);
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Deleted "+resource);
				LOGGER.trace("Deleted "+endpoint);
			}
		} catch (EndpointNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	public static ServiceBuilder<WriteSessionService> serviceBuilder() {
		return new WriteSessionServiceBuilder();
	}
	
	public static WriteSessionService defaultService() {
		return serviceBuilder().build();
	}

}