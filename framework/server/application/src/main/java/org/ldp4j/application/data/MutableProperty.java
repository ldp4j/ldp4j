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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-application:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-application-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;


final class MutableProperty implements Property {

	private final URI predicate;
	private final Individual<?,?> individual;
	private final Set<Value> values;
	private final MutableDataSet dataSet;
	
	MutableProperty(Individual<?,?> individual, MutableDataSet dataSet, URI predicate) {
		this.individual = individual;
		this.dataSet = dataSet;
		this.predicate = predicate;
		this.values=new CopyOnWriteArraySet<Value>();
	}
	
	MutableProperty(MutableProperty property) {
		this(property.individual,property.dataSet,property.predicate);
		this.values.addAll(property.values());
	}

	private MutableDataSet dataSet() {
		return this.dataSet;
	}

	@Override
	public Individual<?, ?> individual() {
		return this.individual;
	}

	@Override
	public URI predicate() {
		return this.predicate;
	}

	@Override
	public Iterator<Value> iterator() {
		return values().iterator();
	}

	@Override
	public Collection<Value> values() {
		return new ArrayList<Value>(this.values);
	}

	@Override
	public int numberOfValues() {
		return this.values.size();
	}

	@Override
	public boolean hasValues() {
		return !this.values.isEmpty();
	}

	@Override
	public void accept(ValueVisitor visitor) {
		for(Value value:values()) {
			value.accept(visitor);
		}
	}

	void addValue(Value value) {
		value.accept(
			new ValueVisitor() {
				@Override
				public void visitLiteral(Literal<?> value) {
					MutableProperty.this.values.add(value);
				}
				@Override
				public void visitIndividual(Individual<?, ?> value) {
					Individual<?,?> target=null;
					if(dataSet().hasIndividual(value.id())) {
						if(dataSet().equals(value.dataSet())) {
							target=value;
						} else {
							target=dataSet().individualOfId(value.id());
						}
					} else {
						target=addIndividualToDataset(value);
					}
					if(target!=value) {
						mergeIndividuals(value,target);
					}
					values.add(target);
				}
				private Individual<?, ?> addIndividualToDataset(Individual<?, ?> value) {
					final AtomicReference<Individual<?,?>> newIndividual=new AtomicReference<Individual<?,?>>();
					value.accept(
						new IndividualVisitor() {
							@Override
							public void visitManagedIndividual(ManagedIndividual individual) {
								newIndividual.set(dataSet().individual(individual.id(), ManagedIndividual.class));
							}
							@Override
							public void visitLocalIndividual(LocalIndividual individual) {
								newIndividual.set(dataSet().individual(individual.id(), LocalIndividual.class));
							}
							@Override
							public void visitExternalIndividual(ExternalIndividual individual) {
								newIndividual.set(dataSet().individual(individual.id(), ExternalIndividual.class));
							}
						}
					);
					return newIndividual.get();
				}
				private void mergeIndividuals(final Individual<?, ?> source, final Individual<?, ?> target) {
					for(final Property property:source.properties()) {
						for(final Value value:property) {
							target.addValue(property.predicate(), value);
						}
					}
				}
			}
		);
	}

	void removeValue(Value value) {
		this.values.remove(value);
	}

}