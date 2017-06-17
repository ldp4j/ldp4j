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

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.ldp4j.application.engine.ApplicationContextCreationException;
import org.ldp4j.application.engine.ApplicationContextTerminationException;
import org.ldp4j.application.engine.ApplicationEngine;
import org.ldp4j.application.engine.ApplicationEngineInitializationException;
import org.ldp4j.application.engine.ApplicationEngineRuntimeException;
import org.ldp4j.application.engine.ApplicationEngineTerminationException;
import org.ldp4j.application.ext.ApplicationShutdownException;
import org.ldp4j.application.kernel.endpoint.EndpointManagementService;
import org.ldp4j.application.kernel.lifecycle.ApplicationLifecycleService;
import org.ldp4j.application.kernel.lifecycle.LifecycleException;
import org.ldp4j.application.kernel.lifecycle.LifecycleManager;
import org.ldp4j.application.kernel.resource.ResourceControllerService;
import org.ldp4j.application.kernel.service.ServiceRegistry;
import org.ldp4j.application.kernel.session.WriteSessionService;
import org.ldp4j.application.kernel.spi.RuntimeDelegate;
import org.ldp4j.application.kernel.template.TemplateManagementService;
import org.ldp4j.application.kernel.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public final class DefaultApplicationEngine extends ApplicationEngine {

	private static final Logger LOGGER=LoggerFactory.getLogger(DefaultApplicationEngine.class);

	private WriteSessionService writeSessionService;
	private EndpointManagementService endpointManagementService;
	private ApplicationLifecycleService applicationLifecycleService;
	private TemplateManagementService templateManagementService;
	private ResourceControllerService resourceControllerService;
	private RuntimeDelegate runtimeDelegate;

	private TransactionManager transactionManager;

	private DefaultApplicationContext activeContext;

	public DefaultApplicationEngine() {
	}

	private static <T> T checkNotNull(T object, String message) {
		if(object==null) {
			throw new ApplicationEngineRuntimeException(message);
		}
		return object;
	}

	private void initialize() {
		setRuntimeManager(RuntimeDelegate.getInstance());
		ServiceRegistry serviceRegistry = ServiceRegistry.getInstance();
		setApplicationLifecycleService(serviceRegistry.getService(ApplicationLifecycleService.class));
		setTemplateManagementService(serviceRegistry.getService(TemplateManagementService.class));
		setEndpointManagementService(serviceRegistry.getService(EndpointManagementService.class));
		setWriteSessionService(serviceRegistry.getService(WriteSessionService.class));
		setResourceControllerService(serviceRegistry.getService(ResourceControllerService.class));
	}

	private void setRuntimeManager(RuntimeDelegate runtimeDelegate) {
		this.runtimeDelegate=checkNotNull(runtimeDelegate,"Resource factory cannot be null");
		this.transactionManager=runtimeDelegate.getTransactionManager();
	}

	private void setWriteSessionService(WriteSessionService service) {
		this.writeSessionService=checkNotNull(service,"Write session service cannot be null");
	}

	private void setEndpointManagementService(EndpointManagementService service) {
		this.endpointManagementService=checkNotNull(service,"Endpoint management service cannot be null");
	}

	private void setApplicationLifecycleService(ApplicationLifecycleService service) {
		this.applicationLifecycleService=checkNotNull(service,"Application lifecycle service cannot be null");
	}

	private void setTemplateManagementService(TemplateManagementService service) {
		this.templateManagementService=checkNotNull(service,"Template management service cannot be null");
	}

	private void setResourceControllerService(ResourceControllerService service) {
		this.resourceControllerService=checkNotNull(service,"Resource controller service cannot be null");
	}

	private <T> void shutdownComponent(T object, List<? super LifecycleException> failures) {
		try {
			LifecycleManager.shutdown(object);
		} catch (LifecycleException e) {
			LOGGER.error(String.format("Could not shutdown component %s",object.getClass().getName()),e);
			failures.add(e);
		}
	}

	private <T> void initializeComponent(T object, Deque<? super T> initializedComponents) throws ComponentLifecycleException {
		try {
			LifecycleManager.init(object);
			initializedComponents.push(object);
		} catch (LifecycleException e) {
			throw new ComponentLifecycleException(object,e);
		}
	}

	private void shutdownComponentsQuietly(Deque<Object> initializedComponents) {
		List<LifecycleException> failures=Lists.newArrayList();
		while(!initializedComponents.isEmpty()) {
			Object component=initializedComponents.pop();
			shutdownComponent(component,failures);
		}
	}

	DefaultApplicationContext activeContext() {
		return this.activeContext;
	}

	WriteSessionService writeSessionService() {
		return this.writeSessionService;
	}

	EndpointManagementService endpointManagementService() {
		return this.endpointManagementService;
	}

	ApplicationLifecycleService applicationLifecycleService() {
		return this.applicationLifecycleService;
	}

	TemplateManagementService templateManagementService() {
		return this.templateManagementService;
	}

	ResourceControllerService resourceControllerService() {
		return this.resourceControllerService;
	}

	TransactionManager transactionManager() {
		return this.transactionManager;
	}

	private static final class DefaultApplicationContextManager extends ApplicationContextManager<DefaultApplicationContext> {

		private final DefaultApplicationEngine defaultApplicationEngine;

		private DefaultApplicationContextManager(DefaultApplicationEngine defaultApplicationEngine) {
			super(DefaultApplicationContext.class);
			this.defaultApplicationEngine = defaultApplicationEngine;
		}

		@Override
		protected DefaultApplicationContext createContext(String applicationClassName) throws ApplicationContextCreationException {
			DefaultApplicationContext currentContext=new DefaultApplicationContext(this.defaultApplicationEngine);
			currentContext.initialize(applicationClassName);
			this.defaultApplicationEngine.activeContext=currentContext;
			return currentContext;
		}

		@Override
		protected boolean doDisposeContext(DefaultApplicationContext applicationContext) throws ApplicationContextTerminationException {
			try {
				this.defaultApplicationEngine.applicationLifecycleService().shutdown();
				applicationContext.shutdown();
				this.defaultApplicationEngine.activeContext=null;
				return this.defaultApplicationEngine.applicationLifecycleService().isShutdown();
			} catch (ApplicationShutdownException e) {
				throw new ApplicationContextTerminationException(e);
			}
		}

	}

	@Override
	protected void setUp() throws ApplicationEngineInitializationException {
		try {
			initialize();
		} catch (Exception e) {
			String errorMessage = "Invalid default application engine setup";
			LOGGER.error(errorMessage,e);
			throw new ApplicationEngineInitializationException(errorMessage,e);
		}
		Deque<Object> initializedComponents=new LinkedList<Object>();
		try {
			initializeComponent(this.runtimeDelegate,initializedComponents);
			initializeComponent(this.templateManagementService,initializedComponents);
			initializeComponent(this.endpointManagementService,initializedComponents);
			initializeComponent(this.resourceControllerService,initializedComponents);
			initializeComponent(this.writeSessionService,initializedComponents);
		} catch (ComponentLifecycleException e) {
			shutdownComponentsQuietly(initializedComponents);
			String errorMessage = String.format("Could not initialize component %s",e.getComponent().getName());
			LOGGER.error(errorMessage,e);
			throw new ApplicationEngineInitializationException(errorMessage,e);
		}
	}

	@Override
	protected void cleanUp() throws ApplicationEngineTerminationException {
		List<LifecycleException> failures=Lists.newArrayList();
		shutdownComponent(this.endpointManagementService,failures);
		shutdownComponent(this.resourceControllerService,failures);
		shutdownComponent(this.writeSessionService,failures);
		shutdownComponent(this.templateManagementService,failures);
		shutdownComponent(this.runtimeDelegate,failures);
		if(!failures.isEmpty()) {
			throw new ApplicationEngineTerminationException("Could not shutdown engine components");
		}
	}

	@Override
	protected ApplicationContextManager<DefaultApplicationContext> applicationContextManager() {
		return new DefaultApplicationContextManager(this);
	}

}
