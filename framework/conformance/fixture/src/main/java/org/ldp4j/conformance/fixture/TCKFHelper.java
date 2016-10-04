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

import java.net.URI;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.FormatUtils;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.ValueVisitor;
import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.data.constraints.Constraints.Cardinality;
import org.ldp4j.application.data.constraints.Constraints.PropertyConstraint;
import org.ldp4j.application.data.constraints.Constraints.Shape;
import org.ldp4j.application.ext.InconsistentContentException;
import org.ldp4j.application.ext.UnsupportedContentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

final class TCKFHelper {

	private static final Map<String,AtomicLong> COUNTERS=
		ImmutableMap.
			<String,AtomicLong>builder().
				put(TCKFResourceHandler.ID,new AtomicLong()).
				put(TCKFBasicContainerHandler.ID,new AtomicLong()).
				put(TCKFDirectContainerHandler.ID,new AtomicLong()).
				put(TCKFIndirectContainerHandler.ID,new AtomicLong()).
				build();

	private static final URI UNKNOWN_PROPERTY = URI.create("http://example.com/ns#comment");

	static final URI READ_ONLY_PROPERTY = URI.create("http://www.example.org/vocab#creationDate");

	private static final Logger LOGGER=LoggerFactory.getLogger(TCKFHelper.class);

	private TCKFHelper() {
	}

	static Name<String> nextName(String templateId) {
		return NamingScheme.getDefault().name(templateId, Long.toHexString(COUNTERS.get(templateId).getAndIncrement()).toUpperCase(Locale.ENGLISH));
	}

	static void enforceConsistency(Name<?> resourceName, String managerId, DataSet newState, DataSet currentState) throws InconsistentContentException, UnsupportedContentException {
		ManagedIndividualId id = ManagedIndividualId.createId(resourceName,managerId);

		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Checking consistency of {}",format(id));
			LOGGER.trace("- Current state:\n{}",currentState);
			LOGGER.trace("- New state:\n{}",newState);
		}

		ManagedIndividual stateIndividual =currentState.individual(id,ManagedIndividual.class);
		Property stateProperty=stateIndividual.property(READ_ONLY_PROPERTY);

		ManagedIndividual inIndividual=newState.individual(id,ManagedIndividual.class);
		Property inProperty=inIndividual.property(READ_ONLY_PROPERTY);

		if(stateProperty==null && inProperty==null) {
			LOGGER.debug("Property '{}' is not defined in the current state nor in the new state",READ_ONLY_PROPERTY);
			return;
		}

		verifyAssertions(stateProperty, inIndividual, inProperty);
	}

	private static void verifyAssertions(Property stateProperty, ManagedIndividual inIndividual, Property inProperty) throws InconsistentContentException, UnsupportedContentException {
		Constraints constraints = createConstraints(stateProperty, inIndividual);
		verifyReadOnlyPropertyIsNotDefined(stateProperty,inProperty,constraints);
		verifyReadOnlyPropertyIsDefined(stateProperty,inProperty,constraints);
		verifyNoValuesHaveBeenAddedToReadOnlyProperty(stateProperty,inProperty, constraints);
		verifyNoValuesHaveBeenRemovedFromReadOnlyProperty(stateProperty,inProperty, constraints);
		verifyAbsenceOfUnknownProperty(inIndividual, constraints);
	}

	private static void verifyAbsenceOfUnknownProperty(ManagedIndividual inIndividual, Constraints constraints) throws UnsupportedContentException {
		LOGGER.debug("Verifing absence of unknown properties...");
		if(inIndividual.property(UNKNOWN_PROPERTY)!=null) {
			LOGGER.error("Unknown property '{}' specified",UNKNOWN_PROPERTY);
			throw new UnsupportedContentException("Unknown property '"+UNKNOWN_PROPERTY+"' specified",constraints);
		}
	}

	private static void verifyNoValuesHaveBeenRemovedFromReadOnlyProperty(Property stateProperty, Property inProperty, Constraints constraints) throws InconsistentContentException {
		for(Value value:stateProperty) {
			LOGGER.debug("Verifing property '{}' existing value {}...",READ_ONLY_PROPERTY,format(value));
			if(!DataSetUtils.hasValue(value,inProperty)) {
				LOGGER.error("Value {} has been removed from property '{}'",format(value),READ_ONLY_PROPERTY);
				throw new InconsistentContentException("Value '"+value+"' has been removed from property '"+READ_ONLY_PROPERTY+"'",constraints);
			}
		}
	}

	private static void verifyNoValuesHaveBeenAddedToReadOnlyProperty(Property stateProperty, Property inProperty, Constraints constraints) throws InconsistentContentException {
		for(Value value:inProperty) {
			LOGGER.debug("Verifing property '{}' input value {}...",READ_ONLY_PROPERTY,format(value));
			if(!DataSetUtils.hasValue(value,stateProperty)) {
				LOGGER.error("New value {} has been added to property '{}'",format(value),READ_ONLY_PROPERTY);
				throw new InconsistentContentException("New value '"+format(value)+"' for property '"+READ_ONLY_PROPERTY+"' has been added",constraints);
			}
		}
	}

	private static void verifyReadOnlyPropertyIsDefined(Property stateProperty, Property inProperty, Constraints constraints) throws InconsistentContentException {
		if(stateProperty!=null && inProperty==null) {
			LOGGER.error("Property '{}' is defined in the current state but it is not defined in the new state",READ_ONLY_PROPERTY);
			throw new InconsistentContentException("Removed all values from property '"+READ_ONLY_PROPERTY+"'",constraints);
		}
	}

	private static void verifyReadOnlyPropertyIsNotDefined(Property stateProperty, Property inProperty, Constraints constraints) throws InconsistentContentException {
		if(stateProperty==null && inProperty!=null) {
			LOGGER.error("Property '{}' is not defined in the current state but it is defined in the new state",READ_ONLY_PROPERTY);
			throw new InconsistentContentException("Added values to property '"+READ_ONLY_PROPERTY+"'",constraints);
		}
	}

	private static Constraints createConstraints(Property stateProperty, ManagedIndividual inIndividual) {
		Shape shape=
			Constraints.
				shape().
					withLabel("shape").
					withComment("An example data shape").
					withPropertyConstraint(createReadOnlyPropertyConstraint(stateProperty)).
					withPropertyConstraint(createUnkownPropertyConstraint());

		return
			Constraints.
				constraints().
					withNodeShape(inIndividual,shape);
	}

	private static PropertyConstraint createUnkownPropertyConstraint() {
		return Constraints.
			propertyConstraint(UNKNOWN_PROPERTY).
				withCardinality(Cardinality.create(0, 0));
	}

	private static PropertyConstraint createReadOnlyPropertyConstraint(Property stateProperty) {
		PropertyConstraint pc=
			Constraints.
				propertyConstraint(READ_ONLY_PROPERTY).
					withLabel("ReadOnlyProperty").
					withComment("An example read-only-property");
		if(stateProperty!=null && stateProperty.hasValues()) {
			Collection<? extends Value> values = stateProperty.values();
			pc.withCardinality(Constraints.Cardinality.create(values.size(), values.size()));
			pc.withValue(values.toArray(new Value[]{}));
		} else {
			pc.withCardinality(Constraints.Cardinality.create(0, 0));
		}
		return pc;
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
					result.set(FormatUtils.formatId(value));
				}
			}
		);
		return result.get();
	}


}
