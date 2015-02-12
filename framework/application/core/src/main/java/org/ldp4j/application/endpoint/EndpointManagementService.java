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
package org.ldp4j.application.endpoint;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.engine.util.ListenerManager;
import org.ldp4j.application.engine.util.Notification;
import org.ldp4j.application.resource.Attachment;
import org.ldp4j.application.resource.Container;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.spi.PersistencyManager;
import org.ldp4j.application.spi.Service;
import org.ldp4j.application.spi.ServiceBuilder;
import org.ldp4j.application.template.AttachedTemplate;
import org.ldp4j.application.template.ContainerTemplate;
import org.ldp4j.application.template.ResourceTemplate;
import org.ldp4j.application.template.TemplateManagementService;

public final class EndpointManagementService implements Service {

	private static final class EndpointCreationNotification implements Notification<EndpointLifecycleListener> {
		private final Endpoint endpoint;

		private EndpointCreationNotification(Endpoint endpoint) {
			this.endpoint = endpoint;
		}

		@Override
		public void propagate(EndpointLifecycleListener listener) {
			listener.endpointCreated(endpoint);
		}
	}

	private static final class EndpointDeletionNotification implements Notification<EndpointLifecycleListener> {
		private final Endpoint endpoint;

		private EndpointDeletionNotification(Endpoint endpoint) {
			this.endpoint = endpoint;
		}

		@Override
		public void propagate(EndpointLifecycleListener listener) {
			listener.endpointDeleted(endpoint);
		}
	}

	private static final class EndpointManagementServiceBuilder extends ServiceBuilder<EndpointManagementService> {

		private EndpointManagementServiceBuilder() {
			super(EndpointManagementService.class);
		}

