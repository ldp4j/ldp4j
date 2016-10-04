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
 *   Artifact    : org.ldp4j.framework:ldp4j-conformance-fixture:0.2.2
 *   Bundle      : ldp4j-conformance-fixture-0.2.2.war
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.conformance.fixture;

import java.util.Date;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetHelper;
import org.ldp4j.application.data.DataSetModificationException;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.Literals;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.ext.InconsistentContentException;
import org.ldp4j.application.ext.Modifiable;
import org.ldp4j.application.ext.UnsupportedContentException;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCKFContainerHandler extends InMemoryContainerHandler implements Modifiable {

	private static final Logger LOGGER=LoggerFactory.getLogger(TCKFContainerHandler.class);

	private TCKFResourceHandler handler;

	protected TCKFContainerHandler(String handlerName) {
		super(handlerName);
	}

	public final void setHandler(TCKFResourceHandler handler) {
		this.handler = handler;
	}

	protected final TCKFResourceHandler handler() {
		return this.handler;
	}

	@Override
	public ResourceSnapshot create(ContainerSnapshot container, DataSet representation, WriteSession session) throws UnsupportedContentException {
		Name<String> name = TCKFHelper.nextName(getHandlerName());


		LOGGER.trace("Creating member of container {} using: \n{}",getHandlerName(),representation);

		DataSetHelper helper=
				DataSetUtils.newHelper(representation);

		ManagedIndividualId newId =
			ManagedIndividualId.
				createId(
					name,
					TCKFResourceHandler.ID);

		try {
			ManagedIndividual individual=helper.manage(newId);
			individual.
				addValue(
					TCKFHelper.READ_ONLY_PROPERTY,
					Literals.of(new Date()).dateTime());
		} catch (DataSetModificationException e) {
			// TODO: Verify this weird error
			Constraints constraints = Constraints.constraints();
			throw new UnsupportedContentException("Could not process request", e, constraints);
		}

		try {
			handler().add(name, representation);
			ResourceSnapshot member = container.addMember(name);
			session.saveChanges();
			return member;
		} catch (WriteSessionException e) {
			handler().remove(name);
			throw new IllegalStateException("Could not create member",e);
		}
	}

	@Override
	public void update(ResourceSnapshot resource, DataSet newState, WriteSession session) throws InconsistentContentException, UnsupportedContentException {
		DataSet currentState = get(resource);
		TCKFHelper.enforceConsistency(resource.name(),getHandlerName(),newState,currentState);
		try {
			add(resource.name(),newState);
			session.modify(resource);
			session.saveChanges();
		} catch (WriteSessionException e) {
			// Recover if failed
			add(resource.name(),currentState);
			throw new IllegalStateException("Update failed",e);
		}
	}


}
