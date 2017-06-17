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
package org.ldp4j.application.kernel.engine;

import java.net.URI;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ldp4j.application.ApplicationContextException;
import org.ldp4j.application.engine.ApplicationEngine;
import org.ldp4j.application.engine.ApplicationEngineException;
import org.ldp4j.application.engine.lifecycle.ApplicationEngineLifecycleListener;
import org.ldp4j.application.engine.util.ListenerManager;
import org.ldp4j.application.engine.util.Notification;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.session.WriteSessionConfiguration;
import org.ldp4j.application.kernel.session.WriteSessionService;
import org.ldp4j.application.kernel.transaction.Transaction;
import org.ldp4j.application.kernel.transaction.TransactionManager;
import org.ldp4j.application.session.ReadSession;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SnapshotResolutionException;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.spi.ResourceSnapshotResolver;
import org.ldp4j.application.spi.RuntimeDelegate;
import org.ldp4j.application.spi.ShutdownListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public final class CoreRuntimeDelegate extends RuntimeDelegate {

	private static final class ShutdownListenerManager extends ApplicationEngineLifecycleListener {

		private final CopyOnWriteArrayList<ListenerManager<ShutdownListener>> managers;

		private ShutdownListenerManager() {
			this.managers=Lists.newCopyOnWriteArrayList();
		}

		private void registerManagers(ListenerManager<ShutdownListener> manager) {
			this.managers.add(manager);
		}

		@Override
		protected void onApplicationEngineShutdown() {
			final Notification<ShutdownListener> notification =
				new Notification<ShutdownListener>() {
					@Override
					public void propagate(ShutdownListener listener) {
						listener.engineShutdown();
					}
				};
			for(ListenerManager<ShutdownListener> manager:managers) {
				manager.notify(notification);
			}
		}
	}

	final class DefaultSnapshotResolver implements ResourceSnapshotResolver {

		private final URI canonicalBase;
		private final ReadSession session;

		protected DefaultSnapshotResolver(URI canonicalBase, ReadSession session) {
			this.canonicalBase = canonicalBase;
			this.session = session;
		}

		@Override
		public URI resolve(ResourceSnapshot resource) {
			ResourceId resourceId =
				ResourceId.createId(
					resource.name(),
					resource.templateId());
			try {
				Endpoint endpoint =
					context().
						resolveResource(
							resourceId);
				URI result = null;
				if(endpoint!=null && endpoint.deleted()==null) {
					result=this.canonicalBase.resolve(endpoint.path());
				}
				return result;
			} catch (ApplicationEngineException e) {
				throw new SnapshotResolutionException("Could not resolve endpoint for resource '"+resourceId+"'",e);
			}
		}

		@Override
		public ResourceSnapshot resolve(URI endpoint) {
			ResourceSnapshot result=null;
			if(!endpoint.isOpaque()) {
				URI path=this.canonicalBase.relativize(endpoint);
				if(!path.isAbsolute()) {
					result = resolveManagedResource(path);
				}
			}
			return result;
		}

		private ResourceSnapshot resolveManagedResource(URI path) {
			try {
				Endpoint endpoint=
					applicationEngine().
						endpointManagementService().
							resolveEndpoint(path.toString());
				ResourceSnapshot result=null;
				if(endpoint!=null && endpoint.deleted()==null) {
					org.ldp4j.application.kernel.resource.Resource resource =
						context().
							resolveResource(endpoint);
					if(resource!=null) {
						ResourceId resourceId = resource.id();
						result=
							this.session.
								find(
									ResourceSnapshot.class,
									resourceId.name(),
									context().resourceTemplate(resource).handlerClass());
					}
				}
				return result;
			} catch (Exception e) {
				throw new SnapshotResolutionException("Could not resolve resource for endpoint '"+this.canonicalBase.resolve(path)+"'",e);
			}
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(CoreRuntimeDelegate.class);
	private static final ShutdownListenerManager MANAGER=new ShutdownListenerManager();

	private final ListenerManager<ShutdownListener> shutdownListeners;

	static {
		ApplicationEngine.registerLifecycleListener(MANAGER);
	}

	public CoreRuntimeDelegate() {
		this.shutdownListeners=ListenerManager.<ShutdownListener>newInstance();
		MANAGER.registerManagers(this.shutdownListeners);
	}

	private DefaultApplicationEngine applicationEngine() throws ApplicationEngineException {
		return
			ApplicationEngine.
				engine().
					unwrap(DefaultApplicationEngine.class);
	}

	private DefaultApplicationContext context() throws ApplicationEngineException {
		return applicationEngine().activeContext();
	}

	private WriteSessionService sessionService() throws ApplicationEngineException {
		return applicationEngine().writeSessionService();
	}

	private TransactionManager transactionManager() throws ApplicationEngineException {
		return applicationEngine().transactionManager();
	}

	@Override
	public boolean isOffline() {
		boolean result=true;
		try {
			result=!applicationEngine().state().isStarted();
		} catch (ApplicationEngineException e) {
			LOGGER.warn("Could not check engine state",e);
		}
		return result;
	}

	@Override
	public WriteSession createSession() throws ApplicationContextException {
		try {
			WriteSessionService sessionService = sessionService();
			WriteSession delegate =
				sessionService.
					createSession(
						WriteSessionConfiguration.
							builder().
								build());
			Transaction transaction=transactionManager().currentTransaction();
			transaction.begin();
			return new TransactionalWriteSession(transaction,delegate);
		} catch (Exception e) {
			throw new ApplicationContextException("Could not create session",e);
		}
	}

	@Override
	public ResourceSnapshotResolver createResourceResolver(URI canonicalBase, ReadSession session) {
		return new DefaultSnapshotResolver(canonicalBase,session);
	}

	@Override
	public void registerShutdownListener(ShutdownListener listener) {
		this.shutdownListeners.registerListener(listener);
	}

}