		public EndpointManagementService build() {
			return
				new EndpointManagementService(
					persistencyManager(),
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

		private static final int MAX_ENDPOINT_CREATION_FAILURE = 3;

	private final PersistencyManager persistencyManager;
	private final TemplateManagementService templateManagementService;
	private final ListenerManager<EndpointLifecycleListener> listenerManager;


	private EndpointManagementService(PersistencyManager persistencyManager, TemplateManagementService templateManagementService) {
		this.persistencyManager = persistencyManager;
		this.templateManagementService = templateManagementService;
		this.listenerManager=ListenerManager.<EndpointLifecycleListener>newInstance();
	}

	private String calculateResourcePath(Resource resource, String desiredPath) throws EndpointNotFoundException {
		if(resource.isRoot()) {
			throw new IllegalStateException("Cannot get path for root resource");
		}
		Resource parent=this.persistencyManager.find(resource.parentId(),Resource.class);
		if(parent==null) {
			throw new IllegalStateException("Could not load resource '"+resource.parentId()+"' from the repository");
		}

		String result=
			parent instanceof Container?
				generatePathForMember(resource,(Container)parent,desiredPath):
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
		StringBuilder builder=new StringBuilder();
		addSegment(builder,endpoint.path());
		addSegment(builder,attachedTemplate.path());
		return builder.toString();
	}

	private String generatePathForMember(Resource child, Container parent, String desiredPath) throws EndpointNotFoundException {
		if(parent.hasMember(child.id())) {
			Endpoint endpoint=getResourceEndpoint(parent.id());
			ContainerTemplate parentTemplate = templateManagementService.findTemplateById(parent.id().templateId(),ContainerTemplate.class);
			if(parentTemplate==null) {
				throw new IllegalStateException("Could not find template resource '"+parent+"'");
			}
			StringBuilder builder=new StringBuilder();
			addSegment(builder,endpoint.path());
			addSegment(builder,parentTemplate.memberPath().or(""));
			addSegment(builder,IdGenerator.nextMemberId(parent));
			addSegment(builder,desiredPath);
//			Object lastSegment = desiredPath;
//			if(lastSegment==null) {
//				lastSegment=IdGenerator.nextMemberId(parent);
//			}
//			addSegment(builder,lastSegment);
			return builder.toString();
		}
		return null;
	}

	private <T> void addSegment(StringBuilder builder, T segment) {
		if(segment!=null) {
			String strSegment=segment.toString();
			if(strSegment!=null && strSegment.length()>0) {
				builder.append(strSegment);
				if(!strSegment.endsWith("/")) {
					builder.append("/");
				}
			}
		}
	}

	private Endpoint createEndpoint(Resource resource, String relativePath, EntityTag entityTag, Date lastModified) throws EndpointCreationException {
		String candidatePath=relativePath;
		int repetitions=0;
		while(repetitions<MAX_ENDPOINT_CREATION_FAILURE) {
			try {
				String resourcePath = calculateResourcePath(resource,candidatePath);
				Endpoint newEndpoint = this.persistencyManager.createEndpoint(resource,resourcePath,entityTag,lastModified);
				this.persistencyManager.add(newEndpoint);
				return newEndpoint;
			} catch (EndpointNotFoundException e) {
				throw new EndpointCreationException("Could not calculate path for resource '"+resource.id()+"'",e);
			} catch (IllegalArgumentException e) {
				// TODO: Define a proper exception
				if(candidatePath!=null) {
					repetitions++;
				}
				candidatePath=null;
			}
		}
		throw new EndpointCreationException("Could not create endpoint for resource '"+resource.id()+"'");
	}

	public void registerEndpointLifecycleListener(EndpointLifecycleListener listener) {
		this.listenerManager.registerListener(listener);
	}

	public void deregisterEndpointLifecycleListener(EndpointLifecycleListener listener) {
		this.listenerManager.deregisterListener(listener);
	}

	public Endpoint getResourceEndpoint(ResourceId resourceId) throws EndpointNotFoundException {
		checkNotNull(resourceId,"Resource identifier cannot be null");
		Endpoint endpoint = this.persistencyManager.endpointOfResource(resourceId);
		if(endpoint==null) {
			throw new EndpointNotFoundException(resourceId);
		}
		return endpoint;
	}

	public Endpoint resolveEndpoint(String path) {
		checkNotNull(path,"Path cannot be null");
		return this.persistencyManager.endpointOfPath(path);
	}

	/**
	 * TODO: Verify that http://tools.ietf.org/html/rfc7232#section-2.2
	 * holds: if the clock in the request is ahead of the clock of the origin
	 * server (i.e., I request from Spain the update of a resource held in USA)
	 * the last-modified data should be changed to that of the request and not
	 * a generated date from the origin server
	 */
	public Endpoint createEndpointForResource(Resource resource, String relativePath, EntityTag entityTag, Date lastModified) throws EndpointCreationException {
		checkNotNull(resource,"Resource cannot be null");
		checkNotNull(entityTag,"Entity tag cannot be null");
		checkNotNull(lastModified,"Last modified cannot be null");
		Endpoint newEndpoint = createEndpoint(resource, relativePath, entityTag, lastModified);
		this.listenerManager.notify(new EndpointCreationNotification(newEndpoint));
		return newEndpoint;
	}

	public Endpoint modifyResourceEndpoint(Resource resource, EntityTag entityTag, Date lastModified) throws EndpointNotFoundException {
		checkNotNull(resource,"ResourceSnapshot cannot be null");
		checkNotNull(entityTag,"Entity tag cannot be null");
		checkNotNull(lastModified,"Last modified cannot be null");
		Endpoint endpoint = this.persistencyManager.endpointOfResource(resource.id());
		if(endpoint==null) {
			throw new EndpointNotFoundException(resource.id());
		}
		endpoint.modify(entityTag, lastModified);
		return endpoint;
	}

	public Endpoint deleteResourceEndpoint(Resource resource) throws EndpointNotFoundException {
		checkNotNull(resource,"ResourceSnapshot cannot be null");
		Endpoint endpoint = this.persistencyManager.endpointOfResource(resource.id());
		if(endpoint==null) {
			throw new EndpointNotFoundException(resource.id());
		}
		this.persistencyManager.remove(endpoint);
		this.listenerManager.notify(new EndpointDeletionNotification(endpoint));
		return endpoint;
	}

	public static ServiceBuilder<EndpointManagementService> serviceBuilder() {
		return new EndpointManagementServiceBuilder();
	}

	public static EndpointManagementService defaultService() {
		return serviceBuilder().build();
	}

}