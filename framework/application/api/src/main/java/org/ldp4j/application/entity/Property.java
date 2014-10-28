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
package org.ldp4j.application.entity;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public final class Property implements Iterable<Value> {

	private abstract class Operation<T> {

		private final Property property;

		private Operation() {
			this.property = Property.this;
		}

		protected final Property property() {
			return this.property;
		}

		final T synchronize() {
			enqueOperation();
			try {
				return operate(this.property);
			} finally {
				dequeOperation();
			}
		}

		private void enqueOperation() {
			Object lock = operationUnblock();
			synchronized(lock) {
				while(!canExecute()) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						// Nothing to do but retrieve
					}
				}
			}
		}

		private boolean canExecute() {
			boolean result=blockingOperations().get()==0;
			if(result) {
				executingOperations().incrementAndGet();
			}
			return result;
		}

		private void dequeOperation() {
			Object lock = operationCompletion();
			synchronized(lock) {
				executingOperations().decrementAndGet();
				lock.notifyAll();
			}
		}

		protected abstract AtomicLong blockingOperations();

		protected abstract AtomicLong executingOperations();

		protected abstract Object operationUnblock();

		protected abstract Object operationCompletion();

		protected abstract T operate(Property property);
	}

	private abstract class Modification extends Operation<Void> {

		private Modification() {
		}

		@Override
		protected AtomicLong executingOperations() {
			return property().runningModifications;
		}

		@Override
		protected AtomicLong blockingOperations() {
			return property().runningRetrievals;
		}

		@Override
		protected Object operationCompletion() {
			return property().modificationCompleted;
		}

		@Override
		protected Object operationUnblock() {
			return property().retrievalCompleted;
		}

		@Override
		protected Void operate(Property property) {
			execute(property);
			return null;
		}

		protected abstract void execute(Property property);

	}

	private abstract class Retrieval<S> extends Operation<S> {

		private Retrieval() {
		}

		@Override
		protected AtomicLong executingOperations() {
			return property().runningRetrievals;
		}

		@Override
		protected AtomicLong blockingOperations() {
			return property().runningModifications;
		}

		@Override
		protected Object operationCompletion() {
			return property().retrievalCompleted;
		}

		@Override
		protected Object operationUnblock() {
			return property().modificationCompleted;
		}

		@Override
		protected S operate(Property property) {
			return execute(property);
		}

		protected abstract S execute(Property property);

	}

	private static class PropertyIteratorBuilder {
	
		private final Iterator<Entity> entities;
		private final Iterator<Literal<?>> literals;
	
		private PropertyIteratorBuilder(Iterator<Literal<?>> literals, Iterator<Entity> entities) {
			this.literals = literals;
			this.entities = entities;
		}
	
		Iterator<Value> build() {
			return
				ImmutableList.
					<Value>builder().
						addAll(literals).
						addAll(entities).
							build().
								iterator();
		}
	
	}

	private final URI predicate;
	private final Entity entity;

	private final Object modificationCompleted;
	private final Object retrievalCompleted;
	private final AtomicLong runningModifications;
	private final AtomicLong runningRetrievals;

	private final CopyOnWriteArrayList<Literal<?>> literals;
	private final CopyOnWriteArrayList<Entity> entities;
	private final AtomicLong numberOfValues;

	private Property(URI predicate, Entity entity, List<Literal<?>> literals, List<Entity> entities) {
		this.predicate=predicate;
		this.entity=entity;
		this.literals=Lists.newCopyOnWriteArrayList(literals);
		this.entities=Lists.newCopyOnWriteArrayList(entities);
		this.numberOfValues=new AtomicLong();
		this.modificationCompleted=new Object();
		this.retrievalCompleted=new Object();
		this.runningModifications=new AtomicLong();
		this.runningRetrievals=new AtomicLong();
	}

	Property(Property property) {
		this(property.predicate,property.entity,property.literals,property.entities);
	}

	Property(URI predicate, Entity entity) {
		this(predicate,entity,Lists.<Literal<?>>newArrayList(),Lists.<Entity>newArrayList());
	}

	private void modify(Modification modification) {
		modification.synchronize();
	}

	private <S> S retrieve(Retrieval<S> retrieval) {
		return retrieval.synchronize();
	}

	void addValue(final Literal<?> value) {
		modify(
			new Modification() {
				@Override
				protected void execute(Property property) {
					if(property.literals.addIfAbsent(value)) {
						property.numberOfValues.incrementAndGet();
					}
				}
			}
		);
	}

	void addValue(final Entity value) {
		modify(
			new Modification() {
				@Override
				protected void execute(Property property) {
					if(property.entities.addIfAbsent(value)) {
						property.numberOfValues.incrementAndGet();
					}
				}
			}
		);
	}

	void removeValue(final Literal<?> value) {
		modify(
			new Modification() {
				@Override
				protected void execute(Property property) {
					if(property.literals.remove(value)) {
						property.numberOfValues.decrementAndGet();
					}
				}
			}
		);
	}

	void removeValue(final Entity value) {
		modify(
			new Modification() {
				@Override
				protected void execute(Property property) {
					if(property.entities.remove(value)) {
						property.numberOfValues.decrementAndGet();
					}
				}
			}
		);
	}

	public Entity entity() {
		return this.entity;
	}

	public URI predicate() {
		return this.predicate;
	}

	public boolean hasValues() {
		return retrieve(
			new Retrieval<Boolean>() {
				@Override
				protected Boolean execute(Property property) {
					return property.numberOfValues.get()>0;
				}

			}
		);
	}

	public boolean hasLiteralValues() {
		return !this.literals.isEmpty();
	}

	public boolean hasEntityValues() {
		return !this.entities.isEmpty();
	}

	public Iterable<Literal<?>> literalValues() {
		return ImmutableList.copyOf(this.literals.iterator());
	}

	public Iterable<Entity> entityValues() {
		return ImmutableList.copyOf(this.entities.iterator());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.predicate,this.entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof Property) {
			Property that=(Property) obj;
			result=
				Objects.equal(this.predicate,that.predicate) &&
				Objects.equal(this.entity,that.entity);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					add("predicate",this.predicate).
					add("entity",this.entity).
					add("values",this.iterator()).
					toString();
	}

	@Override
	public Iterator<Value> iterator() {
		PropertyIteratorBuilder iterators=
			retrieve(
				new Retrieval<PropertyIteratorBuilder>() {
					@Override
					protected PropertyIteratorBuilder execute(Property property) {
						return
							new PropertyIteratorBuilder(
								property.literals.iterator(),
								property.entities.iterator()
							);
					}
				}
			);
		return iterators.build();
	}

}