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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server;

import java.net.URI;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.server.blueprint.ComponentRegistry;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public final class ResourceIndexImpl implements ResourceIndex {

	private final ComponentRegistry registry;

	private final ReadWriteLock lock=new ReentrantReadWriteLock();
	private final BiMap<ManagedIndividualId, URI> index;


	public ResourceIndexImpl(ComponentRegistry registry) {
		this(registry,HashBiMap.<ManagedIndividualId, URI>create());
	}

	@VisibleForTesting
	private ResourceIndexImpl(ComponentRegistry registry, BiMap<ManagedIndividualId, URI> create) {
		this.registry = registry;
		this.index = create;
	}

	private ManagedIndividualId from(ResourceId id) {
		return ManagedIndividualId.createId(id.name(), id.templateId());
	}

	@Override
	public ComponentRegistry getRegistry() {
		return registry;
	}

	@Override
	public boolean isPublished(ResourceId id) {
		lock.readLock().lock();
		try {
			return index.containsKey(from(id));
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean isActive(URI endpoint) {
		lock.readLock().lock();
		try {
			return index.containsValue(endpoint);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void publish(ResourceId id, URI path) {
		lock.writeLock().lock();
		try {
			ManagedIndividualId adaptedId = from(id);
			URI previousPath= index.get(adaptedId);
			ManagedIndividualId previousId = index.inverse().get(path);
			if(previousPath==null && previousId==null) {
				index.put(adaptedId,path);
			} else if(path.equals(previousPath)) {
				return;
			} else {
				throw new IllegalStateException(String.format("Could not publish '%1$s' at '2$%s': '%1$s' --> '%4$s' and '%3$s' --> '%2$s'",adaptedId,path,previousId,previousPath));
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean unpublish(ResourceId id) {
		lock.writeLock().lock();
		try {
			return index.remove(from(id))!=null;
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public URI resolveResource(ManagedIndividualId id) {
		lock.readLock().lock();
		try {
			return index.get(id);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public ManagedIndividualId resolveLocation(URI path) {
		lock.readLock().lock();
		try {
			return index.inverse().get(path);
		} finally {
			lock.readLock().unlock();
		}
	}

}
