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

import java.net.URI;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetHelper;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.Literals;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.ApplicationRuntimeException;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;

/**
 * Base container for managing collections of persons.
 */
public class AbstractPersonContainerHandler extends InMemoryContainerHandler {

	private PersonHandler handler;

	private AtomicInteger id;

	/**
	 * Create a new instance with a given name
	 */
	protected AbstractPersonContainerHandler(String name) {
		super(name);
		this.id=new AtomicInteger();
	}

	/**
	 * Set the person handler associated to this handler.
	 *
	 * @param handler
	 *            the person handler.
	 */
	public final void setHandler(PersonHandler handler) {
		this.handler = handler;
	}

	/**
	 * {@inheritDoc}<br>
	 *
	 * Create a new person resource.
	 */
	@Override
	public final ResourceSnapshot create(ContainerSnapshot container, DataSet representation, WriteSession session) {
		Name<?> name=
			NamingScheme.
				getDefault().
					name(id.incrementAndGet());

		DataSetHelper helper=
					DataSetUtils.newHelper(representation);

		ManagedIndividual individual =
			helper.
				replace(
					DataSetHelper.SELF,
					ManagedIndividualId.createId(name,PersonHandler.ID),
					ManagedIndividual.class);

		individual.
			addValue(
				URI.create("http://www.example.org/vocab#creationDate"),
				Literals.of(new Date()).dateTime());
		try {
			this.handler.add(name, representation);
			ResourceSnapshot member = container.addMember(name);
			session.saveChanges();
			return member;
		} catch (Exception e) {
			this.handler.remove(name);
			throw new ApplicationRuntimeException("Could not create member",e);
		}
	}

}