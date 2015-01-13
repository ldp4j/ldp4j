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
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import org.ldp4j.reflect.TypeVisitor;
import org.ldp4j.reflect.Types;
import org.ldp4j.reflect.model.Invocable;
import org.ldp4j.reflect.model.Parameter;
import org.ldp4j.reflect.spi.ModelFactory;

import static com.google.common.base.Preconditions.*;

public final class ImmutableModelFactory implements ModelFactory {

	private final class GenericTypeChecker extends TypeVisitor.TypeFunction<Boolean> {

		private final Class<?> rawType;

		private GenericTypeChecker(Boolean defaultResult, Class<?> rawType) {
			super(defaultResult);
			this.rawType = rawType;
		}

		@Override
		protected <S, E extends Exception> Boolean visitClass(Class<S> t, E exception) throws E {
			return this.rawType==t;
		}

		@Override
		protected <E extends Exception> Boolean visitParameterizedType(ParameterizedType t, E exception) throws E {
			return this.rawType==t.getRawType();
		}

		@Override
		protected <E extends Exception> Boolean visitGenericArrayType(GenericArrayType t, E exception) throws E {
			return
				!this.rawType.isArray() ||
				new GenericTypeChecker(getDefaultResult(),this.rawType.getComponentType()).
					apply(t.getGenericComponentType());
		}

		@Override
		protected <D extends GenericDeclaration, E extends Exception> Boolean visitTypeVariable(TypeVariable<D> t, E exception) throws E {
			throw new IllegalStateException("Case not covered yet: can't determine if "+this.rawType.getCanonicalName()+" is compatible with "+Types.toString(t));
		}

		@Override
		protected <E extends Exception> Boolean visitWildcardType(WildcardType t, E exception) throws E {
			throw new IllegalStateException("Case not covered yet: can't determine if "+this.rawType.getCanonicalName()+" is compatible with "+Types.toString(t));
		}
	}

	public static ImmutableModelFactory getInstance() {
		return new ImmutableModelFactory();
	}

	private void validate(Class<?>[] parameterTypes, int position, final Class<?> rawType, Type type) {
		checkArgument(0<=position && position<parameterTypes.length,"Invalid parameter position");
		checkArgument(parameterTypes[position]==rawType,"Invalid parameter type");
		checkArgument(new GenericTypeChecker(false, rawType).apply(type),"Incompatible generic parameter type");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <D extends AccessibleObject & Member & GenericDeclaration> Parameter<D> newParameter(D invocable, int position, Class<?> rawType, Type type, Annotation... annotations) {
		if(invocable instanceof Constructor<?>) {
			Constructor<?> constructor=(Constructor<?>)invocable;
			validate(constructor.getParameterTypes(),position,rawType,type);
		} else if(invocable instanceof Method) {
			Method method=(Method)invocable;
			validate(method.getParameterTypes(),position,rawType,type);
		} else {
			throw new IllegalStateException("Unsupported invocable class '"+invocable.getClass().getCanonicalName()+"'");
		}
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
