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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Date;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.ApplicationContextException;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.engine.ApplicationEngine;
import org.ldp4j.application.engine.ApplicationEngineException;
import org.ldp4j.application.engine.lifecycle.ApplicationEngineState;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.endpoint.EndpointManagementService;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.session.WriteSessionConfiguration;
import org.ldp4j.application.kernel.session.WriteSessionService;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.transaction.Transaction;
import org.ldp4j.application.kernel.transaction.TransactionManager;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SnapshotResolutionException;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.spi.ResourceSnapshotResolver;

@RunWith(JMockit.class)
public class CoreRuntimeDelegateTest {

	@Mocked DefaultApplicationEngine engine;
	@Mocked DefaultApplicationContext context;
	@Mocked WriteSessionService sessionService;
	@Mocked WriteSession session;
	@Mocked ResourceSnapshot snapshot;
	@Mocked EndpointManagementService endpointManagementService;
	@Mocked Endpoint endpoint;
	@Mocked Resource resource;
	@Mocked ResourceTemplate template;
	@Mocked TransactionManager transactionManager;
	@Mocked Transaction transaction;


	private void verifyOfflineState(final ApplicationEngineState state, boolean value) throws Exception {
		new Expectations() {{
			engine.unwrap(DefaultApplicationEngine.class);result=engine;
			engine.state();result=state;
		}};
		new MockUp<ApplicationEngine>() {
			@Mock
			public ApplicationEngine engine() throws ApplicationEngineException {
				return engine;
			}
		};
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		assertThat(sut.isOffline(),equalTo(value));
	}

	@Test
	public void testIsOffline$engineShutdown() throws Exception {
		verifyOfflineState(ApplicationEngineState.SHUTDOWN,true);
	}

	@Test
	public void testIsOffline$engineUnavailable() throws Exception {
		verifyOfflineState(ApplicationEngineState.UNAVAILABLE,true);
	}

	@Test
	public void testIsOffline$engineUndefined() throws Exception {
		verifyOfflineState(ApplicationEngineState.UNDEFINED,true);
	}

	@Test
	public void testIsOffline$engineAvailable() throws Exception {
		verifyOfflineState(ApplicationEngineState.AVAILABLE,true);
	}

	@Test
	public void testIsOffline$engineStarted() throws Exception {
		verifyOfflineState(ApplicationEngineState.STARTED,false);
	}

	@Test
	public void testIsOffline$failure() throws Exception {
		new MockUp<ApplicationEngine>() {
			@Mock
			public ApplicationEngine engine() throws ApplicationEngineException {
				throw new ApplicationEngineException("FAILURE");
			}
		};
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		assertThat(sut.isOffline(),equalTo(true));
	}

	@Test
	public void testCreateSession$happyPath() throws Exception {
		new Expectations() {{
			engine.unwrap(DefaultApplicationEngine.class);result=engine;
			engine.writeSessionService();result=sessionService;
			sessionService.createSession((WriteSessionConfiguration)any);result=session;
			engine.transactionManager();result=transactionManager;
			transactionManager.currentTransaction();result=transaction;
		}};
		new MockUp<ApplicationEngine>() {
			@Mock
			public ApplicationEngine engine() throws ApplicationEngineException {
				return engine;
			}
		};
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		assertThat(sut.createSession(),notNullValue());
	}

	@Test
	public void testCreateSession$failure() throws Exception {
		new MockUp<ApplicationEngine>() {
			@Mock
			public ApplicationEngine engine() throws ApplicationEngineException {
				throw new ApplicationEngineException("FAILURE");
			}
		};
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		try {
			sut.createSession();
			fail("Should not be able to create session if the application engine fails");
		} catch (ApplicationContextException e) {
			assertThat(e.getMessage(),equalTo("Could not create session"));
		}
	}

