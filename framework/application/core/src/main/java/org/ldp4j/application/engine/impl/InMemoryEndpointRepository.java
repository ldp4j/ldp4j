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
package org.ldp4j.application.engine.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.application.engine.endpoint.Endpoint;
import org.ldp4j.application.engine.endpoint.EndpointRepository;
import org.ldp4j.application.engine.lifecycle.LifecycleException;
import org.ldp4j.application.engine.lifecycle.Managed;
import org.ldp4j.application.engine.resource.ResourceId;

final class InMemoryEndpointRepository implements Managed, EndpointRepository {

	private final AtomicLong counter=new AtomicLong();

	private ReadWriteLock lock=new ReentrantReadWriteLock();
	private final Map<Long,Endpoint> endpointsById=new HashMap<Long,Endpoint>();
	private final Map<String,Long> endpointsByPath=new HashMap<String,Long>();
	private final Map<ResourceId,Long> endpointsByResourceName=new HashMap<ResourceId,Long>();

	InMemoryEndpointRepository() {
	}

	private Endpoint endpointOfId(Long id) {
		if(id==null) {
			return null;
		}
		return endpointsById.get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long nextIdentifier() {
		return counter.incrementAndGet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Endpoint endpointOfPath(String path) {
		lock.readLock().lock();
		try {
			return endpointOfId(endpointsByPath.get(path));
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Endpoint endpointOfResource(ResourceId id) {
		lock.readLock().lock();
		try {
			return endpointOfId(endpointsByResourceName.get(id));
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(Endpoint endpoint) {
		lock.writeLock().lock();
		try {
			endpointsByResourceName.remove(endpoint.resourceId());
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(Endpoint endpoint) {
		lock.writeLock().lock();
		try {
			if(endpointsById.containsKey(endpoint.id())) {
				throw new IllegalArgumentException("An endpoint with id '"+endpoint.id()+"' already exists");
			}
			if(endpointsByPath.containsKey(endpoint.path())) {
				throw new IllegalArgumentException("An endpoint with path '"+endpoint.path()+"' already exists");
			}
			if(endpointsByResourceName.containsKey(endpoint.resourceId())) {
				throw new IllegalArgumentException("An endpoint with resource name '"+endpoint.resourceId()+"' already exists");
			}
			endpointsById.put(endpoint.id(), endpoint);
			endpointsByPath.put(endpoint.path(), endpoint.id());
			endpointsByResourceName.put(endpoint.resourceId(), endpoint.id());
		} finally {
			lock.writeLock().unlock();
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
		lock.writeLock().lock();
		try {
			endpointsById.clear();
			endpointsByPath.clear();
			endpointsByResourceName.clear();
		} finally {
			lock.writeLock().unlock();
		}
	}

}