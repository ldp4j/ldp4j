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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-data-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Preconditions;

public final class DataSetUtils {

	private static final String LITERAL_DATATYPE_CANNOT_BE_NULL = "Literal datatype cannot be null";
	private static final String LITERAL_VALUE_CANNOT_BE_NULL = "Literal value cannot be null";

	private static final class IndividualFinder implements IndividualVisitor {

		private final DataSet dataSet;
		private Individual<?,?> found=null;

		IndividualFinder(DataSet dataSet) {
			this.dataSet = dataSet;
		}

		Individual<?,?> findOrCreate(Individual<?,?> individual) {
			individual.accept(this);
			return found;
		}

		@Override
		public void visitManagedIndividual(ManagedIndividual individual) {
			found=dataSet.individual(individual.id(),ManagedIndividual.class);
		}

		@Override
		public void visitLocalIndividual(LocalIndividual individual) {
			found=dataSet.individual(individual.id(),LocalIndividual.class);
		}

		@Override
		public void visitExternalIndividual(ExternalIndividual individual) {
			found=dataSet.individual(individual.id(),ExternalIndividual.class);
		}

		@Override
		public void visitRelativeIndividual(RelativeIndividual individual) {
			found=dataSet.individual(individual.id(),RelativeIndividual.class);
		}

		@Override
		public void visitNewIndividual(NewIndividual individual) {
			found=dataSet.individual(individual.id(),NewIndividual.class);
		}
	}

	private static class ValueReplicator implements ValueVisitor {
		private final IndividualFinder finder;
		private Value result=null;

		ValueReplicator(DataSet dataSet) {
			finder=new IndividualFinder(dataSet);
		}

		Value replicate(Value value) {
			value.accept(this);
			return result;
		}
		@Override
		public void visitLiteral(Literal<?> value) {
			result=value;
		}
		@Override
		public void visitIndividual(Individual<?, ?> value) {
			result=finder.findOrCreate(value);
		}
	}

	abstract static class ValueMatcher implements ValueVisitor {

		private boolean matches;

		public final boolean matchesValue(Value propertyValue) {
			this.setMatches(false);
			propertyValue.accept(this);
			return this.matches;
		}

		protected final void setMatches(boolean matches) {
			this.matches = matches;
		}

	}

	static final class LiteralMatcher extends ValueMatcher {

		private final Literal<?> literal;

		LiteralMatcher(Literal<?> literal) {
			this.literal = literal;
		}

		@Override
		public void visitLiteral(Literal<?> value) {
			super.setMatches(literal.get().equals(value.get()));
		}

		@Override
		public void visitIndividual(Individual<?, ?> value) {
		}

	}

	static final class IndividualReferenceMatcher extends ValueMatcher {

		private final Object id;

		IndividualReferenceMatcher(Object id) {
			this.id = id;
		}

		@Override
		public void visitLiteral(Literal<?> value) {
		}

		@Override
		public void visitIndividual(Individual<?, ?> value) {
			super.setMatches(value.id().equals(this.id));
		}

	}

	private DataSetUtils() {
	}

	private static boolean hasValue(ValueMatcher matcher, Collection<? extends Value> values) {
		for(Value value:values) {
			if(matcher.matchesValue(value)) {
				return true;
			}
		}
		return false;
	}

	public static void merge(DataSet source, DataSet target) {
		IndividualFinder finder=new IndividualFinder(target);
		for(Individual<?, ?> individual:source) {
			merge(individual,finder.findOrCreate(individual));
		}

	}

	public static void merge(Individual<?,?> source, Individual<?,?> target) {
		ValueReplicator replicator=new ValueReplicator(target.dataSet());
		for(Property property:source.properties()) {
			for(Value value:property) {
				target.addValue(property.predicate(),replicator.replicate(value));
			}
		}
	}

	public static void remove(DataSet source, DataSet target) {
		IndividualFinder finder=new IndividualFinder(target);
		for(Individual<?, ?> individual:source) {
			remove(individual,finder.findOrCreate(individual));
		}

	}

