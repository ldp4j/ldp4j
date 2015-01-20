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
package org.ldp4j.reflect.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.ldp4j.reflect.meta.MetaClass;
import org.ldp4j.reflect.meta.MetaConstructor;
import org.ldp4j.reflect.meta.MetaField;
import org.ldp4j.reflect.meta.MetaMethod;
import org.ldp4j.reflect.spi.MetaModelFactory;

public final class ImmutableMetaModelFactory implements MetaModelFactory {

	public static ImmutableMetaModelFactory getInstance() {
		return new ImmutableMetaModelFactory();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> MetaClass<T> newMetaClass(Class<T> rawType, Type type) {
		return ImmutableMetaClass.of(rawType, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T,R> MetaMethod<T,R> newMetaMethod(MetaClass<T> declaringClass, Method method, MetaClass<R> type) {
		return ImmutableMetaMethod.of(declaringClass, method, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> MetaConstructor<T> newMetaConstructor(MetaClass<T> declaringClass, Constructor<T> constructor) {
		return ImmutableMetaConstructor.of(declaringClass,constructor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> MetaField<T> newMetaField(MetaClass<T> declaringClass, Field field) {
		return ImmutableMetaField.of(declaringClass, field);
	}

}