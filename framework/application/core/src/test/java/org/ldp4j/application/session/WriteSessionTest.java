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
package org.ldp4j.application.session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.ext.ContainerHandler;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.impl.InMemoryRuntimeInstance;
import org.ldp4j.application.session.UnitOfWork.Visitor;
import org.ldp4j.application.spi.PersistencyManager;
import org.ldp4j.application.spi.RuntimeInstance;
import org.ldp4j.application.template.TemplateManagementService;
import org.ldp4j.example.AddressHandler;
import org.ldp4j.example.BookContainerHandler;
import org.ldp4j.example.BookHandler;
import org.ldp4j.example.PersonHandler;
import org.ldp4j.example.RelativeContainerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class WriteSessionTest {

	private static Logger LOGGER=LoggerFactory.getLogger(WriteSessionTest.class);

	private final class UnitOfWorkInspector implements Visitor {

		@Override
		public void visitNew(DelegatedResourceSnapshot obj) {
			LOGGER.debug("Pending creation: "+obj);
		}

		@Override
		public void visitDirty(DelegatedResourceSnapshot obj) {
			LOGGER.debug("Pending update: "+obj);
		}

		@Override
		public void visitDeleted(DelegatedResourceSnapshot obj) {
			LOGGER.debug("Pending deletion: "+obj);

		}
	}

	private enum Stage {
		PREPARATION("Preparing sut"),
		TERMINATION("Terminating sut"),
		HANDLING("Handling action"),
		;
		private final String prefix;

		Stage(String prefix) {
			this.prefix = prefix;
		}

	}

	private enum Action {
		POST,
		PUT,
		DELETE, CHECK,
		;
	}

	private WriteSessionService writeSessionService;

	private UnitOfWork uow;
	private WriteSession sut;

	@Before
	public void setUp() throws Exception {
		RuntimeInstance.setInstance(new InMemoryRuntimeInstance());
		RuntimeInstance.
			getInstance().
				getServiceRegistry().
					getService(TemplateManagementService.class).
						configure(
							Lists.<Class<?>>newArrayList(),
							Arrays.<ResourceHandler>asList(new PersonHandler()));
		writeSessionService=
			RuntimeInstance.
				getInstance().
					getServiceRegistry().
					getService(WriteSessionService.class);
	}

	@After
	public void tearDown() throws Exception {
		RuntimeInstance.setInstance(null);
	}

	@Test
	public void testSession() throws WriteSessionException {
		// BEGIN initialization
		uow = UnitOfWork.newCurrent();
		PersistencyManager persistencyManager = RuntimeInstance.getInstance().getPersistencyManager();

		org.ldp4j.application.resource.Resource rootResource=persistencyManager.createResource("personTemplate",name("me"),null);
		Endpoint rootEndpoint=persistencyManager.createEndpoint(rootResource,"root",new EntityTag("root"),new Date());
		persistencyManager.add(rootResource);
		persistencyManager.add(rootEndpoint);
		UnitOfWork.setCurrent(null);
		// END Initialization

		// BEGIN First interaction
		prepareSession(Action.PUT, rootResource);
		handleAction(Action.PUT,rootResource);

		ResourceSnapshot me = sut.find(ResourceSnapshot.class,rootResource.id().name(),PersonHandler.class);
		ResourceSnapshot myRelatives = attachResource(me,"personRelatives","myRelatives",RelativeContainerHandler.class);
		ResourceSnapshot address = attachResource(me,"address","myAddress",AddressHandler.class);
		ContainerSnapshot books = attachContainer(me,"books","myBooks",BookContainerHandler.class);
		ResourceSnapshot firstBook = addMember(books,"book1");
		ResourceSnapshot secondBook = addMember(books,"book2");

		terminateSession(Action.PUT, me);
		// END First interaction

		// BEGIN Second interaction
		prepareSession(Action.POST, myRelatives);

		assertAvailable(me, ResourceSnapshot.class, PersonHandler.class);
		assertAvailable(myRelatives, ResourceSnapshot.class, RelativeContainerHandler.class);
		assertAvailable(address, ResourceSnapshot.class, AddressHandler.class);
		assertAvailable(books, ContainerSnapshot.class, BookContainerHandler.class);
		assertAvailable(firstBook, ResourceSnapshot.class, BookHandler.class);
		assertAvailable(secondBook, ResourceSnapshot.class, BookHandler.class);

		handleAction(Action.POST, myRelatives);

		ContainerSnapshot otherRelatives = sut.find(ContainerSnapshot.class,myRelatives.name(),RelativeContainerHandler.class);
		ResourceSnapshot myWife = addMember(otherRelatives,"myWife");

		terminateSession(Action.POST, myRelatives);
		// END Second interaction

		// BEGIN Third interaction
		prepareSession(Action.DELETE, myWife);

		assertAvailable(me, ResourceSnapshot.class, PersonHandler.class);
		assertAvailable(myRelatives, ResourceSnapshot.class, RelativeContainerHandler.class);
		assertAvailable(address, ResourceSnapshot.class, AddressHandler.class);
		assertAvailable(books, ContainerSnapshot.class, BookContainerHandler.class);
		assertAvailable(firstBook, ResourceSnapshot.class, BookHandler.class);
		assertAvailable(secondBook, ResourceSnapshot.class, BookHandler.class);
		assertAvailable(myWife, ResourceSnapshot.class,PersonHandler.class);

		handleAction(Action.DELETE, myWife);

		ResourceSnapshot foundRelative = assertAvailable(myWife,ResourceSnapshot.class,PersonHandler.class);

		deleteResource(foundRelative);

		terminateSession(Action.DELETE, myWife);
		// END Third interaction

		// BEGIN Fourth interaction
		prepareSession(Action.DELETE, me);

		ResourceSnapshot otherMe=assertAvailable(me,ResourceSnapshot.class,PersonHandler.class);
		assertAvailable(myRelatives, ResourceSnapshot.class, RelativeContainerHandler.class);
		assertAvailable(address, ResourceSnapshot.class, AddressHandler.class);
		assertAvailable(books, ContainerSnapshot.class, BookContainerHandler.class);
		assertAvailable(firstBook, ResourceSnapshot.class, BookHandler.class);
		assertAvailable(secondBook, ResourceSnapshot.class, BookHandler.class);
		assertUnavailable(myWife, ResourceSnapshot.class,PersonHandler.class);

		handleAction(Action.DELETE, me);

		deleteResource(otherMe);

		terminateSession(Action.DELETE, me);
		// END Fourth interaction

		// BEGIN Final interaction
		prepareSession(Action.CHECK, me);

		assertUnavailable(myRelatives, ResourceSnapshot.class, RelativeContainerHandler.class);
		assertUnavailable(address, ResourceSnapshot.class, AddressHandler.class);
		assertUnavailable(books, ContainerSnapshot.class, BookContainerHandler.class);
		assertUnavailable(firstBook, ResourceSnapshot.class, BookHandler.class);
		assertUnavailable(secondBook, ResourceSnapshot.class, BookHandler.class);
		assertUnavailable(myWife, ResourceSnapshot.class,PersonHandler.class);
	}

	private Name<?> name(String id) {
		return NamingScheme.getDefault().name(id);
	}

	private void logAction(Stage stage, Action action, ResourceSnapshot object) {
		LOGGER.debug(String.format(">> %s %s(%s)",stage.prefix,action,object.name()));
	}

	private ResourceSnapshot addMember(ContainerSnapshot target, String name) {
		LOGGER.debug("--> Requested adding member '"+name+"' to container '"+target.name()+"'");
		ResourceSnapshot addMember = target.addMember(name(name));
		LOGGER.debug("--> Completed adding member '"+name+"' to container '"+target.name()+"'");
		return addMember;
	}

	private ResourceSnapshot attachResource(ResourceSnapshot target, String attachmentId, String name, Class<? extends ResourceHandler> clazz) {
		LOGGER.debug("--> Requested attaching resource of type '"+clazz.getCanonicalName()+"' as '"+attachmentId+"' to resource '"+target.name()+"'");
		ResourceSnapshot attachResource = target.createAttachedResource(ResourceSnapshot.class,attachmentId,name(name),clazz);
		LOGGER.debug("--> Complete attaching resource of type '"+clazz.getCanonicalName()+"' as '"+attachmentId+"' to resource '"+target.name()+"'");
		return attachResource;
	}

	private ContainerSnapshot attachContainer(ResourceSnapshot target, String attachmentId, String name, Class<? extends ContainerHandler> clazz) {
		LOGGER.debug("--> Requested attaching resource of type '"+clazz.getCanonicalName()+"' as '"+attachmentId+"' to resource '"+target.name()+"'");
		ContainerSnapshot attachContainer = target.createAttachedResource(ContainerSnapshot.class,attachmentId,name(name), clazz);
		LOGGER.debug("--> Completed attaching resource of type '"+clazz.getCanonicalName()+"' as '"+attachmentId+"' to resource '"+target.name()+"'");
		return attachContainer;
	}

	private void deleteResource(ResourceSnapshot resource) {
		LOGGER.debug("--> Requested deletion of "+resource.name());
		sut.delete(resource);
		LOGGER.debug("--> Completed deletion of "+resource.name());
	}

	private void assertUnavailable(ResourceSnapshot resource, Class<? extends ResourceSnapshot> clazz, Class<? extends ResourceHandler> handlerClass) {
		ResourceSnapshot found = sut.find(clazz,resource.name(),handlerClass);
		assertThat(found,nullValue());
	}

	private ResourceSnapshot assertAvailable(ResourceSnapshot resource, Class<? extends ResourceSnapshot> clazz, Class<? extends ResourceHandler> handlerClass) {
		Class<? extends DelegatedResourceSnapshot> delegatedClass = DelegatedResourceSnapshot.class;
		if(clazz==ContainerSnapshot.class) {
			delegatedClass=DelegatedContainerSnapshot.class;
		}
		ResourceSnapshot found = sut.find(clazz,resource.name(),handlerClass);
		assertThat(found,notNullValue());
		assertThat(found,instanceOf(clazz));
		assertThat(found,instanceOf(delegatedClass));
		assertThat(delegatedClass.cast(found).resourceId(), equalTo(delegatedClass.cast(resource).resourceId()));
		return found;
	}

	private void handleAction(Action action, ResourceSnapshot resource) {
		logAction(Stage.HANDLING,action,resource);
	}

	private void terminateSession(Action action, ResourceSnapshot resource) throws WriteSessionException {
		logAction(Stage.TERMINATION,action,resource);
		uow.accept(new UnitOfWorkInspector());
		sut.saveChanges();
		writeSessionService.terminateSession(sut);
	}

	private void prepareSession(Action action, ResourceSnapshot resource) {
		logAction(Stage.PREPARATION,action,resource);
		sut = writeSessionService.createSession(WriteSessionConfiguration.builder().build());
		uow = UnitOfWork.getCurrent();
	}

	private void prepareSession(Action action, org.ldp4j.application.resource.Resource resource) {
		logAction(Stage.PREPARATION, action, resource);
		uow = UnitOfWork.newCurrent();
		sut = writeSessionService.createSession(WriteSessionConfiguration.builder().build());
	}

	private void handleAction(Action action, org.ldp4j.application.resource.Resource resource) {
		logAction(Stage.HANDLING,action,resource);
	}

	private void logAction(Stage stage, Action action,
			org.ldp4j.application.resource.Resource resource) {
		LOGGER.debug(String.format(">> %s %s(%s)",stage.prefix,action,resource.id().name()));
	}

}
