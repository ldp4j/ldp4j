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

import org.ldp4j.reflect.meta.MetaClass;
import org.ldp4j.reflect.util.TypeHierarchyTraversal.Entity;

final class RetrievalSupportFactory {

	private static abstract class AbstractRetrievalSupport<E> implements RetrievalSupport<E> {

		private final EnumSet<Entity> retrievableElements;
		private boolean retrieveObject;

		protected AbstractRetrievalSupport(EnumSet<Entity> retrievableEntities, boolean retrieveObject) {
			this.retrieveObject = retrieveObject;
			this.retrievableElements = EnumSet.copyOf(retrievableEntities);
		}

		public boolean isRetrievable(E element) {
			return element!=null &&
				((this.retrievableElements.contains(Entity.INTERFACE) && isInterface(element)) ||
				 (this.retrievableElements.contains(Entity.CLASS) && (!isObject(element) || this.retrieveObject)));
		}

		protected abstract boolean isInterface(E element);

		protected abstract boolean isObject(E element);

	}

	private static final class MetaClassRetrievalSupport extends AbstractRetrievalSupport<MetaClass<?>> {
		private MetaClassRetrievalSupport(EnumSet<Entity> retrievableEntities, boolean retrieveObject) {
			super(retrievableEntities,retrieveObject);
		}

		@Override
		protected boolean isInterface(MetaClass<?> element) {
			return element.isInterface();
		}

		@Override
		protected boolean isObject(MetaClass<?> element) {
			return element.get()==Object.class;
		}
	}

	private static final class ClassRetrievalSupport extends AbstractRetrievalSupport<Class<?>> {
		private ClassRetrievalSupport(EnumSet<Entity> retrievableEntities, boolean retrieveObject) {
			super(retrievableEntities,retrieveObject);
		}

		@Override
		protected boolean isInterface(Class<?> element) {
			return element.isInterface();
		}

		@Override
		protected boolean isObject(Class<?> element) {
			return element==Object.class;
		}

	}

	static RetrievalSupport<Class<?>> createClassSupport(EnumSet<Entity> retrievableEntities, boolean retrieveObject) {
		return new ClassRetrievalSupport(retrievableEntities,retrieveObject);
	}

	static RetrievalSupport<MetaClass<?>> createMetaclassSupport(EnumSet<Entity> retrievableEntities, boolean retrieveObject)  {
		return new MetaClassRetrievalSupport(retrievableEntities,retrieveObject);
	}
}