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

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.ReferenceQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class WriteSessionCleaner {

	private final class Restarter implements UncaughtExceptionHandler {
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			LOGGER.error("Cleaner thread unexpectedly died. Full stacktrace follows",e);
			restart();
		}
	}

	private final class Worker implements Runnable {
		@Override
		public void run() {
			while (!WriteSessionCleaner.this.terminate) {
				try {
					ContextWriteSessionReference ref=(ContextWriteSessionReference)WriteSessionCleaner.this.referenceQueue.remove();
					ContextWriteSessionState state = ref.state();
					LOGGER.trace("Session {} is now weakly reachable...",state);
					state.dispose();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(WriteSessionCleaner.class);

	private static WriteSessionCleaner instance;

	private final ReferenceQueue<ContextWriteSession> referenceQueue;

	private volatile boolean terminate;

	private Thread thread;

	private WriteSessionCleaner(final ReferenceQueue<ContextWriteSession> referenceQueue) {
		this.referenceQueue = referenceQueue;
	}

	private void bootstrapThread() {
		this.thread=new Thread(new Worker(),"ApplicationContext-WriteSessionCleaner");
		this.thread.setPriority(Thread.MAX_PRIORITY);
		this.thread.setDaemon(true);
		this.thread.setUncaughtExceptionHandler(new Restarter());
		this.thread.start();
	}

	private synchronized void restart() {
		if(isRunning()) {
			bootstrapThread();
		}
	}

	synchronized void start() {
		checkState(!isRunning(),"Write Session Cleaner is already running");
		bootstrapThread();
	}

	synchronized void shutdown() {
		if(isRunning()) {
			this.terminate=true;
			this.thread.interrupt();
			this.thread=null;
		}
	}

	private synchronized boolean isRunning() {
		return this.thread!=null;
	}

	static synchronized void launch(ReferenceQueue<ContextWriteSession> referenceQueue) {
		if(!isActive()) {
			instance=new WriteSessionCleaner(referenceQueue);
			instance.start();
		}
	}

	static synchronized void terminate() {
		if(isActive()) {
			instance.shutdown();
		}
	}

	static synchronized boolean isActive() {
		if(instance==null) {
			return false;
		}
		return instance.isRunning();
	}

}