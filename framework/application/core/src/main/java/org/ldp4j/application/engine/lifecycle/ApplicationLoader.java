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
package org.ldp4j.application.engine.lifecycle;

import org.ldp4j.application.engine.ApplicationConfigurationException;
import org.ldp4j.application.engine.ApplicationContextBootstrapException;
import org.ldp4j.application.engine.service.ServiceRegistry;
import org.ldp4j.application.engine.session.WriteSessionConfiguration;
import org.ldp4j.application.engine.session.WriteSessionService;
import org.ldp4j.application.engine.spi.ModelFactory;
import org.ldp4j.application.engine.spi.RuntimeDelegate;
import org.ldp4j.application.engine.template.TemplateManagementService;
import org.ldp4j.application.engine.transaction.Transaction;
import org.ldp4j.application.engine.transaction.TransactionManager;
import org.ldp4j.application.ext.Application;
import org.ldp4j.application.ext.ApplicationInitializationException;
import org.ldp4j.application.ext.ApplicationSetupException;
import org.ldp4j.application.ext.Configuration;
import org.ldp4j.application.session.WriteSession;


final class ApplicationLoader<T extends Configuration> {

	private final Class<? extends Application<T>> appClass;
	private final TransactionManager transactionManager;

	private WriteSessionService writeSessionService;
	private TemplateManagementService templateManagementService;
	private ModelFactory modelFactory;

	private T configuration;

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
		this.configuration = instantiateConfiguration(application);
		setup(application);
		initialize(application);
		return application;
	}

	T configuration() {
		return this.configuration;
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
			try {
				application.initialize(session);
			} catch (ApplicationInitializationException e) {
				throw new ApplicationConfigurationException(e);
			} finally {
				writeSessionService().terminateSession(session);
			}
		} finally {
			if(transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	private void setup(Application<T> application) throws ApplicationConfigurationException {
		BootstrapImpl<T> bootstrap=
			new BootstrapImpl<T>(
				this.configuration,
				templateManagementService());
		EnvironmentImpl environment=
			new EnvironmentImpl(
				templateManagementService(),
				resourceFactory(),
				RuntimeDelegate.getInstance().getEndpointRepository(),
				RuntimeDelegate.getInstance().getResourceRepository()
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