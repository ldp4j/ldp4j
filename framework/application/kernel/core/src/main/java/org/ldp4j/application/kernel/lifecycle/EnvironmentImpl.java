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
package org.ldp4j.application.kernel.lifecycle;

import java.util.Date;
import java.util.List;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.engine.ApplicationConfigurationException;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.endpoint.EndpointRepository;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.resource.ResourceRepository;
import org.ldp4j.application.kernel.spi.ModelFactory;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.TemplateManagementService;
import org.ldp4j.application.lifecycle.LifecycleEnvironment;
import org.ldp4j.application.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

final class EnvironmentImpl implements Environment {

	private static final Logger LOGGER=LoggerFactory.getLogger(EnvironmentImpl.class);

	private final class RootResource {

		private final Name<?> resourceName;
		private final Class<? extends ResourceHandler> handlerClass;
		private final String path;
		private ResourceTemplate template;

		RootResource(Name<?> resourceName, Class<? extends ResourceHandler> handlerClass, String path) {
			this.resourceName = resourceName;
			this.handlerClass = handlerClass;
			this.path = path;
		}

		Name<?> name() {
			return this.resourceName;
		}

		String path() {
			return path;
		}

		ResourceTemplate template() {
			return this.template;
		}

		ResourceId resourceId() {
			return ResourceId.createId(this.resourceName, this.template.id());
		}

		void validate() throws ApplicationConfigurationException {
			assertNameNotNull();
			assertHandlerClassNotNull();
			this.template = EnvironmentImpl.this.templateManagementService.templateOfHandler(this.handlerClass);
			assertTemplateExists();
			assertValidPath();
		}

		/**
		 * TODO: What about URL encoding issues? Decide were do we take care of
		 * this, as it also impacts the creation of new endpoints during the
		 * normal operation of the LDP4j application
		 */
		private void assertValidPath() throws ApplicationConfigurationException {
			if(this.path==null) {
				throw new ApplicationConfigurationException("No path specified for resource '"+this.resourceName+"'");
			}
			if(this.path.startsWith("/") || !this.path.endsWith("/") || this.path.endsWith("//")) {
				throw new ApplicationConfigurationException("Invalid path '"+this.path+"' specified for resource '"+this.resourceName+"': it must end with a single '/' and have at least one segment");
			}
		}

		private void assertTemplateExists() throws ApplicationConfigurationException {
			if(this.template==null) {
				throw new ApplicationConfigurationException("Unknown resource handler '"+this.handlerClass.getCanonicalName()+"' specified for resource '"+this.resourceName+"'");
			}
		}

		private void assertHandlerClassNotNull() throws ApplicationConfigurationException {
			if(this.handlerClass==null) {
				throw new ApplicationConfigurationException("No handler class specified for resource '"+this.resourceName+"'");
			}
		}

		private void assertNameNotNull() throws ApplicationConfigurationException {
			if(this.resourceName==null) {
				throw new ApplicationConfigurationException("Resource name cannot be null");
			}
		}

	}

	private final List<RootResource> candidates;

	private final ModelFactory modelFactory;

	private final TemplateManagementService templateManagementService;

	private final ResourceRepository resourceRepository;
	private final EndpointRepository endpointRepository;

	private final LifecycleEnvironment lifecycleEnvironment;

	EnvironmentImpl(TemplateManagementService templateManagementService, ModelFactory persistencyManager, EndpointRepository endpointRepository, ResourceRepository resourceRepository, LifecycleEnvironment lifecycleEnvironment) {
		this.templateManagementService = templateManagementService;
		this.resourceRepository = resourceRepository;
		this.endpointRepository = endpointRepository;
		this.modelFactory = persistencyManager;
		this.candidates=Lists.newArrayList();
		this.lifecycleEnvironment=lifecycleEnvironment;
	}

	@Override
	public void publishResource(Name<?> resourceName, Class<? extends ResourceHandler> handlerClass, String path) {
		RootResource rootResource = new RootResource(resourceName,handlerClass,path);
		this.candidates.add(rootResource);
	}

	@Override
	public LifecycleEnvironment lifecycle() {
		return this.lifecycleEnvironment;
	}

	/**
	 * TODO: Check that root resources are published in different "branches"
	 */
	void configureRootResources() throws ApplicationConfigurationException {
		validateRootResources();
		final Date creationDate = new Date();
		for(RootResource entry:this.candidates) {
			publish(entry, creationDate);
			LOGGER.debug("Published resource '{}' at '{}'",entry.resourceId(),entry.path());
		}
	}

	private void publish(RootResource rootResource, Date creationDate) throws ApplicationConfigurationException {
		ResourceId resourceId = rootResource.resourceId();
		String path = rootResource.path();

		Resource prevResource = this.resourceRepository.resourceById(resourceId,Resource.class);
		Endpoint prevEndpoint = this.endpointRepository.endpointOfPath(path);

		if(prevEndpoint!=null && !prevEndpoint.resourceId().equals(resourceId)) {
			throw new ApplicationConfigurationException(String.format("Resource %s cannot be published at '%s' as that path is already in use by a resource %s",toString(resourceId),path,toString(prevEndpoint.resourceId())));
		}

		if(prevEndpoint==null) {
			if(prevResource!=null) {
				throw new ApplicationConfigurationException(String.format("Resource %s cannot be published at '%s' as it is already published at '%s'",toString(resourceId),path,this.endpointRepository.endpointOfResource(resourceId).path()));
			} else {
				Resource resource=this.modelFactory.createResource(rootResource.template(),rootResource.name());
				this.resourceRepository.add(resource);
				Endpoint endpoint=this.modelFactory.createEndpoint(path,resource,creationDate,EntityTag.createStrong(path));
				this.endpointRepository.add(endpoint);
			}
		}

	}

	private void validateRootResources() throws ApplicationConfigurationException {
		BiMap<ResourceId,String> rootResourceMap=HashBiMap.create();
		for(RootResource candidateResource:this.candidates) {
			addPublication(rootResourceMap,candidateResource);
		}
	}

	private void addPublication(final BiMap<ResourceId, String> rootResourceMap, RootResource candidateResource) throws ApplicationConfigurationException {
		candidateResource.validate();
		ResourceId resourceId = candidateResource.resourceId();
		String path = candidateResource.path();
		String prevPath=rootResourceMap.get(resourceId);
		if(prevPath!=null && !prevPath.equals(path)) {
			throw new ApplicationConfigurationException(String.format("Resource %s is already published (%s)",toString(resourceId),prevPath));
		}
		ResourceId prevResource=rootResourceMap.inverse().get(path);
		if(prevResource!=null && !prevResource.equals(resourceId)) {
			throw new ApplicationConfigurationException(String.format("Path '%s' is already used by resource %s",path,toString(prevResource)));
		}
		rootResourceMap.put(resourceId, path);
	}

	private Class<? extends ResourceHandler> resolveHandler(ResourceId resourceId) {
		ResourceTemplate template = this.templateManagementService.templateOfId(resourceId.templateId());
		return template!=null?template.handlerClass():null;
	}

	private String toString(ResourceId resourceId) {
		Class<?> handler=resolveHandler(resourceId);
		if(handler!=null) {
			return String.format("named '%s' with handler class '%s'",resourceId.name(),handler.getName());
		} else {
			return String.format("named '%s' of unknown template '%s'",resourceId.name(),resourceId.templateId());
		}
	}

}