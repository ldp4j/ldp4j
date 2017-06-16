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

import java.lang.ref.ReferenceQueue;
import java.util.Map;

import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.spi.RuntimeDelegate;
import org.ldp4j.application.spi.ShutdownListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;


/**
 * A proxy class that acts as a facade for interacting with the Application
 * Engine proactively.
 */
public final class ApplicationContext {

	private final class CleanerContextWriteSessionStateListener implements ContextWriteSessionStateListener {
		@Override
		public void onDispose(ContextWriteSessionState state) {
			untrackSession(state);
		}
	}

	private static final class ApplicationEngineSingleton {

		private static final ApplicationContext SINGLETON=new ApplicationContext();

		private ApplicationEngineSingleton() {
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ApplicationContext.class);

	private final RuntimeDelegate delegate;
	private final Map<Long,ContextWriteSessionReference> references;
	private final Map<Long,Long> sessionOwner;
	private final Map<Long,Long> threadSession;
	private final ReferenceQueue<ContextWriteSession> referenceQueue;

	private ApplicationContext() {
		this.delegate=RuntimeDelegate.getInstance();
		this.references=Maps.newLinkedHashMap();
		this.sessionOwner=Maps.newLinkedHashMap();
		this.threadSession=Maps.newLinkedHashMap();
		this.referenceQueue=new ReferenceQueue<ContextWriteSession>();
		LOGGER.info("Initialized Application Context");
	}

	private ApplicationContextException failure(String fmt, Object... args) {
		String message=String.format(fmt,args);
		LOGGER.error(message);
		return new ApplicationContextException(message);
	}

	private void setUpWriteSessionCleaner() {
		if(WriteSessionCleaner.isActive()) {
			return;
		}
		WriteSessionCleaner.launch(this.referenceQueue);
		this.delegate.registerShutdownListener(
			new ShutdownListener(){
				@Override
				public void engineShutdown() {
					WriteSessionCleaner.terminate();
				}
			}
		);
	}

	private ContextWriteSession trackSession(ContextWriteSessionState state) {
		ContextWriteSession leakedSession =
			new ContextWriteSession(state);
		ContextWriteSessionReference reference =
			new ContextWriteSessionReference(
				leakedSession,
				state,
				this.referenceQueue);
		this.references.put(state.id(),reference);
		this.sessionOwner.put(state.id(),Thread.currentThread().getId());
		this.threadSession.put(Thread.currentThread().getId(),state.id());
		return leakedSession;
	}

	private synchronized void untrackSession(ContextWriteSessionState session) {
		long sessionId=session.id();
		long ownerId=this.sessionOwner.get(sessionId);
		this.references.remove(sessionId);
		this.sessionOwner.remove(sessionId);
		this.threadSession.remove(ownerId);
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
	public synchronized WriteSession createSession() throws ApplicationContextException {
		if(this.threadSession.containsKey(Thread.currentThread().getId())) {
			throw failure("Thread already has an active session");
		}

		if(this.delegate.isOffline()) {
			throw failure("The Application Engine is off-line");
		}

		WriteSession nativeSession=this.delegate.createSession();
		if(nativeSession==null) {
			throw failure("Could not create native write session");
		}

		setUpWriteSessionCleaner();

		return
			trackSession(
				new ContextWriteSessionState(
					nativeSession,
					new CleanerContextWriteSessionStateListener()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("delegate",this.delegate).
					add("references",this.references).
					add("sessionOwner",this.sessionOwner).
					add("threadSession",this.threadSession).
					toString();
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
