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

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ApplicationRuntimeException;
import org.ldp4j.application.ext.annotations.IndirectContainer;
import org.ldp4j.application.ext.annotations.MembershipRelation;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;

/**
 * Example indirect container template handler.
 */
@IndirectContainer(
	id = BookContainerHandler.ID,
	memberHandler = BookHandler.class,
	membershipRelation=MembershipRelation.HAS_MEMBER,
	membershipPredicate="http://www.ldp4j.org/vocabularies/example#hasBook",
	insertedContentRelation = BookContainerHandler.INSERTED_CONTENT_RELATION
)
public class BookContainerHandler extends InMemoryContainerHandler {

	/**
	 * The inserted content relation of the template defined by the handler.
	 */
	public static final String INSERTED_CONTENT_RELATION = "http://www.ldp4j.org/vocabularies/example#bookshelf";

	/**
	 * The template identifier of the handler.
	 */
	public static final String ID="bookContainerTemplate";

	private BookHandler handler;

	/**
	 * Create a new instance.
	 */
	public BookContainerHandler() {
		super("BookContainer");
	}

	/**
	 * Set the book handler associated to this handler.
	 *
	 * @param handler
	 *            the book handler.
	 */
	public void setBookHandler(BookHandler handler) {
		this.handler = handler;
	}

	/**
	 * Return the book handler associated to this handler.
	 *
	 * @return the book handler associated to this handler.
	 * @throws IllegalStateException
	 *             if no associated book handler has been defined.
	 */
	public BookHandler bookHandler() {
		if(this.handler==null) {
			throw new IllegalStateException("Handler not initialized yet");
		}
		return this.handler;
	}

	/**
	 * {@inheritDoc}<br>
	 *
	 * Create a new book resource.
	 */
	@Override
	public ResourceSnapshot create(ContainerSnapshot container, DataSet representation, WriteSession session) {
		NameProvider nameProvider = nameProvider(container.name());
		Name<?> nextName = nameProvider.nextMemberName();
		try {
			bookHandler().add(nextName,representation);
			ResourceSnapshot newMember = container.addMember(nextName);
			session.saveChanges();
			return newMember;
		} catch (Exception e) {
			bookHandler().remove(nextName);
			throw new ApplicationRuntimeException("Could not create member",e);
		}
	}

}