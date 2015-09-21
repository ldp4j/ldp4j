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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-examples:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-examples-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.example;

import java.net.URI;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.ApplicationContextException;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSets;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.session.ReadSession;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SessionTerminationException;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.ldp4j.application.spi.ResourceSnapshotResolver;
import org.ldp4j.application.spi.RuntimeDelegate;

@RunWith(JMockit.class)
public class DynamicResourceUpdaterTest {

	private static final MockedRuntimeDelegate DELEGATE = new MockedRuntimeDelegate();

	private static final class MockedRuntimeDelegate extends RuntimeDelegate {

		private WriteSession session;
		private boolean failure;

		public void setSession(WriteSession session) {
			this.session = session;
		}

		@Override
		public boolean isOffline() {
			return false;
		}

		@Override
		public WriteSession createSession() throws ApplicationContextException {
			if(this.failure) {
				throw new ApplicationContextException("failure");
			}
			return this.session;
		}

		@Override
		public ResourceSnapshotResolver createResourceResolver(URI canonicalBase, ReadSession session) {
			return null;
		}

		public void setFailure(boolean failure) {
			this.failure = failure;
		}
	}

	private static final Name<String> RID = NamingScheme.getDefault().name("resource");

	@BeforeClass
	public static void setUpBefore() {
		RuntimeDelegate.setInstance(DELEGATE);
	}

	@Test
	public void testRun(@Mocked final WriteSession session, @Mocked  final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceUpdater sut = workingSut(handler,session,snapshot);
		sut.run();
		verifyResult(snapshot,handler,true);
	}

	@Test
	public void testRun$failure$runtimeException(@Mocked final WriteSession session, @Mocked  final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceUpdater sut = runtimeFailureSut(handler,session,snapshot);
		sut.run();
		verifyResult(snapshot,handler,false);
	}

	@Test
	public void testRun$failure$nullWriteSession(@Mocked final WriteSession session, @Mocked  final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceUpdater sut = nullWriteSessionSut(handler,session,snapshot);
		sut.run();
		verifyResult(snapshot,handler,false);
	}

	@Test
	public void testRun$failure$writeSessionCreationException(@Mocked final WriteSession session, @Mocked  final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceUpdater sut = writeSessionCreationFailureSut(handler,session,snapshot);
		sut.run();
		verifyResult(snapshot,handler,false);
	}

	@Test
	public void testRun$failure$writeSessionException(@Mocked final WriteSession session, @Mocked  final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceUpdater sut = writeSessionFailureSut(handler,session,snapshot);
		sut.run();
		verifyResult(snapshot,handler,true);
	}

	@Test
	public void testRun$failure$writeSessionTerminationFailure(@Mocked final WriteSession session, @Mocked  final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceUpdater sut = writeSessionTerminationSut(handler,session,snapshot);
		sut.run();
		verifyResult(snapshot,handler,true);
	}

	@Test
	public void testRun$failure$writeSessionAndTerminationFailure(@Mocked final WriteSession session, @Mocked  final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceUpdater sut = writeSessionFailureWithTerminationFailureSut(handler,session,snapshot);
		sut.run();
		verifyResult(snapshot,handler,true);
	}

	private void verifyResult(final ResourceSnapshot snapshot, DynamicResourceHandler handler, boolean b) {
		DataSet dataSet = handler.get(snapshot);
		Individual<?, ?> resource=dataSet.individualOfId(ManagedIndividualId.createId(RID, DynamicResourceHandler.ID));
		assertThat(resource.hasProperty(DynamicResourceUpdater.REFRESHED_ON),equalTo(b));
	}

	private DynamicResourceUpdater workingSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot) {
		DELEGATE.setSession(session);
		DELEGATE.setFailure(false);
		new Expectations() {{
			snapshot.name();result=RID;
			session.find(ResourceSnapshot.class, RID, DynamicResourceHandler.class);result=snapshot;
		}};
		return createSut(handler);
	}

	private DynamicResourceUpdater runtimeFailureSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot) {
		DELEGATE.setSession(session);
		DELEGATE.setFailure(false);
		new Expectations() {{
			snapshot.name();result=RID;
			session.find(ResourceSnapshot.class, RID, DynamicResourceHandler.class);result=new IllegalStateException("FAILURE");
		}};
		return createSut(handler);
	}

	private DynamicResourceUpdater nullWriteSessionSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot) throws WriteSessionException {
		DELEGATE.setSession(null);
		DELEGATE.setFailure(false);
		new Expectations() {{
			snapshot.name();result=RID;
		}};
		return createSut(handler);
	}

	private DynamicResourceUpdater writeSessionCreationFailureSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot) throws WriteSessionException {
		DELEGATE.setSession(session);
		DELEGATE.setFailure(true);
		new Expectations() {{
			snapshot.name();result=RID;
		}};
		return createSut(handler);
	}

	private DynamicResourceUpdater writeSessionFailureSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot) throws WriteSessionException {
		DELEGATE.setSession(session);
		DELEGATE.setFailure(false);
		new Expectations() {{
			snapshot.name();result=RID;
			session.find(ResourceSnapshot.class, RID, DynamicResourceHandler.class);result=snapshot;
			session.saveChanges();result=new WriteSessionException();
		}};
		return createSut(handler);
	}

	private DynamicResourceUpdater writeSessionFailureWithTerminationFailureSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot) throws Exception {
		DELEGATE.setSession(session);
		DELEGATE.setFailure(false);
		new Expectations() {{
			snapshot.name();result=RID;
			session.find(ResourceSnapshot.class, RID, DynamicResourceHandler.class);result=snapshot;
			session.saveChanges();result=new WriteSessionException("SAVE_FAILURE");
			session.close();result=new SessionTerminationException("CLOSE_FAILURE");
		}};
		return createSut(handler);
	}

	private DynamicResourceUpdater writeSessionTerminationSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot) throws Exception {
		DELEGATE.setSession(session);
		DELEGATE.setFailure(false);
		new Expectations() {{
			snapshot.name();result=RID;
			session.find(ResourceSnapshot.class, RID, DynamicResourceHandler.class);result=snapshot;
			session.close();result=new SessionTerminationException("FAILURE");
		}};
		return createSut(handler);
	}

	private DynamicResourceUpdater createSut(DynamicResourceHandler handler) {
		DataSet dataSet = DataSets.createDataSet(RID);
		dataSet.individual(ManagedIndividualId.createId(RID, DynamicResourceHandler.ID), ManagedIndividual.class);
		handler.add(RID,dataSet);
		DynamicResourceUpdater sut = new DynamicResourceUpdater(handler, RID);
		return sut;
	}
}
