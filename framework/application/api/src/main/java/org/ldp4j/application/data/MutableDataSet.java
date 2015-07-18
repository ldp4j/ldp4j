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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;


final class MutableDataSet implements DataSet {

	private static final String NL=System.lineSeparator();

	private final Name<?> name;
	private final Map<Serializable,Individual<?,?>> individuals;
	private final IndividualFactory factory;

	MutableDataSet(Name<?> name) {
		this.name = name;
		this.individuals=Maps.newLinkedHashMap();
		this.factory=new IndividualFactory(this);
	}

	@Override
	public Name<?> name() {
		return this.name;
	}

	@Override
	public int numberOfIndividuals() {
		return this.individuals.size();
	}

	@Override
	public boolean hasIndividuals() {
		return !this.individuals.isEmpty();
	}

	@Override
	public Collection<Individual<?, ?>> individuals() {
		return new ArrayList<Individual<?,?>>(this.individuals.values());
	}

	@Override
	public Set<Serializable> individualIds() {
		return new LinkedHashSet<Serializable>(this.individuals.keySet());
	}

	@Override
	public boolean hasIndividual(Object id) {
		return this.individuals.containsKey(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable, S extends Individual<T,S>> S individualOfId(T id) {
		return (S)this.individuals.get(id);
	}

	@Override
	public <T extends Serializable, S extends Individual<T, S>> S individual(T id, Class<? extends S> clazz) {
		Individual<T,?> result=individualOfId(id);
		if(result==null) {
			result=factory.newIndividual(clazz,id);
			this.individuals.put(id,result);
		} else if (!clazz.isInstance(result)) {
			throw new IllegalStateException("Existing individual '"+id+"' is not compatible with '"+clazz.getCanonicalName()+"' ("+result.getClass().getCanonicalName()+")");
		}
		return clazz.cast(result);
	}

	@Override
	public Iterator<Individual<?, ?>> iterator() {
		return individuals().iterator();
	}

	@Override
	public boolean isEmpty() {
		for(Individual<?,?> individual:this) {
			for(Property property:individual) {
				if(property.numberOfValues()>0) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder builder=new StringBuilder();
		builder.append("DataSet(").append(FormatUtils.formatName(name)).append(") {").append(NL);
		for(Individual<?,?> individual:this) {
			if(individual.hasProperties()) {
				builder.append("\t").append("- Individual(").append(FormatUtils.formatIndividualId(individual)).append(") {").append(NL);
				for(Property property:individual) {
					builder.append("\t").append("\t").append("+ Property(").append(property.predicate()).append(") {").append(NL);
					for(Value value:property) {
						value.accept(
							new ValueVisitor() {
								@Override
								public void visitLiteral(Literal<?> value) {
									builder.append("\t").append("\t").append("\t").append("* ").append(FormatUtils.formatLiteral(value)).append(NL);
								}
								@Override
								public void visitIndividual(Individual<?, ?> value) {
									builder.append("\t").append("\t").append("\t").append("* ").append(FormatUtils.formatIndividualId(value)).append(NL);
								}
							}
						);
					}
					builder.append("\t").append("\t").append("}").append(NL);
				}
			builder.append("\t").append("}").append(NL);
			}
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public void remove(final Individual<?, ?> src) {
		if(this==src.dataSet()) {
			for(final Individual<?,?> individual:this) {
				for(final Property property:individual) {
					for(Value value:property) {
						value.accept(
							new ValueVisitor() {
								@Override
								public void visitLiteral(Literal<?> value) {
									// Nothing to do
								}
								@Override
								public void visitIndividual(Individual<?, ?> value) {
									if(value==src) {
										individual.removeValue(property.predicate(), value);
									}
								}
							}
						);
					}
				}
				this.individuals.remove(src.id());
			}
		}
	}

}