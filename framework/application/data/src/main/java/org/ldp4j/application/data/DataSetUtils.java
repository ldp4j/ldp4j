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

public final class DataSetUtils {

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

	private DataSetUtils() {
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
	
	public static <T> Literal<T> newLiteral(T value) {
		return new ImmutableLiteral<T>(value);
	}
}
