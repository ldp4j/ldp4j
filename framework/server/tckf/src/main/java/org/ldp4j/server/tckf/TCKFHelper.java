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

import java.net.URI;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.ext.InvalidContentException;
import org.ldp4j.application.session.ResourceSnapshot;

final class TCKFHelper {

	static final URI READ_ONLY_PROPERTY = URI.create("http://www.example.org/vocab#creationDate");

	private TCKFHelper() {
	}

	static void enforceConsistency(ResourceSnapshot resource, DataSet content, DataSet dataSet) throws InvalidContentException {
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
			stateIndividual.property(READ_ONLY_PROPERTY);
		Property inProperty=
			inIndividual.property(READ_ONLY_PROPERTY);

		if(stateProperty==null && inProperty==null) {
			return;
		}
		if(stateProperty==null && inProperty!=null) {
			throw new InvalidContentException("Added values to property '"+READ_ONLY_PROPERTY+"'");
		}
		if(stateProperty!=null && inProperty==null) {
			throw new InvalidContentException("Removed all values from property '"+READ_ONLY_PROPERTY+"'");
		}

		for(Value value:inProperty) {
			boolean newAdded=false;
			for(Value c:stateProperty) {
				if(c.equals(value)) {
					newAdded=true;
					break;
				}
			}
			if(newAdded) {
				throw new InvalidContentException("New value '"+value+"' for property '"+READ_ONLY_PROPERTY+"' has been added");
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
				throw new InvalidContentException("Value '"+value+"' for property '"+READ_ONLY_PROPERTY+"' has been deleted");
			}
		}
	}

	
}
