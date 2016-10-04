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

import javax.persistence.Persistence;

import org.ldp4j.application.kernel.constraints.ConstraintReportRepository;
import org.ldp4j.application.kernel.endpoint.EndpointRepository;
import org.ldp4j.application.kernel.lifecycle.LifecycleException;
import org.ldp4j.application.kernel.resource.ResourceRepository;
import org.ldp4j.application.kernel.spi.ModelFactory;
import org.ldp4j.application.kernel.spi.RuntimeDelegate;
import org.ldp4j.application.kernel.transaction.TransactionManager;

public final class JPARuntimeDelegate extends RuntimeDelegate {

	private final JPAEntityManagerProvider provider;
	private final JPAModelFactory modelFactory;
	private final JPAResourceRepository resourceRepository;
	private final JPATransactionManager transactionManager;
	private final JPAEndpointRepository endpointRepository;
	private final JPAConstraintReportRepository constraintReportRepository;

	public JPARuntimeDelegate() {
		this.provider = new JPAEntityManagerProvider();
		this.transactionManager = new JPATransactionManager(this.provider);
		this.endpointRepository = new JPAEndpointRepository(this.provider);
		this.resourceRepository = new JPAResourceRepository(this.provider);
		this.constraintReportRepository = new JPAConstraintReportRepository(this.provider);
		this.modelFactory = new JPAModelFactory(this.resourceRepository);
	}

	@Override
	public void init() throws LifecycleException {
		this.provider.setEntityManagerFactory(Persistence.createEntityManagerFactory("kernel"));
	}

	@Override
	public void shutdown() throws LifecycleException {
		this.provider.dispose();
	}

	@Override
	public ModelFactory getModelFactory() {
		return this.modelFactory;
	}

	@Override
	public TransactionManager getTransactionManager() {
		return this.transactionManager;
	}

	@Override
	public ResourceRepository getResourceRepository() {
		return this.resourceRepository;
	}

	@Override
	public EndpointRepository getEndpointRepository() {
		return this.endpointRepository;
	}

	@Override
	public ConstraintReportRepository getConstraintReportRepository() {
		return this.constraintReportRepository;
	}

	public void clear() {
		this.provider.close();
	}

}