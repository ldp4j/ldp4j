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
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

final class ImmutableProperty implements Property {

	private static class PropertyMutator {

		private interface Mutator {

			ImmutableProperty tryMutate(ImmutableProperty property, Value value);

		}

		private static abstract class AbstractMutator extends ValueVisitor implements Mutator {

			private ImmutableProperty property;

			private boolean requiresContainment;

			private AbstractMutator(boolean requiresContainment) {
				this.requiresContainment = requiresContainment;
			}

			@Override
			public final ImmutableProperty tryMutate(ImmutableProperty property, Value value) {
				this.property=property;
				value.accept(this);
				return this.property;
			}

			@Override
			final void visitLiteral(Literal<?> literal) {
				if(mustMutate(literal,this.property.literals)) {
					this.property=new ImmutableProperty(this.property);
					mutate(literal, this.property.literals);
				}
			}

			@Override
			final void visitEntity(Entity entity) {
				if(mustMutate(entity,this.property.entities)) {
					this.property=new ImmutableProperty(this.property);
					mutate(entity, this.property.entities);
				}
			}

			private <V> boolean mustMutate(V value, Set<V> values) {
				return values.contains(value)==this.requiresContainment;
			}

			private final <V extends Value> void mutate(V value, Set<V> values) {
				doMutate(value,values);
			}

			protected abstract <V extends Value> void doMutate(V value, Set<V> values);

		}

		private static final class ValueRemover extends AbstractMutator {

			private ValueRemover() {
				super(true);
			}

			protected <V extends Value> void doMutate(V value, Set<V> values) {
				values.remove(value);
			}

		}

		private static final class ValueAppender extends AbstractMutator {

			private ValueAppender() {
				super(false);
			}

			protected <V extends Value> void doMutate(V value, Set<V> values) {
				values.add(value);
			}

		}

		private static ImmutableProperty addValue(ImmutableProperty property, Value value) {
			return new ValueAppender().tryMutate(property,value);
		}

		private static ImmutableProperty removeValue(ImmutableProperty property, Value value) {
			return new ValueRemover().tryMutate(property,value);
		}

	}

	private static final Set<Literal<?>> EMPTY_LITERAL_SET = Sets.<Literal<?>>newHashSet();
	private static final Set<Entity> EMPTY_ENTITY_SET = Sets.<Entity>newHashSet();

	private final URI predicate;
	private final Entity owningEntity;

	private final Set<Literal<?>> literals;
	private final Set<Entity> entities;

	private ImmutableProperty(URI predicate, Entity entity, Set<Literal<?>> literals, Set<Entity> entities) {
		this.predicate=predicate;
		this.owningEntity=entity;
		this.literals=Sets.newLinkedHashSet(literals);
		this.entities=Sets.newLinkedHashSet(entities);
	}

	private ImmutableProperty(ImmutableProperty property) {
		this(property.predicate,property.owningEntity,property.literals,property.entities);
	}

	ImmutableProperty(URI predicate, Entity entity) {
		this(predicate,entity,EMPTY_LITERAL_SET,EMPTY_ENTITY_SET);
	}

	ImmutableProperty merge(ImmutableProperty source) {
		ImmutableProperty result=this;
		if(source!=this) {
	 		result=
 				new ImmutableProperty(
 					this.predicate,
 					this.owningEntity,
 					Sets.union(this.literals, source.literals),
 					Sets.union(this.entities, source.entities));
		}
		return result;
	}

	ImmutableProperty addValue(final Value value) {
		return PropertyMutator.addValue(this,value);
	}

	ImmutableProperty removeValue(final Value value) {
		return PropertyMutator.removeValue(this,value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Entity owningEntity() {
		return this.owningEntity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI predicate() {
		return this.predicate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasValues() {
		return hasLiteralValues() || hasEntityValues();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasLiteralValues() {
		return !this.literals.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasEntityValues() {
		return !this.entities.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<Literal<?>> literalValues() {
		return Collections.unmodifiableSet(this.literals);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<Entity> entityValues() {
		return Collections.unmodifiableSet(this.entities);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Value> iterator() {
		return
			Iterators.
				<Value>concat(
					literalValues().iterator(),
					entityValues().iterator());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Property that) {
		int result=this.owningEntity.identity().compareTo(that.owningEntity().identity());
		if(result==0) {
			result=this.predicate.toString().compareTo(that.predicate().toString());
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.predicate,this.owningEntity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof ImmutableProperty) {
			ImmutableProperty that=(ImmutableProperty) obj;
			result=
				Objects.equal(this.predicate,that.predicate) &&
				Objects.equal(this.owningEntity,that.owningEntity);
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
					add("owningEntity",this.owningEntity).
					toString();
	}

}