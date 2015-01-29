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

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.Lists;

public abstract class ConfigurableTypeHierarchyTraversalStrategy<E,B extends ConfigurableTypeHierarchyTraversalStrategy<E,B>> extends TypeHierarchyTraversal implements TypeHierarchyTraversalStrategy<E> {

	private static final class ConfiguredTraversalStrategy<E> implements TypeHierarchyTraversalStrategy<E> {

		private final NodeDefinition definition;
		private final IntrospectionSupport<E> introspectionSupport;
		private final SearchStrategy strategy;
		private final RetrievalSupport<E> retrievalSupport;
		private final boolean visitRepeated;

		private ConfiguredTraversalStrategy(SearchStrategy strategy, NodeDefinition definition, IntrospectionSupport<E> support, RetrievalSupport<E> retrievalSupport, boolean visitRepeated) {
			this.strategy = strategy;
			this.definition = definition;
			this.introspectionSupport = support;
			this.retrievalSupport = retrievalSupport;
			this.visitRepeated = visitRepeated;
		}

		@Override
		public Iterator<E> traverse(E target) {
			switch(this.strategy) {
				case IN_ORDER_DEPTH_FIRST:
					return new InOrderDepthFirstIterator<E>(this.definition,this.introspectionSupport,this.retrievalSupport,this.visitRepeated,target);
				case POST_ORDER_DEPTH_FIRST:
					return new PostOrderDepthFirstIterator<E>(this.definition,this.introspectionSupport,this.retrievalSupport,this.visitRepeated,target);
				case PRE_ORDER_DEPTH_FIRST:
					return new PreOrderDepthFirstIterator<E>(this.definition,this.introspectionSupport,this.retrievalSupport,this.visitRepeated,target);
				case BREADTH_FIRST:
					return defaultIterator(target);
				default:
					return defaultIterator(target);
			}
		}

		private Iterator<E> defaultIterator(E target) {
			List<E> list=Lists.newArrayList();
			list.add(target);
			return list.iterator();
		}

	}

	private static abstract class DepthFirstIterator<E> implements Iterator<E> {

		private final Stack<TraversableNode<E>> stack;
		private final Clock clock;
		private final RetrievalSupport<E> retrievalSupport;
		private final NodeDefinition definition;
		private E next;
		private final List<TraversableNode<E>> visited;
		private boolean visitRepeated;

		protected DepthFirstIterator(NodeDefinition definition, IntrospectionSupport<E> support, RetrievalSupport<E> retrievalSupport, boolean visitRepeated, E root) {
			this.definition = definition;
			this.retrievalSupport = retrievalSupport;
			this.visitRepeated = visitRepeated;
			this.stack=new Stack<TraversableNode<E>>();
			this.clock = Clock.create();
			this.stack.push(new TraversableNode<E>(root,this.clock,support));
			this.next=null;
			this.visited=Lists.newArrayList();
		}

		@Override
		public boolean hasNext() {
			while(this.next==null && !this.stack.isEmpty()) {
				this.clock.tick();
				TraversableNode<E> node = this.stack.peek();
				if(node.isCompleted()) {
					this.visited.add(this.stack.pop());
				} else {
					processNode(node);
				}
			}
			return this.next!=null;
		}

		@Override
		public final E next() {
			checkState(this.next!=null,"No more elements available");
			E result=this.next;
			this.next=null;
			return result;
		}

		@Override
		public final void remove() {
			throw new UnsupportedOperationException("Method not supported");
		}

		protected abstract void processNode(TraversableNode<E> node);

		protected final boolean isLeftTraversed(TraversableNode<E> node) {
			return node.areChildrenTraversed(this.definition.left());
		}

		@SuppressWarnings("unused")
		protected final boolean isRightTraversed(TraversableNode<E> node) {
			return node.areChildrenTraversed(this.definition.right());
		}

		protected final void traverseLeft(TraversableNode<E> node) {
			pushBranch(node,this.definition.left());
		}

		protected final void traverseRight(TraversableNode<E> node) {
			pushBranch(node,this.definition.right());
		}

		protected final void visit(TraversableNode<E> node) {
			E element=node.visit();
			if(this.retrievalSupport.isRetrievable(element)) {
				this.next=element;
			}
			System.err.printf("%s %s at %d%n",this.next==null?"Discarded":"Visited",element,this.clock.get());
		}

		private void pushBranch(TraversableNode<E> node, Relation branch) {
			System.err.printf("Enqueuing traversal of %s from %s at %d%n",branch,node.element(),this.clock.get());
			Stack<TraversableNode<E>> tmp=new Stack<TraversableNode<E>>();
			while(node.hasMoreRelatedNodes(branch)) {
				TraversableNode<E> found = node.nextRelatedNode(branch);
				if((!this.visited.contains(found) && !this.stack.contains(found)) || this.visitRepeated) {
					System.err.printf("Found %s %s from %s at %d%n",branch,found.element(),node.element(),this.clock.get());
					tmp.add(found);
				}
			}
			this.stack.addAll(tmp);
		}

	}

	private static final class InOrderDepthFirstIterator<E> extends DepthFirstIterator<E> {

