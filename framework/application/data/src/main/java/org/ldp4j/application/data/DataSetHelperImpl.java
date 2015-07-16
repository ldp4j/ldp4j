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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;

final class DataSetHelperImpl extends DataSetHelper {

	private static class DataSetIterator{

		private static final class TripleConsumerAdapter implements ValueVisitor {
			private final Individual<?, ?> individual;
			private final DataSetHelperImpl.TripleConsumer consumer;
			private final URI predicate;

			private TripleConsumerAdapter(Property property, Individual<?, ?> individual, DataSetHelperImpl.TripleConsumer consumer) {
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

		void iterate(DataSetHelperImpl.TripleConsumer processor) {
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

	private final DataSet dataSet;

	DataSetHelperImpl(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	private void rename(final Individual<?,?> from, final Individual<?,?> to) {
		DataSetHelperImpl.DataSetIterator iterator = new DataSetIterator(this.dataSet);
		DataSetHelperImpl.TripleConsumer consumer = new TripleConsumer(){
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

	@Override
	public <T extends Serializable, S extends Individual<T, S>>  S replace(Serializable from, T to, Class<? extends S> clazz) {
		S target=this.dataSet.individual(to, clazz);
		Individual<?, ?> src=this.dataSet.individualOfId(from);
		if(src!=null) {
			rename(src,target);
		}
		return target;

	}

	@Override
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

	@Override
	public <T extends Individual<URI,T>> T self() {
		return relative(SELF);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Individual<URI,T>> T relative(URI path) {
		return (T)this.dataSet.individualOfId(path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualHelper managedIndividual(Name<?> name, String managerId) {
		ManagedIndividualId individualId = ManagedIndividualId.createId(name,managerId);
		ManagedIndividual individual = this.dataSet.individual(individualId, ManagedIndividual.class);
		return new IndividualHelperImpl(individual);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualHelper relativeIndividual(Name<?> name, String managerId, URI path) {
		ManagedIndividualId parentId = ManagedIndividualId.createId(name,managerId);
		RelativeIndividualId individualId=RelativeIndividualId.createId(parentId, path);
		RelativeIndividual individual = this.dataSet.individual(individualId, RelativeIndividual.class);
		return new IndividualHelperImpl(individual);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualHelper localIndividual(Name<?> name) {
		@SuppressWarnings("rawtypes")
		LocalIndividual individual = this.dataSet.individual((Name)name, LocalIndividual.class);
		return new IndividualHelperImpl(individual);
	}

}