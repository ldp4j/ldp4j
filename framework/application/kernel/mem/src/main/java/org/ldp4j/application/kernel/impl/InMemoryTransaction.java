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

import org.ldp4j.application.kernel.transaction.Transaction;

import com.google.common.base.MoreObjects;

final class InMemoryTransaction implements Transaction {

	private interface TransactionState {

		TransactionState begin();

		TransactionState commit();

		TransactionState rollback();

		boolean isActive();

	}

	private final class PendingTransactionState implements TransactionState {

		private static final String TRANSACTION_NOT_INITIATED = "Transaction not initiated";

		@Override
		public TransactionState begin() {
			return new InFlightTransactionState();
		}

		@Override
		public TransactionState commit() {
			throw new IllegalStateException(TRANSACTION_NOT_INITIATED);
		}

		@Override
		public TransactionState rollback() {
			throw new IllegalStateException(TRANSACTION_NOT_INITIATED);
		}

		@Override
		public String toString() {
			return "pending";
		}

		@Override
		public boolean isActive() {
			return false;
		}

	}

	private final class InFlightTransactionState implements TransactionState {

		@Override
		public TransactionState begin() {
			throw new IllegalStateException("Transaction already initiated");
		}

		@Override
		public TransactionState commit() {
			transactionManager.disposeTransaction(InMemoryTransaction.this);
			return new CompletedTransactionState("commited");
		}

		@Override
		public TransactionState rollback() {
			transactionManager.disposeTransaction(InMemoryTransaction.this);
			return new CompletedTransactionState("rolledback");
		}

		@Override
		public String toString() {
			return "in-flight";
		}

		@Override
		public boolean isActive() {
			return true;
		}
	}

	private final class CompletedTransactionState implements TransactionState {

		private static final String TRANSACTION_ALREADY_FINISHED = "Transaction already finished";

		private String message;

		private CompletedTransactionState(String message) {
			this.message = message;
		}

		@Override
		public TransactionState begin() {
			throw new IllegalStateException(TRANSACTION_ALREADY_FINISHED);
		}

		@Override
		public TransactionState commit() {
			throw new IllegalStateException(TRANSACTION_ALREADY_FINISHED);
		}

		@Override
		public TransactionState rollback() {
			throw new IllegalStateException(TRANSACTION_ALREADY_FINISHED);
		}

		@Override
		public String toString() {
			return "completed ("+message+")";
		}

		@Override
		public boolean isActive() {
			return false;
		}

	}

	private final InMemoryTransactionManager transactionManager;
	private final long id;
	private TransactionState state;

	InMemoryTransaction(long id, InMemoryTransactionManager persistencyManager) {
		this.id = id;
		this.transactionManager = persistencyManager;
		this.state=new PendingTransactionState();
	}

	long id() {
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void begin() {
		this.state=this.state.begin();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit() {
		this.state=this.state.commit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rollback() {
		this.state=this.state.rollback();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive() {
		return this.state.isActive();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("id", this.id).
					add("state",this.state).
					add("persistencyManager",this.transactionManager).
					toString();

	}

}
