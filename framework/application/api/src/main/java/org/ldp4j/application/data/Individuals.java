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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.net.URI;

public final class Individuals {

	private static class ValueReplicator implements ValueVisitor {

		private final IndividualFinder finder;
		private Value result=null;

		ValueReplicator(DataSet dataSet) {
			this.finder=new IndividualFinder(dataSet);
		}

		Value replicate(Value value) {
			value.accept(this);
			return this.result;
		}

		@Override
		public void visitLiteral(Literal<?> value) {
			this.result=value;
		}

		@Override
		public void visitIndividual(Individual<?,?> value) {
			this.result=this.finder.findOrCreate(value);
		}

	}

	private Individuals() {
	}

	public static void merge(Individual<?,?> source, Individual<?,?> target) {
		ValueReplicator replicator=new ValueReplicator(target.dataSet());
		for(Property property:source.properties()) {
			for(Value value:property) {
				target.addValue(property.predicate(),replicator.replicate(value));
			}
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
				public void visitIndividual(Individual<?,?> value) {
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



}
