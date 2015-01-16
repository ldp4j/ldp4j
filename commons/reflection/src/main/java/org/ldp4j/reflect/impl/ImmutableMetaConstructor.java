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
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.ldp4j.reflect.meta.MetaClass;
import org.ldp4j.reflect.meta.MetaConstructor;
import org.ldp4j.reflect.meta.MetaParameter;

final class ImmutableMetaConstructor<T>
	extends ImmutableMetaInvocable<T,Constructor<T>,T>
	implements MetaConstructor<T> {

	protected ImmutableMetaConstructor(MetaClass<T> declaringClass,Constructor<T> constructor) {
		super(declaringClass,constructor,declaringClass);
	}

	@Override
	public List<MetaParameter<Constructor<T>>> getParameters() {
		Constructor<T> invocable = get();
		return
			unrollParameters(
				invocable.getParameterTypes(),
				invocable.getGenericParameterTypes(),
				invocable.getParameterAnnotations());
	}

	@SuppressWarnings("unchecked") // Safe by construction
	@Override
	public List<MetaClass<? extends Throwable>> getExceptionTypes() {
		return unrollExceptions((Class<? extends Throwable>[])get().getExceptionTypes());
	}

	@Override
	public boolean isOverridable() {
		return false;
	}

	@Override
	public boolean isVarArgs() {
		return get().isVarArgs();
	}

	@Override
	public T invoke(T receiver, Object... args) throws InvocationTargetException, IllegalAccessException, IllegalArgumentException {
		try {
			return getReturnType().cast(get().newInstance(args));
		} catch (InstantiationException e) {
			throw new InvocationTargetException(e);
		}
	}

	static <T> ImmutableMetaConstructor<T> of(MetaClass<T> declaringClass, Constructor<T> data) {
		return new ImmutableMetaConstructor<T>(declaringClass,data);
	}

}