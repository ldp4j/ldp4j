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
package org.ldp4j.application.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.application.deployment.ApplicationDeployment;
import org.ldp4j.application.lifecycle.LifecycleException;
import org.ldp4j.application.lifecycle.Managed;
import org.ldp4j.application.spi.ApplicationDeploymentRepository;
import org.ldp4j.application.util.Index;
import org.ldp4j.application.util.Index.EntityResolver;
import org.ldp4j.application.util.Index.FacetResolver;
import org.ldp4j.application.util.Index.Facet;

import com.google.common.collect.Maps;

final class InMemoryApplicationDeploymentRepository implements ApplicationDeploymentRepository, Managed {

	private final Lock write;
	private final Lock read;
	private final Map<String,ApplicationDeployment> deployments;
	private final Index<ApplicationDeployment,String> index;
	private final Facet contextFacet;

	InMemoryApplicationDeploymentRepository() {
		ReadWriteLock lock=new ReentrantReadWriteLock();
		this.write=lock.writeLock();
		this.read=lock.readLock();
		this.deployments=Maps.newLinkedHashMap();
		this.index=
			Index.create(
				new EntityResolver<ApplicationDeployment,String>() {
				@Override
				public String id(ApplicationDeployment entity) {
					return entity.context();
				}
				@Override
				public ApplicationDeployment resolve(String id) {
					return deployments.get(id);
				}
			}
		);
		this.contextFacet=this.index.registerFacet(
			new FacetResolver<String,ApplicationDeployment>(){
				@Override
				public String name() {
					return "Context";
				}
				@Override
				public String get(ApplicationDeployment entity) {
					return entity.context();
				}
			}
		);
	}

	@Override
	public void init() throws LifecycleException {
		// Nothing to do
	}

	@Override
	public void shutdown() throws LifecycleException {
		this.write.lock();
		try {
			this.deployments.clear();
			this.index.clear();
		} finally {
			this.write.unlock();
		}
	}

	@Override
	public ApplicationDeployment applicationOfContext(String path) {
		this.read.lock();
		try {
			ApplicationDeployment deployment=null;
			List<ApplicationDeployment> search = this.index.findByFacetValue(this.contextFacet, path);
			if(!search.isEmpty()) {
				deployment=search.get(0);
			}
			return deployment;
		} finally {
			this.read.unlock();
		}
	}

	@Override
	public void add(ApplicationDeployment application) {
		checkNotNull(application,"Application deployment cannot be null");
		this.write.lock();
		try {
			this.deployments.put(application.context(),application);
			this.index.index(application);
		} finally {
			this.write.unlock();
		}
	}

	@Override
	public void remove(ApplicationDeployment application) {
		checkNotNull(application,"Application deployment cannot be null");
		this.write.lock();
		try {
			this.deployments.remove(application.context());
			this.index.remove(application);
		} finally {
			this.write.unlock();
		}
	}

}