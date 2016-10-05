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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-mem:0.2.2
 *   Bundle      : ldp4j-application-kernel-mem-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.impl;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.endpoint.EndpointRepository;
import org.ldp4j.application.kernel.lifecycle.LifecycleException;
import org.ldp4j.application.kernel.lifecycle.Managed;
import org.ldp4j.application.kernel.resource.ResourceId;

import com.google.common.collect.Maps;

final class InMemoryEndpointRepository implements Managed, EndpointRepository {

	private final Map<String,Endpoint> endpointsByPath=Maps.newLinkedHashMap();
	private final Map<ResourceId,Endpoint> endpointsByResourceName=Maps.newLinkedHashMap();

	private final Lock readLock;
	private final Lock writeLock;

	InMemoryEndpointRepository() {
		ReadWriteLock lock=new ReentrantReadWriteLock();
		this.readLock = lock.readLock();
		this.writeLock = lock.writeLock();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Endpoint endpointOfPath(String path) {
		this.readLock.lock();
		try {
			return this.endpointsByPath.get(path);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Endpoint endpointOfResource(ResourceId id) {
		this.readLock.lock();
		try {
			Endpoint endpoint = this.endpointsByResourceName.get(id);
			if(endpoint!=null && endpoint.deleted()!=null) {
				endpoint=null;
			}
			return endpoint;
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(Endpoint endpoint) {
		this.writeLock.lock();
		try {
			if(this.endpointsByPath.containsKey(endpoint.path())) {
				throw new IllegalArgumentException("An endpoint with path '"+endpoint.path()+"' already exists");
			}
			Endpoint other=endpointOfResource(endpoint.resourceId());
			if(other!=null) {
				throw new IllegalArgumentException("An endpoint with resource name '"+endpoint.resourceId()+"' already exists ("+other+")");
			}
			this.endpointsByPath.put(endpoint.path(), endpoint);
			this.endpointsByResourceName.put(endpoint.resourceId(),endpoint);
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws LifecycleException {
		// Nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown() throws LifecycleException {
		this.writeLock.lock();
		try {
			this.endpointsByPath.clear();
			this.endpointsByResourceName.clear();
		} finally {
			this.writeLock.unlock();
		}
	}

}