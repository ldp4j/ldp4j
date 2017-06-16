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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:0.2.2
 *   Bundle      : ldp4j-application-data-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data.validation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.FormatUtils;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.ValueVisitor;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class ValidationConstraintFactory {

	private static final String PROPERTY_CANNOT_BE_NULL              = "Property cannot be null";
	private static final String INDIVIDUAL_IDENTIFIER_CANNOT_BE_NULL = "Individual identifier cannot be null";
	private static final String PREDICATE_CANNOT_BE_NULL             = "Predicate cannot be null";

	private static final class ValidationLogImpl implements ValidationLog {

		private final class ValidationFailureImpl implements ValidationFailure {

			@Override
			public String toString() {
				StringBuilder builder=new StringBuilder();
				dump(builder, removedValues, "Removed required", "from");
				if(!builder.toString().isEmpty() && !addedValues.isEmpty()) {
					builder.append(", ");
				}
				dump(builder, addedValues, "Added undesired", "to");
				return builder.toString();
			}

			private void dump(StringBuilder builder, Map<Property, Value> map, String prefix, String infix) {
				for(Iterator<Entry<Property,Value>> it=map.entrySet().iterator();it.hasNext();) {
					Entry<Property, Value> entry = it.next();
					builder.append(prefix+" value '"+FormatUtils.formatValue(entry.getValue())+"' "+infix+" property '"+entry.getKey().predicate()+"'");
					if(it.hasNext()) {
						builder.append(", ");
					}
				}
			}
		}

		private final Map<Property,Value> removedValues;
		private final Map<Property,Value> addedValues;
		private boolean checked;

		private ValidationLogImpl() {
			this.removedValues=Maps.newHashMap();
			this.addedValues=Maps.newHashMap();
			this.checked=false;
		}

		@Override
		public boolean success() {
			return this.removedValues.isEmpty() && this.addedValues.isEmpty();
		}

		private void markChecked() {
			this.checked=true;
		}

		@Override
		public boolean checked() {
			return this.checked;
		}

		private void addRemovedValue(Property property,Value value) {
			this.removedValues.put(property, value);
		}

		private void addAddedValue(Property property,Value value) {
			this.addedValues.put(property, value);
		}

		@Override
		public ValidationFailure validationFailure() {
			return new ValidationFailureImpl();
		}

	}

	private static class MandatoryPropertyValuesValidationConstraint implements ValidationConstraint<Property> {

		private final Object individualId;
		private final URI predicate;
		private final Collection<Value> values;

		private MandatoryPropertyValuesValidationConstraint(Object individualId, URI predicate, Collection<? extends Value> values) {
			this.individualId = individualId;
			this.predicate = predicate;
			this.values = Collections.unmodifiableCollection(values);
		}

		private MandatoryPropertyValuesValidationConstraint(Object individualId, URI predicate, Value... values) {
			this(individualId,predicate,Arrays.asList(values));
		}

		@Override
		public boolean mustBeChecked() {
			return this.individualId!=null && !this.values.isEmpty();
		}

		@Override
		public final ValidationLog validate(Property property) {
			final ValidationLogImpl log = new ValidationLogImpl();
			if(mustBeAnalyzed(property)) {
				log.markChecked();
				analyze(property,log);
			}
			return log;
		}

		protected void analyze(Property property, ValidationLogImpl log) {
			checkRemovedValues(property, log);
		}

		protected final Collection<Value> constrainedValues() {
			return this.values;
		}

		private void checkRemovedValues(final Property property, final ValidationLogImpl log) {
			ValueVisitor removingVisitor = new ValueVisitor() {
				@Override
				public void visitLiteral(Literal<?> value) {
					if(!property.hasLiteralValue(value)) {
						log.addRemovedValue(property,value);
					}
				}
				@Override
				public void visitIndividual(Individual<?, ?> value) {
					if(!property.hasIdentifiedIndividual(value.id())) {
						log.addRemovedValue(property,value);
					}
				}
			};
			for(Value value:constrainedValues()){
				value.accept(removingVisitor);
			}
		}

		private final boolean mustBeAnalyzed(Property property) {
			return this.predicate.equals(property.predicate()) && (this.individualId==null || this.individualId.equals(property.individual().id()));
		}

		@Override
		public ValidationFailure uncheckedFailure() {
			return new ValidationFailure() {
				@Override
				public String toString() {
					return "Could not check mandatory values for property '"+predicate+"' of individual "+individualId;
				}
			};
		}

		@Override
		public String toString() {
			ToStringHelper stringHelper =
				MoreObjects.
					toStringHelper(constraintName()).
						add("individual",this.individualId==null?"<any>":FormatUtils.formatId(this.individualId)).
						add("predicate", this.predicate);
			List<String> rawValues=Lists.newArrayList();
			for(Value value:values) {
				rawValues.add(FormatUtils.formatValue(value));
			}
			stringHelper.add(constraintInterpretation(),rawValues);
			return stringHelper.toString();
		}

		protected String constraintInterpretation() {
			return "mandatoryValues";
		}

		protected String constraintName() {
			return "MandatoryPropertyValues";
		}

	}

	private static final class ReadOnlyPropertyValidationConstraint extends MandatoryPropertyValuesValidationConstraint {

		private ReadOnlyPropertyValidationConstraint(Object individualId, URI predicate, Collection<? extends Value> values) {
			super(individualId,predicate,values);
		}

		private ReadOnlyPropertyValidationConstraint(Object individualId, URI predicate, Value... values) {
			this(individualId,predicate,Arrays.asList(values));
		}

		@Override
		protected void analyze(Property property, ValidationLogImpl log) {
			super.analyze(property, log);
			checkAddedValues(property, log);
		}

		private void checkAddedValues(final Property property, final ValidationLogImpl log) {
			final Collection<? extends Value> constrainedValues = constrainedValues();
			ValueVisitor addingVisitor = new ValueVisitor() {
				@Override
				public void visitLiteral(Literal<?> value) {
					if(!DataSetUtils.hasLiteral(value,constrainedValues)) {
						log.addAddedValue(property,value);
					}
				}
				@Override
				public void visitIndividual(Individual<?, ?> value) {
					if(!DataSetUtils.hasIdentifiedIndividual(value.id(),constrainedValues)) {
						log.addAddedValue(property,value);
					}
				}
			};
			for(Value value:property){
				value.accept(addingVisitor);
			}
		}

		@Override
		protected String constraintInterpretation() {
			return "uniqueValues";
		}

		@Override
		protected String constraintName() {
			return "ReadOnlyProperty";
		}

	}

	private ValidationConstraintFactory() {
	}

	public static ValidationConstraint<Property> readOnlyProperty(Property property) {
		checkNotNull(property,PROPERTY_CANNOT_BE_NULL);
		return new ReadOnlyPropertyValidationConstraint(property.individual().id(), property.predicate(), property.values());
	}

	public static ValidationConstraint<Property> readOnlyProperty(Object individualId, URI predicate, Value... values) {
		checkNotNull(individualId,INDIVIDUAL_IDENTIFIER_CANNOT_BE_NULL);
		checkNotNull(predicate,PREDICATE_CANNOT_BE_NULL);
		return new ReadOnlyPropertyValidationConstraint(individualId, predicate, values);
	}

	public static ValidationConstraint<Property> readOnlyProperty(URI predicate, Value... values) {
		checkNotNull(predicate,PREDICATE_CANNOT_BE_NULL);
		return new ReadOnlyPropertyValidationConstraint(null, predicate, values);
	}

	public static ValidationConstraint<Property> mandatoryPropertyValues(Property property) {
		checkNotNull(property,INDIVIDUAL_IDENTIFIER_CANNOT_BE_NULL);
		return new MandatoryPropertyValuesValidationConstraint(property.individual().id(), property.predicate(), property.values());
	}

	public static ValidationConstraint<Property> mandatoryPropertyValues(Object individualId, URI predicate, Value... values) {
		checkNotNull(individualId,INDIVIDUAL_IDENTIFIER_CANNOT_BE_NULL);
		checkNotNull(predicate,PREDICATE_CANNOT_BE_NULL);
		return new MandatoryPropertyValuesValidationConstraint(individualId, predicate, values);
	}

	public static ValidationConstraint<Property> mandatoryPropertyValues(URI predicate, Value... values) {
		checkNotNull(predicate,PREDICATE_CANNOT_BE_NULL);
		return new MandatoryPropertyValuesValidationConstraint(null, predicate, values);
	}

	protected static boolean sameIndividual(Property one, Property another) {
		return one.individual().id().equals(another.individual().id());
	}

	protected static boolean samePredicate(Property one, Property another) {
		return one.predicate().equals(another.predicate());
	}

}
