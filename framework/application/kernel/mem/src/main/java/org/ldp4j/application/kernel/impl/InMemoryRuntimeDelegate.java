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

import org.ldp4j.application.kernel.constraints.ConstraintReportRepository;
import org.ldp4j.application.kernel.endpoint.EndpointRepository;
import org.ldp4j.application.kernel.lifecycle.LifecycleException;
import org.ldp4j.application.kernel.resource.ResourceRepository;
import org.ldp4j.application.kernel.spi.ModelFactory;
import org.ldp4j.application.kernel.spi.RuntimeDelegate;
import org.ldp4j.application.kernel.transaction.TransactionManager;

public final class InMemoryRuntimeDelegate extends RuntimeDelegate {

	private final InMemoryModelFactory modelFactory;
	private final InMemoryResourceRepository resourceRepository;
	private final InMemoryEndpointRepository endpointRepository;
	private final InMemoryConstraintReportRepository constraintReportRepository;
	private final InMemoryTransactionManager transactionManager;

	public InMemoryRuntimeDelegate() {
		this.modelFactory= new InMemoryModelFactory();
		this.resourceRepository=new InMemoryResourceRepository();
		this.endpointRepository=new InMemoryEndpointRepository();
		this.constraintReportRepository=new InMemoryConstraintReportRepository();
		this.transactionManager = new InMemoryTransactionManager();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ModelFactory getModelFactory() {
		return this.modelFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConstraintReportRepository getConstraintReportRepository() {
		return this.constraintReportRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EndpointRepository getEndpointRepository() {
		return this.endpointRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResourceRepository getResourceRepository() {
		return this.resourceRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TransactionManager getTransactionManager() {
		return this.transactionManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws LifecycleException {
		this.resourceRepository.init();
		this.constraintReportRepository.init();
		this.endpointRepository.init();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown() throws LifecycleException {
		this.endpointRepository.shutdown();
		this.constraintReportRepository.shutdown();
		this.resourceRepository.shutdown();
	}

}