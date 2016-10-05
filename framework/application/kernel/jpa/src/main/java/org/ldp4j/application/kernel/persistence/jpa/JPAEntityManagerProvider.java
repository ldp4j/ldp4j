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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-jpa:0.2.2
 *   Bundle      : ldp4j-application-kernel-jpa-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.persistence.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JPAEntityManagerProvider implements EntityManagerProvider {

	private static final Logger LOGGER=LoggerFactory.getLogger(JPAEntityManagerProvider.class);

	private final ThreadLocal<EntityManager> manager;
	private final String id;
	private EntityManagerFactory emf;

	JPAEntityManagerProvider() {
		this.id = String.format("%08X",hashCode());
		this.manager=new ThreadLocal<EntityManager>();
	}

	private void trace(String message, Object... args) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("{} - {} - {}",
				this.id,
				String.format(message,args),
				Context.getContext(JPARuntimeDelegate.class.getPackage().getName()));
		}
	}

	private boolean isTransactionActive() {
		boolean result = false;
		EntityManager entityManager = this.manager.get();
		if(entityManager!=null) {
			result=entityManager.getTransaction().isActive();
		}
		return result;
	}

	private EntityManager getManager() {
		EntityManager entityManager = this.manager.get();
		if(entityManager==null) {
			entityManager = this.emf.createEntityManager();
			this.manager.set(entityManager);
			trace("Assigned manager %08X",entityManager.hashCode());
		} else {
			trace("Returned manager %08X",entityManager.hashCode());
		}
		return entityManager;
	}

	void setEntityManagerFactory(EntityManagerFactory emf) {
		this.emf = emf;
	}

	void dispose() {
		if(this.emf!=null && this.emf.isOpen()) {
			this.emf.close();
		}
	}

	@Override
	public EntityManager entityManager() {
		return getManager();
	}

	@Override
	public void close() {
		EntityManager entityManager = this.manager.get();
		if(entityManager!=null) {
			entityManager.close();
			this.manager.remove();
			trace("Disposed manager %08X",entityManager.hashCode());
		} else {
			trace("Nothing to dispose");
		}
	}
	@Override
	public boolean isActive() {
		return isTransactionActive();
	}

}