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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.ext.UnknownResourceException;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.spi.RuntimeDelegate;
import org.ldp4j.commons.testing.Utils;

import com.google.common.base.Throwables;

@RunWith(JMockit.class)
public class ApplicationContextTest {

	@Rule
	public TestName name=new TestName();

	@Rule
	public Timeout timeout=
		Timeout.
			builder().
				withTimeout(5,TimeUnit.SECONDS).
				withLookingForStuckThread(true).
				build();

	@Mocked
	private RuntimeDelegate delegate;

	@Mocked
	private ResourceSnapshot resource;

	@Mocked
	private Name<?> resourceName;

	private ApplicationContext createContext() {
		ApplicationContext sut=ApplicationContext.getInstance();
		Deencapsulation.setField(sut, "delegate",delegate);
		return sut;
	}

	@Test
	public void testGetInstance() throws Exception {
		assertThat(createContext(),notNullValue());
	}

	@Test
	public void verifyIsUtilityClass() throws ClassNotFoundException {
		Class<?> innerClass = Thread.currentThread().getContextClassLoader().loadClass(ApplicationContext.class.getCanonicalName()+"$ApplicationEngineSingleton");
		assertThat(Utils.isUtilityClass(innerClass),equalTo(true));
	}

	@Test
	public void testToString() throws Exception {
		ApplicationContext sut = createContext();
		assertThat(
			sut.toString(),
			not(equalTo(Utils.defaultToString(sut))));
	}

