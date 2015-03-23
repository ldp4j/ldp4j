package org.ldp4j.application.persistence;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public final class PersistencyFacade {

	private final ThreadLocal<EntityManager> manager;
	private final ThreadLocal<Long> timestamp;
	private final EntityManagerFactory emf;

	public PersistencyFacade(EntityManagerFactory emf) {
		this.emf = emf;
		this.manager=new ThreadLocal<EntityManager>();
		this.timestamp=new ThreadLocal<Long>();
	}

	private EntityManager getManager() {
		EntityManager entityManager = this.manager.get();
		if(entityManager==null) {
			entityManager = this.emf.createEntityManager();
			this.manager.set(entityManager);
		}
		return entityManager;
	}

	private Date getTimestamp() {
		Long millis = this.timestamp.get();
		if(millis==null) {
			millis = System.currentTimeMillis();
			this.timestamp.set(millis);
		}
		return new Date(millis);
	}

	public ApplicationManager getApplicationManager() {
		return new ApplicationManager(getManager(), getTimestamp());
	}

	public ResourceManager getResourceManager() {
		return new ResourceManager(getManager(),getTimestamp());
	}

	public void disposeManagers() {
		EntityManager entityManager = this.manager.get();
		if(entityManager!=null) {
			this.manager.set(null);
			entityManager.close();
		}
	}

}
