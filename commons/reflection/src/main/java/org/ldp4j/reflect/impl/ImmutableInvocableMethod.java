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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.ldp4j.reflect.model.Invocable;
import org.ldp4j.reflect.model.Parameter;

import com.google.common.collect.ImmutableList;

final class ImmutableInvocableMethod extends ImmutableInvocable<Method> {

	private ImmutableInvocableMethod(Method method) {
		super(method,false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getReturnType() {
		return delegate().getDeclaringClass();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Type getGenericReturnType() {
		return delegate().getDeclaringClass();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Parameter<Invocable>> getParameters() {
		Method delegate = delegate();
		return
			ImmutableInvocable.unrollParameters(
				(Invocable)this,
				delegate.getParameterTypes(),
				delegate.getGenericParameterTypes(),
				delegate.getParameterAnnotations());
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked") // Safe by construction according to JLS
	@Override
	public List<Class<? extends Throwable>> getExceptionTypes() {
		return
			ImmutableInvocable.
				unrollExceptions(
					(Class<? extends Throwable>[])delegate().getExceptionTypes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Type> getGenericExceptionTypes() {
		return ImmutableList.copyOf(delegate().getGenericExceptionTypes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isVarArgs() {
		return delegate().isVarArgs();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object invoke(Object receiver, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return delegate().invoke(receiver, args);
	}

	static ImmutableInvocableMethod of(Method method) {
		return new ImmutableInvocableMethod(method);
	}

}