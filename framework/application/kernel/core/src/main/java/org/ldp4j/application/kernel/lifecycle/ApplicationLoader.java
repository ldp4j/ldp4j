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
package org.ldp4j.application.kernel.lifecycle;

import org.ldp4j.application.engine.ApplicationConfigurationException;
import org.ldp4j.application.engine.ApplicationContextBootstrapException;
import org.ldp4j.application.ext.Application;
import org.ldp4j.application.ext.ApplicationInitializationException;
import org.ldp4j.application.ext.ApplicationSetupException;
import org.ldp4j.application.ext.Configuration;
import org.ldp4j.application.kernel.service.ServiceRegistry;
import org.ldp4j.application.kernel.session.WriteSessionConfiguration;
import org.ldp4j.application.kernel.session.WriteSessionService;
import org.ldp4j.application.kernel.spi.ModelFactory;
import org.ldp4j.application.kernel.spi.RuntimeDelegate;
import org.ldp4j.application.kernel.template.TemplateManagementService;
import org.ldp4j.application.kernel.transaction.Transaction;
import org.ldp4j.application.kernel.transaction.TransactionManager;
import org.ldp4j.application.session.SessionTerminationException;
import org.ldp4j.application.session.WriteSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final class ApplicationLoader<T extends Configuration> {

	private static final Logger LOGGER=LoggerFactory.getLogger(ApplicationLoader.class);

	private final Class<? extends Application<T>> appClass;
	private final TransactionManager transactionManager;

	private WriteSessionService writeSessionService;
	private TemplateManagementService templateManagementService;
	private ModelFactory modelFactory;

	private T configuration;

	private DefaultLifecycleEnvironment lifecycleEnvironment;

	private ApplicationLoader(Class<? extends Application<T>> appClass) {
		this.appClass=appClass;

		RuntimeDelegate instance = RuntimeDelegate.getInstance();

		this.transactionManager=instance.getTransactionManager();
		this.modelFactory=instance.getModelFactory();

		ServiceRegistry serviceRegistry=ServiceRegistry.getInstance();
		this.writeSessionService=serviceRegistry.getService(WriteSessionService.class);
		this.templateManagementService=serviceRegistry.getService(TemplateManagementService.class);
	}

	private ModelFactory resourceFactory() {
		return this.modelFactory;
	}

	private WriteSessionService writeSessionService() {
		return this.writeSessionService;
	}

	private TemplateManagementService templateManagementService() {
		return this.templateManagementService;
	}

	ApplicationLoader<T> withPersistencyManager(ModelFactory persistencyManager) {
		if(persistencyManager!=null) {
			this.modelFactory=persistencyManager;
		}
		return this;
	}

	ApplicationLoader<T> withWriteSessionService(WriteSessionService writeSessionService) {
		if(writeSessionService!=null) {
			this.writeSessionService = writeSessionService;
		}
		return this;
	}

	ApplicationLoader<T> withTemplateManagementService(TemplateManagementService templateManagementService) {
		if(templateManagementService!=null) {
			this.templateManagementService = templateManagementService;
		}
		return this;
	}

	Application<T> bootstrap() throws ApplicationContextBootstrapException {
		Application<T> application=instantiateApplication();
		this.configuration=instantiateConfiguration(application);
		setup(application);
		initialize(application);
		return application;
	}

	T configuration() {
		return this.configuration;
	}

	DefaultLifecycleEnvironment lifecycleEnvironment() {
		return this.lifecycleEnvironment;
	}

	private void initialize(Application<T> application) throws ApplicationConfigurationException {
		Transaction transaction=this.transactionManager.currentTransaction();
		transaction.begin();
		try {
			WriteSession session =
				writeSessionService().
					createSession(
						WriteSessionConfiguration.
							builder().
								build());
			Throwable failure=null;
			try {
				application.initialize(session);
			} catch (ApplicationInitializationException e) {
				failure=e;
			} finally {
				failure=closeQuietly(session, failure);
			}
			if(failure!=null) {
				throw new ApplicationConfigurationException(failure);
			}
		} finally {
			if(transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	private Throwable closeQuietly(WriteSession session, Throwable failure) {
		Throwable result=failure;
		try {
			session.close();
		} catch (SessionTerminationException e) {
			LOGGER.error("Could not terminate session",e);
			if(result==null) {
				result=e;
			}
		}
		return result;
	}

	private void setup(Application<T> application) throws ApplicationConfigurationException {
		BootstrapImpl<T> bootstrap=
			new BootstrapImpl<T>(
				this.configuration,
				templateManagementService());
		this.lifecycleEnvironment=new DefaultLifecycleEnvironment();
		EnvironmentImpl environment=
			new EnvironmentImpl(
				templateManagementService(),
				resourceFactory(),
				RuntimeDelegate.getInstance().getEndpointRepository(),
				RuntimeDelegate.getInstance().getResourceRepository(),
				lifecycleEnvironment
			);
		try {
			application.setup(environment,bootstrap);
			bootstrap.configureTemplates();
			environment.configureRootResources();
		} catch (ApplicationSetupException e) {
			throw new ApplicationConfigurationException(e);
		}
	}

	private Application<T> instantiateApplication() throws ApplicationContextBootstrapException {
		try {
			return this.appClass.newInstance();
		} catch (InstantiationException e) {
			throw new ApplicationContextBootstrapException(e);
		} catch (IllegalAccessException e) {
			throw new ApplicationContextBootstrapException(e);
		}
	}

	private T instantiateConfiguration(Application<T> app) throws ApplicationContextBootstrapException {
		try {
			Class<T> configurationClass = app.getConfigurationClass();
			return configurationClass.newInstance();
		} catch (InstantiationException e) {
			throw new ApplicationContextBootstrapException("Could not load configuration", e);
		} catch (IllegalAccessException e) {
			throw new ApplicationContextBootstrapException("Could not load configuration", e);
		}
	}

	static <T extends Configuration> ApplicationLoader<T> newInstance(Class<? extends Application<T>> appClass) throws ApplicationContextBootstrapException {
		return new ApplicationLoader<T>(appClass);
	}

}