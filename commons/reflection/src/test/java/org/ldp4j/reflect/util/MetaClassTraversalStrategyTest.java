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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ldp4j.reflect.Reflection;
import org.ldp4j.reflect.Types;
import org.ldp4j.reflect.harness.Childclass;
import org.ldp4j.reflect.impl.ImmutableRuntimeDelegate;
import org.ldp4j.reflect.meta.MetaClass;
import org.ldp4j.reflect.spi.RuntimeDelegate;
import org.ldp4j.reflect.util.TypeHierarchyTraversal.Relation;
import org.ldp4j.reflect.util.TypeHierarchyTraversal.SearchStrategy;

import com.google.common.collect.Sets;


public class MetaClassTraversalStrategyTest {

	@Before
	public void setUp() {
		RuntimeDelegate.setInstance(new ImmutableRuntimeDelegate());
	}

	@After
	public void tearDown() {
		RuntimeDelegate.setInstance(null);
	}

	@Test
	public void testClassTraversalAlternatives() throws Exception {
		MetaClassHierarchyIterator iterator = MetaClassHierarchyIterator.of(Childclass.class);
		System.out.println("Hierarchy iteration...");
		while(iterator.hasNext()) {
			MetaClass<?> next=iterator.next();
			System.out.println(Types.toString(next.getType()));
		}
		System.out.println("Strategy traversal...");
		MetaClassTraversalStrategy strategy =
			MetaClassTraversalStrategy.
				create().
					traverseFirst(Relation.EXTENDS).
					includeObject().
					searchStrategy(SearchStrategy.PRE_ORDER_DEPTH_FIRST);
		Iterator<MetaClass<?>> it2 = strategy.traverse(Reflection.of(Childclass.class));
		while(it2.hasNext()) {
			MetaClass<?> next=it2.next();
			System.out.println(Types.toString(next.getType()));
		}
		System.out.println("Manual traversal...");
		Queue<MetaClass<?>> queue=new LinkedList<MetaClass<?>>();
		queue.offer(Reflection.of(Childclass.class));
		Set<MetaClass<?>> visited=Sets.newLinkedHashSet();
		while(!queue.isEmpty()) {
			MetaClass<?> next=queue.poll();
			if(!visited.contains(next)) {
				visited.add(next);
				System.out.println(Types.toString(next.getType()));
				MetaClass<?> superclass = next.getSuperclass();
				if(superclass!=null) {
					queue.offer(superclass);
				}
				queue.addAll(next.getInterfaces());
			}
		}

	}

}
