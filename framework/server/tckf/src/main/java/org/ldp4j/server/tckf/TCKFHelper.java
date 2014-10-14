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
import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.FormatUtils;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.ValueVisitor;
import org.ldp4j.application.ext.InvalidContentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TCKFHelper {

	static final URI READ_ONLY_PROPERTY = URI.create("http://www.example.org/vocab#creationDate");
	
	private static Logger LOGGER=LoggerFactory.getLogger(TCKFHelper.class);

	private TCKFHelper() {
	}

	static void enforceConsistency(Name<?> resourceName, String managerId, DataSet newState, DataSet currentState) throws InvalidContentException {
		ManagedIndividualId id = ManagedIndividualId.createId(resourceName,managerId);
		LOGGER.debug("Checking consistency of {}",format(id));
		LOGGER.trace("- Current state:\n{}",currentState);
		LOGGER.trace("- New state:\n{}",newState);
		ManagedIndividual stateIndividual = 
			currentState.
				individual(
					id, 
					ManagedIndividual.class);

		Property stateProperty=
				stateIndividual.property(READ_ONLY_PROPERTY);

		ManagedIndividual inIndividual = 
			newState.
				individual(
					id, 
					ManagedIndividual.class);

		Property inProperty=
			inIndividual.property(READ_ONLY_PROPERTY);

		if(stateProperty==null && inProperty==null) {
			LOGGER.debug("Property '{}' is not defined in the current state nor in the new state",READ_ONLY_PROPERTY);
			return;
		}
		if(stateProperty==null && inProperty!=null) {
			LOGGER.error("Property '{}' is not defined in the current state but it is defined in the new state",READ_ONLY_PROPERTY);
			throw new InvalidContentException("Added values to property '"+READ_ONLY_PROPERTY+"'");
		}
		if(stateProperty!=null && inProperty==null) {
			LOGGER.error("Property '{}' is defined in the current state but it is not defined in the new state",READ_ONLY_PROPERTY);
			throw new InvalidContentException("Removed all values from property '"+READ_ONLY_PROPERTY+"'");
		}

		for(Value value:inProperty) {
			LOGGER.debug("Verifing property '{}' input value {}...",READ_ONLY_PROPERTY,format(value));
			if(!DataSetUtils.hasValue(value,stateProperty)) {
				LOGGER.error("New value {} has been added to property '{}'",format(value),READ_ONLY_PROPERTY);
				throw new InvalidContentException("New value '"+format(value)+"' for property '"+READ_ONLY_PROPERTY+"' has been added");
			}
		}
		for(Value value:stateProperty) {
			LOGGER.debug("Verifing property '{}' existing value {}...",READ_ONLY_PROPERTY,format(value));
			if(!DataSetUtils.hasValue(value,inProperty)) {
				LOGGER.error("Value {} has been removed from property '{}'",format(value),READ_ONLY_PROPERTY);
				throw new InvalidContentException("Value '"+value+"' has been removed from property '"+READ_ONLY_PROPERTY+"'");
			}
		}
	}

	private static String format(ManagedIndividualId id) {
		return String.format("%s {Managed by: %s}",id.name(),id.managerId());
	}

	private static String format(Value value) {
		final AtomicReference<String> result=new AtomicReference<String>();
		value.accept(
			new ValueVisitor() {
				@Override
				public void visitLiteral(Literal<?> value) {
					result.set(String.format("%s [%s]",value.get(),value.get().getClass().getCanonicalName()));
				}
				@Override
				public void visitIndividual(Individual<?, ?> value) {
					result.set(FormatUtils.formatIndividualId(value));
				}
			}
		);
		return result.get();
	}

	
}
