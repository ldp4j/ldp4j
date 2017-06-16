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
package org.ldp4j.application.data;

import java.util.Arrays;
import java.util.Collection;

public final class DataSetUtils {

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

	public static boolean hasLiteral(Literal<?> literal, Value... values) {
		return hasLiteral(literal,Arrays.asList(values));
	}

	public static boolean hasLiteral(Literal<?> literal, Collection<? extends Value> values) {
		return hasValue(new LiteralMatcher(literal),values);
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
		final class ValueFinder implements ValueVisitor {

			private boolean found=false;

			@Override
			public void visitLiteral(Literal<?> value) {
				this.found=hasLiteral(value, values);
			}

			@Override
			public void visitIndividual(Individual<?, ?> value) {
				this.found=hasIdentifiedIndividual(value.id(), values);
			}
		}
		ValueFinder finder=new ValueFinder();
		value.accept(finder);
		return finder.found;
	}

	public static DataSetHelper newHelper(DataSet dataSet) {
		return new DataSetHelperImpl(dataSet);
	}

	public static IndividualHelper newHelper(Individual<?,?> individual) {
		return new IndividualHelperImpl(individual);
	}

}
