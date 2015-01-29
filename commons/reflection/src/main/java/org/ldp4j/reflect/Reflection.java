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
package org.ldp4j.reflect;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;

import org.ldp4j.reflect.meta.MetaClass;
import org.ldp4j.reflect.meta.MetaConstructor;
import org.ldp4j.reflect.meta.MetaField;
import org.ldp4j.reflect.meta.MetaMethod;
import org.ldp4j.reflect.model.Invocable;
import org.ldp4j.reflect.model.Parameter;
import org.ldp4j.reflect.spi.RuntimeDelegate;

public final class Reflection {

	private Reflection() {
		// Prevent instantiation
	}

	public static <T> MetaClass<T> of(Class<T> clazz) {
		return newMetaClass(clazz,clazz);
	}

	public static MetaClass<?> ofReturnType(Method method) {
		return
			Reflection.
				newMetaClass(
					method.getReturnType(),
					method.getGenericReturnType());
	}

	public static MetaField<?> of(Field field) {
		return
			RuntimeDelegate.
				getInstance().
					getMetaModelFactory().
						newMetaField(
							Reflection.of(field.getDeclaringClass()),
							field);
	}

	public static MetaMethod<?,?> of(Method method) {
		return
			RuntimeDelegate.
				getInstance().
					getMetaModelFactory().
						newMetaMethod(
							Reflection.of(method.getDeclaringClass()),
							method,
							Reflection.ofReturnType(method));
	}

	public static <T> MetaConstructor<T> of(Constructor<T> constructor) {
		return
			RuntimeDelegate.
				getInstance().
					getMetaModelFactory().
						newMetaConstructor(
							Reflection.of(constructor.getDeclaringClass()),
							constructor);
	}

	public static <D extends AccessibleObject & Member & GenericDeclaration> Parameter<Invocable> parameterOf(D invocable, int position) {
		checkNotNull(invocable,"Invocable cannot be null");
		checkArgument(0<=position,"Parameter position cannot be null");
		Invocable declaration=null;
		if(invocable instanceof Method) {
			declaration=RuntimeDelegate.getInstance().getModelFactory().newInvocable((Method)invocable);
		} else if(invocable instanceof Constructor<?>) {
			declaration=RuntimeDelegate.getInstance().getModelFactory().newInvocable((Constructor<?>)invocable);
		} else {
			checkArgument(false,"Unsupported invocable type %s",Types.toString(invocable.getClass()));
		}
		List<Parameter<Invocable>> parameters = declaration.getParameters();
		checkArgument(parameters.size()>position);
		return parameters.get(position);
	}

	private static <T> MetaClass<T> newMetaClass(Class<T> rawType, Type type) {
		return RuntimeDelegate.getInstance().getMetaModelFactory().newMetaClass(rawType,type);
	}

	public static <D extends AccessibleObject & Member & GenericDeclaration> ParameterBuilder<D> parameterOf(D invocable) {
		checkNotNull(invocable,"Invocable cannot be null");
		return new ParameterBuilder<D>(invocable);
	}

	public static final class ParameterBuilder<D extends AccessibleObject & Member & GenericDeclaration> {

		private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

		private final D invocable;
		private Integer position;
		private Class<?> rawType;
		private Type type;
		private Annotation[] annotations=EMPTY_ANNOTATION_ARRAY;

		private ParameterBuilder(D invocable) {
			this.invocable = invocable;
		}

		public ParameterBuilder<D> withPosition(int position) {
			checkArgument(position>=0);
			this.position=position;
			return this;
		}

		public ParameterBuilder<D> withRawType(Class<?> rawType) {
			this.rawType = rawType;
			return this;
		}

		public ParameterBuilder<D> withType(Type type) {
			this.type = type;
			return this;
		}

		public ParameterBuilder<D> withAnnotations(Annotation... annotations) {
			if(annotations!=null && annotations.length==0) {
				this.annotations=Arrays.copyOf(annotations,annotations.length);
			}
			return this;
		}

		public Parameter<D> build() {
			return
				RuntimeDelegate.getInstance().getModelFactory().newParameter(
					invocable,
					checkNotNull(position,"No parameter position specified"),
					checkNotNull(rawType,"No parameter raw type specified"),
					checkNotNull(type,"No parameter type specified"),
					annotations
				);
		}

	}

	public static Class<?> erasureOf(Type type) {
		if(type instanceof Class<?>) {
			return (Class<?>)type;
		} else if(type instanceof ParameterizedType) {
			ParameterizedType cType=(ParameterizedType)type;
			return erasureOf(cType.getRawType());
		} else if(type instanceof GenericArrayType) {
			GenericArrayType cType=(GenericArrayType)type;
			return erasureOf(cType.getGenericComponentType());
		} else if(type instanceof WildcardType) {
			WildcardType cType=(WildcardType)type;
			return erasureOf(cType.getLowerBounds()[0]);
		} else if(type instanceof TypeVariable<?>) {
			TypeVariable<?> cType=(TypeVariable<?>)type;
			return erasureOf(cType.getBounds()[0]);
		} else {
			throw new IllegalArgumentException("Unknown type '"+type.getClass().getCanonicalName()+"'");
		}
	}

}
