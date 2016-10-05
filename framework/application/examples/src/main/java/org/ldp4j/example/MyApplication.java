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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-examples:0.2.2
 *   Bundle      : ldp4j-application-examples-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.example;

import static org.ldp4j.application.data.IndividualReferenceBuilder.newReference;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ldp4j.application.data.DataDSL;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.Application;
import org.ldp4j.application.ext.ApplicationInitializationException;
import org.ldp4j.application.ext.ApplicationSetupException;
import org.ldp4j.application.ext.Configuration;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.ldp4j.application.setup.Bootstrap;
import org.ldp4j.application.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * An example LDP4j application.
 */
public class MyApplication extends Application<Configuration> {

	static final class LoggedUncaughtExceptionHandler implements UncaughtExceptionHandler {

		private final Logger logger; // NOSONAR

		LoggedUncaughtExceptionHandler(Logger logger) {
			this.logger = logger;
		}

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			this.logger.error(String.format("Thread %s died",t.getName()),e);
		}
	}

	private static final String PERSON_CONTAINER_NAME    = "PersonContainer";
	private static final String PERSON_RESOURCE_NAME     = "PersonResource";
	private static final String RELATIVE_CONTAINER_NAME  = "RelativeContainer";
	private static final String QUERYABLE_RESOURCE_NAME  = "QueryableResource";
	private static final String DYNAMIC_RESOURCE_NAME    = "DynamicResource";
	private static final String BAD_DATA_RESOURCE_NAME   = "BadDataResource";

	/**
	 * The path for a root person resource
	 */
	public static final String ROOT_PERSON_RESOURCE_PATH    = "rootPersonResource/";

	/**
	 * The path for a root person container resource
	 */
	public static final String ROOT_PERSON_CONTAINER_PATH   = "rootPersonContainer/";

	/**
	 * The path for a root queryable resource
	 */
	public static final String ROOT_QUERYABLE_RESOURCE_PATH = "rootQueryableResource/";

	/**
	 * The path for a root dynamic  resource
	 */
	public static final String ROOT_DYNAMIC_RESOURCE_PATH   = "rootDynamicResource/";

	/**
	 * The path for a root bad data resource
	 */
	public static final String ROOT_BAD_DATA_RESOURCE_PATH  = "rootBadDataResource/";

	private static final Logger LOGGER=LoggerFactory.getLogger(MyApplication.class);

	private final Name<String> personResourceName;
	private final Name<String> personContainerName;
	private final Name<String> relativeContainerResourceName;
	private final Name<String> queryableResourceName;
	private final Name<String> dynamicResourceName;
	private final Name<String> badDataResourceName;

	private DynamicResourceHandler dynamicResourceHandler;
	private ScheduledExecutorService executorService;

	/**
	 * Create a new instance
	 */
	public MyApplication() {
		this.personResourceName = NamingScheme.getDefault().name(PERSON_RESOURCE_NAME);
		this.personContainerName = NamingScheme.getDefault().name(PERSON_CONTAINER_NAME);
		this.relativeContainerResourceName = NamingScheme.getDefault().name(RELATIVE_CONTAINER_NAME);
		this.queryableResourceName = NamingScheme.getDefault().name(QUERYABLE_RESOURCE_NAME);
		this.dynamicResourceName = NamingScheme.getDefault().name(DYNAMIC_RESOURCE_NAME);
		this.badDataResourceName = NamingScheme.getDefault().name(BAD_DATA_RESOURCE_NAME);
	}

	private DataSet getInitialData(String templateId, String name) {
		return
			DataDSL.
				dataSet().
					individual(newReference().toManagedIndividual(templateId).named(name)).
						hasProperty("http://www.ldp4j.org/vocabulary/example#creationDate").
							withValue(new Date()).
						hasProperty("http://www.ldp4j.org/vocabulary/example#age").
							withValue(34).
						hasLink("http://www.ldp4j.org/vocabulary/example#hasFather").
							toIndividual(newReference().toLocalIndividual().named("Michel")).
								hasLink("http://www.ldp4j.org/vocabulary/example#hasWife").
									referringTo(newReference().toLocalIndividual().named("Consuelo")).
						build();
	}

	/** Exposed for testing */
	protected final Name<String> personResourceName() {
		return this.personResourceName;
	}

	/** Exposed for testing */
	protected final Name<String> personContainerName() {
		return this.personContainerName;
	}

	/** Exposed for testing */
	protected final Name<String> relativeContainerResourceName() {
		return this.relativeContainerResourceName;
	}

	/** Exposed for testing */
	protected final Name<String> dynamicResourceName() {
		return this.dynamicResourceName;
	}

	/** Exposed for testing */
	protected final Name<String> badDataResourceName() {
		return this.badDataResourceName;
	}

	/** Exposed for testing */
	protected final DynamicResourceHandler dynamicResourceHandler() {
		return this.dynamicResourceHandler;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setup(Environment environment, Bootstrap<Configuration> bootstrap) throws ApplicationSetupException {
		LOGGER.info("Configuring application: {}, {}",environment,bootstrap);
		PersonHandler resourceHandler = new PersonHandler();
		PersonContainerHandler containerHandler=new PersonContainerHandler();
		RelativeContainerHandler relativesHandler=new RelativeContainerHandler();
		QueryableResourceHandler queryableHandler=new QueryableResourceHandler();
		this.dynamicResourceHandler = new DynamicResourceHandler();
		BadDataResourceHandler badDataHandler=new BadDataResourceHandler();

		containerHandler.setHandler(resourceHandler);
		relativesHandler.setHandler(resourceHandler);

		resourceHandler.add(this.personResourceName, getInitialData(PersonHandler.ID,PERSON_RESOURCE_NAME));
		containerHandler.add(this.personContainerName, getInitialData(PersonContainerHandler.ID,PERSON_CONTAINER_NAME));
		relativesHandler.add(this.relativeContainerResourceName, getInitialData(RelativeContainerHandler.ID,RELATIVE_CONTAINER_NAME));
		queryableHandler.add(this.queryableResourceName, getInitialData(QueryableResourceHandler.ID,QUERYABLE_RESOURCE_NAME));
		this.dynamicResourceHandler.add(this.dynamicResourceName, getInitialData(DynamicResourceHandler.ID,DYNAMIC_RESOURCE_NAME));
		badDataHandler.add(this.badDataResourceName, badDataHandler.getRepresentation());

		bootstrap.addHandler(resourceHandler);
		bootstrap.addHandler(containerHandler);
		bootstrap.addHandler(relativesHandler);
		bootstrap.addHandler(queryableHandler);
		bootstrap.addHandler(this.dynamicResourceHandler);
		bootstrap.addHandler(badDataHandler);

		environment.publishResource(this.personResourceName, PersonHandler.class, ROOT_PERSON_RESOURCE_PATH);
		environment.publishResource(this.personContainerName, PersonContainerHandler.class, ROOT_PERSON_CONTAINER_PATH);
		environment.publishResource(this.queryableResourceName, QueryableResourceHandler.class, ROOT_QUERYABLE_RESOURCE_PATH);
		environment.publishResource(this.dynamicResourceName, DynamicResourceHandler.class, ROOT_DYNAMIC_RESOURCE_PATH);
		environment.publishResource(this.badDataResourceName, BadDataResourceHandler.class, ROOT_BAD_DATA_RESOURCE_PATH);

		this.executorService =
			Executors.
				newScheduledThreadPool(
					1,
					new ThreadFactoryBuilder().
						setNameFormat("daemon-updater-thread-%d").
						setUncaughtExceptionHandler(new LoggedUncaughtExceptionHandler(LOGGER)).
						build());
		LOGGER.info("Configuration completed.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(WriteSession session) throws ApplicationInitializationException {
		LOGGER.info("Initializing application: {}",session);
		ResourceSnapshot person = session.find(ResourceSnapshot.class,this.personResourceName,PersonHandler.class);
		LOGGER.info("Root resource.......: "+person);
		LOGGER.info("Root basic container: "+session.find(ResourceSnapshot.class,this.personContainerName,PersonContainerHandler.class));

		ContainerSnapshot relativesContainer = person.createAttachedResource(ContainerSnapshot.class, PersonHandler.RELATIVES_ID, this.relativeContainerResourceName, RelativeContainerHandler.class);
		LOGGER.info("Attached resource...: "+relativesContainer);
		try {
			session.saveChanges();
		} catch (WriteSessionException e) {
			throw new ApplicationInitializationException("Could not initialize application",e);
		}
		this.executorService.
		scheduleAtFixedRate(
			new DynamicResourceUpdater(this.dynamicResourceHandler,this.dynamicResourceName),
			1,
			15,TimeUnit.SECONDS);
		this.executorService.
			schedule(
				new DynamicResourceResolver(this.dynamicResourceHandler,this.dynamicResourceName),
				3,TimeUnit.SECONDS);
		LOGGER.info("Initialization completed.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown() {
		LOGGER.info("Shutting down application...");
		this.executorService.shutdown();
		boolean finished=this.executorService.isTerminated();
		while(!finished) {
			try {
				this.executorService.awaitTermination(100, TimeUnit.MILLISECONDS);
				finished=this.executorService.isTerminated();
			} catch (InterruptedException e) {
				finished=true;
			}
		}
		LOGGER.info("Shutdown completed.");
	}

}
