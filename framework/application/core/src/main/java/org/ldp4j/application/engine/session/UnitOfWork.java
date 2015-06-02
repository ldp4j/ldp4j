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
package org.ldp4j.application.engine.session;

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
			LOGGER.trace("Registered "+category+" object '"+resource.name()+"' of type '"+resource.getClass().getCanonicalName()+"'");
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
		checkNotNull(snapshot,"Snapshot cannot be null");
		checkState(!newObjects.contains(snapshot),"Snapshot has been already registered");
		checkState(!dirtyObjects.contains(snapshot),"Snapshot has been already modified");
		checkState(!deletedObjects.contains(snapshot),"Snapshot has been already deleted");
		newObjects.add(snapshot);
		traceRegistration(snapshot, "new");
		handler.notifyObjectCreation(snapshot);
	}

	public void registerDirty(DelegatedResourceSnapshot resource) {
		checkNotNull(resource,"Snapshot cannot be null");
		checkState(!deletedObjects.contains(resource),"Snapshot has been already deleted");
		if(!dirtyObjects.contains(resource) && !newObjects.contains(resource)) {
			dirtyObjects.add(resource);
			traceRegistration(resource, "dirty");
			handler.notifyObjectUpdate(resource);
		}
	}

	public void registerDeleted(DelegatedResourceSnapshot snapshot) {
		checkNotNull(snapshot,"Snapshot cannot be null");
		if(newObjects.remove(snapshot)) {
			return;
		}
		dirtyObjects.remove(snapshot);
		if(!deletedObjects.contains(snapshot)) {
			deletedObjects.add(snapshot);
			traceRegistration(snapshot, "deleted");
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