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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.application.kernel.constraints.ConstraintReport;
import org.ldp4j.application.kernel.constraints.ConstraintReportId;
import org.ldp4j.application.kernel.constraints.ConstraintReportRepository;
import org.ldp4j.application.kernel.lifecycle.LifecycleException;
import org.ldp4j.application.kernel.lifecycle.Managed;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

final class InMemoryConstraintReportRepository implements Managed, ConstraintReportRepository {

	private final ReadWriteLock lock=new ReentrantReadWriteLock();
	private final Multimap<ResourceId, String> failureIds;
	private final Map<ConstraintReportId,ConstraintReport> reports;

	InMemoryConstraintReportRepository() {
		this.reports=Maps.newLinkedHashMap();
		this.failureIds=LinkedHashMultimap.create();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConstraintReport constraintReportOfId(ConstraintReportId id) {
		checkNotNull(id,"Constraint report identifier cannot be null");
		this.lock.readLock().lock();
		try {
			return this.reports.get(id);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(ConstraintReport report) {
		checkNotNull(report,"Constraint report cannot be null");
		this.lock.writeLock().lock();
		try {
			ConstraintReportId id = report.id();
			this.failureIds.put(id.resourceId(),id.failureId());
			this.reports.put(id,report);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(ConstraintReport report) {
		checkNotNull(report,"Constraint report cannot be null");
		this.lock.writeLock().lock();
		try {
			ConstraintReportId reportId = report.id();
			this.failureIds.remove(reportId.resourceId(),reportId.failureId());
			this.reports.remove(reportId);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeByResource(Resource resource) {
		checkNotNull(resource,"Resource cannot be null");
		this.lock.writeLock().lock();
		try {
			ResourceId resourceId = resource.id();
			for(String constraintsId:this.failureIds.get(resourceId)) {
				this.reports.remove(ConstraintReportId.create(resourceId, constraintsId));
			}
			this.failureIds.removeAll(resourceId);
		} finally {
			this.lock.writeLock().unlock();
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
		this.lock.writeLock().lock();
		try {
			this.reports.clear();
		} finally {
			this.lock.writeLock().unlock();
		}
	}

}