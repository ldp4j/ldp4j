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
package org.ldp4j.application.kernel.resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;
import static org.ldp4j.application.data.IndividualReferenceBuilder.newReference;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Callable;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ldp4j.application.data.DataDSL;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.IndividualReference;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.endpoint.EndpointRepository;
import org.ldp4j.application.kernel.impl.InMemoryRuntimeDelegate;
import org.ldp4j.application.kernel.service.ServiceRegistry;
import org.ldp4j.application.kernel.session.WriteSessionConfiguration;
import org.ldp4j.application.kernel.spi.ModelFactory;
import org.ldp4j.application.kernel.spi.RuntimeDelegate;
import org.ldp4j.application.kernel.template.TemplateManagementService;
import org.ldp4j.application.kernel.transaction.Transaction;
import org.ldp4j.application.sdk.QueryBuilder;
import org.ldp4j.example.BookContainerHandler;
import org.ldp4j.example.BookHandler;
import org.ldp4j.example.NameProvider;
import org.ldp4j.example.PersonHandler;
import org.ldp4j.example.QueryableResourceHandler;

import com.google.common.collect.Lists;

public class ResourceControllerServiceTest {

	private ResourceControllerService sut;
	private TemplateManagementService tms;
	private ModelFactory modelFactory;

	private <T extends Resource> T publishResource(Class<? extends T> clazz, String templateId, Name<?> resourceName, String path) {
		Transaction transaction = RuntimeDelegate.getInstance().getTransactionManager().currentTransaction();
		transaction.begin();

		Resource newResource=this.modelFactory.createResource(this.tms.templateOfId(templateId),resourceName);
		T resource=clazz.cast(newResource);
		RuntimeDelegate.getInstance().getResourceRepository().add(resource);
		Endpoint endpoint=this.modelFactory.createEndpoint(path,resource,new Date(),EntityTag.createStrong(path));
		RuntimeDelegate.getInstance().getEndpointRepository().add(endpoint);

		transaction.commit();
		return resource;
	}

	private DataSet getInitialData(IndividualReference<?, ?> reference, Date date) {
		DataSet initial=
			DataDSL.
				dataSet().
					individual(reference). // Initial context
						hasProperty(PersonHandler.READ_ONLY_PROPERTY.toString()).
							withValue(date).
						hasProperty("age").
							withValue(34).
						hasLink("hasFather").
							toIndividual(newReference().toLocalIndividual().named("Michel")). // New individual and context
								hasLink("hasWife").
									referringTo(newReference().toLocalIndividual().named("Consuelo")). // New individual, same context
						build(); // Create data set
		return initial;
	}

	private Name<String> name(String id) {
		return NamingScheme.getDefault().name(id);
	}

	private DataSet getUpdatedData(IndividualReference<?, ?> reference, Date date) {
		DataSet initial=
			DataDSL.
				dataSet().
					individual(reference). // Initial context
						hasProperty(PersonHandler.READ_ONLY_PROPERTY.toString()).
							withValue(date).
						hasProperty("age").
							withValue(34).
						hasLink("hasWife").
							referringTo(newReference().toLocalIndividual().named("Ana María")).
						hasLink("hasFather").
							toIndividual(newReference().toLocalIndividual().named("Michel")). // New individual and context
								hasLink("hasWife").
									referringTo(newReference().toLocalIndividual().named("Consuelo")). // New individual, same context
						build(); // Create data set
		return initial;
	}

	private <T extends ResourceHandler> T getHandler(Class<? extends T> handlerClass) {
		return tms.getHandler(handlerClass);
	}

	private void updateLastModifiedDate(Resource resource, Date lastModifiedDate) {
		Endpoint endpoint = endpoint(resource);
		if(endpoint!=null) {
			endpoint.modify(endpoint.entityTag(), lastModifiedDate);
		}
	}

	private Endpoint endpoint(Resource resource) {
		EndpointRepository er = RuntimeDelegate.getInstance().getEndpointRepository();
		Endpoint endpoint = er.endpointOfResource(resource.id());
		return endpoint;
	}

	private Date getLastModifiedDate(Resource resource) {
		Endpoint endpoint = endpoint(resource);
		Date result=null;
		if(endpoint!=null) {
			result=endpoint.lastModified();
		}
		return result;
	}

	@BeforeClass
	public static void setUpBefore() throws Exception {
		ServiceRegistry.setInstance(null);
		RuntimeDelegate.setInstance(new InMemoryRuntimeDelegate());
		PersonHandler personHandler = new PersonHandler();
		QueryableResourceHandler queryHandler=new QueryableResourceHandler();
		ServiceRegistry.
			getInstance().
				getService(TemplateManagementService.class).
					configure(
						Lists.<Class<?>>newArrayList(),
						Arrays.<ResourceHandler>asList(personHandler,queryHandler));
	}

