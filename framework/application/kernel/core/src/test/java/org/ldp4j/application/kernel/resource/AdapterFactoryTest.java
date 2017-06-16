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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.2
 *   Bundle      : ldp4j-application-kernel-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.fail;

import java.util.Set;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSets;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.ApplicationRuntimeException;
import org.ldp4j.application.ext.ContainerHandler;
import org.ldp4j.application.ext.Deletable;
import org.ldp4j.application.ext.Modifiable;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.ext.Queryable;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.kernel.session.WriteSessionConfiguration;
import org.ldp4j.application.kernel.session.WriteSessionService;
import org.ldp4j.application.session.AttachmentSnapshot;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ReadSession;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SnapshotVisitor;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.commons.testing.Utils;

@RunWith(JMockit.class)
public class AdapterFactoryTest {

	private static class FailingHandler implements ContainerHandler, Queryable, Modifiable, Deletable {

		@Override
		public DataSet get(ResourceSnapshot resource) throws ApplicationRuntimeException {
			throw new ApplicationRuntimeException("Failure");
		}

		@Override
		public void delete(ResourceSnapshot resource, WriteSession session) throws ApplicationRuntimeException {
			throw new ApplicationRuntimeException("Failure");
		}

		@Override
		public void update(ResourceSnapshot resource, DataSet content, WriteSession session) throws ApplicationRuntimeException {
			throw new ApplicationRuntimeException("Failure");
		}

		@Override
		public DataSet query(ResourceSnapshot resource, Query query, ReadSession session) throws ApplicationRuntimeException {
			throw new ApplicationRuntimeException("Failure");
		}

		@Override
		public ResourceSnapshot create(ContainerSnapshot container, DataSet representation, WriteSession session) throws ApplicationRuntimeException {
			throw new ApplicationRuntimeException("Failure");
		}

	}

	private static class ViolationHandler implements ContainerHandler, Queryable {

		@Override
		public DataSet get(ResourceSnapshot resource) {
			return null;
		}

		@Override
		public DataSet query(ResourceSnapshot resource, Query query, ReadSession session) {
			return null;
		}

		@Override
		public ResourceSnapshot create(ContainerSnapshot container, DataSet representation, WriteSession session) {
			return null;
		}

	}

	private static class SimpleHandler implements ResourceHandler {

		private DataSet dataSet;

		public SimpleHandler(DataSet dataSet) {
			this.dataSet = dataSet;
		}

		@Override
		public DataSet get(ResourceSnapshot resource) {
			return this.dataSet;
		}

	}

	private static class MockResourceSnapshot implements ResourceSnapshot {

		private String templateId;
		private ResourceHandler handler;

		private void setTemplateId(String templateId) {
			this.templateId = templateId;
		}

		private void setHandler(ResourceHandler handler) {
			this.handler = handler;
		}

		@Override
		public String templateId() {
			return this.templateId;
		}

		@Override
		public Class<? extends ResourceHandler> handlerClass() {
			return this.handler.getClass();
		}

		@Override
		public void accept(SnapshotVisitor visitor) {
			visitor.visitResourceSnapshot(this);
		}

		@Override
		public Name<?> name() {
			throw fail();
		}

		@Override
		public boolean isRoot() {
			throw fail();
		}

		@Override
		public ResourceSnapshot parent() {
			throw fail();
		}

		@Override
		public Set<? extends AttachmentSnapshot> attachments() {
			throw fail();
		}

		@Override
		public AttachmentSnapshot attachmentByResource(ResourceSnapshot resource) {
			throw fail();
		}

		@Override
		public AttachmentSnapshot attachmentById(String attachmentId) {
			throw fail();
		}

		@Override
		public <T extends ResourceSnapshot> T createAttachedResource(Class<? extends T> snapshotClass, String attachmentId, Name<?> name, Class<? extends ResourceHandler> handlerClass) {
			throw fail();
		}

		@Override
		public boolean removeAttachment(AttachmentSnapshot attachment) {
			throw fail();
		}

		protected final IllegalStateException fail() {
			return new IllegalStateException("Method should not be invoked");
		}

	}

	private static class MockContainerSnapshot extends MockResourceSnapshot implements ContainerSnapshot {

		@Override
		public void accept(SnapshotVisitor visitor) {
			visitor.visitContainerSnapshot(this);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends ContainerHandler> handlerClass() {
			return (Class<? extends ContainerHandler>)super.handlerClass();
		}

		@Override
		public Set<? extends ResourceSnapshot> members() {
			throw fail();
		}

		@Override
		public ResourceSnapshot addMember(Name<?> resourceName) {
			throw fail();
		}

		@Override
		public boolean hasMember(ResourceSnapshot resource) {
			throw fail();
		}

		@Override
		public boolean removeMember(ResourceSnapshot resource) {
			throw fail();
		}

	}

	@Test
	public void verifyIsUtilityClass() {
		assertThat(Utils.isUtilityClass(AdapterFactory.class),equalTo(true));
	}

