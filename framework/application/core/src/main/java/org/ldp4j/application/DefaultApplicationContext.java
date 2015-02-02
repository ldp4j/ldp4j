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

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.endpoint.EndpointLifecycleListener;
import org.ldp4j.application.engine.ApplicationContextCreationException;
import org.ldp4j.application.engine.context.ApplicationContext;
import org.ldp4j.application.engine.context.ApplicationContextException;
import org.ldp4j.application.engine.context.ApplicationContextOperation;
import org.ldp4j.application.engine.context.ApplicationExecutionException;
import org.ldp4j.application.engine.context.Capabilities;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.application.engine.context.PublicResourceVisitor;
import org.ldp4j.application.engine.lifecycle.ApplicationLifecycleListener;
import org.ldp4j.application.ext.Application;
import org.ldp4j.application.ext.Configuration;
import org.ldp4j.application.ext.Deletable;
import org.ldp4j.application.ext.Modifiable;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.resource.Container;
import org.ldp4j.application.resource.FeatureExecutionException;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.session.WriteSessionConfiguration;
import org.ldp4j.application.template.ResourceTemplate;
import org.ldp4j.application.template.TemplateIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public final class DefaultApplicationContext implements ApplicationContext {

	private final class DefaultApplicationOperation implements ApplicationContextOperation {

		private DefaultApplicationOperation() {
			getContext().operationController.beginTransaction();
		}

		@Override
		public DefaultApplicationContext getContext() {
			return DefaultApplicationContext.this;
		}

		@Override
		public PublicResource findResource(String path) {
			return DefaultApplicationContext.this.findResource(path);
		}

		@Override
		public PublicResource resolveResource(String path) {
			return DefaultApplicationContext.this.resolveResource(path);
		}

		@Override
		public PublicResource resolveResource(ManagedIndividualId id) {
			return DefaultApplicationContext.this.resolveResource(id);
		}

		@Override
		public void dispose() {
			getContext().operationController.endTransaction();
		}

	}

	private static final class ApplicationContextOperationController {

		private final ThreadLocal<AtomicLong> counters;

		private ApplicationContextOperationController() {
			this.counters=new ThreadLocal<AtomicLong>();
		}

		public void beginTransaction() {
			AtomicLong counter = getCounter(false);
			LOGGER.
				info("Started transaction {}({}).{},",
					Thread.currentThread().getName(),
					Thread.currentThread().getId(),
					counter.incrementAndGet());
		}

		private AtomicLong getCounter(boolean required) {
			AtomicLong counter = this.counters.get();
			if(counter==null) {
				if(required) {
					throw new IllegalStateException("Transaction not initiated for thread "+Thread.currentThread().getName()+"("+Thread.currentThread().getId()+")");
				}
				counter=new AtomicLong();
				this.counters.set(counter);
			}
			return counter;
		}

		public void endTransaction() {
			LOGGER.
				info("Completed transaction {}({}).{},",
					Thread.currentThread().getName(),
					Thread.currentThread().getId(),
					getCounter(true));
		}

	}

	private static final class GonePublicResource implements PublicResource {

		private final Endpoint endpoint;

		private GonePublicResource(Endpoint endpoint) {
			this.endpoint = endpoint;
		}

		@Override
		public Status status() {
			return Status.GONE;
		}

		@Override
		public String path() {
			return endpoint.path();
		}

		@Override
		public EntityTag entityTag() {
			return endpoint.entityTag();
		}

		@Override
		public Date lastModified() {
			return endpoint.lastModified();
		}

		@Override
		public Capabilities capabilities() {
			return new MutableCapabilities();
		}

		@Override
		public Map<String, PublicResource> attachments() {
			return Collections.emptyMap();
		}

		@Override
		public ManagedIndividualId individualId() {
			return ManagedIndividualId.createId(endpoint.resourceId().name(), endpoint.resourceId().templateId());
		}

		@Override
		public <T> T accept(PublicResourceVisitor<T> visitor) {
			throw new UnsupportedOperationException("The endpoint is gone");
		}

		@Override
		public DataSet entity(ContentPreferences contentPreferences) throws ApplicationExecutionException {
			throw new UnsupportedOperationException("The endpoint is gone");
		}

		@Override
		public void delete() throws ApplicationExecutionException {
			throw new UnsupportedOperationException("The endpoint is gone");
		}

		@Override
		public void modify(DataSet dataSet) throws ApplicationExecutionException {
			throw new UnsupportedOperationException("The endpoint is gone");
		}
	}

	private final class LocalEndpointLifecycleListener implements EndpointLifecycleListener {
		@Override
		public void endpointCreated(Endpoint endpoint) {
		}
		@Override
		public void endpointDeleted(Endpoint endpoint) {
			DefaultApplicationContext.this.goneEndpoints.put(endpoint.path(),endpoint);
		}
	}

	private static Logger LOGGER=LoggerFactory.getLogger(DefaultApplicationContext.class);

	private Application<Configuration> application;

	private final DefaultPublicResourceFactory factory;
	private final EndpointLifecycleListener endpointLifecycleListener;
	private final Map<String,Endpoint> goneEndpoints;

	private final DefaultApplicationEngine engine;

	private final ApplicationContextOperationController operationController;

	DefaultApplicationContext(DefaultApplicationEngine engine) {
		this.engine = engine;
		this.factory=DefaultPublicResourceFactory.newInstance(this);
		this.goneEndpoints=Maps.newLinkedHashMap();
		this.endpointLifecycleListener = new LocalEndpointLifecycleListener();
		this.operationController=new ApplicationContextOperationController();
	}

	private static <T> T checkNotNull(T object, String message) {
		if(object==null) {
			throw new ApplicationContextException(message);
		}
		return object;
	}

	private String applicationFailureMessage(String message, Object... objects) {
		return "[" + this.application.getName() + "] " + String.format(message,objects);
	}

	private Application<Configuration> application() {
		return this.application;
	}

	DataSet getResource(Endpoint endpoint) throws ApplicationExecutionException {
		ResourceId resourceId=endpoint.resourceId();
		Resource resource = this.engine().resourceRepository().find(resourceId,Resource.class);
		if(resource==null) {
			String errorMessage = applicationFailureMessage("Could not find resource for endpoint '%s'",endpoint);
			LOGGER.error(errorMessage);
			throw new ApplicationExecutionException(errorMessage);
		}
		try {
			return this.engine().resourceControllerService().getResource(resource);
		} catch (Exception e) {
			String errorMessage = applicationFailureMessage("Resource '%s' retrieval failed ",endpoint);
			throw createException(errorMessage,e);
		}
	}

	private ApplicationExecutionException createException(String errorMessage, Exception e) {
		LOGGER.error(errorMessage,e);
		if(e instanceof FeatureExecutionException) {
			return new ApplicationExecutionException(errorMessage,e.getCause());
		}
		throw new ApplicationContextException(errorMessage,e);
	}

	Resource resolveResource(Endpoint endpoint) {
		return this.engine().resourceRepository().find(endpoint.resourceId(), Resource.class);
	}

	Endpoint resolveResource(ResourceId id) {
		return this.engine().endpointRepository().endpointOfResource(id);
	}

	Resource createResource(Endpoint endpoint, DataSet dataSet, String desiredPath) throws ApplicationExecutionException {
		ResourceId resourceId=endpoint.resourceId();
		Container resource = this.engine().resourceRepository().find(resourceId,Container.class);
		if(resource==null) {
			String errorMessage = applicationFailureMessage("Could not find container for endpoint '%s'",endpoint);
			LOGGER.error(errorMessage);
			throw new ApplicationExecutionException(errorMessage);
		}
		try {
			return this.engine().resourceControllerService().createResource(resource,dataSet,desiredPath);
		} catch (Exception e) {
			String errorMessage = applicationFailureMessage("Resource create failed at '%s'",endpoint);
			throw createException(errorMessage,e);
		}
	}

	void deleteResource(Endpoint endpoint) throws ApplicationExecutionException {
		ResourceId resourceId=endpoint.resourceId();
		Resource resource = this.engine().resourceRepository().find(resourceId,Resource.class);
		if(resource==null) {
			String errorMessage = applicationFailureMessage("Could not find container for endpoint '%s'",endpoint);
			LOGGER.error(errorMessage);
			throw new ApplicationExecutionException(errorMessage);
		}
		try {
			this.engine().resourceControllerService().deleteResource(resource, WriteSessionConfiguration.builder().build());
		} catch (Exception e) {
			String errorMessage = applicationFailureMessage("Resource deletion failed at '%s'",endpoint);
			throw createException(errorMessage,e);
		}
	}

	void modifyResource(Endpoint endpoint, DataSet dataSet) throws ApplicationExecutionException {
		ResourceId resourceId=endpoint.resourceId();
		Resource resource = this.engine().resourceRepository().find(resourceId,Resource.class);
		if(resource==null) {
			String errorMessage = applicationFailureMessage("Could not find resource for endpoint '%s'",endpoint);
			LOGGER.error(errorMessage);
			throw new ApplicationExecutionException(errorMessage);
		}
		try {
			this.engine().resourceControllerService().updateResource(resource,dataSet, WriteSessionConfiguration.builder().build());
		} catch (Exception e) {
			String errorMessage = applicationFailureMessage("Resource modification failed at '%s'",endpoint);
			throw createException(errorMessage,e);
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
		return this.engine().templateManagementService().findTemplateById(resource.id().templateId());
	}

	public void initialize(String applicationClassName) throws ApplicationContextCreationException {
		try {
			this.engine().endpointManagementService().registerEndpointLifecycleListener(this.endpointLifecycleListener);
			this.application = this.engine().applicationLifecycleService().initialize(applicationClassName);
		} catch (ApplicationContextCreationException e) {
			String errorMessage = "Application '"+applicationClassName+"' initilization failed";
			LOGGER.error(errorMessage,e);
			throw e;
		}
	}

	public boolean shutdown() {
		this.engine().endpointManagementService().deregisterEndpointLifecycleListener(this.endpointLifecycleListener);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String applicationName() {
		return application().getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String applicationClassName() {
		return this.application.getClass().getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PublicResource findResource(final String path) {
		checkNotNull(path,"Endpoint path cannot be null");
		PublicResource resolved = resolveResource(path);
		if(resolved==null) {
			Endpoint endpoint=this.goneEndpoints.get(path);
			if(endpoint!=null) {
				resolved=new GonePublicResource(endpoint);
			}
		}
		return resolved;
	}

	@Override
	public ApplicationContextOperation createOperation() {
		return new DefaultApplicationOperation();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PublicResource resolveResource(final String path) {
		checkNotNull(path,"Endpoint path cannot be null");
		PublicResource resolved=null;
		Endpoint endpoint = this.engine().endpointManagementService().resolveEndpoint(path);
		if(endpoint!=null) {
			resolved = this.factory.createResource(endpoint);
		}
		return resolved;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PublicResource resolveResource(ManagedIndividualId id) {
		checkNotNull(id,"Individual identifier cannot be null");
		return this.factory.createResource(ResourceId.createId(id.name(), id.managerId()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerApplicationLifecycleListener(ApplicationLifecycleListener listener) {
		checkNotNull(listener,"Application lifecycle listener cannot be null");
		this.engine().applicationLifecycleService().registerApplicationLifecycleListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deregisterApplicationLifecycleListener(ApplicationLifecycleListener listener) {
		checkNotNull(listener,"Application lifecycle listener cannot be null");
		this.engine().applicationLifecycleService().deregisterApplicationLifecycleListener(listener);
	}

	public DefaultApplicationEngine engine() {
		return engine;
	}

}
