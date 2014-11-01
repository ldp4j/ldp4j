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

import org.ldp4j.application.EntityTag;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.spi.EndpointRepository;
import org.ldp4j.application.spi.Service;
import org.ldp4j.application.spi.ServiceBuilder;

public final class EndpointFactoryService implements Service {

	private static final class EndpointFactoryBuilder extends ServiceBuilder<EndpointFactoryService> {

		private EndpointFactoryBuilder() {
			super(EndpointFactoryService.class);
		}
		
		@Override
		public EndpointFactoryService build() {
			return 
				new EndpointFactoryService(
					endpointRepository());
		}

	}

	private final EndpointRepository endpointRepository;

	private EndpointFactoryService(EndpointRepository endpointRepository) {
		this.endpointRepository = endpointRepository;
	}

	public Endpoint createEndpoint(Resource resource, String path, EntityTag entityTag, Date lastModified) {
		checkNotNull(resource,"Endpoint's resource cannot be null");
		checkNotNull(entityTag,"Endpoint's entity tag cannot be null");
		checkNotNull(lastModified,"Endpoint's Last modified data cannot be null");
		return new EndpointImpl(this.endpointRepository.nextIdentifier(),path,resource.id(),entityTag,lastModified);
	}

	public static ServiceBuilder<EndpointFactoryService> serviceBuilder() {
		return new EndpointFactoryBuilder();
	}
	
	public static EndpointFactoryService defaultFactory() {
		return serviceBuilder().build();
	}

}