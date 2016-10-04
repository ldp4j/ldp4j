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
import static org.junit.Assert.fail;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.session.SessionTerminationException;
import org.ldp4j.application.session.WriteSession;

@RunWith(JMockit.class)
public class ContextWriteSessionStateTest {

	@Injectable
	private WriteSession delegate;

	@Injectable
	private ContextWriteSessionStateListener listener;

	@Tested
	private ContextWriteSessionState sut;

	@Test
	public void testClose$circuitBreak() throws Exception {
		new Expectations() {{
			delegate.close();times=1;
			listener.onDispose(sut);times=1;
		}};
		this.sut.close();
		this.sut.close();
	}

	@Test
	public void testDispose$happyPath() throws Exception {
		new Expectations() {{
			delegate.close();times=1;
			listener.onDispose(sut);times=1;
		}};
		this.sut.dispose();
	}

	@Test
	public void testDispose$failurePath() throws Exception {
		new Expectations() {{
			listener.onDispose(sut);times=1;
			delegate.close();result=new SessionTerminationException("Failure");
		}};
		this.sut.dispose();
	}

	@Test
	public void testVerifyExecutability$breakIfCompleted() throws Exception {
		new Expectations() {{
			delegate.saveChanges();times=1;
		}};
		this.sut.saveChanges();
		try {
			this.sut.saveChanges();
			fail("Should break circuit if already completed");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Session has already been completed"));
		}
	}

	@Test
	public void testVerifyExecutability$breakIfDisposed() throws Exception {
		new Expectations() {{
			listener.onDispose(sut);times=1;
			delegate.close();times=1;
		}};
		this.sut.close();
		try {
			this.sut.saveChanges();
			fail("Should break circuit if already disposed");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Session has already been disposed"));
		}
	}

	@Test
	public void testVerifyExecutability$allowDisposalAfterCompletion() throws Exception {
		new Expectations() {{
			delegate.saveChanges();times=1;
			listener.onDispose(sut);times=1;
			delegate.close();times=1;
		}};
		this.sut.saveChanges();
		this.sut.close();
	}

}
