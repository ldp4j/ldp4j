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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-examples:0.2.2
 *   Bundle      : ldp4j-application-examples-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.example;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.URI;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSets;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literals;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.UnknownResourceException;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SessionTerminationException;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.ldp4j.application.spi.ResourceSnapshotResolver;
import org.ldp4j.application.spi.RuntimeDelegate;

@RunWith(JMockit.class)
public class DynamicResourceResolverTest {

	private static final URI ENDPOINT = DynamicResourceResolver.CANONICAL_BASE.resolve("path/");

	private static final Name<String> RID = NamingScheme.getDefault().name("resource");

	private static MockedRuntimeDelegate DELEGATE;

	@BeforeClass
	public static void setUpBefore() {
		RuntimeDelegate.setInstance(new MockedRuntimeDelegate());
		ApplicationContext instance = ApplicationContext.getInstance();
		Deencapsulation.setField(
			instance,
			"delegate",
			RuntimeDelegate.getInstance());
		DELEGATE=Deencapsulation.getField(instance, "delegate");
	}

	@Mocked ResourceSnapshotResolver resolver;

	@Test
	public void testRun(@Mocked final WriteSession session, @Mocked final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceResolver sut = workingSut(handler,session,snapshot);
		sut.run();
		verifyResult(snapshot,handler,true,true, "OK");
	}

	@Test
	public void testRun$roundtripFailure$noSnapshotFound(@Mocked final WriteSession session, @Mocked final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceResolver sut = roundtripFailureSut(handler,session,snapshot,null);
		sut.run();
		verifyResult(snapshot,handler,true,true,"KO");
	}

