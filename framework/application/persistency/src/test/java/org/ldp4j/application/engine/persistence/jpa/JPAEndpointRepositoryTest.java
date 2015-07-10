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

import java.util.Date;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.engine.endpoint.Endpoint;
import org.ldp4j.application.engine.endpoint.EndpointRepository;
import org.ldp4j.application.engine.persistence.jpa.JPARuntimeDelegate;
import org.ldp4j.application.engine.resource.ResourceId;
import org.ldp4j.application.engine.transaction.Transaction;
import org.ldp4j.application.engine.transaction.TransactionManager;

public class JPAEndpointRepositoryTest {

	private EndpointRepository sut;
	private TransactionManager txManager;
	private JPARuntimeDelegate delegate;

	@Before
	public void setUp() {
		delegate = new JPARuntimeDelegate();
		this.sut = delegate.getEndpointRepository();
		this.txManager=delegate.getTransactionManager();
	}

	@Test
	public void testAdd() throws Exception {
		Name<String> name = NamingScheme.getDefault().name("resource");
		ResourceId resourceId = ResourceId.createId(name,"template");
		EntityTag entityTag = new EntityTag("Entity tag");
		final Endpoint ep1 = Endpoint.create("path",resourceId,new Date(), entityTag);
		withinTransaction(
			new Runnable() {
				@Override
				public void run() {
					sut.add(ep1);
				}
			}
		);
		delegate.clear();
		withinTransaction(
			new Runnable() {
				@Override
				public void run() {
					Endpoint result = sut.endpointOfPath(ep1.path());
					System.out.println(result);
				}
			}
		);
		delegate.clear();
		withinTransaction(
			new Runnable() {
				@Override
				public void run() {
					Endpoint result = sut.endpointOfResource(ep1.resourceId());
					System.out.println(result);
				}
			}
		);
		delegate.clear();
	}

	private void withinTransaction(Runnable callable) {
		Transaction tx = txManager.currentTransaction();
		tx.begin();
		try {
			callable.run();
			tx.commit();
		} finally {
			if(!tx.isCompleted()) {
				tx.rollback();
			}
		}
	}

}
