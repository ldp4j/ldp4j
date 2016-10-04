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

import static com.google.common.base.Preconditions.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.application.kernel.lifecycle.LifecycleException;
import org.ldp4j.application.kernel.lifecycle.Managed;
import org.ldp4j.application.kernel.resource.Container;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.resource.ResourceRepository;
import org.ldp4j.application.kernel.resource.ResourceVisitor;

final class InMemoryResourceRepository implements Managed, ResourceRepository {

	private final ReadWriteLock lock=new ReentrantReadWriteLock();
	private final Map<ResourceId,Resource> resources=new LinkedHashMap<ResourceId,Resource>();
	private final Map<ResourceId,Container> containers=new LinkedHashMap<ResourceId,Container>();

	InMemoryResourceRepository() {
	}

	private Resource find(ResourceId id) {
		lock.readLock().lock();
		try {
			Resource result=resources.get(id);
			if(result==null) {
				result=containers.get(id);
			}
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Resource> T resourceById(ResourceId id, Class<? extends T> expectedResourceClass) {
		checkNotNull(expectedResourceClass,"Expected resource class cannot be null");
		Resource found=find(id);
		if(expectedResourceClass.isInstance(found)) {
			return expectedResourceClass.cast(found);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Resource resourceOfId(ResourceId id) {
		lock.readLock().lock();
		try {
			return resources.get(id);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Container containerOfId(ResourceId id) {
		lock.readLock().lock();
		try {
			return containers.get(id);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(Resource resource) {
		checkNotNull(resource,"Resource cannot be null");
		lock.writeLock().lock();
		try {
			checkState(find(resource.id())==null,"A resource with identifier '%s' already exists",resource.id());
			resource.accept(
				new ResourceVisitor() {
					@Override
					public void visitResource(Resource resource) {
						InMemoryResourceRepository.this.resources.put(resource.id(),resource);
					}
					@Override
					public void visitContainer(Container resource) {
						InMemoryResourceRepository.this.containers.put(resource.id(),resource);
					}
				}
			);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(Resource resource) {
		checkNotNull(resource,"Resource cannot be null");
		lock.writeLock().lock();
		try {
			resources.remove(resource.id());
			containers.remove(resource.id());
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
			resources.clear();
			containers.clear();
		} finally {
			lock.writeLock().unlock();
		}
	}

}