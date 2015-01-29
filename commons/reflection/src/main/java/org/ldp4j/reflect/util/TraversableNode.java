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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-reflection:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-reflection-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.reflect.util;

import static com.google.common.base.Preconditions.checkState;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

import org.ldp4j.reflect.util.TypeHierarchyTraversal.Relation;

import com.google.common.collect.Maps;

final class TraversableNode<E> implements Traversable {

	private final class RelationIterator implements Iterator<TraversableNode<E>> {

		private final Relation relation;
		private final Iterator<E> iterator;
		private boolean finished;

		private RelationIterator(Relation entity, List<E> nodes) {
			this.relation=entity;
			this.iterator=nodes.iterator();
			this.finished=false;
		}

		@Override
		public boolean hasNext() {
			boolean result = this.iterator.hasNext();
			if(!this.finished && !result) {
				this.finished=true;
				TraversableNode.this.traversableSupport.childrenTraversedAt(this.relation,TraversableNode.this.clock.get());
			}
			return result;
		}

		@Override
		public TraversableNode<E> next() {
			checkState(!this.finished, "No more %s related nodes available",this.relation);
			return
				new TraversableNode<E>(
					this.iterator.next(),
					TraversableNode.this.clock,
					TraversableNode.this.introspectionSupport);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Removal not supported");
		}
	}

	private final E element;
	private final IntrospectionSupport<E> introspectionSupport;
	private final Clock clock;

	private final TraversableSupport traversableSupport;

	private final EnumMap<Relation,Iterator<TraversableNode<E>>> relationIterators;

	TraversableNode(E element, Clock clock, IntrospectionSupport<E> introspectionSupport) {
		this.element = element;
		this.clock = clock;
		this.introspectionSupport = introspectionSupport;
		this.traversableSupport=new TraversableSupport();
		this.traversableSupport.discoveredAt(clock.get());
		this.relationIterators=unrollRelationIterators(element,introspectionSupport);
	}

	private EnumMap<Relation, Iterator<TraversableNode<E>>> unrollRelationIterators(E element, IntrospectionSupport<E> support) {
		EnumMap<Relation,Iterator<TraversableNode<E>>> iterators=Maps.newEnumMap(Relation.class);
		for(Relation relation:Relation.values()) {
			iterators.put(relation,new RelationIterator(relation,support.introspect(element,relation)));
		}
		return iterators;
	}

	public E element() {
		return this.element;
	}

	public E visit() {
		this.traversableSupport.visitedAt(clock.get());
		return this.element;
	}

	public boolean hasMoreRelatedNodes(Relation relation) {
		return this.relationIterators.get(relation).hasNext();
	}

	public TraversableNode<E> nextRelatedNode(Relation relation) {
		checkState(hasMoreRelatedNodes(relation),"No more %s related nodes available",relation);
		return this.relationIterators.get(relation).next();
	}

	@Override
	public boolean isDiscovered() {
		return this.traversableSupport.isDiscovered();
	}

	@Override
	public boolean isVisited() {
		return this.traversableSupport.isVisited();
	}

	@Override
	public boolean areChildrenTraversed(Relation entity) {
		return this.traversableSupport.areChildrenTraversed(entity);
	}

	@Override
	public boolean isTraversed() {
		return this.traversableSupport.isTraversed();
	}

	@Override
	public boolean isCompleted() {
		return this.traversableSupport.isCompleted();
	}

	@Override
	public int discoveryTimestamp() {
		return this.traversableSupport.discoveryTimestamp();
	}

	@Override
	public int traversalTimestamp() {
		return this.traversableSupport.traversalTimestamp();
	}

	@Override
	public int childrenTraversalTimestamp(Relation entity) {
		return this.traversableSupport.childrenTraversalTimestamp(entity);
	}

	@Override
	public int completionTimestamp() {
		return this.traversableSupport.completionTimestamp();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return this.element.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj==null) {
			return false;
		} else if(obj instanceof TraversableNode<?>) {
			TraversableNode<?> that=(TraversableNode<?>)obj;
			return this.element.equals(that.element);
		} else {
			return false;
		}
	}

}