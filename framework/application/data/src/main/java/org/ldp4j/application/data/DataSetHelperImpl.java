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

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import com.google.common.collect.Lists;

final class DataSetHelperImpl extends DataSetHelper {

	private final class IndividualRenamer implements TripleConsumer {

		private final Individual<?, ?> from;
		private final Individual<?, ?> to;

		private IndividualRenamer(final Individual<?, ?> from, final Individual<?, ?> to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public void consume(final Individual<?, ?> subject, final URI predicate, final Literal<?> object) {
			if(subject==this.from) {
				this.to.addValue(predicate,object);
			}
		}

		@Override
		public void consume(final Individual<?, ?> subject, final URI predicate, final Individual<?, ?> object) {
			if(subject==this.from) {
				Value value=object;
				if(value==this.from) {
					value=this.to;
				}
				this.to.addValue(predicate,value);
			} else if(subject!=this.to && object==this.from) {
				subject.removeValue(predicate, object);
				subject.addValue(predicate,this.to);
			}
		}
	}

	private static class DataSetIterator{

		private static final class TripleConsumerAdapter implements ValueVisitor {
			private final Individual<?, ?> individual;
			private final DataSetHelperImpl.TripleConsumer consumer;
			private final URI predicate;

			private TripleConsumerAdapter(final Property property, final Individual<?, ?> individual, final DataSetHelperImpl.TripleConsumer consumer) {
				this.predicate  = property.predicate();
				this.individual = individual;
				this.consumer   = consumer;
			}

			@Override
			public void visitLiteral(final Literal<?> value) {
				this.consumer.consume(this.individual,this.predicate,value);
			}

			@Override
			public void visitIndividual(final Individual<?, ?> value) {
				this.consumer.consume(this.individual,this.predicate,value);
			}

		}

		private final DataSet dataSet;

		private DataSetIterator(final DataSet dataSet) {
			this.dataSet = dataSet;
		}

		void iterate(final DataSetHelperImpl.TripleConsumer processor) {
			for(final Individual<?,?> individual:this.dataSet) {
				for(final Property property:individual) {
					final DataSetIterator.TripleConsumerAdapter visitor = new TripleConsumerAdapter(property,individual, processor);
					for(final Value value:property) {
						value.accept(visitor);
					}
				}
			}
		}

	}

	private static final class NewIndividualIdCollector implements IndividualVisitor {

		private final List<URI> newIds=Lists.newArrayList();
		private final ManagedIndividualId id;

		private boolean hasSelf=false;
		private boolean idInUse=false;

		private NewIndividualIdCollector(final ManagedIndividualId id) {
			this.id = id;
		}

		@Override
		public void visitManagedIndividual(final ManagedIndividual individual) {
			this.idInUse=this.id.equals(individual.id());
		}

		@Override
		public void visitRelativeIndividual(final RelativeIndividual individual) {
			// Nothing to do
		}

		@Override
		public void visitLocalIndividual(final LocalIndividual individual) {
			// Nothing to do
		}

		@Override
		public void visitExternalIndividual(final ExternalIndividual individual) {
			// Nothing to do
		}

		@Override
		public void visitNewIndividual(final NewIndividual individual) {
			final URI path=individual.path();
			this.hasSelf=this.hasSelf || path.equals(SELF);
			this.newIds.add(path);
		}

		List<URI> getCollectedIds() throws DataSetModificationException {
			if(!this.hasSelf) {
				throw new DataSetModificationException("No default new individual defined");
			}
			return this.newIds;
		}

		void collect(final Individual<?, ?> individual) throws DataSetModificationException {
			individual.accept(this);
			if(this.idInUse) {
				throw new DataSetModificationException("The data set already has an individual identified as '"+this.id+"'");
			}
		}

	}

	private final DataSet dataSet;

	DataSetHelperImpl(final DataSet dataSet) {
		this.dataSet = dataSet;
	}

	private void rename(final Individual<?,?> from, final Individual<?,?> to) {
		final DataSetIterator iterator = new DataSetIterator(this.dataSet);
		final TripleConsumer consumer = new IndividualRenamer(from, to);
		iterator.iterate(consumer);
		this.dataSet.remove(from);
	}

	private List<URI> getNewIds(final ManagedIndividualId id) throws DataSetModificationException {
		final NewIndividualIdCollector collector=new NewIndividualIdCollector(id);
		for(final Individual<?,?> individual:this.dataSet) {
			collector.collect(individual);
		}
		return collector.getCollectedIds();
	}

	@Override
	public <T extends Serializable, S extends Individual<T, S>>  S replace(final Serializable from, final T to, final Class<? extends S> clazz) {
		S target=null;
		final Individual<?, ?> src=this.dataSet.individualOfId(from);
		if(src!=null) {
			target=this.dataSet.individual(to, clazz);
			rename(src,target);
		}
		return target;

	}

	@Override
	public ManagedIndividual manage(final ManagedIndividualId id) throws DataSetModificationException {
		final List<URI> newIds=getNewIds(id);
		newIds.remove(SELF);
		final ManagedIndividual self=replace(SELF,id,ManagedIndividual.class);
		for(final URI newId:newIds) {
			final RelativeIndividualId relativeId=RelativeIndividualId.createId(id, newId);
			replace(newId,relativeId,RelativeIndividual.class);
		}
		return self;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Individual<URI,T>> T self() {
		return (T)relative(SELF);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Individual<URI,T>> T relative(final URI path) {
		return (T)this.dataSet.individualOfId(path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualHelper managedIndividual(final Name<?> name, final String managerId) {
		final ManagedIndividualId individualId = ManagedIndividualId.createId(name,managerId);
		final ManagedIndividual individual = this.dataSet.individual(individualId, ManagedIndividual.class);
		return new IndividualHelperImpl(individual);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualHelper relativeIndividual(final Name<?> name, final String managerId, final URI path) {
		final ManagedIndividualId parentId = ManagedIndividualId.createId(name,managerId);
		final RelativeIndividualId individualId=RelativeIndividualId.createId(parentId, path);
		final RelativeIndividual individual = this.dataSet.individual(individualId, RelativeIndividual.class);
		return new IndividualHelperImpl(individual);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualHelper localIndividual(final Name<?> name) {
		@SuppressWarnings("rawtypes")
		final
		LocalIndividual individual = this.dataSet.individual((Name)name, LocalIndividual.class);
		return new IndividualHelperImpl(individual);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualHelper externalIndividual(final URI uri) {
		Individual<?, ?> individual=null;
		if(uri.isAbsolute()) {
			individual=this.dataSet.individual(uri, ExternalIndividual.class);
		} else {
			individual=this.dataSet.individual(uri, NewIndividual.class);
		}
		return new IndividualHelperImpl(individual);
	}

}