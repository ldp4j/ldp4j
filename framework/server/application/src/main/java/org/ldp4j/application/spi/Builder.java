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
package org.ldp4j.application.spi;

import org.ldp4j.application.endpoint.EndpointFactoryService;
import org.ldp4j.application.resource.ResourceFactoryService;

public abstract class Builder<T,B extends Builder<T,B>> {
	
	private EndpointRepository endpointRepository;
	private ResourceRepository resourceRepository;

	private RuntimeInstance runtimeInstance;

	public final B setRuntimeInstance(RuntimeInstance runtimeInstance) {
		if(runtimeInstance==null) {
			throw new IllegalArgumentException("RuntimeInstance cannot be null");
		}
		this.runtimeInstance = runtimeInstance;
		return builder();
	}

	public final B withEndpointRepository(EndpointRepository endpointRepository) {
		this.endpointRepository = endpointRepository;
		return builder();
	}
	
	public final B withResourceRepository(ResourceRepository resourceRepository) {
		this.resourceRepository = resourceRepository;
		return builder();
	}

	
	private final RuntimeInstance runtimeInstance() {
		if(runtimeInstance!=null) {
			return runtimeInstance;
		} else {
			return RuntimeInstance.getInstance();
		}
	}

	private final RepositoryRegistry repositoryRegistry() {
		return runtimeInstance().getRepositoryRegistry();
	}

	protected final ResourceFactoryService resourceFactoryService() {
		return service(ResourceFactoryService.class);
	}

	protected final EndpointFactoryService endpointFactoryService() {
		return service(EndpointFactoryService.class);
	}

	protected final ResourceHandlerRegistry resourceHandlerRegistry() {
		return runtimeInstance().getResourceHandlerRegistry();
	}

	protected final <S extends Service> S service(Class<? extends S> serviceClass) {
		return runtimeInstance().getServiceRegistry().getService(serviceClass);
	}

	protected final ResourceRepository resourceRepository() {
		if(this.resourceRepository==null) {
			this.resourceRepository=repositoryRegistry().getResourceRepository();
		}
		return this.resourceRepository;
	}

	protected final EndpointRepository endpointRepository() {
		if(this.endpointRepository==null) {
			this.endpointRepository=repositoryRegistry().getEndpointRepository();
		}
		return this.endpointRepository;
	}

	protected abstract B builder();

	public abstract T build();

}