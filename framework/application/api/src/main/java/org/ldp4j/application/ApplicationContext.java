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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.0
 *   Bundle      : ldp4j-application-api-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SessionTerminationException;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
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

	private static final class ApplicationEngineSingleton {

		private static final ApplicationContext SINGLETON=new ApplicationContext();

		private ApplicationEngineSingleton() {
		}

	}

	private final class State implements WriteSession {

		private final long id;
		private final WriteSession delegate;

		private boolean disposed;
		private boolean completed;

		private State(WriteSession delegate) {
			this.delegate = delegate;
			this.id=SESSION_COUNTER.incrementAndGet();
		}

		private void doDispose() throws SessionTerminationException {
			clearSession(this);
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

	private final class ContextWriteSession implements WriteSession {

		private final State state;

		private ContextWriteSession(State state) {
			this.state = state;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <S extends ResourceSnapshot> S resolve(Class<? extends S> snapshotClass, Individual<?, ?> individual) {
			return this.state.resolve(snapshotClass,individual);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <S extends ResourceSnapshot> S find(Class<? extends S> snapshotClass, Name<?> id, Class<? extends ResourceHandler> handlerClass) {
			return this.state.find(snapshotClass,id,handlerClass);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void modify(ResourceSnapshot resource) {
			this.state.modify(resource);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void delete(ResourceSnapshot resource) {
			this.state.delete(resource);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void saveChanges() throws WriteSessionException {
			this.state.saveChanges();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void discardChanges() throws WriteSessionException {
			this.state.discardChanges();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void close() throws SessionTerminationException {
			this.state.close();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						add("state",this.state).
						toString();
		}

	}

	private static final class ContextWriteSessionReference extends WeakReference<ContextWriteSession> {

		private State state;

		public ContextWriteSessionReference(ContextWriteSession referent, State state) {
			super(referent,REFERENCE_QUEUE);
			this.state = state;
		}

		State state() {
			return this.state;
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						omitNullValues().
							add("enqueued",super.isEnqueued()).
							add("state",this.state).
							toString();
		}


	}

	private static class WriteSessionCleaner extends Thread {

		private static WriteSessionCleaner instance;

		private final AtomicBoolean terminate;

		private WriteSessionCleaner() {
			setPriority(Thread.MAX_PRIORITY);
			setName("ApplicationContext-WriteSessionCleaner");
			setDaemon(true);
			setUncaughtExceptionHandler(
				new UncaughtExceptionHandler() {
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						LOGGER.error("Cleaner thread unexpectedly died. Full stacktrace follows",e);
						launch();
					}
				}
			);
			this.terminate=new AtomicBoolean(false);
		}

		@Override
		public void run() {
			while (!this.terminate.get()) {
				try {
					ContextWriteSessionReference ref=(ContextWriteSessionReference)REFERENCE_QUEUE.remove();
					ApplicationContext.State session = ref.state();
					LOGGER.trace("Session {} is now weakly reachable...",session);
					session.dispose();
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}

		static void launch() {
			instance=new WriteSessionCleaner();
			instance.start();
		}

		static void terminate() {
			instance.terminate.set(true);
			instance.interrupt();
		}

		static boolean isActive() {
			return instance!=null;
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ApplicationContext.class);

	private static final AtomicLong SESSION_COUNTER=new AtomicLong();

	private static final ReferenceQueue<ContextWriteSession> REFERENCE_QUEUE=new ReferenceQueue<ContextWriteSession>();

	private final RuntimeDelegate delegate;
	private final Map<Long,ContextWriteSessionReference> references;
	private final Map<Long,Long> sessionOwner;
	private final Map<Long,Long> threadSession;

	private ApplicationContext() {
		this.delegate=RuntimeDelegate.getInstance();
		this.references=Maps.newLinkedHashMap();
		this.sessionOwner=Maps.newLinkedHashMap();
		this.threadSession=Maps.newLinkedHashMap();
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

	private synchronized boolean clearSession(State session) {
		long sessionId=session.id();
		long ownerId=this.sessionOwner.get(sessionId);
		this.references.remove(sessionId);
		this.sessionOwner.remove(sessionId);
		this.threadSession.remove(ownerId);
		return ownerId==Thread.currentThread().getId();
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
			throw failure(null,"Thread already has an active session");
		}
		if(this.delegate.isOffline()) {
			throw failure(null,"The Application Engine is off-line");
		}
		if(!WriteSessionCleaner.isActive()) {
			WriteSessionCleaner.launch();
			this.delegate.registerShutdownListener(
				new ShutdownListener(){
					@Override
					public void engineShutdown() {
						WriteSessionCleaner.terminate();
					}
				}
			);
		}

		WriteSession nativeSession=this.delegate.createSession();
		if(nativeSession==null) {
			throw failure(null,"Could not create native write session");
		}
		State state = new State(nativeSession);
		ContextWriteSession leakedSession = new ContextWriteSession(state);
		ContextWriteSessionReference reference = new ContextWriteSessionReference(leakedSession,state);
		this.references.put(state.id(),reference);
		this.sessionOwner.put(state.id(),Thread.currentThread().getId());
		this.threadSession.put(Thread.currentThread().getId(),state.id());
		return leakedSession;
	}


	/**
	 * Dispose a {@code WriteSession}. Once the session has been disposed it
	 * will not be active (usable) any longer.
	 *
	 * @param session
	 *            the session to be disposed
	 * @throws NullPointerException
	 *             if the session is null.
	 * @throws ApplicationContextException
	 *             if the session cannot be disposed, e.g., the session is not
	 *             owned by the current thread.
	 * @deprecated Use the {@link WriteSession#close()} method instead.
	 */
	@Deprecated
	public void disposeSession(WriteSession session) throws ApplicationContextException {
		checkNotNull(session,"Session cannot be null");
		if(!ContextWriteSession.class.isInstance(session)) {
			throw failure(null,"Unknown session %s",session);
		}
		try {
			session.close();
		} catch (SessionTerminationException e) {
			throw failure(e,"Could not close session '%X'",session.hashCode());
		}
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