	@Test
	public void testRun$roundtripFailure$differentName(@Mocked final WriteSession session, @Mocked final ResourceSnapshot snapshot,@Mocked final ResourceSnapshot resolved) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceResolver sut = roundtripFailureSut(handler,session,snapshot,resolved);
		new Expectations() {{
			resolved.name();result=NamingScheme.getDefault().name("other");
		}};
		sut.run();
		verifyResult(snapshot,handler,true,true,"KO");
	}

	@Test
	public void testRun$roundtripFailure$differentHandler(@Mocked final WriteSession session, @Mocked final ResourceSnapshot snapshot,@Mocked final ResourceSnapshot resolved) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceResolver sut = roundtripFailureSut(handler,session,snapshot,resolved);
		new Expectations() {{
			resolved.name();result=RID;
			resolved.handlerClass();result=PersonHandler.class;
		}};
		sut.run();
		verifyResult(snapshot,handler,true,true,"KO");
	}

	@Test
	public void testRun$failure$runtimeException(@Mocked final WriteSession session, @Mocked  final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceResolver sut = runtimeFailureSut(handler,session,snapshot);
		sut.run();
		verifyResult(snapshot,handler,false,false,null);
	}

	@Test
	public void testRun$failure$nullWriteSession(@Mocked final WriteSession session, @Mocked  final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceResolver sut = nullWriteSessionSut(handler,session,snapshot);
		sut.run();
		verifyResult(snapshot,handler,false,false,null);
	}

	@Test
	public void testRun$failure$writeSessionCreationException(@Mocked final WriteSession session, @Mocked  final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceResolver sut = writeSessionCreationFailureSut(handler,session,snapshot);
		sut.run();
		verifyResult(snapshot,handler,false,false,null);
	}

	@Test
	public void testRun$failure$writeSessionException(@Mocked final WriteSession session, @Mocked  final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceResolver sut = writeSessionFailureSut(handler,session,snapshot);
		sut.run();
		verifyResult(snapshot,handler,true,true, "OK");
	}

	@Test
	public void testRun$failure$writeSessionTerminationFailure(@Mocked final WriteSession session, @Mocked  final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceResolver sut = writeSessionTerminationSut(handler,session,snapshot);
		sut.run();
		verifyResult(snapshot,handler,true,true,"OK");
	}

	@Test
	public void testRun$failure$writeSessionAndTerminationFailure(@Mocked final WriteSession session, @Mocked  final ResourceSnapshot snapshot) throws Exception {
		DynamicResourceHandler handler = new DynamicResourceHandler();
		DynamicResourceResolver sut = writeSessionFailureWithTerminationFailureSut(handler,session,snapshot);
		sut.run();
		verifyResult(snapshot,handler,true,true,"OK");
	}

	private void verifyResult(final ResourceSnapshot snapshot, DynamicResourceHandler handler, boolean resolveToUri, boolean resolveRoundtrip, String roundtrip) throws UnknownResourceException {
		DataSet dataSet = handler.get(snapshot);
		Individual<?, ?> resource=dataSet.individualOfId(ManagedIndividualId.createId(RID, DynamicResourceHandler.ID));
		assertThat("Could "+(resolveToUri?"not":"")+" resolve uri",resource.hasProperty(DynamicResourceResolver.SNAPSHOT_ENDPOINT),equalTo(resolveToUri));
		if(resolveToUri) {
			assertThat("Invalid uri resolution",resource.property(DynamicResourceResolver.SNAPSHOT_ENDPOINT).hasLiteralValue(Literals.newLiteral(ENDPOINT)),equalTo(resolveRoundtrip));
		}
		assertThat("Could "+(resolveRoundtrip?"not":"")+" resolve roundtrip",resource.hasProperty(DynamicResourceResolver.SNAPSHOT_RESOLUTION),equalTo(resolveRoundtrip));
		if(resolveRoundtrip) {
			assertThat("Invalid roundtrip resolution",resource.property(DynamicResourceResolver.SNAPSHOT_RESOLUTION).hasLiteralValue(Literals.of(roundtrip)),equalTo(resolveRoundtrip));
		}
	}

	private DynamicResourceResolver workingSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot) {
		DELEGATE.setSession(session);
		DELEGATE.setFailure(false);
		DELEGATE.setResolver(resolver);
		new Expectations() {{
			session.find(ResourceSnapshot.class, RID, DynamicResourceHandler.class);result=snapshot;
			snapshot.name();result=RID;
			snapshot.templateId();result=DynamicResourceHandler.ID;
			resolver.resolve(snapshot);result=ENDPOINT;
			resolver.resolve(ENDPOINT);result=snapshot;
		}};
		return createSut(handler);
	}


	private DynamicResourceResolver roundtripFailureSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot, final ResourceSnapshot resolved) {
		DELEGATE.setSession(session);
		DELEGATE.setFailure(false);
		DELEGATE.setResolver(resolver);
		new Expectations() {{
			session.find(ResourceSnapshot.class, RID, DynamicResourceHandler.class);result=snapshot;
			snapshot.name();result=RID;
			snapshot.templateId();result=DynamicResourceHandler.ID;
			resolver.resolve(snapshot);result=ENDPOINT;
			resolver.resolve(ENDPOINT);result=resolved;
		}};
		return createSut(handler);
	}

	private DynamicResourceResolver runtimeFailureSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot) {
		DELEGATE.setSession(session);
		DELEGATE.setFailure(false);
		new Expectations() {{
			snapshot.name();result=RID;
			session.find(ResourceSnapshot.class, RID, DynamicResourceHandler.class);result=new IllegalStateException("FAILURE");
		}};
		return createSut(handler);
	}

	private DynamicResourceResolver nullWriteSessionSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot) throws WriteSessionException {
		DELEGATE.setSession(null);
		DELEGATE.setFailure(false);
		new Expectations() {{
			snapshot.name();result=RID;
		}};
		return createSut(handler);
	}

	private DynamicResourceResolver writeSessionCreationFailureSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot) throws WriteSessionException {
		DELEGATE.setSession(session);
		DELEGATE.setFailure(true);
		new Expectations() {{
			snapshot.name();result=RID;
		}};
		return createSut(handler);
	}

	private DynamicResourceResolver writeSessionFailureSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot) throws WriteSessionException {
		DELEGATE.setSession(session);
		DELEGATE.setFailure(false);
		new Expectations() {{
			session.find(ResourceSnapshot.class, RID, DynamicResourceHandler.class);result=snapshot;
			snapshot.name();result=RID;
			snapshot.templateId();result=DynamicResourceHandler.ID;
			resolver.resolve(snapshot);result=ENDPOINT;
			resolver.resolve(ENDPOINT);result=snapshot;
			session.saveChanges();result=new WriteSessionException();
		}};
		return createSut(handler);
	}

	private DynamicResourceResolver writeSessionFailureWithTerminationFailureSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot) throws Exception {
		DELEGATE.setSession(session);
		DELEGATE.setFailure(false);
		new Expectations() {{
			session.find(ResourceSnapshot.class, RID, DynamicResourceHandler.class);result=snapshot;
			snapshot.name();result=RID;
			snapshot.templateId();result=DynamicResourceHandler.ID;
			resolver.resolve(snapshot);result=ENDPOINT;
			resolver.resolve(ENDPOINT);result=snapshot;
			session.saveChanges();result=new WriteSessionException("SAVE_FAILURE");
			session.close();result=new SessionTerminationException("CLOSE_FAILURE");
		}};
		return createSut(handler);
	}

	private DynamicResourceResolver writeSessionTerminationSut(DynamicResourceHandler handler, final WriteSession session, final ResourceSnapshot snapshot) throws Exception {
		DELEGATE.setSession(session);
		DELEGATE.setFailure(false);
		new Expectations() {{
			session.find(ResourceSnapshot.class, RID, DynamicResourceHandler.class);result=snapshot;
			snapshot.name();result=RID;
			snapshot.templateId();result=DynamicResourceHandler.ID;
			resolver.resolve(snapshot);result=ENDPOINT;
			resolver.resolve(ENDPOINT);result=snapshot;
			session.close();result=new SessionTerminationException("FAILURE");
		}};
		return createSut(handler);
	}

	private DynamicResourceResolver createSut(DynamicResourceHandler handler) {
		DataSet dataSet = DataSets.createDataSet(RID);
		dataSet.individual(ManagedIndividualId.createId(RID, DynamicResourceHandler.ID), ManagedIndividual.class);
		handler.add(RID,dataSet);
		return new DynamicResourceResolver(handler, RID);
	}
}
