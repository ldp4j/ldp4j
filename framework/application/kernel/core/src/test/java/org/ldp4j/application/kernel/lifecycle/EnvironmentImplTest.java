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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.engine.ApplicationConfigurationException;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.ext.ApplicationRuntimeException;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.ext.UnknownResourceException;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.endpoint.EndpointRepository;
import org.ldp4j.application.kernel.impl.InMemoryRuntimeDelegate;
import org.ldp4j.application.kernel.impl.TestingModelFactory;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.resource.ResourceRepository;
import org.ldp4j.application.kernel.service.ServiceRegistry;
import org.ldp4j.application.kernel.spi.ModelFactory;
import org.ldp4j.application.kernel.spi.RuntimeDelegate;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.TemplateManagementService;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.example.PersonHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class EnvironmentImplTest {

	private static class UnknownHandler implements ResourceHandler {

		@Override
		public DataSet get(ResourceSnapshot resource)
				throws UnknownResourceException, ApplicationRuntimeException {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private static final String VALID_PATH = "path/";

	private static final Logger LOGGER=LoggerFactory.getLogger(EnvironmentImplTest.class);

	private static final Name<String> NAME1 = NamingScheme.getDefault().name("name1");
	private static final Name<String> NAME2 = NamingScheme.getDefault().name("name2");

	private TemplateManagementService templateManagementService;
	private EnvironmentImpl sut;

	private ResourceRepository resourceRepository;

	private EndpointRepository endpointRepository;

	private Resource resource;

	private ModelFactory modelFactory;

	private Endpoint endpoint;

	@Rule
	public TestName name=new TestName();

	@Before
	public void setUp() throws Exception {
		ServiceRegistry.setInstance(null);
		RuntimeDelegate.setInstance(new InMemoryRuntimeDelegate());
		PersonHandler personHandler = new PersonHandler();
		ServiceRegistry.
			getInstance().
				getService(TemplateManagementService.class).
					configure(
						Lists.<Class<?>>newArrayList(),
						Arrays.<ResourceHandler>asList(personHandler));
		this.templateManagementService =
			ServiceRegistry.
				getInstance().
					getService(TemplateManagementService.class);
		this.resourceRepository=
			RuntimeDelegate.
				getInstance().
					getResourceRepository();
		this.endpointRepository=
			RuntimeDelegate.
				getInstance().
					getEndpointRepository();
		this.modelFactory = RuntimeDelegate.getInstance().getModelFactory();
		Class<? extends ResourceHandler> handlerClass = PersonHandler.class;
		this.resource=createResource(handlerClass, NAME1);
		this.endpoint=createEndpoint(VALID_PATH,this.resource);
		this.sut=
			new EnvironmentImpl(
				this.templateManagementService,
				modelFactory,
				this.endpointRepository,
				this.resourceRepository, new DefaultLifecycleEnvironment());
	}

	@After
	public void tearDown() throws Exception {
		RuntimeDelegate.setInstance(null);
	}

	private Endpoint createEndpoint(String path, Resource resource) {
		return
			this.modelFactory.
				createEndpoint(path, resource, new Date(), EntityTag.createStrong("etag"));
	}

	private Resource createResource(Class<? extends ResourceHandler> handlerClass, Name<?> name) {
		return
			this.modelFactory.
				createResource(
					this.templateManagementService.templateOfHandler(handlerClass),
					name);
	}

	private void logFailure(ApplicationConfigurationException e) {
		LOGGER.debug("{}: {}",name.getMethodName(),e.getMessage());
	}

	@Test
	public void testPublishResource$nullName() {
		this.sut.publishResource(null,PersonHandler.class,VALID_PATH);
		try {
			this.sut.configureRootResources();
			fail("Should not allow publishing root resources with null ID");
		} catch (ApplicationConfigurationException e) {
			logFailure(e);
		}
	}

	@Test
	public void testPublishResource$nullHandler() {
		this.sut.publishResource(NAME1,null,VALID_PATH);
		try {
			this.sut.configureRootResources();
			fail("Should not allow publishing root resources with null handler");
		} catch (ApplicationConfigurationException e) {
			logFailure(e);
		}
	}

	@Test
	public void testPublishResource$nullPath() {
		this.sut.publishResource(NAME1,PersonHandler.class,null);
		try {
			this.sut.configureRootResources();
			fail("Should not allow publishing root resources with null path");
		} catch (ApplicationConfigurationException e) {
			logFailure(e);
		}
	}

	@Test
	public void testPublishResource$badPath$noTrailingSlash() {
		this.sut.publishResource(NAME1,PersonHandler.class,"path");
		try {
			this.sut.configureRootResources();
			fail("Should not allow publishing root resources with bad path (no trailing slash)");
		} catch (ApplicationConfigurationException e) {
			logFailure(e);
		}
	}

	@Test
	public void testPublishResource$badPath$doubleTrailingSlash() {
		this.sut.publishResource(NAME1,PersonHandler.class,"path//");
		try {
			this.sut.configureRootResources();
			fail("Should not allow publishing root resources with bad path (double trailing slash)");
		} catch (ApplicationConfigurationException e) {
			logFailure(e);
		}
	}

	@Test
	public void testPublishResource$badPath$singleSlash() {
		this.sut.publishResource(NAME1,PersonHandler.class,"/");
		try {
			this.sut.configureRootResources();
			fail("Should not allow publishing root resources with bad path (single slash)");
		} catch (ApplicationConfigurationException e) {
			logFailure(e);
		}
	}

	@Test
	public void testPublishResource$badPath$absolutePath() {
		this.sut.publishResource(NAME1,PersonHandler.class,"/path/");
		try {
			this.sut.configureRootResources();
			fail("Should not allow publishing root resources with absolute path");
		} catch (ApplicationConfigurationException e) {
			logFailure(e);
		}
	}

	@Test
	public void testPublishResource$unknownHandler() {
		this.sut.publishResource(NAME1,UnknownHandler.class,VALID_PATH);
		try {
			this.sut.configureRootResources();
			fail("Should not allow publishing root resources with unknown handler");
		} catch (ApplicationConfigurationException e) {
			logFailure(e);
		}
	}

	@Test
	public void testPublishResource$regular() throws ApplicationConfigurationException {
		this.sut.publishResource(NAME1,PersonHandler.class,"path1/");
		this.sut.publishResource(NAME2,PersonHandler.class,"path2/");
		this.sut.configureRootResources();
		verifyPublicationSideAffect(NAME1, PersonHandler.class, "path1/");
		verifyPublicationSideAffect(NAME2, PersonHandler.class, "path2/");
	}

	@Test
	public void testPublishResource$preexisting$noClash() throws ApplicationConfigurationException {
		this.endpointRepository.add(this.endpoint);
		this.resourceRepository.add(this.resource);
		this.sut.publishResource(NAME1,PersonHandler.class,VALID_PATH);
		this.sut.configureRootResources();
		Resource resource1 = verifyResource(NAME1, PersonHandler.class);
		assertThat(resource1,sameInstance(this.resource));
		Endpoint endpoint1 = verifyEndpoint(VALID_PATH, resource1);
		assertThat(endpoint1,sameInstance(this.endpoint));
	}

	@Test
	public void testPublishResource$preexisting$pathClash() throws ApplicationConfigurationException {
		this.endpointRepository.add(this.endpoint);
		this.resourceRepository.add(this.resource);
		this.sut.publishResource(NAME2,PersonHandler.class,VALID_PATH);
		try {
			this.sut.configureRootResources();
			fail("Should not allow publishing root resources with preexisting path clash");
		} catch (ApplicationConfigurationException e) {
			logFailure(e);
		}
	}

	@Test
	public void testPublishResource$preexisting$corrupted() throws ApplicationConfigurationException {
		Resource createResource = TestingModelFactory.createResource(NAME1, "UnknownTemplate");
		this.endpointRepository.add(createEndpoint(VALID_PATH, createResource));
		this.resourceRepository.add(createResource);
		this.sut.publishResource(NAME2,PersonHandler.class,VALID_PATH);
		try {
			this.sut.configureRootResources();
			fail("Should not allow publishing root resources with preexisting path clash");
		} catch (ApplicationConfigurationException e) {
			logFailure(e);
		}
	}

	@Test
	public void testPublishResource$preexisting$idClash() throws ApplicationConfigurationException {
		this.endpointRepository.add(this.endpoint);
		this.resourceRepository.add(this.resource);
		this.sut.publishResource(NAME1,PersonHandler.class,"anotherPath/");
		try {
			this.sut.configureRootResources();
			fail("Should not allow publishing root resources with preexisting id clash");
		} catch (ApplicationConfigurationException e) {
			logFailure(e);
		}
	}


	private void verifyPublicationSideAffect(Name<String> name, Class<? extends ResourceHandler> clazz, String path) {
		Resource resource = verifyResource(name, clazz);
		verifyEndpoint(path, resource);
	}

	private Endpoint verifyEndpoint(String path, Resource resource) {
		Endpoint endpoint = findEndpoint(resource);
		assertThat(endpoint,notNullValue());
		assertThat(endpoint.path(),equalTo(path));
		assertThat(endpoint.deleted(),nullValue());
		return endpoint;
	}

	private Resource verifyResource(Name<String> name,
			Class<? extends ResourceHandler> clazz) {
		Resource resource = findResource(name, clazz);
		assertThat(resource,notNullValue());
		assertThat((Object)resource.id().name(),equalTo((Object)name));
		return resource;
	}

	private Resource findResource(Name<?> name, Class<? extends ResourceHandler> handlerClass) {
		ResourceTemplate template = this.templateManagementService.templateOfHandler(handlerClass);
		ResourceId id = ResourceId.createId(name,template);
		return this.resourceRepository.resourceOfId(id);
	}

	private Endpoint findEndpoint(Resource resource) {
		return this.endpointRepository.endpointOfResource(resource.id());
	}

	@Test
	public void testPublishResource$repeatedName() {
		this.sut.publishResource(NAME1,PersonHandler.class,"path1/");
		this.sut.publishResource(NAME1,PersonHandler.class,"path2/");
		try {
			this.sut.configureRootResources();
			fail("Should not allow publishing two root resources with same ID");
		} catch (ApplicationConfigurationException e) {
			assertThat(e.getMessage(),containsString(NAME1.id()));
			logFailure(e);
		}
	}

	@Test
	public void testPublishResource$repeatedPath() {
		this.sut.publishResource(NAME1,PersonHandler.class,VALID_PATH);
		this.sut.publishResource(NAME2,PersonHandler.class,VALID_PATH);
		try {
			this.sut.configureRootResources();
			fail("Should not allow publishing two root resources with same path");
		} catch (ApplicationConfigurationException e) {
			assertThat(e.getMessage(),containsString("path"));
			logFailure(e);
		}
	}
}
