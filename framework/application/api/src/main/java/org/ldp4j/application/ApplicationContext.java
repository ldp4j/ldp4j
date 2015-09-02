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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.ldp4j.application.spi.RuntimeDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.*;


/**
 * A proxy class that acts as a facade for interacting with the Application
 * Engine proactively.
 */
public final class ApplicationContext {

	private static final class ApplicationEngineSingleton {

		private static final ApplicationContext SINGLETON=new ApplicationContext();

		private ApplicationEngineSingleton() {
		}

	}

	private final class SafeWriteSession implements WriteSession {

		private final WriteSession nativeSession;

		private boolean dispossed;
		private boolean completed;

		private SafeWriteSession(WriteSession session) {
			this.nativeSession = session;
		}

		private void verifyExecutability() {
			checkState(!this.dispossed,"Session has already been dispossed");
			checkState(!this.completed,"Session has already been completed");
		}

		@Override
		public <S extends ResourceSnapshot> S resolve(Class<? extends S> snapshotClass, Individual<?, ?> individual) {
			verifyExecutability();
			return this.nativeSession.resolve(snapshotClass,individual);
		}

		@Override
		public <S extends ResourceSnapshot> S find(Class<? extends S> snapshotClass, Name<?> id, Class<? extends ResourceHandler> handlerClass) {
			verifyExecutability();
			return this.nativeSession.find(snapshotClass,id,handlerClass);
		}

		@Override
		public void modify(ResourceSnapshot resource) {
			verifyExecutability();
			this.nativeSession.modify(resource);
		}

		@Override
		public void delete(ResourceSnapshot resource) {
			verifyExecutability();
			this.nativeSession.delete(resource);
		}

		@Override
		public void saveChanges() throws WriteSessionException {
			verifyExecutability();
			this.completed=true;
			this.nativeSession.saveChanges();
		}

		@Override
		public void discardChanges() throws WriteSessionException {
			verifyExecutability();
			this.completed=true;
			this.nativeSession.discardChanges();
		}

		void dispose() throws ApplicationContextException {
			if(!this.dispossed) {
				this.dispossed=true;
				ApplicationContext.this.session.remove();
				ApplicationContext.this.delegate.terminateSession(this.nativeSession);
			}
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ApplicationContext.class);

	private final RuntimeDelegate delegate;

	private final ThreadLocal<WriteSession> session;

	private ApplicationContext() {
		this.delegate=RuntimeDelegate.getInstance();
		this.session=new ThreadLocal<WriteSession>();
		LOGGER.info("Initialized Application Context");
	}

	private ApplicationContextException failure(Throwable cause, String fmt, Object... args) {
		String message=String.format(fmt,args);

		if(cause!=null) {
			LOGGER.error(message+". Full stacktrace follows",cause);
		} else {
			LOGGER.error(message);
		}

		return new ApplicationContextException(message,cause);
	}

	/**
	 * Create a {@code WriteSession}. Only one write session can be active per
	 * thread. Sessions should not be shared among threads.
	 *
	 * @return the write session
	 * @throws ApplicationContextException
	 *             if no write session can be created for whichever reason,
	 *             e.g., the Application Engine is off-line or is not available.
	 */
	public WriteSession createSession() throws ApplicationContextException {
		if(this.session.get()!=null) {
			throw failure(null,"Thread already has an active session");
		}
		try {
			if(this.delegate.isOffline()) {
				throw failure(null,"The Application Engine is off-line");
			}
			return new SafeWriteSession(this.delegate.createSession());
		} catch (UnsupportedOperationException e) {
			throw failure(e,"No Application Engine is available");
		}
	}


	/**
	 * Dispose a {@code WriteSession}. Once the session has been disposed it
	 * will not be active (usable) any longer.
	 *
	 * @param session
	 *            the session to be disposed
	 * @throws NullPointerException
	 *             if the session is null.
	 * @throws IllegalArgumentException
	 *             if the session to be disposed was not created by the
	 *             Application Context.
	 * @throws ApplicationContextException
	 *             if the session cannot be disposed, e.g., the session is not
	 *             owned by the current thread.
	 */
	public void disposeSession(WriteSession session) throws ApplicationContextException {
		checkNotNull(session,"Session cannot be null");
		checkArgument(session instanceof SafeWriteSession,"Unknown session");
		if(this.session.get()!=session) {
			throw failure(null,"Session '%s' is not owned by current thread",session);
		}
		SafeWriteSession safeWriteSession = (SafeWriteSession)session;
		safeWriteSession.dispose();
	}


	/**
	 * Get the Application context.
	 *
	 * @return the application context.
	 */
	public static ApplicationContext getInstance() {
		return ApplicationEngineSingleton.SINGLETON;
	}

}
