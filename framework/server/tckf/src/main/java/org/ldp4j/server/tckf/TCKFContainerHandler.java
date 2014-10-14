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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-tckf:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-tckf-1.0.0-SNAPSHOT.war
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.tckf;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetHelper;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.InvalidContentException;
import org.ldp4j.application.ext.Modifiable;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.ldp4j.example.InMemoryContainerHandler;

public class TCKFContainerHandler extends InMemoryContainerHandler implements Modifiable {

	private final AtomicInteger id;

	private TCKFResourceHandler handler;

	protected TCKFContainerHandler(String handlerName) {
		super(handlerName);
		this.id=new AtomicInteger();
	}

	public final void setHandler(TCKFResourceHandler handler) {
		this.handler = handler;
	}
	
	protected final TCKFResourceHandler handler() {
		return this.handler;
	}
	
	protected final int nextId() {
		return this.id.incrementAndGet();
	}
	
	@Override
	public ResourceSnapshot create(ContainerSnapshot container, DataSet representation, WriteSession session) {
		Name<?> name=
			NamingScheme.
				getDefault().
					name(nextId());
		
		DataSetHelper helper=
				DataSetHelper.newInstance(representation);
		
		ManagedIndividual individual = 
			helper.
				replace(
					DataSetHelper.SELF, 
					ManagedIndividualId.createId(name,getHandlerName()), 
					ManagedIndividual.class);

		individual.
			addValue(
				TCKFHelper.READ_ONLY_PROPERTY, 
				DataSetUtils.newLiteral(new Date()));
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
	public void update(ResourceSnapshot resource, DataSet newState, WriteSession session) throws InvalidContentException {
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
