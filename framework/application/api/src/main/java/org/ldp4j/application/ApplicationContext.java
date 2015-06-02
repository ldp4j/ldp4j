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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-1.0.0-SNAPSHOT.jar
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

public final class ApplicationContext {

	private static final Logger LOGGER=LoggerFactory.getLogger(ApplicationContext.class);

	private final class SafeWriteSession implements WriteSession {

		private final WriteSession session;

		private boolean dispossed;
		private boolean completed;


		private SafeWriteSession(WriteSession session) {
			this.session = session;
		}

		@Override
		public <S extends ResourceSnapshot> S resolve(Class<? extends S> snapshotClass, Individual<?, ?> individual) {
			S result=null;
			if(!this.dispossed) {
				result=session.resolve(snapshotClass,individual);
			}
			return result;
		}

		@Override
		public <S extends ResourceSnapshot> S find(Class<? extends S> snapshotClass, Name<?> id, Class<? extends ResourceHandler> handlerClass) {
			S result=null;
			if(!this.dispossed) {
				result=session.find(snapshotClass,id,handlerClass);
			}
			return result;
		}

		@Override
		public void modify(ResourceSnapshot resource) {
			if(this.dispossed) {
				throw new IllegalStateException("Session has already been terminated");
			}
			session.modify(resource);
		}

		@Override
		public void delete(ResourceSnapshot resource) {
			if(this.dispossed) {
				throw new IllegalStateException("Session has already been terminated");
			}
			this.session.delete(resource);
		}

		@Override
		public void saveChanges() throws WriteSessionException {
			if(this.dispossed) {
				throw new IllegalStateException("Session has already been terminated");
			}
			if(this.completed) {
				throw new WriteSessionException("Session has already been completed");
			}
			this.completed=true;
			this.session.saveChanges();
		}

		@Override
		public void discardChanges() throws WriteSessionException {
			if(this.dispossed) {
				throw new IllegalStateException("Session has already been terminated");
			}
			if(this.completed) {
				throw new WriteSessionException("Session has already been completed");
			}
			this.completed=true;
			this.session.discardChanges();
		}

		void dispose() throws ApplicationContextException {
			this.dispossed=true;
			ApplicationContext.this.sessions.remove();
			ApplicationContext.this.delegate.terminateSession(this.session);
		}

	}

	private static class ApplicationEngineSingleton {

		private static final ApplicationContext SINGLETON=new ApplicationContext();

	}

	private final RuntimeDelegate delegate;

	private final ThreadLocal<WriteSession> sessions;

	private ApplicationContext() {
		this.delegate=RuntimeDelegate.getInstance();
		this.sessions=new ThreadLocal<WriteSession>();
		LOGGER.info("Initialized Application Context");
	}

	public WriteSession createSession() throws ApplicationContextException {
		if(this.sessions.get()!=null) {
			throw new ApplicationContextException("Thread already owns a session");
		}
		try {
			if(this.delegate.isOffline()) {
				throw new ApplicationContextException("The Application Engine is off-line");
			}
			return new SafeWriteSession(this.delegate.createSession());
		} catch (UnsupportedOperationException e) {
			throw new ApplicationContextException("No Application Engine is available");
		}
	}

	public void disposeSession(WriteSession session) throws ApplicationContextException {
		checkNotNull(session,"Session cannot be null");
		checkArgument(session instanceof SafeWriteSession,"Unknown session");
		if(this.sessions.get()!=session) {
			throw new ApplicationContextException("Session '"+session+"' is not owned by current thread");
		}
		SafeWriteSession safeWriteSession = (SafeWriteSession)session;
		safeWriteSession.dispose();
	}

	public static ApplicationContext getInstance() {
		return ApplicationEngineSingleton.SINGLETON;
	}

}