	@Test
	public void testResolverSnapshotResolver$resolveEndpoint$happyPath() throws Exception {
		final URI base = URI.create("http://www.examples.org/base/");
		final String path="resource/path/";
		final ResourceId resourceId = ResourceId.createId(NamingScheme.getDefault().name("name"),"template");
		new Expectations() {{
			engine.unwrap(DefaultApplicationEngine.class);result=engine;
			engine.endpointManagementService();result=endpointManagementService;
			endpointManagementService.resolveEndpoint(path);result=endpoint;
			endpoint.deleted();result=null;
			engine.unwrap(DefaultApplicationEngine.class);result=engine;
			engine.activeContext();result=context;
			context.resolveResource(endpoint);result=resource;
			resource.id();result=resourceId;
			engine.unwrap(DefaultApplicationEngine.class);result=engine;
			engine.activeContext();result=context;
			context.resourceTemplate(resource);result=template;
			template.handlerClass();result=ResourceHandler.class;
			session.find(ResourceSnapshot.class,resourceId.name(),ResourceHandler.class);result=snapshot;
		}};
		new MockUp<ApplicationEngine>() {
			@Mock
			public ApplicationEngine engine() throws ApplicationEngineException {
				return engine;
			}
		};
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		ResourceSnapshotResolver resolver = sut.createResourceResolver(base, session);
		assertThat(resolver.resolve(base.resolve(path)),sameInstance(snapshot));
	}

	@Test
	public void testResolverSnapshotResolver$resolveEndpoint$kernelResourceNotFound() throws Exception {
		final URI base = URI.create("http://www.examples.org/base/");
		final String path="resource/path/";
		new Expectations() {{
			engine.unwrap(DefaultApplicationEngine.class);result=engine;
			engine.endpointManagementService();result=endpointManagementService;
			endpointManagementService.resolveEndpoint(path);result=endpoint;
			endpoint.deleted();result=null;
			engine.unwrap(DefaultApplicationEngine.class);result=engine;
			engine.activeContext();result=context;
			context.resolveResource(endpoint);result=null;
		}};
		new MockUp<ApplicationEngine>() {
			@Mock
			public ApplicationEngine engine() throws ApplicationEngineException {
				return engine;
			}
		};
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		ResourceSnapshotResolver resolver = sut.createResourceResolver(base, session);
		assertThat(resolver.resolve(base.resolve(path)),nullValue());
	}

	@Test
	public void testResolverSnapshotResolver$resolveEndpoint$endpointGone() throws Exception {
		final URI base = URI.create("http://www.examples.org/base/");
		final String path="resource/path/";
		new Expectations() {{
			engine.unwrap(DefaultApplicationEngine.class);result=engine;
			engine.endpointManagementService();result=endpointManagementService;
			endpointManagementService.resolveEndpoint(path);result=endpoint;
			endpoint.deleted();result=new Date();
		}};
		new MockUp<ApplicationEngine>() {
			@Mock
			public ApplicationEngine engine() throws ApplicationEngineException {
				return engine;
			}
		};
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		ResourceSnapshotResolver resolver = sut.createResourceResolver(base, session);
		assertThat(resolver.resolve(base.resolve(path)),nullValue());
	}

	@Test
	public void testResolverSnapshotResolver$resolveEndpoint$endpointNotFound() throws Exception {
		final URI base = URI.create("http://www.examples.org/base/");
		final String path="resource/path/";
		new Expectations() {{
			engine.unwrap(DefaultApplicationEngine.class);result=engine;
			engine.endpointManagementService();result=endpointManagementService;
			endpointManagementService.resolveEndpoint(path);result=null;
		}};
		new MockUp<ApplicationEngine>() {
			@Mock
			public ApplicationEngine engine() throws ApplicationEngineException {
				return engine;
			}
		};
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		ResourceSnapshotResolver resolver = sut.createResourceResolver(base, session);
		assertThat(resolver.resolve(base.resolve(path)),nullValue());
	}

	@Test
	public void testResolverSnapshotResolver$resolveEndpoint$engineFailure() throws Exception {
		final URI base = URI.create("http://www.examples.org/base/");
		final String path="resource/path/";
		new MockUp<ApplicationEngine>() {
			@Mock
			public ApplicationEngine engine() throws ApplicationEngineException {
				throw new ApplicationEngineException("FAILURE");
			}
		};
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		ResourceSnapshotResolver resolver = sut.createResourceResolver(base, session);
		try {
			resolver.resolve(base.resolve(path));
			fail("Should not resolve endpoint if engine fails");
		} catch (SnapshotResolutionException e) {
			assertThat(e.getMessage(),equalTo("Could not resolve resource for endpoint 'http://www.examples.org/base/resource/path/'"));
		}
	}

	@Test
	public void testResolverSnapshotResolver$resolveEndpoint$opaque() throws Exception {
		final URI base = URI.create("http://www.examples.org/base/");
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		ResourceSnapshotResolver resolver = sut.createResourceResolver(base, session);
		assertThat(resolver.resolve(URI.create("urn:opaque")),nullValue());
	}

