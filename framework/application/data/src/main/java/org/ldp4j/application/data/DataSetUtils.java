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

	/**
	 * Merge the individuals of the source dataset in the target dataset
	 *
	 * @deprecated
	 * @param source
	 *            The dataset which defines the individuals to be merged
	 * @param target
	 *            The dataset in which the individuals will be merged
	 */
	@Deprecated
	public static void merge(DataSet source, DataSet target) {
		DataSets.merge(source, target);

	}

	/**
	 * Merge the properties of the source individual in the target individual
	 *
	 * @deprecated
	 * @param source
	 *            The individual which defines the properties to be merged
	 * @param target
	 *            The individual in which the properties will be merged
	 */
	@Deprecated
	public static void merge(Individual<?,?> source, Individual<?,?> target) {
		Individuals.merge(source, target);
	}

	/**
	 * Remove the individuals of the source dataset from a target dataset
	 *
	 * @deprecated
	 * @param source
	 *            The dataset which defines the individuals to be removed
	 * @param target
	 *            The dataset whose individuals will be removed
	 */
	@Deprecated
	public static void remove(DataSet source, DataSet target) {
		DataSets.remove(source, target);
	}

	/**
	 * Remove the content properties of the source individual from a target
	 * individual
	 *
	 * @deprecated
	 * @param source
	 *            The individual which defines the properties to be removed
	 * @param target
	 *            The individual whose properties will be removed
	 */
	@Deprecated
	public static void remove(Individual<?,?> source, final Individual<?,?> target) {
		Individuals.remove(source, target);
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

	/**
	 * @deprecated
	 */
	@Deprecated
	public static boolean hasLiteral(Literal<?> literal, Property property) {
		return property.hasLiteralValue(literal);
	}

	public static boolean hasLiteral(Literal<?> literal, Value... values) {
		return hasLiteral(literal,Arrays.asList(values));
	}

	public static boolean hasLiteral(Literal<?> literal, Collection<? extends Value> values) {
		return hasValue(new LiteralMatcher(literal),values);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public static boolean hasIdentifiedIndividual(Object id, Property property) {
		return property.hasIdentifiedIndividual(id);
	}

	public static boolean hasIdentifiedIndividual(Object id, Value... values) {
		return hasIdentifiedIndividual(id,Arrays.asList(values));
	}

	public static boolean hasIdentifiedIndividual(Object id, Collection<? extends Value> values) {
		return hasValue(new IndividualReferenceMatcher(id),values);
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
