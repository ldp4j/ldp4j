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

import static com.google.common.base.Preconditions.checkState;

import java.util.Date;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.engine.ApplicationContextCreationException;
import org.ldp4j.application.engine.context.ApplicationContext;
import org.ldp4j.application.engine.context.ApplicationContextException;
import org.ldp4j.application.engine.context.ApplicationContextOperation;
import org.ldp4j.application.engine.context.ApplicationExecutionException;
import org.ldp4j.application.engine.context.Capabilities;
import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.engine.context.InvalidIndirectIdentifierException;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.application.engine.lifecycle.ApplicationLifecycleListener;
import org.ldp4j.application.ext.Application;
import org.ldp4j.application.ext.ApplicationRuntimeException;
import org.ldp4j.application.ext.Configuration;
import org.ldp4j.application.ext.Deletable;
import org.ldp4j.application.ext.InvalidContentException;
import org.ldp4j.application.ext.Modifiable;
import org.ldp4j.application.ext.Namespaces;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.ext.Queryable;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.kernel.constraints.ConstraintReport;
import org.ldp4j.application.kernel.constraints.ConstraintReportId;
import org.ldp4j.application.kernel.constraints.ConstraintReportRepository;
import org.ldp4j.application.kernel.constraints.ConstraintReportTransformer;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.endpoint.EndpointRepository;
import org.ldp4j.application.kernel.resource.Container;
import org.ldp4j.application.kernel.resource.FeatureExecutionException;
import org.ldp4j.application.kernel.resource.FeaturePostconditionException;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.resource.ResourceRepository;
import org.ldp4j.application.kernel.session.WriteSessionConfiguration;
import org.ldp4j.application.kernel.spi.RuntimeDelegate;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.TemplateIntrospector;
import org.ldp4j.application.kernel.transaction.Transaction;
import org.ldp4j.application.kernel.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultApplicationContext implements ApplicationContext {

	private final class DefaultApplicationOperation implements ApplicationContextOperation {

		private final HttpRequest request;
		private final Transaction transaction;

		private DefaultApplicationOperation(HttpRequest request) {
			this.request = request;
			this.transaction=getContext().operationController.beginTransaction();
		}

		HttpRequest getRequest() {
			return this.request;
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
			try {
				getContext().operationController.endTransaction(this.transaction);
			} finally {
				getContext().currentOperation.remove();
			}
		}

	}

	private final class ApplicationContextOperationController {

		private final TransactionManager transactionManager;

		private ApplicationContextOperationController() {
			this.transactionManager = RuntimeDelegate.getInstance().getTransactionManager();
		}

		public Transaction beginTransaction() {
			Transaction transaction = this.transactionManager.currentTransaction();
			transaction.begin();
			LOGGER.
				info("Started transaction {}.{},",
					Thread.currentThread().getName(),
					transaction);
			return transaction;
		}

		public void endTransaction(Transaction transaction) {
			if(transaction.isActive()) {
				transaction.rollback();
			}
			LOGGER.
				info("Completed transaction {}.{},",
					Thread.currentThread().getName(),
					transaction);
		}

	}

	private static final String APPLICATION_LIFECYCLE_LISTENER_CANNOT_BE_NULL = "Application lifecycle listener cannot be null";
	private static final String APPLICATION_INITILIZATION_FAILED              = "Application '{}' initilization failed";
	private static final String COULD_NOT_FIND_CONTAINER_FOR_ENDPOINT         = "Could not find container for endpoint '%s'";
	private static final String COULD_NOT_FIND_RESOURCE_FOR_ENDPOINT          = "Could not find resource for endpoint '%s'";
	private static final String RESOURCE_RETRIEVAL_FAILED                     = "Resource '%s' retrieval failed ";
	private static final String RESOURCE_QUERY_FAILED                         = "Resource '%s' query failed ";
	private static final String RESOURCE_CREATION_FAILED                      = "Resource creation failed at '%s'";
	private static final String RESOURCE_DELETION_FAILED                      = "Resource deletion failed at '%s'";
	private static final String RESOURCE_MODIFICATION_FAILED                  = "Resource modification failed at '%s'";

	private static final Logger LOGGER=LoggerFactory.getLogger(DefaultApplicationContext.class);

	private Application<Configuration> application;

	private Configuration configuration;

	private final DefaultPublicResourceFactory factory;

	private final DefaultApplicationEngine engine;

	private final ApplicationContextOperationController operationController;

	private final ThreadLocal<DefaultApplicationOperation> currentOperation;

	private final ResourceRepository resourceRepository;
	private final EndpointRepository endpointRepository;
	private final ConstraintReportRepository constraintReportRepository;


	DefaultApplicationContext(DefaultApplicationEngine engine) {
		this.engine=engine;
		this.resourceRepository=RuntimeDelegate.getInstance().getResourceRepository();
		this.endpointRepository=RuntimeDelegate.getInstance().getEndpointRepository();
		this.constraintReportRepository=RuntimeDelegate.getInstance().getConstraintReportRepository();
		this.factory=DefaultPublicResourceFactory.newInstance(this);
		this.operationController=new ApplicationContextOperationController();
		this.currentOperation=new ThreadLocal<DefaultApplicationOperation>();
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

	private ApplicationExecutionException createException(String errorMessage, Exception e) {
		LOGGER.error(errorMessage,e);
		if(e instanceof FeatureExecutionException) {
			return new ApplicationExecutionException(errorMessage,e.getCause());
		} else if(e instanceof FeaturePostconditionException) {
			return new ApplicationExecutionException(errorMessage,new ApplicationRuntimeException(e.getMessage()));
		}
		throw new ApplicationContextException(errorMessage,e);
	}

	private PublicResource findResource(final String path) {
		return resolveResource(path);
	}

	private PublicResource resolveResource(final String path) {
		checkNotNull(path,"Endpoint path cannot be null");
		Endpoint endpoint=
			engine().
				endpointManagementService().
					resolveEndpoint(path);
		return this.factory.createResource(endpoint);
	}

	private PublicResource resolveResource(ManagedIndividualId id) {
		checkNotNull(id,"Individual identifier cannot be null");
		return this.factory.createResource(resolveResource(ResourceId.createId(id.name(), id.managerId())));
	}

	private void processConstraintValidationFailure(Resource resource, Throwable failure) {
		if(failure.getCause() instanceof InvalidContentException) {
			InvalidContentException cause=(InvalidContentException)failure.getCause();
			registerConstraintReport(resource, cause);
		}
	}

	// TODO: How do we do this transactionally
	private void registerConstraintReport(Resource resource, InvalidContentException error) {
		ConstraintReport report=
			resource.
				addConstraintReport(
					error.getConstraints(),
					new Date(),
					currentRequest());
		this.constraintReportRepository.add(report);
		LOGGER.debug("Constraint validation failed. Registered constraint report {}",report.id());
		error.setConstraintsId(report.id().failureId());
	}

	private HttpRequest currentRequest() {
		DefaultApplicationOperation result = this.currentOperation.get();
		checkState(result!=null,"No in-flight operation");
		return result.getRequest();
	}

	/**
	 * Enforce http://tools.ietf.org/html/rfc7232#section-2.2:
	 * if the clock in the request is ahead of the clock of the origin
	 * server (e.g., I request from Spain the update of a resource held in USA)
	 * the last-modified data should be changed to that of the request and not
	 * a generated date from the origin server
	 */
	private Date lastModified() {
		Date clientDate = currentRequest().clientDate();
		Date lastModified=new Date();
		if(clientDate!=null && clientDate.after(lastModified)) {
			lastModified=clientDate;
		}
		return lastModified;
	}

	private Resource loadResource(ResourceId resourceId) {
		return this.resourceRepository.resourceById(resourceId,Resource.class);
	}

	DataSet getResource(Endpoint endpoint) throws ApplicationExecutionException {
		ResourceId resourceId=endpoint.resourceId();
		Resource resource = loadResource(resourceId);
		if(resource==null) {
			String errorMessage = applicationFailureMessage(COULD_NOT_FIND_RESOURCE_FOR_ENDPOINT,endpoint);
			LOGGER.error(errorMessage);
			throw new ApplicationExecutionException(errorMessage);
		}
		try {
			WriteSessionConfiguration config=
				DefaultApplicationContextHelper.
					create(this.engine().templateManagementService()).
						createConfiguration(resource,lastModified());
			return this.engine().resourceControllerService().getResource(resource,config);
		} catch (Exception e) {
			String errorMessage = applicationFailureMessage(RESOURCE_RETRIEVAL_FAILED,endpoint);
			throw createException(errorMessage,e);
		}
	}

	DataSet query(Endpoint endpoint, Query query) throws ApplicationExecutionException {
		ResourceId resourceId=endpoint.resourceId();
		Resource resource = loadResource(resourceId);
		if(resource==null) {
			String errorMessage = applicationFailureMessage(COULD_NOT_FIND_RESOURCE_FOR_ENDPOINT,endpoint);
			LOGGER.error(errorMessage);
			throw new ApplicationExecutionException(errorMessage);
		}
		try {
			WriteSessionConfiguration config=
				DefaultApplicationContextHelper.
					create(this.engine().templateManagementService()).
						createConfiguration(resource,lastModified());
			return this.engine().resourceControllerService().queryResource(resource,query,config);
		} catch (Exception e) {
			String errorMessage = applicationFailureMessage(RESOURCE_QUERY_FAILED,endpoint);
			throw createException(errorMessage,e);
		}
	}

	void registerContentFailure(Endpoint endpoint, InvalidContentException error) {
		registerConstraintReport(loadResource(endpoint.resourceId()),error);
	}

	Resource resolveResource(Endpoint endpoint) {
		return loadResource(endpoint.resourceId());
	}

	Endpoint resolveResource(ResourceId id) {
		return this.endpointRepository.endpointOfResource(id);
	}

	Resource createResource(Endpoint endpoint, DataSet dataSet, String desiredPath) throws ApplicationExecutionException {
		ResourceId resourceId=endpoint.resourceId();
		Container resource = this.resourceRepository.containerOfId(resourceId);
		if(resource==null) {
			String errorMessage = applicationFailureMessage(COULD_NOT_FIND_CONTAINER_FOR_ENDPOINT,endpoint);
			LOGGER.error(errorMessage);
			throw new ApplicationExecutionException(errorMessage);
		}
		try {
			WriteSessionConfiguration config=
				DefaultApplicationContextHelper.
					create(this.engine().templateManagementService()).
						createConfiguration(resource,dataSet,desiredPath,lastModified());
			return this.engine().resourceControllerService().createResource(resource,dataSet,config);
		} catch (FeatureExecutionException e) {
			processConstraintValidationFailure(resource, e);
			String errorMessage = applicationFailureMessage(RESOURCE_CREATION_FAILED,endpoint);
			throw createException(errorMessage,e);
		} catch (InvalidIndirectIdentifierException e) {
			// Just rethrow
			throw e;
		} catch (Exception e) {
			String errorMessage = applicationFailureMessage(RESOURCE_CREATION_FAILED,endpoint);
			throw createException(errorMessage,e);
		}
	}

	void deleteResource(Endpoint endpoint) throws ApplicationExecutionException {
		ResourceId resourceId=endpoint.resourceId();
		Resource resource = loadResource(resourceId);
		if(resource==null) {
			String errorMessage = applicationFailureMessage(COULD_NOT_FIND_CONTAINER_FOR_ENDPOINT,endpoint);
			LOGGER.error(errorMessage);
			throw new ApplicationExecutionException(errorMessage);
		}
		try {
			WriteSessionConfiguration config=
				DefaultApplicationContextHelper.
					create(this.engine().templateManagementService()).
						createConfiguration(resource,lastModified());
			this.engine().resourceControllerService().deleteResource(resource,config);
		} catch (Exception e) {
			String errorMessage = applicationFailureMessage(RESOURCE_DELETION_FAILED,endpoint);
			throw createException(errorMessage,e);
		}
	}

	void modifyResource(Endpoint endpoint, DataSet dataSet) throws ApplicationExecutionException {
		ResourceId resourceId=endpoint.resourceId();
		Resource resource = loadResource(resourceId);
		if(resource==null) {
			String errorMessage = applicationFailureMessage(COULD_NOT_FIND_RESOURCE_FOR_ENDPOINT,endpoint);
			LOGGER.error(errorMessage);
			throw new ApplicationExecutionException(errorMessage);
		}
		try {
			WriteSessionConfiguration config=
				DefaultApplicationContextHelper.
					create(this.engine().templateManagementService()).
						createConfiguration(resource,lastModified());
			this.engine().resourceControllerService().updateResource(resource,dataSet,config);
		} catch (FeatureExecutionException e) {
			processConstraintValidationFailure(resource, e);
			String errorMessage = applicationFailureMessage(RESOURCE_MODIFICATION_FAILED,endpoint);
			throw createException(errorMessage,e);
		} catch (Exception e) {
			String errorMessage = applicationFailureMessage(RESOURCE_MODIFICATION_FAILED,endpoint);
			throw createException(errorMessage,e);
		}
	}

	DataSet getConstraintReport(Endpoint endpoint, String constraintsId) throws ApplicationExecutionException {
		ResourceId resourceId=endpoint.resourceId();
		// TODO: Check if it is really necessary
		Resource resource = loadResource(resourceId);
		if(resource==null) {
			String errorMessage = applicationFailureMessage(COULD_NOT_FIND_RESOURCE_FOR_ENDPOINT,endpoint);
			LOGGER.error(errorMessage);
			throw new ApplicationExecutionException(errorMessage);
		}

		ConstraintReport report=
			this.constraintReportRepository.
				constraintReportOfId(
					ConstraintReportId.
						create(resource.id(),constraintsId));
		if(report==null) {
			return null;
		}

		return
			ConstraintReportTransformer.
				create(resource, report).
					transform(endpoint);
	}


	Capabilities endpointCapabilities(Endpoint endpoint) {
		MutableCapabilities result=new MutableCapabilities();
		Resource resource = resolveResource(endpoint);
		ResourceTemplate template=resourceTemplate(resource);
		Class<? extends ResourceHandler> handlerClass = template.handlerClass();
		result.setQueryable(Queryable.class.isAssignableFrom(handlerClass));
		result.setModifiable(Modifiable.class.isAssignableFrom(handlerClass));
		result.setDeletable(Deletable.class.isAssignableFrom(handlerClass) && !resource.isRoot());
		// TODO: Analyze how to provide patch support
		result.setPatchable(false);
		TemplateIntrospector introspector = TemplateIntrospector.newInstance(template);
		result.setFactory(introspector.isContainer());
		return result;
	}

	ResourceTemplate resourceTemplate(Resource resource) {
		return this.engine().templateManagementService().templateOfId(resource.id().templateId());
	}

	void initialize(String applicationClassName) throws ApplicationContextCreationException {
		try {
			this.application=this.engine().applicationLifecycleService().initialize(applicationClassName);
			this.configuration=this.engine().applicationLifecycleService().configuration();
		} catch (ApplicationContextCreationException e) {
			LOGGER.error(APPLICATION_INITILIZATION_FAILED,applicationClassName,e);
			throw e;
		}
	}

	boolean shutdown() {
		return true;
	}

	DefaultApplicationEngine engine() {
		return this.engine;
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
	public Namespaces applicationNamespaces() {
		return this.configuration.namespaces();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ApplicationContextOperation createOperation(HttpRequest request) {
		checkNotNull(request,"Http request cannot be null");
		DefaultApplicationOperation operation=this.currentOperation.get();
		checkState(operation==null,"An operation is ongoing on the current thread");
		operation=new DefaultApplicationOperation(request);
		this.currentOperation.set(operation);
		return operation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerApplicationLifecycleListener(ApplicationLifecycleListener listener) {
		checkNotNull(listener,APPLICATION_LIFECYCLE_LISTENER_CANNOT_BE_NULL);
		this.engine().applicationLifecycleService().registerApplicationLifecycleListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deregisterApplicationLifecycleListener(ApplicationLifecycleListener listener) {
		checkNotNull(listener,APPLICATION_LIFECYCLE_LISTENER_CANNOT_BE_NULL);
		this.engine().applicationLifecycleService().deregisterApplicationLifecycleListener(listener);
	}

}