	@Test
	public void testResolverSnapshotResolver$resolveEndpoint$notResolvable() throws Exception {
		final URI base = URI.create("http://www.examples.org/base/");
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		ResourceSnapshotResolver resolver = sut.createResourceResolver(base, session);
		assertThat(resolver.resolve(URI.create("http://www.examples.org/another_base/resource")),nullValue());
	}

	@Test
	public void testResolverSnapshotResolver$resolveSnapshot$happyPath() throws Exception {
		final URI base = URI.create("http://www.examples.org/base/");
		final String path="resource/path/";
		final ResourceId resourceId = ResourceId.createId(NamingScheme.getDefault().name("name"),"template");
		new Expectations() {{
			snapshot.name();result=resourceId.name();
			snapshot.templateId();result=resourceId.templateId();
			engine.unwrap(DefaultApplicationEngine.class);result=engine;
			engine.activeContext();result=context;
			context.resolveResource(resourceId);result=endpoint;
			endpoint.deleted();result=null;
			endpoint.path();result=path;
		}};
		new MockUp<ApplicationEngine>() {
			@Mock
			public ApplicationEngine engine() throws ApplicationEngineException {
				return engine;
			}
		};
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		ResourceSnapshotResolver resolver = sut.createResourceResolver(base, session);
		assertThat(resolver.resolve(snapshot),equalTo(base.resolve(path)));
	}

	@Test
	public void testResolverSnapshotResolver$resolveSnapshot$resourceGone() throws Exception {
		final URI base = URI.create("http://www.examples.org/base/");
		final ResourceId resourceId = ResourceId.createId(NamingScheme.getDefault().name("name"),"template");
		new Expectations() {{
			snapshot.name();result=resourceId.name();
			snapshot.templateId();result=resourceId.templateId();
			engine.unwrap(DefaultApplicationEngine.class);result=engine;
			engine.activeContext();result=context;
			context.resolveResource(resourceId);result=endpoint;
			endpoint.deleted();result=new Date();
		}};
		new MockUp<ApplicationEngine>() {
			@Mock
			public ApplicationEngine engine() throws ApplicationEngineException {
				return engine;
			}
		};
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		ResourceSnapshotResolver resolver = sut.createResourceResolver(base, session);
		assertThat(resolver.resolve(snapshot),nullValue());
	}

	@Test
	public void testResolverSnapshotResolver$resolveSnapshot$resourceNotfound() throws Exception {
		final URI base = URI.create("http://www.examples.org/base/");
		final ResourceId resourceId = ResourceId.createId(NamingScheme.getDefault().name("name"),"template");
		new Expectations() {{
			snapshot.name();result=resourceId.name();
			snapshot.templateId();result=resourceId.templateId();
			engine.unwrap(DefaultApplicationEngine.class);result=engine;
			engine.activeContext();result=context;
			context.resolveResource(resourceId);result=null;
		}};
		new MockUp<ApplicationEngine>() {
			@Mock
			public ApplicationEngine engine() throws ApplicationEngineException {
				return engine;
			}
		};
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		ResourceSnapshotResolver resolver = sut.createResourceResolver(base, session);
		assertThat(resolver.resolve(snapshot),nullValue());
	}

	@Test
	public void testResolverSnapshotResolver$resolveSnapshot$engineFailure() throws Exception {
		final URI base = URI.create("http://www.examples.org/base/");
		final ResourceId resourceId = ResourceId.createId(NamingScheme.getDefault().name("name"),"template");
		new Expectations() {{
			snapshot.name();result=resourceId.name();
			snapshot.templateId();result=resourceId.templateId();
		}};
		new MockUp<ApplicationEngine>() {
			@Mock
			public ApplicationEngine engine() throws ApplicationEngineException {
				throw new ApplicationEngineException("FAILURE");
			}
		};
		CoreRuntimeDelegate sut=new CoreRuntimeDelegate();
		ResourceSnapshotResolver resolver = sut.createResourceResolver(base, session);
		try {
			assertThat(resolver.resolve(snapshot),nullValue());
			fail("Should not resolve snapshot if engine fails");
		} catch (SnapshotResolutionException e) {
			assertThat(e.getMessage(),equalTo("Could not resolve endpoint for resource '"+resourceId+"'"));
		}
	}

}
