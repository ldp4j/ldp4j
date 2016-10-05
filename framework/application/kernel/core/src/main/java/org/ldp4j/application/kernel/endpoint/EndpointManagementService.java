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
package org.ldp4j.application.kernel.endpoint;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.engine.util.ListenerManager;
import org.ldp4j.application.engine.util.Notification;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.endpoint.EndpointRepository;
import org.ldp4j.application.kernel.resource.Attachment;
import org.ldp4j.application.kernel.resource.Container;
import org.ldp4j.application.kernel.resource.Member;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.resource.ResourceRepository;
import org.ldp4j.application.kernel.resource.Slug;
import org.ldp4j.application.kernel.service.Service;
import org.ldp4j.application.kernel.service.ServiceBuilder;
import org.ldp4j.application.kernel.spi.ModelFactory;
import org.ldp4j.application.kernel.spi.RuntimeDelegate;
import org.ldp4j.application.kernel.template.AttachedTemplate;
import org.ldp4j.application.kernel.template.ContainerTemplate;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.TemplateManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		@Override
		public EndpointManagementService build() {
			return
				new EndpointManagementService(
					super.service(TemplateManagementService.class));
		}

	}

	private static final String RESOURCE_IDENTIFIER_CANNOT_BE_NULL = "Resource identifier cannot be null";
	private static final String PATH_CANNOT_BE_NULL                = "Path cannot be null";
	private static final String LAST_MODIFIED_CANNOT_BE_NULL       = "Last modified cannot be null";
	private static final String ENTITY_TAG_CANNOT_BE_NULL          = "Entity tag cannot be null";
	private static final String RESOURCE_CANNOT_BE_NULL            = "Resource cannot be null";

	private static final Logger LOGGER=LoggerFactory.getLogger(EndpointManagementService.class);

	// TODO: Make this configurable, or subject to a policy
	private static final int MAX_ENDPOINT_CREATION_FAILURE = 3;

	private final TemplateManagementService templateManagementService;
	private final ListenerManager<EndpointLifecycleListener> listenerManager;

	private final ModelFactory modelFactory;
	private final EndpointRepository endpointRepository;
	private final ResourceRepository resourceRepository;

	private EndpointManagementService(TemplateManagementService templateManagementService) {
		this.templateManagementService = templateManagementService;
		this.modelFactory=RuntimeDelegate.getInstance().getModelFactory();
		this.endpointRepository=RuntimeDelegate.getInstance().getEndpointRepository();
		this.resourceRepository=RuntimeDelegate.getInstance().getResourceRepository();
		this.listenerManager=ListenerManager.<EndpointLifecycleListener>newInstance();
	}

	private String calculateResourcePath(Resource resource, String desiredPath) throws EndpointNotFoundException {
		if(resource.isRoot()) {
			throw new IllegalStateException("Cannot get path for root resource");
		}
		Resource parent=this.resourceRepository.resourceById(resource.parentId(),Resource.class);
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
		ResourceTemplate parentTemplate=this.templateManagementService.templateOfId(parent.id().templateId());
		AttachedTemplate attachedTemplate = parentTemplate.attachedTemplate(attachment.id());
		return
			PathBuilder.
				create().
					addSegment(endpoint.path()).
					addSegment(attachedTemplate.path()).
					addSegment(attachment.version()>0?attachment.version():null).
					build();
	}

	private String generatePathForMember(Resource child, Container parent, String desiredPath) throws EndpointNotFoundException {
		Member member = parent.findMember(child.id());
		if(member!=null) {
			Endpoint endpoint=getResourceEndpoint(parent.id());
			ContainerTemplate parentTemplate=this.templateManagementService.templateOfId(parent.id().templateId(),ContainerTemplate.class);
			if(parentTemplate==null) {
				throw new IllegalStateException("Could not find template resource '"+parent+"'");
			}
			String slugPath=getSlugPath(parent, desiredPath);
			return
				PathBuilder.
					create().
						addSegment(endpoint.path()).
						addSegment(parentTemplate.memberPath().or("")).
						addSegment(member.number()).
						addSegment(slugPath).
						build();
		}
		return null;
	}

	private String getSlugPath(Container parent, String desiredPath) {
		String slugPath=null;
		if(desiredPath!=null) {
			Slug slug=parent.findSlug(desiredPath);
			if(slug==null) {
				slug=parent.addSlug(desiredPath);
			}
			slugPath=slug.nextPath();
		}
		return slugPath;
	}

	private Endpoint createEndpoint(Resource resource, String relativePath, EntityTag entityTag, Date lastModified) throws EndpointCreationException {
		String candidatePath=relativePath;
		int repetitions=0;
		while(repetitions<MAX_ENDPOINT_CREATION_FAILURE) {
			LOGGER.debug("({}) Creating endpoint for {} [{},{},{}]",repetitions,resource.id(),entityTag,lastModified,relativePath);
			try {
				String resourcePath = calculateResourcePath(resource,candidatePath);
				LOGGER.debug("({}) Trying resource path {} ",repetitions,resourcePath);
				Endpoint newEndpoint = this.modelFactory.createEndpoint(resourcePath, resource, lastModified, entityTag);
				this.endpointRepository.add(newEndpoint);
				return newEndpoint;
			} catch (EndpointNotFoundException e) {
				throw new EndpointCreationException("Could not calculate path for resource '"+resource.id()+"'",e);
			} catch (IllegalArgumentException e) {
				LOGGER.debug("Could not create endpoint ({} --> {})",repetitions,candidatePath,e);
				repetitions++;
				candidatePath=null;
			}
		}
		throw new EndpointCreationException("Could not create endpoint for resource '"+resource.id()+"' after "+MAX_ENDPOINT_CREATION_FAILURE+" tries");
	}

	/**
	 * Enforce http://tools.ietf.org/html/rfc7232#section-2.2:
	 * if the clock in the request is ahead of the clock of the origin
	 * server (e.g., I request from Spain the update of a resource held in USA)
	 * the last-modified data should be changed to that of the request and not
	 * a generated date from the origin server
	 */
	private Date getModificationDate(Endpoint endpoint, Date modificationDate) {
		Date result=endpoint.lastModified();
		if(modificationDate.after(result)) {
			result=modificationDate;
		}
		return result;
	}

	public void registerEndpointLifecycleListener(EndpointLifecycleListener listener) {
		this.listenerManager.registerListener(listener);
	}

	public void deregisterEndpointLifecycleListener(EndpointLifecycleListener listener) {
		this.listenerManager.deregisterListener(listener);
	}

	public Endpoint getResourceEndpoint(ResourceId resourceId) throws EndpointNotFoundException {
		checkNotNull(resourceId,RESOURCE_IDENTIFIER_CANNOT_BE_NULL);
		Endpoint endpoint = this.endpointRepository.endpointOfResource(resourceId);
		if(endpoint==null) {
			throw new EndpointNotFoundException(resourceId);
		}
		return endpoint;
	}

	public Endpoint resolveEndpoint(String path) {
		checkNotNull(path,PATH_CANNOT_BE_NULL);
		return this.endpointRepository.endpointOfPath(path);
	}

	public Endpoint createEndpointForResource(Resource resource, String relativePath, EntityTag entityTag, Date lastModified) throws EndpointCreationException {
		checkNotNull(resource,RESOURCE_CANNOT_BE_NULL);
		checkNotNull(entityTag,ENTITY_TAG_CANNOT_BE_NULL);
		checkNotNull(lastModified,LAST_MODIFIED_CANNOT_BE_NULL);
		Endpoint newEndpoint = createEndpoint(resource, relativePath, entityTag, lastModified);
		this.listenerManager.notify(new EndpointCreationNotification(newEndpoint));
		return newEndpoint;
	}

	public Endpoint modifyResourceEndpoint(Resource resource, EntityTag entityTag, Date lastModified) throws EndpointNotFoundException {
		checkNotNull(resource,RESOURCE_CANNOT_BE_NULL);
		checkNotNull(entityTag,ENTITY_TAG_CANNOT_BE_NULL);
		checkNotNull(lastModified,LAST_MODIFIED_CANNOT_BE_NULL);
		Endpoint endpoint = this.endpointRepository.endpointOfResource(resource.id());
		if(endpoint==null) {
			throw new EndpointNotFoundException(resource.id());
		}
		endpoint.
			modify(
				entityTag,
				getModificationDate(endpoint,lastModified));
		return endpoint;
	}

	public Endpoint deleteResourceEndpoint(Resource resource, Date deletionDate) throws EndpointNotFoundException {
		checkNotNull(resource,RESOURCE_CANNOT_BE_NULL);
		Endpoint endpoint = this.endpointRepository.endpointOfResource(resource.id());
		if(endpoint==null) {
			throw new EndpointNotFoundException(resource.id());
		}
		endpoint.delete(getModificationDate(endpoint,deletionDate));
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