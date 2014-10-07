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

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.endpoint.EndpointLifecycleListener;
import org.ldp4j.application.endpoint.EndpointManagementService;
import org.ldp4j.application.ext.Application;
import org.ldp4j.application.ext.Configuration;
import org.ldp4j.application.ext.Deletable;
import org.ldp4j.application.ext.Modifiable;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.lifecycle.ApplicationInitializationException;
import org.ldp4j.application.lifecycle.ApplicationLifecycleListener;
import org.ldp4j.application.lifecycle.ApplicationLifecycleService;
import org.ldp4j.application.lifecycle.LifecycleException;
import org.ldp4j.application.lifecycle.LifecycleManager;
import org.ldp4j.application.resource.Container;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceControllerService;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.session.WriteSessionService;
import org.ldp4j.application.spi.EndpointRepository;
import org.ldp4j.application.spi.RepositoryRegistry;
import org.ldp4j.application.spi.ResourceRepository;
import org.ldp4j.application.spi.RuntimeInstance;
import org.ldp4j.application.spi.ServiceRegistry;
import org.ldp4j.application.template.ResourceTemplate;
import org.ldp4j.application.template.TemplateIntrospector;
import org.ldp4j.application.template.TemplateManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApplicationContext {

	private static Logger LOGGER=LoggerFactory.getLogger(ApplicationContext.class);
	
	private static ApplicationContext context;

	private ResourceRepository resourceRepository;
	private EndpointRepository endpointRepository;
	private ApplicationLifecycleService applicationLifecycleService;
	private TemplateManagementService templateManagementService;
	private EndpointManagementService endpointManagementService;
	private WriteSessionService writeSessionService;

	private Application<Configuration> application;

	private ResourceControllerService resourceControllerService;

	private final PublicResourceFactory factory;

	private ApplicationContext() {
		this.factory = PublicResourceFactory.newInstance(this);
	}

	private static <T> T checkNotNull(T object, String message) {
		if(object==null) {
			throw new ApplicationContextException(message);
		}
		return object;
	}

	private ApplicationContext withWriteSessionService(WriteSessionService service) {
		this.writeSessionService = checkNotNull(service,"Write session service cannot be null");
		return this;
	}

	private ApplicationContext withEndpointManagementService(EndpointManagementService service) {
		this.endpointManagementService = checkNotNull(service,"Endpoint management service cannot be null");
		return this;
	}

	private ApplicationContext withApplicationLifecycleService(ApplicationLifecycleService service) {
		this.applicationLifecycleService = checkNotNull(service,"Application lifecycle service cannot be null");
		return this;
	}

	private ApplicationContext withTemplateManagementService(TemplateManagementService service) {
		this.templateManagementService = checkNotNull(service,"Template management service cannot be null");
		return this;
	}

	private ApplicationContext withResourceControllerService(ResourceControllerService service) {
		this.resourceControllerService = checkNotNull(service,"Resource controller service cannot be null");
		return this;
	}

	private ApplicationContext withResourceRepository(ResourceRepository resourceRepository) {
		this.resourceRepository=checkNotNull(resourceRepository,"Resource repository cannot be null");
		return this;
	}

	private ApplicationContext withEndpointRepository(EndpointRepository endpointRepository) {
		this.endpointRepository=checkNotNull(endpointRepository,"Endpoint repository cannot be null");
		return this;
	}

	private String applicationFailureMessage(String message, Object... objects) {
		return "[" + this.application.getName() + "]" + String.format(message,objects);
	}

	private Application<Configuration> application() {
		return application;
	}

	private void initializeComponents() throws LifecycleException {
		LifecycleManager.init(this.resourceRepository);
		LifecycleManager.init(this.endpointRepository);
		LifecycleManager.init(this.endpointManagementService);
		LifecycleManager.init(this.resourceControllerService);
		LifecycleManager.init(this.templateManagementService);
		LifecycleManager.init(this.writeSessionService);
	}

	private void shutdownComponents() {
		shutdown(this.endpointManagementService);
		shutdown(this.resourceControllerService);
		shutdown(this.templateManagementService);
		shutdown(this.writeSessionService);
		shutdown(this.endpointRepository);
		shutdown(this.resourceRepository);
	}

	private <T> void shutdown(T object) {
		try {
			LifecycleManager.shutdown(object);
		} catch (LifecycleException e) {
			LOGGER.error("Could not shutdown "+object,e);
		}
	}

	DataSet getResource(Endpoint endpoint) throws ApplicationExecutionException {
		ResourceId resourceId=endpoint.resourceId();
		Resource resource = this.resourceRepository.find(resourceId,Resource.class);
		if(resource==null) {
			String errorMessage = applicationFailureMessage("Could not find resource for endpoint '%s'",endpoint);
			LOGGER.error(errorMessage);
			throw new ApplicationExecutionException(errorMessage);
		}
		try {
			return this.resourceControllerService.getResource(resource);
		} catch (Exception e) {
			String errorMessage = applicationFailureMessage("Resource '%s' retrieval failed ",endpoint);
			LOGGER.error(errorMessage,e);
			throw new ApplicationExecutionException(errorMessage,e);
		}
	}

	Resource resolveResource(Endpoint endpoint) {
		return this.resourceRepository.find(endpoint.resourceId(), Resource.class);
	}

	Resource createResource(Endpoint endpoint, DataSet dataSet) throws ApplicationExecutionException {
		ResourceId resourceId=endpoint.resourceId();
		Container resource = this.resourceRepository.find(resourceId,Container.class);
		if(resource==null) {
			String errorMessage = applicationFailureMessage("Could not find container for endpoint '%s'",endpoint);
			LOGGER.error(errorMessage);
			throw new ApplicationExecutionException(errorMessage);
		}
		try {
			return this.resourceControllerService.createResource(resource, dataSet);
		} catch (Exception e) {
			String errorMessage = applicationFailureMessage("Resource create failed at '%s'",endpoint);
			LOGGER.error(errorMessage,e);
			throw new ApplicationExecutionException(errorMessage,e);
		}
	}

	void deleteResource(Endpoint endpoint) throws ApplicationExecutionException {
		ResourceId resourceId=endpoint.resourceId();
		Resource resource = this.resourceRepository.find(resourceId,Resource.class);
		if(resource==null) {
			String errorMessage = applicationFailureMessage("Could not find container for endpoint '%s'",endpoint);
			LOGGER.error(errorMessage);
			throw new ApplicationExecutionException(errorMessage);
		}
		try {
			this.resourceControllerService.deleteResource(resource);
		} catch (Exception e) {
			String errorMessage = applicationFailureMessage("Resource deletion failed at '%s'",endpoint);
			LOGGER.error(errorMessage,e);
			throw new ApplicationExecutionException(errorMessage,e);
		}
	}

	void modifyResource(Endpoint endpoint, DataSet dataSet) throws ApplicationExecutionException {
		ResourceId resourceId=endpoint.resourceId();
		Resource resource = this.resourceRepository.find(resourceId,Resource.class);
		if(resource==null) {
			String errorMessage = applicationFailureMessage("Could not find resource for endpoint '%s'",endpoint);
			LOGGER.error(errorMessage);
			throw new ApplicationExecutionException(errorMessage);
		}
		try {
			this.resourceControllerService.updateResource(resource,dataSet);
		} catch (Exception e) {
			String errorMessage = applicationFailureMessage("Resource modification failed at '%s'",endpoint);
			LOGGER.error(errorMessage,e);
			throw new ApplicationExecutionException(errorMessage,e);
		}
	}

	Capabilities endpointCapabilities(Endpoint endpoint) {
		MutableCapabilities result=new MutableCapabilities();
		Resource resource = resolveResource(endpoint);
		ResourceTemplate template=resourceTemplate(resource);
		Class<? extends ResourceHandler> handlerClass = template.handlerClass();
		result.setModifiable(Modifiable.class.isAssignableFrom(handlerClass));
		result.setDeletable(Deletable.class.isAssignableFrom(handlerClass) && !resource.isRoot());
		// TODO: Analyze how to provide patch support
		result.setPatchable(false);
		TemplateIntrospector introspector = TemplateIntrospector.newInstance(template);
		result.setFactory(introspector.isContainer());
		return result;
	}

	ResourceTemplate resourceTemplate(Resource resource) {
		return this.templateManagementService.findTemplateById(resource.id().templateId());
	}

	public void initialize(String applicationClassName) throws ApplicationInitializationException {
		try {
			this.application = this.applicationLifecycleService.initialize(applicationClassName);
		} catch (ApplicationInitializationException e) {
			String errorMessage = "Application '"+applicationClassName+"' initilization failed";
			LOGGER.error(errorMessage,e);
			shutdownComponents();
			throw e;
		}
	}

	public boolean shutdown() {
		this.applicationLifecycleService.shutdown();
		shutdownComponents();
		return this.applicationLifecycleService.isShutdown();
	}

	public String applicationName() {
		return application().getName();
	}

	public String applicationClassName() {
		return this.application.getClass().getName();
	}

	public PublicResource findResource(Endpoint endpoint) {
		return this.factory.createResource(endpoint);
	}

	public Endpoint resolveEndpoint(String path) {
		checkNotNull(path,"Endpoint path cannot be null");
		return this.endpointManagementService.resolveEndpoint(path);
	}

	public Endpoint resolveResource(ResourceId id) {
		return this.endpointRepository.endpointOfResource(id);
	}

	public void registerApplicationLifecycleListener(ApplicationLifecycleListener listener) {
		checkNotNull(listener,"Application lifecycle listener cannot be null");
		this.applicationLifecycleService.registerApplicationLifecycleListener(listener);
	}

	public void deregisterApplicationLifecycleListener(ApplicationLifecycleListener listener) {
		checkNotNull(listener,"Application lifecycle listener cannot be null");
		this.applicationLifecycleService.deregisterApplicationLifecycleListener(listener);
	}

	public void registerEndpointLifecyleListener(EndpointLifecycleListener listener) {
		checkNotNull(listener,"Endpoint lifecycle listener cannot be null");
		this.endpointManagementService.registerEndpointLifecycleListener(listener);
	}

	public void deregisterEndpointLifecycleListener(EndpointLifecycleListener listener) {
		checkNotNull(listener,"Endpoint lifecycle listener cannot be null");
		this.endpointManagementService.deregisterEndpointLifecycleListener(listener);
	}

	private static void setCurrentContext(ApplicationContext context) {
		ApplicationContext.context = context;
	}
	
	private static ApplicationContext newApplicationContext() {
		RuntimeInstance instance = RuntimeInstance.getInstance();
		RepositoryRegistry repositoryRegistry = instance.getRepositoryRegistry();
		ServiceRegistry serviceRegistry = instance.getServiceRegistry();
		ApplicationContext context=
			new ApplicationContext().
				withEndpointRepository(repositoryRegistry.getEndpointRepository()).
				withResourceRepository(repositoryRegistry.getResourceRepository()).
				withApplicationLifecycleService(serviceRegistry.getService(ApplicationLifecycleService.class)).
				withTemplateManagementService(serviceRegistry.getService(TemplateManagementService.class)).
				withEndpointManagementService(serviceRegistry.getService(EndpointManagementService.class)).
				withWriteSessionService(serviceRegistry.getService(WriteSessionService.class)).
				withResourceControllerService(serviceRegistry.getService(ResourceControllerService.class));
		return context;
	}

	public static synchronized ApplicationContext currentContext() {
		if(ApplicationContext.context==null) {
			ApplicationContext.context=newApplicationContext();
		}
		return ApplicationContext.context;
	}
	
	public static synchronized ApplicationContext createContext(String applicationClassName) {
		// Candidate application context configuration
		ApplicationContext context = newApplicationContext();

		// Candidate application context component initialization
		try {
			context.initializeComponents();
		} catch (LifecycleException e) {
			String errorMessage = "Could not initialize application context components";
			LOGGER.error(errorMessage,e);
			throw new ApplicationContextException(errorMessage,e);
		}
		
		// Candidate application context target application initialization
		try {
			context.initialize(applicationClassName);
		} catch (ApplicationInitializationException e) {
			String errorMessage = "Application '"+applicationClassName+"' initilization failed";
			LOGGER.error(errorMessage,e);
			context.shutdownComponents();
			throw new ApplicationContextException(errorMessage,e);
		}

		// Current application context setup
		setCurrentContext(context);
		return currentContext();
	}

}
