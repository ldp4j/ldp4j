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
import static org.hamcrest.Matchers.notNullValue;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.spi.RuntimeDelegate;
import org.ldp4j.application.spi.ShutdownListener;

@RunWith(JMockit.class)
public class ApplicationContextExtendedTest {

	@Mocked WriteSession nativeSession;

	@Test
	public void testTerminatesWriteCleaner() throws Exception {
		final AtomicBoolean launched=new AtomicBoolean();
		final AtomicBoolean terminated=new AtomicBoolean();
		final AtomicReference<ShutdownListener> listener=new AtomicReference<ShutdownListener>();
		new MockUp<WriteSessionCleaner>() {
			@Mock
			void launch(ReferenceQueue<ContextWriteSession> queue) {
				assertThat(queue,notNullValue());
				launched.set(true);
			}
			@Mock
			void terminate() {
				terminated.set(true);
			}
			@Mock
			boolean isActive() {
				return false;
			}
		};
		RuntimeDelegate delegate =
			new MockUp<RuntimeDelegate>() {
				@Mock
				WriteSession createSession() {
					return nativeSession;
				}
				@Mock
				boolean isOffline() {
					return false;
				}
				@Mock
				void registerShutdownListener(ShutdownListener aListener) {
					listener.set(aListener);
				}
			}.getMockInstance();
		ApplicationContext sut = setUpContext(delegate);
		WriteSession session = sut.createSession();
		assertThat(session,notNullValue());
		assertThat(launched.get(),equalTo(true));
		assertThat(listener.get(),notNullValue());
		session.close();
		listener.get().engineShutdown();
		assertThat(launched.get(),equalTo(true));
		new Verifications() {{
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	private ApplicationContext setUpContext(RuntimeDelegate delegate) {
		ApplicationContext sut=ApplicationContext.getInstance();
		Deencapsulation.setField(sut, "delegate",delegate);
		return sut;
	}

}