	@AfterClass
	public static void tearDownAfter() throws Exception {
		RuntimeDelegate.setInstance(null);
	}

	@Before
	public void setUp() throws Exception {
		this.sut =
			ServiceRegistry.
				getInstance().
					getService(ResourceControllerService.class);
		this.tms =
			ServiceRegistry.
				getInstance().
					getService(TemplateManagementService.class);
		this.modelFactory=
			RuntimeDelegate.
				getInstance().
					getModelFactory();
	}

	public <T> T transactional(Callable<T> callable) throws Exception {
		Transaction transaction = RuntimeDelegate.getInstance().getTransactionManager().currentTransaction();
		transaction.begin();
		try {
			return callable.call();
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				assertThat(transaction.isActive(),equalTo(false));
			} catch (Exception e) {
				e.printStackTrace();
				transaction.rollback();
			}
		}
	}

	@Test
	public void testGetResource() throws Exception {
		String resourcePath = "get";
		Name<?> resourceName = name(resourcePath);
		final DataSet initial = getInitialData(newReference().toLocalIndividual().named("Miguel"), new Date());

		// BEGIN initialization
		final Resource resource = publishResource(Resource.class,PersonHandler.ID, resourceName, resourcePath);
		PersonHandler handler = getHandler(PersonHandler.class);
		handler.add(resourceName, initial);
		// END Initialization

		transactional(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					DataSet data = sut.getResource(resource,getSessionConfiguration(resource));
					assertThat(data,notNullValue());
					assertThat(data,sameInstance(initial));
					return null;
				}
			}
		);
	}

	@Test
	public void testQueryResource() throws Exception {
		String resourcePath = "querySuccess";
		final Name<?> resourceName = name(resourcePath);
		final DataSet initial = getInitialData(newReference().toLocalIndividual().named("Miguel"), new Date());

		// BEGIN initialization
		final Resource resource = publishResource(Resource.class,QueryableResourceHandler.ID, resourceName, resourcePath);
		QueryableResourceHandler handler = getHandler(QueryableResourceHandler.class);
		handler.add(resourceName, initial);
		// END Initialization

		transactional(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					Query query = QueryBuilder.newInstance().withParameter("param1", "value1").build();
					DataSet data = sut.queryResource(resource,query,getSessionConfiguration(resource));
					assertThat(data,notNullValue());
					assertThat(data,not(sameInstance(initial)));
					return null;
				}
			}
		);
	}

	@Test
	public void testCreateResource() throws Exception {
		String resourcePath = "post";
		final Name<String> resourceName = name(resourcePath);
		final DataSet initialData = getInitialData(newReference().toLocalIndividual().named("Miguel"), new Date());

		// BEGIN initialization
		final Container resource = publishResource(Container.class,BookContainerHandler.ID,resourceName,resourcePath);

		NameProvider nameProvider = NameProvider.create(resourceName);
		final Name<String> id = NamingScheme.getDefault().name("book1");
		nameProvider.addMemberName(id);

		final BookHandler resourceHandler = getHandler(BookHandler.class);
		BookContainerHandler containerHandler = getHandler(BookContainerHandler.class);
		containerHandler.add(resourceName, null);
		containerHandler.setBookHandler(resourceHandler);
		containerHandler.addNameProvider(resourceName, nameProvider);
		// END Initialization

		final Resource newResource =
			transactional(
				new Callable<Resource>() {
					@Override
					public Resource call() throws Exception {
						Resource result=sut.createResource(resource,initialData,getSessionConfiguration(resource));
						assertThat(result,notNullValue());
						assertThat((Object)result.id().name(),equalTo((Object)id));
						assertThat(result.id().templateId(),equalTo(BookHandler.ID));
						assertThat(resourceHandler.hasResource(resourceName),equalTo(false));
						return result;
					}
				}
			);

		transactional(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					DataSet data = sut.getResource(newResource,getSessionConfiguration(newResource));
					assertThat(data,sameInstance(initialData));
					return null;
				}
			}
		);
	}

	@Test
	public void testUpdateResource() throws Exception {
		String resourcePath = "update";
		Name<?> resourceName = name(resourcePath);
		Date date = new Date();
		final DataSet initial = getInitialData(newReference().toManagedIndividual(PersonHandler.ID).named(resourcePath), date);
		final DataSet updatedDate = getUpdatedData(newReference().toManagedIndividual(PersonHandler.ID).named(resourcePath), date);
		System.out.println(initial);

		// BEGIN initialization
		final Resource resource = publishResource(Resource.class,PersonHandler.ID, resourceName, resourcePath);
		PersonHandler handler = getHandler(PersonHandler.class);
		handler.add(resourceName, initial);
		// END Initialization

		transactional(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					DataSet data = sut.getResource(resource,getSessionConfiguration(resource));
					assertThat(data,sameInstance(initial));
					return null;
				}
			}
		);
		transactional(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						sut.updateResource(resource,updatedDate,getSessionConfiguration(resource));
					} catch (FeatureException e) {
						e.printStackTrace();
						fail("Should not fail update");
					}
					return null;
				}
			}
		);
		transactional(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					DataSet data = sut.getResource(resource,getSessionConfiguration(resource));
					assertThat(data,sameInstance(updatedDate));
					return null;
				}
			}
		);
	}

	@Test
	public void testDeleteResource() throws Exception {
		String resourcePath = "delete";
		final Name<?> resourceName = name(resourcePath);
		final DataSet initial = getInitialData(newReference().toLocalIndividual().named("Miguel"), new Date());

		// BEGIN initialization
		final Resource resource = publishResource(Resource.class,PersonHandler.ID, resourceName, resourcePath);
		final PersonHandler handler = getHandler(PersonHandler.class);
		handler.add(resourceName, initial);
		// END Initialization

		transactional(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					DataSet data = sut.getResource(resource,getSessionConfiguration(resource));
					assertThat(data,sameInstance(initial));
					return null;
				}
			}
		);
		transactional(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					sut.deleteResource(resource,getSessionConfiguration(resource));
					assertThat(handler.hasResource(resourceName),equalTo(false));
					return null;
				}
			}
		);
	}

	@Test
	public void testUpdateResourceTimeOffset() throws Exception {
		String resourcePath = "updateTimeOffset";
		Name<?> resourceName = name(resourcePath);
		Date date = new Date();
		final DataSet initial = getInitialData(newReference().toManagedIndividual(PersonHandler.ID).named(resourcePath), date);
		final DataSet updatedDate = getUpdatedData(newReference().toManagedIndividual(PersonHandler.ID).named(resourcePath), date);
		System.out.println(initial);

		// BEGIN initialization
		final Resource resource = publishResource(Resource.class,PersonHandler.ID,resourceName, resourcePath);
		PersonHandler handler = getHandler(PersonHandler.class);
		handler.add(resourceName, initial);
		// END Initialization

		transactional(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					DataSet data = sut.getResource(resource,getSessionConfiguration(resource));
					assertThat(data,sameInstance(initial));
					return null;
				}
			}
		);

		Date olmd = getLastModifiedDate(resource);
		Date nlmd = new Date(olmd.getTime()+24*60*60*1000);
		updateLastModifiedDate(resource, nlmd);

		transactional(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						sut.updateResource(resource,updatedDate,getSessionConfiguration(resource));
					} catch (FeatureException e) {
						e.printStackTrace();
						fail("Should not fail update");
					}
					return null;
				}
			}
		);

		assertThat(getLastModifiedDate(resource),equalTo(nlmd));

		transactional(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					DataSet data = sut.getResource(resource,getSessionConfiguration(resource));
					assertThat(data,sameInstance(updatedDate));
					return null;
				}
			}
		);
	}

	@Test
	public void testDeleteResourceTimeOffset() throws Exception {
		String resourcePath = "deleteTimeOffset";
		final Name<?> resourceName = name(resourcePath);
		final DataSet initial = getInitialData(newReference().toLocalIndividual().named("Miguel"), new Date());

		// BEGIN initialization
		final Resource resource = publishResource(Resource.class,PersonHandler.ID, resourceName, resourcePath);
		final PersonHandler handler = getHandler(PersonHandler.class);
		handler.add(resourceName, initial);
		// END Initialization

		transactional(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					DataSet data = sut.getResource(resource,getSessionConfiguration(resource));
					assertThat(data,sameInstance(initial));
					return null;
				}
			}
		);

		Date olmd = getLastModifiedDate(resource);
		Date nlmd = new Date(olmd.getTime()+24*60*60*1000);
		updateLastModifiedDate(resource, nlmd);
		Endpoint endpoint=endpoint(resource);

		transactional(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					sut.deleteResource(resource,getSessionConfiguration(resource));
					assertThat(handler.hasResource(resourceName),equalTo(false));
					return null;
				}
			}
		);

		assertThat(endpoint.deleted(),equalTo(nlmd));

	}

	private WriteSessionConfiguration getSessionConfiguration(final Resource resource) {
		return
			WriteSessionConfiguration.
				builder().
					withTarget(resource).
					build();
	}

}