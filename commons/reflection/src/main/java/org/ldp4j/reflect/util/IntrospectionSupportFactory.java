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

import java.util.EnumSet;
import java.util.List;

import org.ldp4j.reflect.meta.MetaClass;
import org.ldp4j.reflect.util.TypeHierarchyTraversal.Relation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

final class IntrospectionSupportFactory {

	private static abstract class AbstractIntrospectionSupport<E> implements IntrospectionSupport<E> {

		private final EnumSet<Relation> allowedTraversals;

		protected AbstractIntrospectionSupport(EnumSet<Relation> allowedTraversals) {
			this.allowedTraversals = EnumSet.copyOf(allowedTraversals);
		}

		@Override
		public List<E> introspect(E element, Relation target) {
			Builder<E> builder = ImmutableList.<E>builder();
			if(canTraverse(target)) {
				if(target.equals(Relation.IMPLEMENTS)) {
					builder.addAll(getImplementedElements(element));
				} else {
					builder.addAll(getExtendedElements(element));
				}
			}
			return builder.build();
		}

		private boolean canTraverse(Relation traverse) {
			return this.allowedTraversals.contains(traverse);
		}

		protected final List<E> create(E... elements) {
			Builder<E> builder = ImmutableList.<E>builder();
			if(elements!=null) {
				for(E e:elements) {
					if(e!=null) {
						builder.add(elements);
					}
				}
			}
			return builder.build();
		}

		protected abstract List<E> getImplementedElements(E element);

		protected abstract List<E> getExtendedElements(E element);

	}

	private static final class MetaClassIntrospectionSupport extends IntrospectionSupportFactory.AbstractIntrospectionSupport<MetaClass<?>> {
		private MetaClassIntrospectionSupport(EnumSet<Relation> allowedTraversals) {
			super(allowedTraversals);
		}

		@Override
		protected List<MetaClass<?>> getImplementedElements(MetaClass<?> element) {
			if(element.isInterface()) {
				return create();
			} else {
				return element.getInterfaces();
			}
		}

		@Override
		protected List<MetaClass<?>> getExtendedElements(MetaClass<?> element) {
			if(element.isInterface()) {
				return element.getInterfaces();
			} else {
				return create(element.getSuperclass());
			}
		}
	}

	private static final class ClassIntrospectionSupport extends IntrospectionSupportFactory.AbstractIntrospectionSupport<Class<?>> {

		private static final Class<?>[] EMPTY_ELEMENTS = new Class<?>[0];

		private ClassIntrospectionSupport(EnumSet<Relation> allowedTraversals) {
			super(allowedTraversals);
		}

		@Override
		protected List<Class<?>> getImplementedElements(Class<?> element) {
			Class<?>[] elements=EMPTY_ELEMENTS;
			if(!element.isInterface()) {
				elements=element.getInterfaces();
			}
			return create(elements);
		}

		@Override
		protected List<Class<?>> getExtendedElements(Class<?> element) {
			Class<?>[] elements=EMPTY_ELEMENTS;
			if(element.isInterface()) {
				elements=element.getInterfaces();
			} else {
				elements=new Class<?>[]{element.getSuperclass()};
			}
			return create(elements);
		}
	}

	static IntrospectionSupport<Class<?>> createClassSupport(EnumSet<Relation> traversableRelations) {
		return new ClassIntrospectionSupport(traversableRelations);
	}

	static IntrospectionSupport<MetaClass<?>> createMetaclassSupport(EnumSet<Relation> traversableRelations) {
		return new MetaClassIntrospectionSupport(traversableRelations);
	}
}