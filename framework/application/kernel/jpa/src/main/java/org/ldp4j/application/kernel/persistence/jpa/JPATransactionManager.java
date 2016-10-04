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

import javax.persistence.EntityTransaction;

import org.ldp4j.application.kernel.transaction.Transaction;
import org.ldp4j.application.kernel.transaction.TransactionException;
import org.ldp4j.application.kernel.transaction.TransactionManager;

final class JPATransactionManager implements TransactionManager {

	private final class JPATransaction implements Transaction {

		private EntityTransaction nativeTransaction() {
			return provider.entityManager().getTransaction();
		}

		@Override
		public boolean isActive() {
			return provider.isActive();
		}

		@Override
		public void begin() {
			try {
				nativeTransaction().begin();
			} catch (Exception e) {
				throw new TransactionException("Begin failed",e);
			}
		}

		@Override
		public void commit() {
			try {
				nativeTransaction().commit();
			} catch (Exception e) {
				throw new TransactionException("Commit failed",e);
			} finally {
				if(!provider.isActive()) {
					provider.close();
				}
			}
		}

		@Override
		public void rollback() {
			if(provider.isActive()) {
				try {
					nativeTransaction().rollback();
				} catch (Exception e) {
					throw new TransactionException("Rollback failed",e);
				} finally {
					provider.close();
				}
			}
		}

	}

	private final EntityManagerProvider provider;

	JPATransactionManager(EntityManagerProvider provider) {
		this.provider = provider;
	}

	@Override
	public Transaction currentTransaction() {
		return new JPATransaction();
	}
}