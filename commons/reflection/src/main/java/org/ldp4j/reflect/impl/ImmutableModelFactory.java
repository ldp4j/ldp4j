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

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.ldp4j.reflect.model.Invocable;
import org.ldp4j.reflect.model.Parameter;
import org.ldp4j.reflect.spi.ModelFactory;

public final class ImmutableModelFactory implements ModelFactory {

	public static ImmutableModelFactory getInstance() {
		return new ImmutableModelFactory();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <D extends AccessibleObject & Member & GenericDeclaration> Parameter<D> newParameter(D invocable, int position, Class<?> rawType, Type type, Annotation... annotations) {
		return ImmutableParameter.of(invocable, position, rawType, type, annotations);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Invocable newInvocable(Constructor<T> constructor) {
		return ImmutableInvocableConstructor.of(constructor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Invocable newInvocable(Method method) {
		return ImmutableInvocableMethod.of(method);
	}

}
