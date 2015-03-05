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

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.application.data.validation.ValidationReport;
import org.ldp4j.application.lifecycle.LifecycleException;
import org.ldp4j.application.lifecycle.Managed;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

final class InMemoryFailureRepository implements Managed {

	private final ReadWriteLock lock=new ReentrantReadWriteLock();
	private final Map<ResourceId,Map<String,ValidationReport>> reports;

	InMemoryFailureRepository() {
		this.reports=Maps.newLinkedHashMap();
	}

	private String nextFailureId(Map<String, ValidationReport> reports) {
		String failureId=null;
		do {
			failureId=UUID.randomUUID().toString();
		} while(!reports.containsKey(failureId));
		return failureId;
	}

	ValidationReport validationReport(Resource resource, String failureId) {
		checkNotNull(resource,"Resource cannot be null");
		lock.readLock().lock();
		try {
			ValidationReport result=null;
			Map<String, ValidationReport> map = reports.get(resource.id());
			if(map!=null) {
				result=map.get(failureId);
			}
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	Set<String> failuresOfResource(Resource resource, String failureId) {
		checkNotNull(resource,"Resource cannot be null");
		lock.readLock().lock();
		try {
			Set<String> result=Sets.newLinkedHashSet();
			Map<String, ValidationReport> reports = this.reports.get(resource.id());
			if(reports!=null) {
				result.addAll(reports.keySet());
			}
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	String add(Resource resource, ValidationReport report) {
		checkNotNull(resource,"Resource cannot be null");
		lock.writeLock().lock();
		try {
			Map<String, ValidationReport> reports = this.reports.get(resource.id());
			if(reports!=null) {
				reports=Maps.newLinkedHashMap();
				this.reports.put(resource.id(), reports);
			}
			String failureId = nextFailureId(reports);
			reports.put(failureId, report);
			return failureId;
		} finally {
			lock.writeLock().unlock();
		}
	}

	void remove(Resource resource, String failureId) {
		checkNotNull(resource,"Resource cannot be null");
		this.lock.writeLock().lock();
		try {
			Map<String, ValidationReport> reports = this.reports.get(resource.id());
			if(reports!=null) {
				reports.remove(failureId);
			}
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	void removeAll(Resource resource) {
		checkNotNull(resource,"Resource cannot be null");
		this.lock.writeLock().lock();
		try {
			this.reports.remove(resource.id());
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public void init() throws LifecycleException {
		// Nothing to do
	}

	@Override
	public void shutdown() throws LifecycleException {
		this.lock.writeLock().lock();
		try {
			this.reports.clear();
		} finally {
			this.lock.writeLock().unlock();
		}
	}
}