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
package org.ldp4j.application.endpoint;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.ldp4j.application.resource.Attachment;
import org.ldp4j.application.resource.Container;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.spi.EndpointRepository;
import org.ldp4j.application.spi.ResourceRepository;
import org.ldp4j.application.spi.Service;
import org.ldp4j.application.spi.ServiceBuilder;
import org.ldp4j.application.template.AttachedTemplate;
import org.ldp4j.application.template.ContainerTemplate;
import org.ldp4j.application.template.ResourceTemplate;
import org.ldp4j.application.template.TemplateManagementService;

public final class EndpointManagementService implements Service {
	
	private static final class EndpointManagementServiceBuilder extends ServiceBuilder<EndpointManagementService> {
		
		private EndpointManagementServiceBuilder() {
			super(EndpointManagementService.class);
		}

		public EndpointManagementService build() {
			return 
				new EndpointManagementService(
					endpointRepository(), 
					endpointFactoryService(),
					resourceRepository(),
					service(TemplateManagementService.class));
		}
		
	}

	// TODO: We need to devise a mechanism for persisting the ids of the members
	private static final class IdGenerator {
		
		private final static ConcurrentMap<ResourceId,AtomicLong> CONTAINER_COUNTER=new ConcurrentHashMap<ResourceId, AtomicLong>(); 
		
		static long nextMemberId(Container container) {
			AtomicLong counter = CONTAINER_COUNTER.putIfAbsent(container.id(), new AtomicLong(-1));
			if(counter==null) {
				counter=CONTAINER_COUNTER.get(container.id());
			}
			return counter.incrementAndGet();
		}
		
	}

	private final EndpointRepository endpointRepository;
	private final EndpointFactoryService factoryService;
	private final TemplateManagementService templateManagementService;
	private final ResourceRepository resourceRepository;

	private EndpointManagementService(EndpointRepository endpointRepository, EndpointFactoryService endpointFactoryService, ResourceRepository resourceRepository, TemplateManagementService templateManagementService) {
		this.endpointRepository = endpointRepository;
		this.factoryService = endpointFactoryService;
		this.resourceRepository = resourceRepository;
		this.templateManagementService = templateManagementService;
	}

	private String calculateResourcePath(Resource resource) throws EndpointNotFoundException {
		if(resource.isRoot()) {
			throw new IllegalStateException("Cannot get path for root resource");
		}
		Resource parent=this.resourceRepository.find(resource.parentId(),Resource.class);
		if(parent==null) {
			throw new IllegalStateException("Could not load resource '"+resource.parentId()+"' from the repository");
		}
		
		String result=
			parent instanceof Container?
				generatePathForMember(resource,(Container)parent):
				null;
		if(result==null) {
			result = generatePathForAttachment(resource,parent);
			if(result==null) {
				throw new IllegalStateException("Could not determine path for resource '"+resource.id()+"' with parent '"+parent.id()+"'");
			}
			
		}
		return result;
	}

	private String generatePathForAttachment(Resource child, Resource parent) throws EndpointNotFoundException {
		Attachment attachment=parent.findAttachment(child.id());
		if(attachment==null) {
			return null;
		}
		Endpoint endpoint=getResourceEndpoint(parent.id());
		ResourceTemplate parentTemplate=this.templateManagementService.findTemplateById(parent.id().templateId());
		AttachedTemplate attachedTemplate = parentTemplate.attachedTemplate(attachment.id());
		return endpoint.path()+"/"+attachedTemplate.path();
	}

	private String generatePathForMember(Resource child, Container parent) throws EndpointNotFoundException {
		if(parent.hasMember(child.id())) {
			Endpoint endpoint=getResourceEndpoint(parent.id());
			ContainerTemplate parentTemplate = templateManagementService.findTemplateById(parent.id().templateId(),ContainerTemplate.class);
			if(parentTemplate==null) {
				throw new IllegalStateException("Could not find template resource '"+parent+"'");
			}
			String memberPath = parentTemplate.memberPath().or("");
			if(memberPath.length()>0) {
				memberPath="/"+memberPath;
			}
			return endpoint.path()+memberPath+"/"+IdGenerator.nextMemberId(parent);
		}
		return null;
	}

	public Endpoint getResourceEndpoint(ResourceId resourceId) throws EndpointNotFoundException {
		checkNotNull(resourceId,"Resource identifier cannot be null");
		Endpoint endpoint = endpointRepository.endpointOfResource(resourceId);
		if(endpoint==null) {
			throw new EndpointNotFoundException(resourceId);
		}
		return endpoint;
	}
	
	public Endpoint createEndpointForResource(Resource resource, EntityTag entityTag, Date lastModified) throws EndpointCreationException {
		checkNotNull(resource,"ResourceSnapshot cannot be null");
		checkNotNull(entityTag,"Entity tag cannot be null");
		checkNotNull(lastModified,"Last modified cannot be null");
		try {
			String basePath = calculateResourcePath(resource);
			Endpoint newEndpoint = factoryService.createEndpoint(resource,basePath,entityTag,lastModified);
			endpointRepository.add(newEndpoint);
			return newEndpoint;
		} catch (EndpointNotFoundException e) {
			throw new EndpointCreationException("Could not calculate path for resource '"+resource.id()+"'",e);
		}
	}
	
	public Endpoint modifyResourceEndpoint(Resource resource, EntityTag entityTag, Date lastModified) throws EndpointNotFoundException {
		checkNotNull(resource,"ResourceSnapshot cannot be null");
		checkNotNull(entityTag,"Entity tag cannot be null");
		checkNotNull(lastModified,"Last modified cannot be null");
		Endpoint endpoint = endpointRepository.endpointOfResource(resource.id());
		if(endpoint==null) {
			throw new EndpointNotFoundException(resource.id());
		}
		endpoint.modify(entityTag, lastModified);
		return endpoint;
	}

	public Endpoint deleteResourceEndpoint(Resource resource) throws EndpointNotFoundException {
		checkNotNull(resource,"ResourceSnapshot cannot be null");
		Endpoint endpoint = endpointRepository.endpointOfResource(resource.id());
		if(endpoint==null) {
			throw new EndpointNotFoundException(resource.id());
		}
		endpointRepository.remove(endpoint);
		return endpoint;
	}
	
	public static ServiceBuilder<EndpointManagementService> serviceBuilder() {
		return new EndpointManagementServiceBuilder();
	}
	
	public static EndpointManagementService defaultService() {
		return serviceBuilder().build();
	}
	
}