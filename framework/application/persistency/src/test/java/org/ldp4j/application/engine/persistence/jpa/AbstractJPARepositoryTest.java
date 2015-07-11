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

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.engine.lifecycle.LifecycleException;
import org.ldp4j.application.engine.resource.Container;
import org.ldp4j.application.engine.resource.Resource;
import org.ldp4j.application.engine.service.ServiceRegistry;
import org.ldp4j.application.engine.spi.RuntimeDelegate;
import org.ldp4j.application.engine.template.TemplateManagementService;
import org.ldp4j.application.engine.transaction.Transaction;
import org.ldp4j.application.engine.transaction.TransactionManager;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.example.PersonHandler;

import com.google.common.collect.Lists;

public abstract class AbstractJPARepositoryTest<T> {

	protected interface Task<E> {

		void execute(E repository) throws Exception;

	}

	private T sut;
	private TransactionManager txManager;
	private JPARuntimeDelegate delegate;

	@BeforeClass
	public static void setUpBefore() throws Exception {
		ServiceRegistry.setInstance(null);
		RuntimeDelegate.setInstance(new JPARuntimeDelegate());
		PersonHandler personHandler = new PersonHandler();
		ServiceRegistry.
			getInstance().
				getService(TemplateManagementService.class).
					configure(
						Lists.<Class<?>>newArrayList(),
						Arrays.<ResourceHandler>asList(personHandler));
	}

	@AfterClass
	public static void tearDownAfter() throws Exception {
		RuntimeDelegate.setInstance(null);
	}

	@Before
	public void setUp() throws LifecycleException {
		this.delegate = (JPARuntimeDelegate)RuntimeDelegate.getInstance();
		this.delegate.init();
		this.txManager=delegate.getTransactionManager();
		this.sut = getSubjectUnderTest(this.delegate);
	}

	@After
	public void tearDown() throws LifecycleException {
		this.delegate.shutdown();
	}

	protected final Container rootContainer(Name<?> name, String templateId) {
		return (Container)this.delegate.getResourceFactory().createResource(ServiceRegistry.getInstance().getService(TemplateManagementService.class).templateOfId(templateId), name);
	}

	protected final Resource rootResource(Name<?> name, String templateId) {
		return this.delegate.getResourceFactory().createResource(ServiceRegistry.getInstance().getService(TemplateManagementService.class).templateOfId(templateId), name);
	}



	protected final void clear() {
		this.delegate.clear();
	}

	protected final void withinTransaction(Task<T> task) throws Exception{
		Transaction tx = txManager.currentTransaction();
		tx.begin();
		try {
			task.execute(this.sut);
			tx.commit();
		} finally {
			if(!tx.isCompleted()) {
				tx.rollback();
			}
		}
	}

	protected abstract T getSubjectUnderTest(JPARuntimeDelegate delegate);

}
