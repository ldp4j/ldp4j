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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application;

import java.util.List;
import java.util.Stack;

import org.ldp4j.application.endpoint.EndpointManagementService;
import org.ldp4j.application.engine.ApplicationEngine;
import org.ldp4j.application.engine.ApplicationEngineInitializationException;
import org.ldp4j.application.engine.ApplicationEngineRuntimeException;
import org.ldp4j.application.engine.ApplicationEngineTerminationException;
import org.ldp4j.application.engine.ApplicationInitializationException;
import org.ldp4j.application.lifecycle.ApplicationLifecycleService;
import org.ldp4j.application.lifecycle.LifecycleException;
import org.ldp4j.application.lifecycle.LifecycleManager;
import org.ldp4j.application.resource.ResourceControllerService;
import org.ldp4j.application.session.WriteSessionService;
import org.ldp4j.application.spi.EndpointRepository;
import org.ldp4j.application.spi.RepositoryRegistry;
import org.ldp4j.application.spi.ResourceRepository;
import org.ldp4j.application.spi.RuntimeInstance;
import org.ldp4j.application.spi.ServiceRegistry;
import org.ldp4j.application.template.TemplateManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DefaultApplicationEngine extends ApplicationEngine {

	private static final Logger LOGGER=LoggerFactory.getLogger(DefaultApplicationEngine.class);

	private WriteSessionService writeSessionService;
	private EndpointManagementService endpointManagementService;
	private ApplicationLifecycleService applicationLifecycleService;
	private TemplateManagementService templateManagementService;
	private ResourceControllerService resourceControllerService;
	private ResourceRepository resourceRepository;
	private EndpointRepository endpointRepository;

	public DefaultApplicationEngine() {
	}

	private static <T> T checkNotNull(T object, String message) {
		if(object==null) {
			throw new ApplicationEngineRuntimeException(message);
		}
		return object;
	}

	private void initialize() {
		RuntimeInstance instance = RuntimeInstance.getInstance();

		RepositoryRegistry repositoryRegistry = instance.getRepositoryRegistry();
		setEndpointRepository(repositoryRegistry.getEndpointRepository());
		setResourceRepository(repositoryRegistry.getResourceRepository());

		ServiceRegistry serviceRegistry = instance.getServiceRegistry();
		setApplicationLifecycleService(serviceRegistry.getService(ApplicationLifecycleService.class));
		setTemplateManagementService(serviceRegistry.getService(TemplateManagementService.class));
		setEndpointManagementService(serviceRegistry.getService(EndpointManagementService.class));
		setWriteSessionService(serviceRegistry.getService(WriteSessionService.class));
		setResourceControllerService(serviceRegistry.getService(ResourceControllerService.class));
	}

	private void setWriteSessionService(WriteSessionService service) {
		this.writeSessionService = checkNotNull(service,"Write session service cannot be null");
	}

	private void setEndpointManagementService(EndpointManagementService service) {
		this.endpointManagementService = checkNotNull(service,"Endpoint management service cannot be null");
	}

	private void setApplicationLifecycleService(ApplicationLifecycleService service) {
		this.applicationLifecycleService = checkNotNull(service,"Application lifecycle service cannot be null");
	}

	private void setTemplateManagementService(TemplateManagementService service) {
		this.templateManagementService = checkNotNull(service,"Template management service cannot be null");
	}

	private void setResourceControllerService(ResourceControllerService service) {
		this.resourceControllerService = checkNotNull(service,"Resource controller service cannot be null");
	}

	private void setResourceRepository(ResourceRepository resourceRepository) {
		this.resourceRepository=checkNotNull(resourceRepository,"Resource repository cannot be null");
	}

	private void setEndpointRepository(EndpointRepository endpointRepository) {
		this.endpointRepository=checkNotNull(endpointRepository,"Endpoint repository cannot be null");
	}

	private <T> void shutdownComponent(T object, List<? super LifecycleException> failures) {
		try {
			LifecycleManager.shutdown(object);
		} catch (LifecycleException e) {
			LOGGER.error(String.format("Could not shutdown component %s",object.getClass().getName()),e);
			failures.add(e);
		}
	}

	private <T> void initializeComponent(T object, Stack<? super T> initializedComponents) throws ComponentLifecycleException {
		try {
			LifecycleManager.init(object);
			initializedComponents.push(object);
		} catch (LifecycleException e) {
			throw new ComponentLifecycleException(object,e);
		}
	}

	private void shutdownComponentsQuietly(Stack<Object> initializedComponents) {
		List<LifecycleException> failures=Lists.newArrayList();
		while(!initializedComponents.isEmpty()) {
			Object component=initializedComponents.pop();
			shutdownComponent(component,failures);
		}
	}

	WriteSessionService writeSessionService() {
		return writeSessionService;
	}

	EndpointManagementService endpointManagementService() {
		return endpointManagementService;
	}

	ApplicationLifecycleService applicationLifecycleService() {
		return applicationLifecycleService;
	}

	TemplateManagementService templateManagementService() {
		return templateManagementService;
	}

	ResourceControllerService resourceControllerService() {
		return resourceControllerService;
	}

	ResourceRepository resourceRepository() {
		return resourceRepository;
	}

	EndpointRepository endpointRepository() {
		return endpointRepository;
	}

	private static final class DefaultApplicationContextManager extends ApplicationContextManager<DefaultApplicationContext> {

		private final DefaultApplicationEngine defaultApplicationEngine;

		private DefaultApplicationContextManager(DefaultApplicationEngine defaultApplicationEngine) {
			super(DefaultApplicationContext.class);
			this.defaultApplicationEngine = defaultApplicationEngine;
		}

		@Override
		protected DefaultApplicationContext createContext(String applicationClassName) throws ApplicationInitializationException {
			DefaultApplicationContext currentContext=new DefaultApplicationContext(this.defaultApplicationEngine);
			currentContext.initialize(applicationClassName);
			return currentContext;
		}

		@Override
		protected void doDisposeContext(DefaultApplicationContext applicationContext) {
			applicationContext.shutdown();
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
		Stack<Object> initializedComponents=new Stack<Object>();
		try {
			initializeComponent(this.endpointRepository,initializedComponents);
			initializeComponent(this.endpointManagementService,initializedComponents);
			initializeComponent(this.resourceControllerService,initializedComponents);
			initializeComponent(this.templateManagementService,initializedComponents);
			initializeComponent(this.writeSessionService,initializedComponents);
		} catch (ComponentLifecycleException e) {
			shutdownComponentsQuietly(initializedComponents);
			String errorMessage = String.format("Could not initialize component %s",e.getComponent().getClass().getName());
			LOGGER.error(errorMessage,e);
			throw new ApplicationEngineInitializationException(errorMessage,e);
		}
	}

	@Override
	protected void cleanUp() throws ApplicationEngineTerminationException {
		List<LifecycleException> failures=Lists.newArrayList();
		shutdownComponent(this.endpointManagementService,failures);
		shutdownComponent(this.resourceControllerService,failures);
		shutdownComponent(this.templateManagementService,failures);
		shutdownComponent(this.writeSessionService,failures);
		shutdownComponent(this.endpointRepository,failures);
		shutdownComponent(this.resourceRepository,failures);
		if(!failures.isEmpty()) {
			throw new ApplicationEngineTerminationException("Could not shutdown engine components");
		}
	}

	@Override
	protected ApplicationContextManager<DefaultApplicationContext> applicationContextManager() {
		return new DefaultApplicationContextManager(this);
	}

}
