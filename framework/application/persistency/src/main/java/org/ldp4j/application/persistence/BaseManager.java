package org.ldp4j.application.persistence;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public abstract class BaseManager {

	private final EntityManager entityManager;
	private final Date timestamp;

	protected BaseManager(EntityManager entityManager, Date timestamp) {
		this.entityManager = entityManager;
		this.timestamp = timestamp;

	}

	protected final EntityManager getManager() {
		return this.entityManager;
	}

	protected final Date getTimestamp() {
		return this.timestamp;
	}

	public final void beginTransaction() {
		this.entityManager.getTransaction().begin();
	}

	public final void commitTransaction() {
		this.entityManager.getTransaction().commit();
	}

	public final void rollbackTransaction() {
		EntityTransaction transaction = this.entityManager.getTransaction();
		if(transaction.isActive()) {
			transaction.rollback();
		}
	}

}