	@Test
	public void adapterExposesResourceIdentifier(final @Mocked Resource resource, final @Mocked WriteSessionService service, final @Mocked WriteSessionConfiguration configuration, final @Mocked WriteSession session) throws Exception {
		Adapter adapter=prepareAdapter(resource, service,configuration, new MockResourceSnapshot(), new FailingHandler(),session);
		assertThat(adapter.resourceId(),equalTo(ResourceId.createId(resourceName(), templateId())));
	}

	@Test
	public void createdAdapterFailsOnInternalGetFailure(final @Mocked Resource resource, final @Mocked WriteSessionService service, final @Mocked WriteSessionConfiguration configuration, final @Mocked WriteSession session) throws Exception {
		Adapter adapter=prepareAdapter(resource, service,configuration, new MockResourceSnapshot(), new FailingHandler(),session);
		try {
			adapter.get();
			fail("Should fail if an exception is thrown on get");
		} catch(FeatureException e) {
			verifyExpectedFeatureFailure(e, ResourceHandler.class, session);
		}
	}

	@Test
	public void createdAdapterFailsOnInternalQueryFailure(final @Mocked Resource resource, final @Mocked WriteSessionService service, final @Mocked WriteSessionConfiguration configuration, final @Mocked WriteSession session) throws Exception {
		Adapter adapter=prepareAdapter(resource, service,configuration, new MockResourceSnapshot(), new FailingHandler(),session);
		try {
			adapter.query(null);
			fail("Should fail if an exception is thrown on query");
		} catch(FeatureException e) {
			verifyExpectedFeatureFailure(e, Queryable.class, session);
		}
	}

	@Test
	public void createdAdapterFailsOnInternalUpdateFailure(final @Mocked Resource resource, final @Mocked WriteSessionService service, final @Mocked WriteSessionConfiguration configuration, final @Mocked WriteSession session) throws Exception {
		Adapter adapter=prepareAdapter(resource, service,configuration, new MockResourceSnapshot(), new FailingHandler(),session);
		try {
			adapter.update(null);
			fail("Should fail if an exception is thrown on update");
		} catch(FeatureException e) {
			verifyExpectedFeatureFailure(e, Modifiable.class, session);
		}
	}

	@Test
	public void createdAdapterFailsOnInternalDeleteFailure(final @Mocked Resource resource, final @Mocked WriteSessionService service, final @Mocked WriteSessionConfiguration configuration, final @Mocked WriteSession session) throws Exception {
		Adapter adapter=prepareAdapter(resource, service,configuration, new MockResourceSnapshot(), new FailingHandler(),session);
		try {
			adapter.delete();
			fail("Should fail if an exception is thrown on delete");
		} catch(FeatureException e) {
			verifyExpectedFeatureFailure(e, Deletable.class, session);
		}
	}

	@Test
	public void createdAdapterFailsOnInternalCreateFailure(final @Mocked Resource resource, final @Mocked WriteSessionService service, final @Mocked WriteSessionConfiguration configuration, final @Mocked WriteSession session) throws Exception {
		Adapter adapter=prepareAdapter(resource, service,configuration, new MockContainerSnapshot(), new FailingHandler(),session);
		try {
			adapter.create(null);
			fail("Should fail if an exception is thrown on create");
		} catch(FeatureException e) {
			verifyExpectedFeatureFailure(e, ContainerHandler.class, session);
		}
	}

	@Test
	public void creationOnResourceSnapshotFails(final @Mocked Resource resource, final @Mocked WriteSessionService service, final @Mocked WriteSessionConfiguration configuration, final @Mocked WriteSession session) throws Exception {
		Adapter adapter=prepareAdapter(resource, service,configuration, new MockResourceSnapshot(), new FailingHandler(),session);
		try {
			adapter.create(null);
			fail("Should fail if an exception is thrown on create");
		} catch(UnsupportedFeatureException e) {
			verifyExpectedFeatureException(e, ContainerHandler.class, FailingHandler.class, session);
		}
	}

	@Test
	public void createdAdapterFailsOnQueryIfHandlerIsNotQueryable(final @Mocked Resource resource, final @Mocked WriteSessionService service, final @Mocked WriteSessionConfiguration configuration, final @Mocked WriteSession session) throws Exception {
		Adapter adapter=prepareAdapter(resource, service,configuration, new MockResourceSnapshot(), simpleHandler(),session);
		try {
			adapter.query(null);
			fail("Should fail if an exception is thrown on query");
		} catch(FeatureException e) {
			verifyExpectedFeatureException(e, Queryable.class, SimpleHandler.class, session);
		}
	}

	@Test
	public void createdAdapterFailsOnUpdateIfHandlerIsNotModifiable(final @Mocked Resource resource, final @Mocked WriteSessionService service, final @Mocked WriteSessionConfiguration configuration, final @Mocked WriteSession session) throws Exception {
		Adapter adapter=prepareAdapter(resource, service,configuration, new MockResourceSnapshot(), simpleHandler(),session);
		try {
			adapter.update(null);
			fail("Should fail if an exception is thrown on update");
		} catch(FeatureException e) {
			verifyExpectedFeatureException(e, Modifiable.class, SimpleHandler.class, session);
		}
	}

