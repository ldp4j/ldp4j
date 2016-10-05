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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class WriteSessionCleanerTest {

	@Rule
	public Timeout timeout=
		Timeout.
			builder().
				withTimeout(5,TimeUnit.SECONDS).
				withLookingForStuckThread(true).
				build();

	@Before
	public void setUp() throws InterruptedException {
		WriteSessionCleaner instance = Deencapsulation.getField(WriteSessionCleaner.class, WriteSessionCleaner.class);
		if(instance!=null) {
			instance.shutdown();
			Thread lastThread=Deencapsulation.getField(instance,Thread.class);
			if(lastThread!=null) {
				lastThread.join();
			}
		}
		Deencapsulation.setField(WriteSessionCleaner.class, "instance", null);
	}

	@Test
	public void assertThatIsActiveWhenNotLaunchedShouldBeFalse() {
		assertThat(WriteSessionCleaner.isActive(),equalTo(false));
	}

	@Test
	public void assertThatTerminateWhenInactiveDoesNotBreak() {
		WriteSessionCleaner.terminate();
	}

	@Test
	public void assertThatLaunchOnlyAllowsOneInstance() {
		ReferenceQueue<ContextWriteSession> referenceQueue=new ReferenceQueue<ContextWriteSession>();
		int t1=Thread.activeCount();
		WriteSessionCleaner.launch(referenceQueue);
		awaitStartUp();
		int t2=Thread.activeCount();
		WriteSessionCleaner.launch(referenceQueue);
		int t3=Thread.activeCount();
		assertThat(t2,greaterThan(t1));
		assertThat(t3,equalTo(t2));
	}

	@Test
	public void testLifecycle(@Mocked final ContextWriteSession referent) throws Exception {
		ReferenceQueue<ContextWriteSession> referenceQueue=new ReferenceQueue<ContextWriteSession>();
		final CountDownLatch disposed=new CountDownLatch(1);
		ContextWriteSessionState state = new MockUp<ContextWriteSessionState>() {
			@Mock
			void dispose() {
				disposed.countDown();
			}
		}.getMockInstance();
		ContextWriteSessionReference reference=new ContextWriteSessionReference(referent, state, referenceQueue);
		WriteSessionCleaner.launch(referenceQueue);
		Deencapsulation.invoke(referenceQueue, "enqueue", reference);
		disposed.await();
		WriteSessionCleaner.terminate();
		awaitTermination();
	}

	@Test
	public void testCannotStartTwice() throws Exception {
		ReferenceQueue<ContextWriteSession> referenceQueue=new ReferenceQueue<ContextWriteSession>();
		WriteSessionCleaner sut =
				Deencapsulation.
					newInstance(WriteSessionCleaner.class,referenceQueue);
		sut.start();
		Thread firstThread=Deencapsulation.getField(sut,Thread.class);
		try {
			sut.start();
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Write Session Cleaner is already running"));
		} finally {
			sut.shutdown();
			firstThread.join();
		}
	}

	@Test
	public void testTerminatesWhenRequested() throws Exception {
		ReferenceQueue<ContextWriteSession> referenceQueue=new ReferenceQueue<ContextWriteSession>();
		WriteSessionCleaner.launch(referenceQueue);
		awaitStartUp();
		WriteSessionCleaner.terminate();
		awaitTermination();
	}

	@Test
	public void testRestartsOnFailure() throws Exception {
		final CountDownLatch started=new CountDownLatch(1);
		final CountDownLatch failed=new CountDownLatch(1);
		ReferenceQueue<ContextWriteSession> referenceQueue=new ReferenceQueue<ContextWriteSession>() {
			@Override
			public Reference<? extends ContextWriteSession> remove() throws InterruptedException {
				started.await();
				failed.countDown();
				throw new Error("Failure");
			}
		};
		WriteSessionCleaner sut =
			Deencapsulation.
				newInstance(WriteSessionCleaner.class,referenceQueue);
		sut.start();
		Thread firstThread=Deencapsulation.getField(sut,Thread.class);
		started.countDown();
		failed.await();
		firstThread.join();
		sut.shutdown();
		Thread lastThread=Deencapsulation.getField(sut,Thread.class);
		if(lastThread!=null) {
			lastThread.join();
		}
	}

	@Test
	public void testRestartsOnFailureUnlessTerminationIsNotified() throws Exception {
		final CountDownLatch fail=new CountDownLatch(1);
		ReferenceQueue<ContextWriteSession> referenceQueue=new ReferenceQueue<ContextWriteSession>() {
			@Override
			public Reference<? extends ContextWriteSession> remove() throws InterruptedException {
				while(true) {
					try {
						fail.await();
						throw new Error("Failure");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		WriteSessionCleaner sut =
			Deencapsulation.
				newInstance(WriteSessionCleaner.class,referenceQueue);
		sut.start();
		Thread firstThread=Deencapsulation.getField(sut,Thread.class);
		sut.shutdown();
		fail.countDown();
		firstThread.join();
		awaitTermination();
	}

	@Test
	public void testDiscardsInterruptionsIfNotTerminated() throws Exception {
		ReferenceQueue<ContextWriteSession> referenceQueue=new ReferenceQueue<ContextWriteSession>() {
			private int restarts=0;
			@Override
			public Reference<? extends ContextWriteSession> remove() throws InterruptedException {
				this.restarts++;
				if(this.restarts==1) {
					throw new InterruptedException("Failure");
				} else {
					return super.remove();
				}
			}

		};
		WriteSessionCleaner.launch(referenceQueue);
		awaitStartUp();
		WriteSessionCleaner.terminate();
		awaitTermination();
	}

	private void awaitTermination() {
		while(WriteSessionCleaner.isActive()) {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	private void awaitStartUp() {
		while(!WriteSessionCleaner.isActive()) {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

}
