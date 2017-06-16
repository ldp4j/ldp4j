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
package org.ldp4j.application.kernel.engine;

import java.util.Date;

import org.ldp4j.application.engine.context.Capabilities;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.engine.context.PublicEndpoint;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.template.ResourceTemplate;

abstract class DefaultPublicEndpoint implements PublicEndpoint {

	private final Endpoint endpoint;
	private final DefaultApplicationContext applicationContext;
	private final DefaultPublicResourceFactory factory;

	private ResourceTemplate template;
	private Resource resource;
	private Capabilities capabilities;

	protected DefaultPublicEndpoint(DefaultApplicationContext applicationContext, Endpoint endpoint) {
		this.applicationContext = applicationContext;
		this.endpoint = endpoint;
		this.factory = DefaultPublicResourceFactory.newInstance(applicationContext);
	}

	protected final boolean isRoot() {
		return resolveAs(Resource.class).isRoot();
	}

	protected final DefaultPublicResource parent() {
		return createResource(resolveAs(Resource.class).parentId());
	}

	protected final Endpoint endpoint() {
		return this.endpoint;
	}

	protected final DefaultApplicationContext applicationContext() {
		return this.applicationContext;
	}

	protected final DefaultPublicResource createResource(ResourceId resourceId) {
		return this.factory.createResource(this.applicationContext.resolveResource(resourceId));
	}

	protected final ResourceTemplate template() {
		if(this.template==null) {
			this.template = this.applicationContext.resourceTemplate(resolveAs(Resource.class));
			if(this.template==null) {
				throw new IllegalStateException("Could not find template for resource "+resource.id());
			}
		}
		return this.template;
	}

	protected final <T extends Resource> T resolveAs(Class<? extends T> clazz) {
		if(this.resource==null) {
			this.resource = this.applicationContext.resolveResource(this.endpoint);
			if(this.resource==null) {
				throw new IllegalStateException("Expected resource at "+endpoint().path());
			}
		}
		if(!clazz.isInstance(this.resource)) {
			throw new IllegalStateException("Expected resource "+this.resource.id()+" to be an instance of "+clazz.getCanonicalName());
		}
		return clazz.cast(this.resource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Status status() {
		Status result=Status.PUBLISHED;
		if(this.endpoint.deleted()!=null) {
			return Status.GONE;
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String path() {
		return this.endpoint.path();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final EntityTag entityTag() {
		return this.endpoint.entityTag();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Date lastModified() {
		return new Date(endpoint.lastModified().getTime());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Capabilities capabilities() {
		if(this.capabilities==null) {
			this.capabilities = this.applicationContext.endpointCapabilities(this.endpoint);
		}
		return this.capabilities;
	}

}
