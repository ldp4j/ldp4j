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
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.application.ConstraintReport;
import org.ldp4j.application.ConstraintReportId;
import org.ldp4j.application.lifecycle.LifecycleException;
import org.ldp4j.application.lifecycle.Managed;
import org.ldp4j.application.resource.ResourceId;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

final class InMemoryConstraintReportRepository implements Managed {

	private final ReadWriteLock lock=new ReentrantReadWriteLock();
	private final Multimap<ResourceId, String> constraintIds;
	private final Map<ConstraintReportId,ConstraintReport> reports;

	InMemoryConstraintReportRepository() {
		this.reports=Maps.newLinkedHashMap();
		this.constraintIds=LinkedHashMultimap.create();
	}

	private String nextConstraintsId(ResourceId resourceId) {
		String constraintId=null;
		do {
			constraintId=UUID.randomUUID().toString();
		} while(this.constraintIds.get(resourceId).contains(constraintId));
		this.constraintIds.put(resourceId, constraintId);
		return constraintId;
	}

	ConstraintReport constraintReportOfId(ConstraintReportId id) {
		checkNotNull(id,"Constraint report identifier cannot be null");
		lock.readLock().lock();
		try {
			return this.reports.get(id);
		} finally {
			lock.readLock().unlock();
		}
	}

	void add(InMemoryConstraintReport report) {
		checkNotNull(report,"Constraint report cannot be null");
		this.lock.writeLock().lock();
		try {
			report.setConstraintsId(nextConstraintsId(report.resourceId()));
			this.reports.put(report.id(),report);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	void remove(ConstraintReport report) {
		checkNotNull(report,"Constraint report cannot be null");
		this.lock.writeLock().lock();
		try {
			ConstraintReportId reportId = report.id();
			this.constraintIds.remove(reportId.resourceId(),reportId.constraintsId());
			this.reports.remove(reportId);
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