	@Test
	public void testCreateSession$happyPath(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		WriteSession session = createContext().createSession();
		assertThat(session,notNullValue());
		session.close();
		new Verifications() {{
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testCreateSession$nullNativeSession() throws Exception {
		new Expectations() {{
			delegate.createSession();result=null;
		}};
		try {
			createContext().createSession();
		} catch (ApplicationContextException e) {
			assertThat(e.getMessage(),notNullValue());
		}
	}

	@Test
	public void testCreateSession$offline(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.isOffline();result=true;
		}};
		try {
			createContext().createSession();
			fail("Should not create session when off-line");
		} catch (ApplicationContextException e) {
			assertThat(e.getMessage(),notNullValue());
		}
	}

	@Test
	public void testCreateSession$singleSessionPerThread(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		try {
			sut.createSession();
			fail("Should not allow creating multiple sessions per thread");
		} catch(ApplicationContextException e) {
			assertThat(e.getMessage(),notNullValue());
		} finally {
			session.close();
		}
		new Verifications() {{
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$active$find(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
			nativeSession.find(ResourceSnapshot.class,resourceName,CustomResourceHandler.class);result=resource;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		assertThat(
			session.find(ResourceSnapshot.class,this.resourceName,CustomResourceHandler.class),
			sameInstance(this.resource));
		session.close();
		new Verifications() {{
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$active$find(@Mocked final WriteSession nativeSession, @Mocked final Individual<?,?> individual) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
			nativeSession.resolve(ResourceSnapshot.class,individual);result=resource;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		assertThat(
			session.resolve(ResourceSnapshot.class,individual),
			sameInstance(this.resource));
		session.close();
		new Verifications() {{
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$active$modify(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.modify(resource);
		session.close();
		new Verifications() {{
			nativeSession.modify(resource);maxTimes=1;minTimes=1;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$active$delete(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.delete(resource);
		session.close();
		new Verifications() {{
			nativeSession.delete(resource);maxTimes=1;minTimes=1;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$active$saveChanges(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.saveChanges();
		session.close();
		verifySessionUsage(nativeSession);
	}

	@Test
	public void testWriteSession$active$discardChanges(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.discardChanges();
		session.close();
		new Verifications() {{
			nativeSession.discardChanges();maxTimes=1;minTimes=1;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$closed$find(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.close();
		try {
			session.find(ResourceSnapshot.class,this.resourceName,CustomResourceHandler.class);
			fail("Session should not find resources after being closed");
		} catch (Exception e) {
			assertThat(Throwables.getRootCause(e),instanceOf(IllegalStateException.class));
		}
		new Verifications() {{
			nativeSession.find(ResourceSnapshot.class,resourceName,CustomResourceHandler.class);maxTimes=0;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$closed$find(@Mocked final WriteSession nativeSession, @Mocked final Individual<?,?> individual) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.close();
		try {
			session.resolve(ResourceSnapshot.class,individual);
			fail("Session should not resolve resources after being closed");
		} catch (Exception e) {
			assertThat(Throwables.getRootCause(e),instanceOf(IllegalStateException.class));
		}
		new Verifications() {{
			nativeSession.resolve(ResourceSnapshot.class,individual);maxTimes=0;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$closed$modify(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.close();
		try {
			session.modify(resource);
			fail("Session should not modify resources after being closed");
		} catch (Exception e) {
			assertThat(Throwables.getRootCause(e),instanceOf(IllegalStateException.class));
		}
		new Verifications() {{
			nativeSession.modify(resource);maxTimes=0;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$closed$delete(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.close();
		try {
			session.delete(resource);
			fail("Session should not delete resources after being closed");
		} catch (Exception e) {
			assertThat(Throwables.getRootCause(e),instanceOf(IllegalStateException.class));
		}
		new Verifications() {{
			nativeSession.delete(resource);maxTimes=0;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$closed$saveChanges(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.close();
		try {
			session.saveChanges();
			fail("Session should not save changes after being closed");
		} catch (Exception e) {
			assertThat(Throwables.getRootCause(e),instanceOf(IllegalStateException.class));
		}
		new Verifications() {{
			nativeSession.saveChanges();maxTimes=0;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$closed$discardChanges(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.close();
		try {
			session.discardChanges();
			fail("Session should not discard changes after being closed");
		} catch (Exception e) {
			assertThat(Throwables.getRootCause(e),instanceOf(IllegalStateException.class));
		}
		new Verifications() {{
			nativeSession.discardChanges();maxTimes=0;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$completed$find(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.saveChanges();
		try {
			session.find(ResourceSnapshot.class,this.resourceName,CustomResourceHandler.class);
			fail("Session should not find resources after being closed");
		} catch (Exception e) {
			assertThat(Throwables.getRootCause(e),instanceOf(IllegalStateException.class));
		} finally {
			session.close();
		}
		new Verifications() {{
			nativeSession.find(ResourceSnapshot.class,resourceName,CustomResourceHandler.class);maxTimes=0;
			nativeSession.saveChanges();maxTimes=1;minTimes=1;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$completed$find(@Mocked final WriteSession nativeSession, @Mocked final Individual<?,?> individual) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.saveChanges();
		try {
			session.resolve(ResourceSnapshot.class,individual);
			fail("Session should not resolve resources after being completed");
		} catch (Exception e) {
			assertThat(Throwables.getRootCause(e),instanceOf(IllegalStateException.class));
		} finally {
			session.close();
		}
		new Verifications() {{
			nativeSession.resolve(ResourceSnapshot.class,individual);maxTimes=0;
			nativeSession.saveChanges();maxTimes=1;minTimes=1;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$completed$modify(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.saveChanges();
		try {
			session.modify(resource);
			fail("Session should not modify resources after being completed");
		} catch (Exception e) {
			assertThat(Throwables.getRootCause(e),instanceOf(IllegalStateException.class));
		} finally {
			session.close();
		}
		new Verifications() {{
			nativeSession.modify(resource);maxTimes=0;
			nativeSession.saveChanges();maxTimes=1;minTimes=1;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$completed$delete(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.saveChanges();
		try {
			session.delete(resource);
			fail("Session should not delete resources after being completed");
		} catch (Exception e) {
			assertThat(Throwables.getRootCause(e),instanceOf(IllegalStateException.class));
		} finally {
			session.close();
		}
		new Verifications() {{
			nativeSession.delete(resource);maxTimes=0;
			nativeSession.saveChanges();maxTimes=1;minTimes=1;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	@Test
	public void testWriteSession$completed$saveChanges(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.saveChanges();
		try {
			session.saveChanges();
			fail("Session should not save changes after being completed");
		} catch (Exception e) {
			assertThat(Throwables.getRootCause(e),instanceOf(IllegalStateException.class));
		} finally {
			session.close();
		}
		verifySessionUsage(nativeSession);
	}

	@Test
	public void testWriteSession$completed$discardChanges(@Mocked final WriteSession nativeSession) throws Exception {
		new Expectations() {{
			delegate.createSession();result=nativeSession;
		}};
		ApplicationContext sut = createContext();
		WriteSession session = sut.createSession();
		session.saveChanges();
		try {
			session.discardChanges();
			fail("Session should not discard changes after being completed");
		} catch (Exception e) {
			assertThat(Throwables.getRootCause(e),instanceOf(IllegalStateException.class));
		} finally {
			session.close();
		}
		new Verifications() {{
			nativeSession.discardChanges();maxTimes=0;
			nativeSession.saveChanges();maxTimes=1;minTimes=1;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	private void verifySessionUsage(final WriteSession nativeSession) throws Exception {
		new Verifications() {{
			nativeSession.saveChanges();maxTimes=1;minTimes=1;
			nativeSession.close();maxTimes=1;minTimes=1;
		}};
	}

	public static class CustomResourceHandler implements ResourceHandler {
		@Override
		public DataSet get(ResourceSnapshot resource) throws UnknownResourceException {
			return null;
		}
	}

}
