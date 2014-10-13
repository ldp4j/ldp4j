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

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.ext.Deletable;
import org.ldp4j.application.ext.InvalidContentException;
import org.ldp4j.application.ext.Modifiable;
import org.ldp4j.application.ext.annotations.Resource;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.ldp4j.example.InMemoryResourceHandler;

@Resource(
	id=TCKFResourceHandler.ID
)
public class TCKFResourceHandler extends InMemoryResourceHandler implements Modifiable, Deletable {

	public static final String ID="ResourceHandler";
	
	public TCKFResourceHandler() {
		super(ID);
	}

	@Override
	public void delete(ResourceSnapshot resource, WriteSession session) {
		DataSet dataSet = get(resource);
		try {
			remove(resource.name());
			session.delete(resource);
			session.saveChanges();
		} catch (WriteSessionException e) {
			// Recover if failed
			add(resource.name(),dataSet);
			throw new IllegalStateException("Deletion failed",e);
		}
	}

	@Override
	public void update(ResourceSnapshot resource, DataSet content, WriteSession session) throws InvalidContentException {
		DataSet dataSet = get(resource);
		enforceConsistency(resource, content, dataSet);
		
		try {
			add(resource.name(),content);
			session.modify(resource);
			session.saveChanges();
		} catch (WriteSessionException e) {
			// Recover if failed
			add(resource.name(),dataSet);
			throw new IllegalStateException("Update failed",e);
		}
	}

	protected void enforceConsistency(ResourceSnapshot resource, DataSet content, DataSet dataSet) throws InvalidContentException {
		ManagedIndividualId id = ManagedIndividualId.createId(resource.name(),TCKFResourceHandler.ID);
		ManagedIndividual stateIndividual = 
			dataSet.
				individual(
					id, 
					ManagedIndividual.class);
		ManagedIndividual inIndividual = 
			content.
				individual(
					id, 
					ManagedIndividual.class);
		Property stateProperty=
			stateIndividual.property(TCKFContainerHandler.READ_ONLY_PROPERTY);
		Property inProperty=
			inIndividual.property(TCKFContainerHandler.READ_ONLY_PROPERTY);

		for(Value value:inProperty) {
			boolean newAdded=false;
			for(Value c:stateProperty) {
				if(c.equals(value)) {
					newAdded=true;
					break;
				}
			}
			if(newAdded) {
				throw new InvalidContentException("New value '"+value+"' for property '"+TCKFContainerHandler.READ_ONLY_PROPERTY+"' has been added");
			}
		}
		for(Value value:stateProperty) {
			boolean deleted=true;
			for(Value c:inProperty) {
				if(c.equals(value)) {
					deleted=false;
					break;
				}
			}
			if(deleted) {
				throw new InvalidContentException("Value '"+value+"' for property '"+TCKFContainerHandler.READ_ONLY_PROPERTY+"' has been deleted");
			}
		}
	}

}
