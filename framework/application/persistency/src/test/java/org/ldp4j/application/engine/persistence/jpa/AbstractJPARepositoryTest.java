package org.ldp4j.application.engine.persistence.jpa;

import org.junit.After;
import org.junit.Before;
import org.ldp4j.application.engine.lifecycle.LifecycleException;
import org.ldp4j.application.engine.transaction.Transaction;
import org.ldp4j.application.engine.transaction.TransactionManager;

public abstract class AbstractJPARepositoryTest<T> {

	protected interface Task<E> {

		void execute(E repository) throws Exception;

	}

	private T sut;
	private TransactionManager txManager;
	private JPARuntimeDelegate delegate;

	@Before
	public void setUp() throws LifecycleException {
		this.delegate = new JPARuntimeDelegate();
		this.delegate.init();
		this.txManager=delegate.getTransactionManager();
		this.sut = getSubjectUnderTest(this.delegate);
	}

	@After
	public void tearDown() throws LifecycleException {
		this.delegate.shutdown();
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