	@Test
	public void createdAdapterFailsOnDeleteIfHandlerIsNotDeletable(final @Mocked Resource resource, final @Mocked WriteSessionService service, final @Mocked WriteSessionConfiguration configuration, final @Mocked WriteSession session) throws Exception {
		Adapter adapter=prepareAdapter(resource, service,configuration, new MockResourceSnapshot(), simpleHandler(),session);
		try {
			adapter.delete();
			fail("Should fail if an exception is thrown on delete");
		} catch(FeatureException e) {
			verifyExpectedFeatureException(e, Deletable.class, SimpleHandler.class, session);
		}
	}

	@Test
	public void createdAdapterFailsOnCreateIfHandlerIsNotAContainerHandler(final @Mocked Resource resource, final @Mocked WriteSessionService service, final @Mocked WriteSessionConfiguration configuration, final @Mocked WriteSession session) throws Exception {
		Adapter adapter=prepareAdapter(resource, service,configuration, new MockContainerSnapshot(), simpleHandler(),session);
		try {
			adapter.create(null);
			fail("Should fail if an exception is thrown on create");
		} catch(FeatureException e) {
			verifyExpectedFeatureException(e, ContainerHandler.class, SimpleHandler.class, session);
		}
	}

	@Test
	public void createdAdapterFailsOnCreateIfHandlerViolatesPostcondition(final @Mocked Resource resource, final @Mocked WriteSessionService service, final @Mocked WriteSessionConfiguration configuration, final @Mocked WriteSession session) throws Exception {
		Adapter adapter=prepareAdapter(resource, service,configuration, new MockContainerSnapshot(), violationHandler(),session);
		try {
			adapter.create(null);
			fail("Should fail if create postcondition is violated");
		} catch(FeaturePostconditionException e) {
			verifyExpectedFeatureException(e, ContainerHandler.class, ViolationHandler.class, session);
		}
	}

	@Test
	public void createdAdapterFailsOnGetIfHandlerViolatesPostcondition(final @Mocked Resource resource, final @Mocked WriteSessionService service, final @Mocked WriteSessionConfiguration configuration, final @Mocked WriteSession session) throws Exception {
		Adapter adapter=prepareAdapter(resource, service,configuration, new MockContainerSnapshot(), violationHandler(),session);
		try {
			adapter.get();
			fail("Should fail if get postcondition is violated");
		} catch(FeaturePostconditionException e) {
			verifyExpectedFeatureException(e, ResourceHandler.class, ViolationHandler.class, session);
		}
	}

	@Test
	public void createdAdapterFailsOnQueryIfHandlerViolatesPostcondition(final @Mocked Resource resource, final @Mocked WriteSessionService service, final @Mocked WriteSessionConfiguration configuration, final @Mocked WriteSession session) throws Exception {
		Adapter adapter=prepareAdapter(resource, service,configuration, new MockContainerSnapshot(), violationHandler(),session);
		try {
			adapter.query(null);
			fail("Should fail if query postcondition is violated");
		} catch(FeaturePostconditionException e) {
			verifyExpectedFeatureException(e, Queryable.class, ViolationHandler.class, session);
		}
	}

	private void verifyExpectedFeatureFailure(FeatureException failure, Class<?> featureClass, WriteSession session) {
		assertThat(failure.getCause(),instanceOf(ApplicationRuntimeException.class));
		verifyExpectedFeatureException(failure, featureClass, FailingHandler.class,session);
	}

	private void verifyExpectedFeatureException(FeatureException failure, Class<?> featureClass, Class<?> handlerClass, final WriteSession session) {
		assertThat(failure.getFeatureClassName(),equalTo(featureClass.getName()));
		assertThat(failure.getHandlerClassName(),equalTo(handlerClass.getName()));
		assertThat(failure.getTemplateId(),equalTo(templateId()));
		try {
			new Verifications() {{
				session.close();
			}};
		} catch (Exception e) {
		}
	}

	private Adapter prepareAdapter(
			final Resource resource,
			final WriteSessionService service,
			final WriteSessionConfiguration configuration,
			final MockResourceSnapshot snapshot,
			final ResourceHandler handler,
			final WriteSession session) {
		new Expectations() {{
			service.createSession(configuration);result=session;
			resource.id();result=ResourceId.createId(resourceName(), templateId());
			service.attach((WriteSession)any,resource,handler.getClass());result=snapshot;
		}};
		snapshot.setTemplateId(templateId());
		snapshot.setHandler(handler);
		return AdapterFactory.newAdapter(resource,handler,service,configuration);
	}

	private SimpleHandler simpleHandler() {
		return new SimpleHandler(DataSets.createDataSet(resourceName()));
	}

	private ViolationHandler violationHandler() {
		return new ViolationHandler();
	}

	private Name<String> resourceName() {
		return NamingScheme.getDefault().name("id");
	}

	private String templateId() {
		return "managerId";
	}

}
