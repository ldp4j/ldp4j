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
package org.ldp4j.application.kernel.session;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UnitOfWork {

	private final class NullEventHandler implements EventHandler {

		@Override
		public void notifyObjectCreation(DelegatedResourceSnapshot obj) {
			// Nothing to do
		}

		@Override
		public void notifyObjectUpdate(DelegatedResourceSnapshot obj) {
			// Nothing to do
		}

		@Override
		public void notifyObjectDeletion(DelegatedResourceSnapshot obj) {
			// Nothing to do
		}

	}

	public interface EventHandler {

		public void notifyObjectCreation(DelegatedResourceSnapshot obj);

		public void notifyObjectUpdate(DelegatedResourceSnapshot obj);

		public void notifyObjectDeletion(DelegatedResourceSnapshot obj);

	}

	public interface Visitor {

		void visitNew(DelegatedResourceSnapshot obj);

		void visitDirty(DelegatedResourceSnapshot obj);

		void visitDeleted(DelegatedResourceSnapshot obj);

	}

	private static final String NEW     = "new";
	private static final String DIRTY   = "dirty";
	private static final String DELETED = "deleted";

	private static final String REGISTERED_OBJECT_OF_TYPE            = "Registered {} object '{}' of type '{}'";
	private static final String SNAPSHOT_CANNOT_BE_NULL              = "Snapshot cannot be null";
	private static final String SNAPSHOT_HAS_BEEN_ALREADY_DELETED    = "Snapshot has been already deleted";
	private static final String SNAPSHOT_HAS_BEEN_ALREADY_REGISTERED = "Snapshot has been already registered";
	private static final String SNAPSHOT_HAS_BEEN_ALREADY_MODIFIED   = "Snapshot has been already modified";

	private static final Logger LOGGER=LoggerFactory.getLogger(UnitOfWork.class);

	private static ThreadLocal<UnitOfWork> CURRENT=new ThreadLocal<UnitOfWork>();

	private final List<DelegatedResourceSnapshot> newObjects=new ArrayList<DelegatedResourceSnapshot>();
	private final List<DelegatedResourceSnapshot> dirtyObjects=new ArrayList<DelegatedResourceSnapshot>();
	private final List<DelegatedResourceSnapshot> deletedObjects=new ArrayList<DelegatedResourceSnapshot>();

	private EventHandler handler;

	private UnitOfWork() {
		this.handler=new NullEventHandler();
	}

	private void traceRegistration(DelegatedResourceSnapshot resource, String category) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(REGISTERED_OBJECT_OF_TYPE,category,resource.name(),resource.getClass().getCanonicalName());
		}
	}

	public void accept(Visitor visitor) {
		for(DelegatedResourceSnapshot obj:newObjects) {
			visitor.visitNew(obj);
		}
		for(DelegatedResourceSnapshot obj:dirtyObjects) {
			visitor.visitDirty(obj);
		}
		for(DelegatedResourceSnapshot obj:deletedObjects) {
			visitor.visitDeleted(obj);
		}
	}

	public void registerNew(DelegatedResourceSnapshot snapshot) {
		checkNotNull(snapshot,SNAPSHOT_CANNOT_BE_NULL);
		checkState(!newObjects.contains(snapshot),SNAPSHOT_HAS_BEEN_ALREADY_REGISTERED);
		checkState(!dirtyObjects.contains(snapshot),SNAPSHOT_HAS_BEEN_ALREADY_MODIFIED);
		checkState(!deletedObjects.contains(snapshot),SNAPSHOT_HAS_BEEN_ALREADY_DELETED);
		newObjects.add(snapshot);
		traceRegistration(snapshot,NEW);
		handler.notifyObjectCreation(snapshot);
	}

	public void registerDirty(DelegatedResourceSnapshot resource) {
		checkNotNull(resource,SNAPSHOT_CANNOT_BE_NULL);
		checkState(!deletedObjects.contains(resource),SNAPSHOT_HAS_BEEN_ALREADY_DELETED);
		if(!dirtyObjects.contains(resource) && !newObjects.contains(resource)) {
			dirtyObjects.add(resource);
			traceRegistration(resource,DIRTY);
			handler.notifyObjectUpdate(resource);
		}
	}

	public void registerDeleted(DelegatedResourceSnapshot snapshot) {
		checkNotNull(snapshot,SNAPSHOT_CANNOT_BE_NULL);
		if(newObjects.remove(snapshot)) {
			return;
		}
		dirtyObjects.remove(snapshot);
		if(!deletedObjects.contains(snapshot)) {
			deletedObjects.add(snapshot);
			traceRegistration(snapshot,DELETED);
			handler.notifyObjectDeletion(snapshot);
		}
	}

	public void setEventHandler(EventHandler handler) {
		if(handler==null) {
			this.handler=new NullEventHandler();
		} else {
			this.handler = handler;
		}
	}

	public static UnitOfWork newCurrent() {
		UnitOfWork uow = new UnitOfWork();
		setCurrent(uow);
		return uow;
	}

	public static void setCurrent(UnitOfWork uow) {
		CURRENT.set(uow);
	}

	public static UnitOfWork getCurrent() {
		return CURRENT.get();
	}

}