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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-persistency:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-persistency-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.persistence;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public final class PersistencyFacade {

	private final ThreadLocal<EntityManager> manager;
	private final ThreadLocal<Long> timestamp;
	private final EntityManagerFactory emf;

	public PersistencyFacade(EntityManagerFactory emf) {
		this.emf = emf;
		this.manager=new ThreadLocal<EntityManager>();
		this.timestamp=new ThreadLocal<Long>();
	}

	private EntityManager getManager() {
		EntityManager entityManager = this.manager.get();
		if(entityManager==null) {
			entityManager = this.emf.createEntityManager();
			this.manager.set(entityManager);
		}
		return entityManager;
	}

	private Date getTimestamp() {
		Long millis = this.timestamp.get();
		if(millis==null) {
			millis = System.currentTimeMillis();
			this.timestamp.set(millis);
		}
		return new Date(millis);
	}

	public ApplicationManager getApplicationManager() {
		return new ApplicationManager(getManager(), getTimestamp());
	}

	public ResourceManager getResourceManager() {
		return new ResourceManager(getManager(),getTimestamp());
	}

	public void disposeManagers() {
		EntityManager entityManager = this.manager.get();
		if(entityManager!=null) {
			this.manager.set(null);
			entityManager.close();
		}
	}

}
