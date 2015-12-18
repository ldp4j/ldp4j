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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadingTest {

	private static class DaemonThread extends Thread {

		private static DaemonThread instance;

		private final AtomicBoolean terminate;

		private DaemonThread() {
			setPriority(Thread.MAX_PRIORITY);
			setName("DaemonThread");
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
			LOGGER.debug("Daemon started");
			while (!this.terminate.get()) {
				try {
					Reference<? extends ThreadingTest> ref=REFERENCE_QUEUE.remove();
					LOGGER.debug("Reference {} is now weakly reachable...",ref);
				} catch (InterruptedException e) {
					LOGGER.debug("Daemon interrupted");
				}
			}
			LOGGER.debug("Daemon terminated");
		}

		static void launch() {
			LOGGER.debug("Launching...");
			instance=new DaemonThread();
			instance.start();
		}

		static void terminate() {
			LOGGER.debug("Terminating...");
			instance.terminate.set(true);
			instance.interrupt();
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ThreadingTest.class);

	private static final ReferenceQueue<ThreadingTest> REFERENCE_QUEUE=new ReferenceQueue<ThreadingTest>();

	@Test
	public void testTermination() throws Exception {
		DaemonThread.launch();
		Thread.sleep(5000);
		DaemonThread.terminate();
		DaemonThread.instance.join();
	}

}
