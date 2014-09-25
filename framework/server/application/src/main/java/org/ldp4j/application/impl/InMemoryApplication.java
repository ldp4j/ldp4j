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
package org.ldp4j.application.impl;

import org.ldp4j.application.spi.RepositoryRegistry;
import org.ldp4j.application.spi.ResourceHandlerRegistry;
import org.ldp4j.application.spi.RuntimeInstance;
import org.ldp4j.application.spi.ServiceRegistry;

public final class InMemoryApplication extends RuntimeInstance {

	private final ServiceRegistry serviceRegistry;
	private final ResourceHandlerRegistry resourceHandlerRegistry;
	private final RepositoryRegistry repositoryRegistry;

	public InMemoryApplication() {
		this.serviceRegistry = new InMemoryServiceRegistry();
		this.resourceHandlerRegistry=new InMemoryResourceHandlerRegistry();
		this.repositoryRegistry = new InMemoryRepositoryRegistry();
	}
	
	@Override
	public RepositoryRegistry getRepositoryRegistry() {
		return repositoryRegistry;
	}

	@Override
	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	@Override
	public ResourceHandlerRegistry getResourceHandlerRegistry() {
		return resourceHandlerRegistry;
	}

}