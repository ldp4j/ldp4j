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

public final class ClassTraversalStrategy
	extends ConfigurableTypeHierarchyTraversalStrategy<Class<?>,ClassTraversalStrategy> {

	private ClassTraversalStrategy() {
		super();
	}

	@Override
	protected IntrospectionSupport<Class<?>> introspectionSupport() {
		return IntrospectionSupportFactory.createClassSupport(traversableRelations());
	}

	@Override
	protected RetrievalSupport<Class<?>> retrievalSupport() {
		return RetrievalSupportFactory.createClassSupport(retrievableEntities(),retrieveObject());
	}

	@Override
	protected ClassTraversalStrategy instance() {
		return this;
	}

	@Override
	public Iterator<Class<?>> traverse(Class<?> target) {
		return build().traverse(target);
	}

	public static ClassTraversalStrategy create() {
		return new ClassTraversalStrategy();
	}

}