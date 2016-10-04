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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application;

import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.atomic.AtomicLong;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SessionTerminationException;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

final class ContextWriteSessionState implements WriteSession {

	private static final Logger LOGGER=LoggerFactory.getLogger(ApplicationContext.class);

	private static final AtomicLong SESSION_COUNTER=new AtomicLong();

	private final long id;
	private final WriteSession delegate;
	private final ContextWriteSessionStateListener listener;

	private boolean disposed;
	private boolean completed;

	ContextWriteSessionState(WriteSession delegate, ContextWriteSessionStateListener listener) {
		this.delegate = delegate;
		this.listener = listener;
		this.id=SESSION_COUNTER.incrementAndGet();
	}

	private void doDispose() throws SessionTerminationException {
		this.listener.onDispose(this);
		this.disposed=true;
		this.delegate.close();
	}

	private void verifyExecutability() {
		checkState(!this.disposed,"Session has already been disposed");
		checkState(!this.completed,"Session has already been completed");
	}

	long id() {
		return this.id;
	}

	synchronized void dispose() {
		LOGGER.warn("Closing session {} which was not closed by the user...",this);
		try {
			doDispose();
		} catch (SessionTerminationException e) {
			LOGGER.error("Could not close session {}",this,e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <S extends ResourceSnapshot> S resolve(Class<? extends S> snapshotClass, Individual<?, ?> individual) {
		verifyExecutability();
		return this.delegate.resolve(snapshotClass,individual);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <S extends ResourceSnapshot> S find(Class<? extends S> snapshotClass, Name<?> id, Class<? extends ResourceHandler> handlerClass) {
		verifyExecutability();
		return this.delegate.find(snapshotClass,id,handlerClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void modify(ResourceSnapshot resource) {
		verifyExecutability();
		this.delegate.modify(resource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void delete(ResourceSnapshot resource) {
		verifyExecutability();
		this.delegate.delete(resource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void saveChanges() throws WriteSessionException {
		verifyExecutability();
		this.delegate.saveChanges();
		this.completed=true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void discardChanges() throws WriteSessionException {
		verifyExecutability();
		this.delegate.discardChanges();
		this.completed=true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void close() throws SessionTerminationException {
		if(this.disposed) {
			return;
		}
		doDispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("id",this.id).
					add("completed",this.completed).
					add("disposed",this.disposed).
					add("delegate",this.delegate).
					toString();
	}

}