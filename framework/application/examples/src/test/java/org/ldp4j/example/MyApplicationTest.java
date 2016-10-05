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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literals;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ApplicationInitializationException;
import org.ldp4j.application.ext.Configuration;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.ldp4j.application.setup.Bootstrap;
import org.ldp4j.application.setup.Environment;
import org.ldp4j.application.spi.ResourceSnapshotResolver;
import org.ldp4j.application.spi.RuntimeDelegate;
import org.ldp4j.example.MyApplication.LoggedUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

@RunWith(JMockit.class)
public class MyApplicationTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(MyApplicationTest.class);
	private static final URI ENDPOINT = DynamicResourceResolver.CANONICAL_BASE.resolve("path/");

	@Mocked WriteSession initSession;
	@Mocked WriteSession resolverSession;
	@Mocked ResourceSnapshot snapshot;
	@Mocked ResourceSnapshot personSnapshot;
	@Mocked ContainerSnapshot personContainerSnapshot;
	@Mocked ResourceSnapshotResolver resolver;
	@Mocked ApplicationContext context;
	@Mocked RuntimeDelegate delegate;
	@Mocked Environment environment;
	@Mocked Bootstrap<Configuration> bootstrap;

	@Test
	public void testLifecycle() throws Exception {
		final MyApplication application=new MyApplication();
		LOGGER.info("Setting up application...");
		application.setup(environment, bootstrap);
		new Expectations() {{
			// For the resource resolver
			resolverSession.find(ResourceSnapshot.class,application.dynamicResourceName(),DynamicResourceHandler.class);result=snapshot;
			snapshot.name();result=application.dynamicResourceName();
			snapshot.templateId();result=DynamicResourceHandler.ID;
			snapshot.handlerClass();result=DynamicResourceHandler.class;
			RuntimeDelegate.getInstance();result=delegate;
			delegate.createResourceResolver(DynamicResourceResolver.CANONICAL_BASE,resolverSession);result=resolver;
			resolver.resolve(snapshot);result=ENDPOINT;
			resolver.resolve(ENDPOINT);result=snapshot;

			// For the application initialization
			initSession.find(ResourceSnapshot.class,application.personResourceName(),PersonHandler.class);result=personSnapshot;
			personSnapshot.createAttachedResource(ContainerSnapshot.class,PersonHandler.RELATIVES_ID,application.relativeContainerResourceName(),RelativeContainerHandler.class);result=null;
			initSession.find(ResourceSnapshot.class,application.personContainerName(),PersonContainerHandler.class);result=personContainerSnapshot;
		}};

		LOGGER.info("Initializing application...");
		application.initialize(initSession);

		LOGGER.info("Awaiting updater and resolver termination...");
		Stopwatch timer=Stopwatch.createStarted();
		while(timer.elapsed(TimeUnit.SECONDS)<2);

		LOGGER.info("Shutting down application...");
		application.shutdown();

		DataSet dataSet = application.dynamicResourceHandler().get(snapshot);
		LOGGER.info("Updated resource:\n{}",dataSet);

		Individual<?,?> individual=dataSet.individualOfId(individualId(application.dynamicResourceName()));

		assertThat(
			individual.property(DynamicResourceUpdater.REFRESHED_ON).numberOfValues(),greaterThanOrEqualTo(1));
		assertThat(
			individual.property(DynamicResourceResolver.SNAPSHOT_ENDPOINT).hasLiteralValue(Literals.newLiteral(ENDPOINT)),equalTo(true));
		assertThat(
			individual.property(DynamicResourceResolver.SNAPSHOT_RESOLUTION).hasLiteralValue(Literals.of("OK")),equalTo(true));
	}

	@Test
	public void testLifecycle$initalizeFailure() throws Exception {
		final MyApplication application=new MyApplication();
		LOGGER.info("Setting up application...");
		application.setup(environment, bootstrap);
		new Expectations() {{
			// For the application initialization
			initSession.find(ResourceSnapshot.class,application.personResourceName(),PersonHandler.class);result=personSnapshot;
			personSnapshot.createAttachedResource(ContainerSnapshot.class,PersonHandler.RELATIVES_ID,application.relativeContainerResourceName(),RelativeContainerHandler.class);result=null;
			initSession.find(ResourceSnapshot.class,application.personContainerName(),PersonContainerHandler.class);result=personContainerSnapshot;
			initSession.saveChanges();result=new WriteSessionException("FAILURE");
		}};

		LOGGER.info("Initializing application...");
		try {
			application.initialize(initSession);
			fail("Should not initialize the application if the session fails");
		} catch (ApplicationInitializationException e) {
			assertThat(e.getCause(),instanceOf(WriteSessionException.class));
			assertThat(e.getCause().getMessage(),equalTo("FAILURE"));
		}
	}

	@Test
	public void testLifecycle$shutdownFailure(@Mocked final ScheduledExecutorService executor) throws Exception {
		final MyApplication application=new MyApplication();

		new MockUp<Executors>() {
			@Mock
			public ScheduledExecutorService newScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory) {
				return executor;
			}
		};

		LOGGER.info("Setting up application...");
		application.setup(environment, bootstrap);
		new Expectations() {{
			// For the application initialization
			initSession.find(ResourceSnapshot.class,application.personResourceName(),PersonHandler.class);result=personSnapshot;
			personSnapshot.createAttachedResource(ContainerSnapshot.class,PersonHandler.RELATIVES_ID,application.relativeContainerResourceName(),RelativeContainerHandler.class);result=null;
			initSession.find(ResourceSnapshot.class,application.personContainerName(),PersonContainerHandler.class);result=personContainerSnapshot;
			executor.isTerminated();result=false;
			executor.awaitTermination(anyLong,(TimeUnit)any);result=new InterruptedException("EXIT");
		}};

		LOGGER.info("Initializing application...");
		application.initialize(initSession);

		LOGGER.info("Shutting down application...");
		application.shutdown();
	}

	@Test
	public void testLogger(@Mocked final Logger logger) {
		LoggedUncaughtExceptionHandler sut = new MyApplication.LoggedUncaughtExceptionHandler(logger);
		final Thread t = Thread.currentThread();
		final IllegalStateException failure = new IllegalStateException();
		sut.uncaughtException(t,failure);
		new Verifications() {{
			logger.error(String.format("Thread %s died",t.getName()),failure);
		}};
	}

	private ManagedIndividualId individualId(Name<?> resourceName) {
		return
			ManagedIndividualId.
				createId(
					resourceName,
					DynamicResourceHandler.ID);
	}

}
