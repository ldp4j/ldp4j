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

import org.ldp4j.application.ApplicationApiRuntimeException;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.ext.ApplicationException;
import org.ldp4j.application.ext.ContainerHandler;
import org.ldp4j.application.ext.Deletable;
import org.ldp4j.application.ext.Modifiable;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.ext.Queryable;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.kernel.session.WriteSessionConfiguration;
import org.ldp4j.application.kernel.session.WriteSessionService;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SessionTerminationException;
import org.ldp4j.application.session.SnapshotVisitor;
import org.ldp4j.application.session.WriteSession;

final class AdapterFactory {

	private static class ResourceAdapter<T extends ResourceSnapshot> implements Adapter {

		private final T resource;
		private final ResourceId resourceId;
		private final ResourceHandler delegate;
		private final WriteSession session;
		private final WriteSessionService service;

		private ResourceAdapter(T resource, ResourceId resourceId, ResourceHandler delegate, WriteSession session, WriteSessionService service) {
			this.resource = resource;
			this.resourceId = resourceId;
			this.delegate = delegate;
			this.session = session;
			this.service = service;
		}

		protected final WriteSession writeSession() {
			return this.session;
		}

		protected final <S> S as(Class<? extends S> clazz) throws UnsupportedFeatureException {
			if(clazz.isInstance(this.delegate)) {
				return clazz.cast(this.delegate);
			}
			throw new UnsupportedFeatureException(resource(),clazz);
		}

		protected final T resource() {
			return this.resource;
		}

		protected final FeatureException featureException(Throwable cause, Class<?> feature) {
			return new FeatureExecutionException(resource(),feature,cause);
		}

		protected final void finalizeSession() {
			try {
				this.session.close();
			} catch (SessionTerminationException e) {
				throw new CouldNotTerminateSessionException(e);
			}
		}

		protected final Resource detach(ResourceSnapshot snapshot) {
			return this.service.detach(this.session, snapshot);
		}

		protected final void checkResponseNotNull(Object response, Class<?> feature, String violation) throws FeaturePostconditionException {
			if(response==null) {
				throw new FeaturePostconditionException(resource(),feature,violation);
			}
		}

		@Override
		public final ResourceId resourceId() {
			return this.resourceId;
		}

		@Override
		public final DataSet get() throws FeatureException {
			try {
				DataSet dataSet = this.delegate.get(resource());
				checkResponseNotNull(dataSet,ResourceHandler.class,"No data set returned");
				return dataSet;
			} catch (ApplicationException | ApplicationApiRuntimeException e) {
				throw featureException(e,ResourceHandler.class);
			} finally {
				finalizeSession();
			}
		}

		@Override
		public final DataSet query(Query query) throws FeatureException {
			try {
				DataSet dataSet = as(Queryable.class).query(resource(), query, writeSession());
				checkResponseNotNull(dataSet,Queryable.class,"No data set returned");
				return dataSet;
			} catch (ApplicationException | ApplicationApiRuntimeException e) {
				throw featureException(e,Queryable.class);
			} finally {
				finalizeSession();
			}
		}

		@Override
		public final void update(DataSet content) throws FeatureException {
			try {
				as(Modifiable.class).update(resource(), content, writeSession());
			} catch (ApplicationException | ApplicationApiRuntimeException e) {
				throw featureException(e,Modifiable.class);
			} finally {
				finalizeSession();
			}
		}

		@Override
		public final void delete() throws FeatureException {
			try {
				as(Deletable.class).delete(resource(),writeSession());
			} catch (ApplicationException | ApplicationApiRuntimeException e) {
				throw featureException(e,Deletable.class);
			} finally {
				finalizeSession();
			}
		}

		@Override
		public Resource create(DataSet content) throws FeatureException {
			finalizeSession();
			throw new UnsupportedFeatureException(resource(),ContainerHandler.class);
		}

	}

	private static class ContainerAdapter extends ResourceAdapter<ContainerSnapshot> {

		private ContainerAdapter(ContainerSnapshot container, ResourceId resourceId, ResourceHandler delegate, WriteSession session, WriteSessionService service) {
			super(container,resourceId,delegate,session,service);
		}

		@Override
		public Resource create(DataSet content) throws FeatureException {
			try {
				ResourceSnapshot create = as(ContainerHandler.class).create(resource(),content,writeSession());
				checkResponseNotNull(create, ContainerHandler.class, "No resource created");
				return detach(create);
			} catch (ApplicationException | ApplicationApiRuntimeException e) {
				throw featureException(e,ContainerHandler.class);
			} finally {
				finalizeSession();
			}
		}

	}

	private static final class FactoryVistor implements SnapshotVisitor {

		private final ResourceHandler delegate;
		private final WriteSession session;
		private final ResourceId resourceId;
		private final WriteSessionService service;
		private ResourceAdapter<?> adapter;

		private FactoryVistor(ResourceId resourceId, WriteSession session, WriteSessionService service, ResourceHandler handler) {
			this.resourceId = resourceId;
			this.session = session;
			this.service = service;
			this.delegate= handler;
		}

		public Adapter getAdapter() {
			return adapter;
		}

		@Override
		public void visitResourceSnapshot(ResourceSnapshot resource) {
			this.adapter=new ResourceAdapter<ResourceSnapshot>(resource,this.resourceId,this.delegate,this.session,this.service);
		}

		@Override
		public void visitContainerSnapshot(ContainerSnapshot resource) {
			this.adapter=new ContainerAdapter(resource,this.resourceId,this.delegate,this.session,this.service);
		}

	}

	private AdapterFactory() {
	}

	static Adapter newAdapter(Resource resource, ResourceHandler resourceHandler, WriteSessionService writeSessionService, WriteSessionConfiguration configuration) {
		WriteSession session = writeSessionService.createSession(configuration);
		ResourceSnapshot snapshot = writeSessionService.attach(session,resource,resourceHandler.getClass());
		FactoryVistor factory = new FactoryVistor(resource.id(),session,writeSessionService,resourceHandler);
		snapshot.accept(factory);
		return factory.getAdapter();
	}

}