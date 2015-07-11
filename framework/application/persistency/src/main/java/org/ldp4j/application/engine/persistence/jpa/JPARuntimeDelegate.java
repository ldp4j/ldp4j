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
package org.ldp4j.application.engine.persistence.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.ldp4j.application.engine.constraints.ConstraintReportRepository;
import org.ldp4j.application.engine.endpoint.EndpointRepository;
import org.ldp4j.application.engine.lifecycle.LifecycleException;
import org.ldp4j.application.engine.resource.ResourceFactory;
import org.ldp4j.application.engine.resource.ResourceRepository;
import org.ldp4j.application.engine.spi.RuntimeDelegate;
import org.ldp4j.application.engine.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JPARuntimeDelegate extends RuntimeDelegate {

	private final class JPAEntityManagerProvider implements EntityManagerProvider {

		@Override
		public EntityManager entityManager() {
			return getManager();
		}

		@Override
		public void close() {
			disposeManager();
		}
		@Override
		public boolean isActive() {
			return isTransactionActive();
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(JPARuntimeDelegate.class);

	private final ThreadLocal<EntityManager> manager;

	private EntityManagerFactory emf;

	private final JPAEntityManagerProvider provider;

	private final String id;

	private final JPAResourceFactory resourceFactory;

	private JPAResourceRepository resourceRepository;

	public JPARuntimeDelegate() {
		this.manager=new ThreadLocal<EntityManager>();
		this.provider = new JPAEntityManagerProvider();
		this.id = String.format("%08X",hashCode());
		this.resourceRepository = new JPAResourceRepository(this.provider);
		this.resourceFactory = new JPAResourceFactory(this.resourceRepository);
	}

	@Override
	public void init() throws LifecycleException {
		this.emf=Persistence.createEntityManagerFactory("kernel");
	}

	@Override
	public void shutdown() throws LifecycleException {
		if(this.emf!=null) {
			this.emf.close();
		}
	}

	@Override
	public ResourceFactory getResourceFactory() {
		return this.resourceFactory;
	}

	@Override
	public TransactionManager getTransactionManager() {
		return new JPATransactionManager(this.provider);
	}

	@Override
	public ResourceRepository getResourceRepository() {
		return this.resourceRepository;
	}

	@Override
	public EndpointRepository getEndpointRepository() {
		return new JPAEndpointRepository(this.provider);
	}

	@Override
	public ConstraintReportRepository getConstraintReportRepository() {
		return new JPAConstraintReportRepository(this.provider);
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

	private void disposeManager() {
		EntityManager entityManager = this.manager.get();
		if(entityManager!=null) {
			entityManager.close();
			this.manager.remove();
			trace("Disposed manager %08X",entityManager.hashCode());
		} else {
			trace("Nothing to dispose");
		}
	}

	public void clear() {
		disposeManager();
	}

}