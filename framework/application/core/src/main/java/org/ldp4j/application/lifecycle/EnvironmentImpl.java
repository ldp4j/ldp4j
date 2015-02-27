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
package org.ldp4j.application.lifecycle;

import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.engine.ApplicationConfigurationException;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.setup.Environment;
import org.ldp4j.application.spi.PersistencyManager;
import org.ldp4j.application.template.ResourceTemplate;
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

		ResourceId resourceId() {
			return ResourceId.createId(this.resourceName, this.template.id());
		}

		String path() {
			return path;
		}


		void validate() throws ApplicationConfigurationException {
			if(this.resourceName==null) {
				throw new ApplicationConfigurationException("Resource name cannot be null");
			}
			if(this.handlerClass==null) {
				throw new ApplicationConfigurationException("No handler class specified for resource '"+this.resourceName+"'");
			}
			this.template = EnvironmentImpl.this.persistencyManager.templateOfHandler(handlerClass);
			if(this.template==null) {
				throw new ApplicationConfigurationException("Unknown resource handler '"+this.handlerClass.getCanonicalName()+"' specified for resource '"+this.resourceName+"'");
			}
			if(this.path==null) {
				throw new ApplicationConfigurationException("No path specified for resource '"+this.resourceName+"'");
			}
			if(!this.path.endsWith("/") || this.path.endsWith("//") || this.path.length()==1) {
				throw new ApplicationConfigurationException("Invalid path '"+this.path+"' specified for resource '"+this.resourceName+"': it must end with a single '/' and have at least one segment");
			}
		}

	}

	private final List<RootResource> candidates;

	private final PersistencyManager persistencyManager;

	EnvironmentImpl(PersistencyManager persistencyManager) {
		this.candidates=Lists.newArrayList();
		this.persistencyManager = persistencyManager;
	}

	@Override
	public void publishResource(Name<?> resourceName, Class<? extends ResourceHandler> handlerClass, String path) {
		RootResource rootResource = new RootResource(resourceName,handlerClass,path);
		this.candidates.add(rootResource);
	}

	void configureRootResources() throws ApplicationConfigurationException {
		BiMap<ResourceId, String> rootResources = getRootResourceMap();
		for(Entry<ResourceId, String> entry:rootResources.entrySet()) {
			publish(Resource.class,entry.getKey(),entry.getValue());
			LOGGER.debug("Published resource '"+entry.getKey()+"' at '"+entry.getValue()+"'");
		}
	}

	private BiMap<ResourceId, String> getRootResourceMap() throws ApplicationConfigurationException {
		BiMap<ResourceId,String> rootResourceMap=HashBiMap.create();
		for(RootResource candidateResource:this.candidates) {
			addPublication(rootResourceMap,candidateResource);
		}
		return rootResourceMap;
	}

	private void addPublication(final BiMap<ResourceId, String> rootResourceMap, RootResource candidateResource) throws ApplicationConfigurationException {
		candidateResource.validate();
		ResourceId resourceId = candidateResource.resourceId();
		String path = candidateResource.path();
		String prevPath=rootResourceMap.get(resourceId);
		if(prevPath!=null && !prevPath.equals(path)) {
			throw new ApplicationConfigurationException("Resource "+toString(resourceId)+"' is already published ("+prevPath+")");
		}
		ResourceId prevResource=rootResourceMap.inverse().get(path);
		if(prevResource!=null && !prevResource.equals(resourceId)) {
			throw new ApplicationConfigurationException("Path '"+path+"' is already used for resource "+toString(prevResource));
		}
		rootResourceMap.put(resourceId, path);
	}

	private Class<? extends ResourceHandler> resolveHandler(ResourceId resourceId) {
		ResourceTemplate template = this.persistencyManager.templateOfId(resourceId.templateId());
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

	private <T extends Resource> void publish(Class<? extends T> clazz, ResourceId resourceId, String path) throws ApplicationConfigurationException {
		Resource prevResource = this.persistencyManager.resourceOfId(resourceId);
		Endpoint prevEndpoint = this.persistencyManager.endpointOfPath(path);

		if(prevEndpoint!=null && !prevEndpoint.resourceId().equals(resourceId)) {
			throw new ApplicationConfigurationException("Resource "+toString(resourceId)+" cannot be published at '"+path+"' as that path is already in use by a resource "+toString(prevEndpoint.resourceId()));
		}

		if(prevEndpoint==null && prevResource!=null) {
			throw new ApplicationConfigurationException("Resource "+toString(resourceId)+" cannot be published at '"+path+"' as it is already published at '"+this.persistencyManager.endpointOfResource(resourceId).path()+"'");
		}

		if(prevResource==null && prevEndpoint==null) {
			T resource=this.persistencyManager.createResource(resourceId.templateId(),resourceId.name(),null,clazz);
			this.persistencyManager.add(resource);
			Endpoint endpoint=this.persistencyManager.createEndpoint(resource,path,new EntityTag(path),new Date());
			this.persistencyManager.add(endpoint);
		}

	}

}