	public static void remove(Individual<?,?> source, final Individual<?,?> target) {
		for(Property property:source.properties()) {
			final URI propertyId=property.predicate();
			ValueVisitor visitor = new ValueVisitor(){
				@Override
				public void visitLiteral(Literal<?> value) {
					target.removeValue(propertyId, value);
				}
				@Override
				public void visitIndividual(Individual<?, ?> value) {
					Individual<?, ?> cValue = target.dataSet().individualOfId(value.id());
					if(cValue!=null) {
						target.removeValue(propertyId, cValue);
					}
				}
			};
			for(Value value:property) {
				value.accept(visitor);
			}
		}
	}

	/**
	 * Create a new literal
	 * @deprecated
	 * @param value The value for the literal
	 * @return A literal wrapping the specified value
	 */
	@Deprecated
	public static <T extends Serializable> Literal<T> newLiteral(T value) {
		Preconditions.checkNotNull(value,LITERAL_VALUE_CANNOT_BE_NULL);
		return new ImmutableLiteral<T>(value);
	}

	/**
	 * Create a new typed literal
	 * @deprecated
	 * @param value The value for the literal
	 * @param datatype The datatype of the literal
	 * @return A typed literal wrapping the specified value
	 */
	@Deprecated
	public static <T extends Serializable> TypedLiteral<T> newTypedLiteral(T value, URI datatype) {
		Preconditions.checkNotNull(value,LITERAL_VALUE_CANNOT_BE_NULL);
		Preconditions.checkNotNull(datatype,LITERAL_DATATYPE_CANNOT_BE_NULL);
		return new ImmutableTypedLiteral<T>(value,datatype);
	}

	/**
	 * Create a new language literal
	 * @deprecated
	 * @param value The value for the literal
	 * @param language The language in which the value is defined
	 * @return A language literal wrapping the specified value
	 */
	@Deprecated
	public static LanguageLiteral newLanguageLiteral(String value, String language) {
		return Literals.newLanguageLiteral(value, language);
	}

	public static boolean hasLiteral(Literal<?> literal, Property property) {
		return hasLiteral(literal,property.values());
	}

	public static boolean hasLiteral(Literal<?> literal, Value... values) {
		return hasLiteral(literal,Arrays.asList(values));
	}

	public static boolean hasLiteral(Literal<?> literal, Collection<? extends Value> values) {
		return hasValue(new DataSetUtils.LiteralMatcher(literal),values);
	}

	public static boolean hasIdentifiedIndividual(Object id, Property property) {
		return hasIdentifiedIndividual(id,property.values());
	}

	public static boolean hasIdentifiedIndividual(Object id, Value... values) {
		return hasIdentifiedIndividual(id,Arrays.asList(values));
	}

	public static boolean hasIdentifiedIndividual(Object id, Collection<? extends Value> values) {
		return hasValue(new DataSetUtils.IndividualReferenceMatcher(id),values);
	}

	public static boolean hasValue(Value value, Property property) {
		return hasValue(value,property.values());
	}

	public static boolean hasValue(Value value, Value... values) {
		return hasValue(value,Arrays.asList(values));
	}

	public static boolean hasValue(Value value, final Collection<? extends Value> values) {
		final AtomicBoolean found=new AtomicBoolean(false);
		value.accept(new ValueVisitor() {
			@Override
			public void visitLiteral(Literal<?> value) {
				found.set(hasLiteral(value, values));
			}
			@Override
			public void visitIndividual(Individual<?, ?> value) {
				found.set(hasIdentifiedIndividual(value.id(), values));
			}
		});
		return found.get();
	}

	public static DataSetHelper newHelper(DataSet dataSet) {
		return new DataSetHelperImpl(dataSet);
	}

	public static IndividualHelper newHelper(Individual<?,?> individual) {
		return new IndividualHelperImpl(individual);
	}

}