		private InOrderDepthFirstIterator(NodeDefinition definition, IntrospectionSupport<E> support, RetrievalSupport<E> retrievalSupport, boolean visitRepeated, E root) {
			super(definition,support,retrievalSupport,visitRepeated,root);
		}

		@Override
		protected void processNode(TraversableNode<E> node) {
			if(!node.isVisited()) {
				visit(node);
			} else {
				traverseRight(node);
				traverseLeft(node);
			}
		}

	}

	private static final class PreOrderDepthFirstIterator<E> extends DepthFirstIterator<E> {

		private PreOrderDepthFirstIterator(NodeDefinition definition, IntrospectionSupport<E> support, RetrievalSupport<E> retrievalSupport, boolean visitRepeated, E root) {
			super(definition,support,retrievalSupport,visitRepeated,root);
		}

		@Override
		protected void processNode(TraversableNode<E> node) {
			if(!isLeftTraversed(node)) {
				traverseLeft(node);
			} else if(!node.isVisited()) {
				visit(node);
			} else {
				traverseRight(node);
			}
		}

	}

	private static final class PostOrderDepthFirstIterator<E> extends DepthFirstIterator<E> {

		private PostOrderDepthFirstIterator(NodeDefinition definition, IntrospectionSupport<E> support, RetrievalSupport<E> retrievalSupport, boolean visitRepeated, E root) {
			super(definition,support,retrievalSupport,visitRepeated,root);
		}

		@Override
		protected void processNode(TraversableNode<E> node) {
			if(node.isTraversed()) {
				visit(node);
			} else {
				traverseLeft(node);
				traverseRight(node);
			}
		}

	}

	private static final SearchStrategy DEFAULT_SEARCH_STRATEGY = SearchStrategy.IN_ORDER_DEPTH_FIRST;
	private static final Relation DEFAULT_TRAVERSE_FIRST_RELATION = Relation.EXTENDS;

	private final EnumSet<Relation> traverse;
	private final EnumSet<Entity> retrieve;

	private boolean retrieveObject;
	private Relation traverseFirst;
	private SearchStrategy searchStrategy;
	private boolean visitDuplicates;
	private B instance;

	protected ConfigurableTypeHierarchyTraversalStrategy() {
		this.traverse=EnumSet.allOf(Relation.class);
		this.retrieve=EnumSet.allOf(Entity.class);
		this.retrieveObject=false;
		this.visitDuplicates=false;
		this.traverseFirst=DEFAULT_TRAVERSE_FIRST_RELATION;
		this.searchStrategy=DEFAULT_SEARCH_STRATEGY;
		this.instance=instance();
	}

	protected abstract B instance();

	protected final NodeDefinition nodeDefinition() {
		return NodeDefinition.create(this.traverseFirst);
	}

	protected final EnumSet<Relation> traversableRelations() {
		return EnumSet.copyOf(this.traverse);
	}

	protected final EnumSet<Entity> retrievableEntities() {
		return EnumSet.copyOf(this.retrieve);
	}

	protected final boolean retrieveObject() {
		return this.retrieveObject;
	}

	protected final SearchStrategy searchStrategy() {
		return this.searchStrategy;
	}

	protected final boolean visitRepeated() {
		return this.visitDuplicates;
	}

	protected final TypeHierarchyTraversalStrategy<E> build() {
		return
			new ConfiguredTraversalStrategy<E>(
				searchStrategy(),
				nodeDefinition(),
				introspectionSupport(),
				retrievalSupport(),
				visitRepeated());
	}

	protected abstract IntrospectionSupport<E> introspectionSupport();
	protected abstract RetrievalSupport<E> retrievalSupport();

	public B include(Entity... entities) {
		this.retrieve.addAll(Lists.newArrayList(entities));
		return this.instance;
	}

	public B exclude(Entity... entities) {
		this.retrieve.removeAll(Lists.newArrayList(entities));
		return this.instance;
	}

	public B traverse(Relation... entities) {
		this.traverse.addAll(Lists.newArrayList(entities));
		return this.instance;
	}

	public B skip(Relation... entities) {
		this.traverse.removeAll(Lists.newArrayList(entities));
		return this.instance;
	}

	public B includeObject() {
		this.retrieveObject=true;
		return this.instance;
	}

	public B excludeObject() {
		this.retrieveObject=false;
		return this.instance;
	}

	public B traverseFirst(Relation relation) {
		if(relation!=null) {
			this.traverseFirst = relation;
		} else {
			this.traverseFirst = DEFAULT_TRAVERSE_FIRST_RELATION;
		}
		return this.instance;
	}

	public B searchStrategy(SearchStrategy searchStrategy) {
		if(searchStrategy!=null) {
			this.searchStrategy = searchStrategy;
		} else {
			this.searchStrategy = DEFAULT_SEARCH_STRATEGY;
		}
		return this.instance;
	}

	public B visitVisited() {
		this.visitDuplicates=true;
		return this.instance;
	}

	public B skipVisited() {
		this.visitDuplicates=false;
		return this.instance;
	}

}
