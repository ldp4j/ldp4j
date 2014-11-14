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

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.ValueVisitor;

import com.google.common.collect.Lists;

public final class DataSetHelper {

	public static final URI SELF = URI.create("");

	public static interface TripleProcessor<T> extends DataSetHelper.TripleConsumer {

		T getResult();

	}

	public static interface TripleConsumer {

		void consume(Individual<?,?> subject, URI predicate, Literal<?> object);

		void consume(Individual<?,?> subject, URI predicate, Individual<?,?> object);

	}

	private static class DataSetIterator{

		private static final class TripleConsumerAdapter implements ValueVisitor {
			private final Individual<?, ?> individual;
			private final DataSetHelper.TripleConsumer consumer;
			private final URI predicate;

			private TripleConsumerAdapter(Property property, Individual<?, ?> individual, DataSetHelper.TripleConsumer consumer) {
				this.predicate  = property.predicate();
				this.individual = individual;
				this.consumer   = consumer;
			}

			@Override
			public void visitLiteral(Literal<?> value) {
				this.consumer.consume(this.individual,this.predicate,value);
			}

			@Override
			public void visitIndividual(Individual<?, ?> value) {
				this.consumer.consume(this.individual,this.predicate,value);
			}

		}

		private final DataSet dataSet;

		private DataSetIterator(DataSet dataSet) {
			this.dataSet = dataSet;
		}

		void iterate(DataSetHelper.TripleConsumer processor) {
			for(Individual<?,?> individual:this.dataSet) {
				for(Property property:individual) {
					DataSetIterator.TripleConsumerAdapter visitor = new TripleConsumerAdapter(property,individual, processor);
					for(Value value:property) {
						value.accept(visitor);
					}
				}
			}
		}

	}

	public static Individual<?, ?> to;

	private final DataSet dataSet;

	private DataSetHelper(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	private void rename(final Individual<?,?> from, final Individual<?,?> to) {
		DataSetHelper.DataSetIterator iterator = new DataSetIterator(this.dataSet);
		DataSetHelper.TripleConsumer consumer = new TripleConsumer(){
			@Override
			public void consume(Individual<?, ?> subject, URI predicate, Literal<?> object) {
				if(subject==from) {
					to.addValue(predicate,object);
				}
			}
			@Override
			public void consume(Individual<?, ?> subject, URI predicate, Individual<?, ?> object) {
				if(subject==from) {
					Value value=object;
					if(value==from) {
						value=to;
					}
					to.addValue(predicate,value);
				} else if(subject!=to && object==from) {
					subject.removeValue(predicate, object);
					subject.addValue(predicate, to);
				}
			}
		};
		iterator.iterate(consumer);
		this.dataSet.remove(from);
	}

	public <T, S extends Individual<T, S>>  S replace(Object from, T to, Class<? extends S> clazz) {
		S target=this.dataSet.individual(to, clazz);
		Individual<Object, ?> src=this.dataSet.individualOfId(from);
		if(src!=null) {
			rename(src,target);
		}
		return target;

	}

	public ManagedIndividual manage(final ManagedIndividualId id) throws DataSetModificationException {
		final AtomicBoolean idInUse=new AtomicBoolean(false);
		final AtomicBoolean hasSelf=new AtomicBoolean(false);
		final List<URI> newIds=Lists.newArrayList();
		for(Individual<?,?> individual:this.dataSet) {
			individual.accept(
				new IndividualVisitor() {
					@Override
					public void visitManagedIndividual(ManagedIndividual individual) {
						idInUse.set(true);
					}
					@Override
					public void visitRelativeIndividual(RelativeIndividual individual) {
						// Nothing to do
					}
					@Override
					public void visitLocalIndividual(LocalIndividual individual) {
						// Nothing to do
					}
					@Override
					public void visitExternalIndividual(ExternalIndividual individual) {
						// Nothing to do
					}
					@Override
					public void visitNewIndividual(NewIndividual individual) {
						URI path = individual.path();
						hasSelf.compareAndSet(false,path.equals(SELF));
						newIds.add(path);
					}
				}
			);
			if(idInUse.get()) {
				break;
			}
		}
		if(idInUse.get()) {
			throw new DataSetModificationException("The data set already has an individual identified as '"+id);
		}
		if(!hasSelf.get()) {
			throw new DataSetModificationException("No default new individual defined");
		}
		newIds.remove(SELF);
		ManagedIndividual self = replace(SELF,id,ManagedIndividual.class);
		for(URI newId:newIds) {
			RelativeIndividualId relativeId=RelativeIndividualId.createId(id, newId);
			replace(newId,relativeId,RelativeIndividual.class);
		}
		return self;
	}

	public static DataSetHelper newInstance(DataSet dataSet) {
		return new DataSetHelper(dataSet);
	}

	public Individual<?,?> self() {
		return relative(SELF);
	}

	public Individual<?,?> relative(URI path) {
		return this.dataSet.individualOfId(path);
	}

}