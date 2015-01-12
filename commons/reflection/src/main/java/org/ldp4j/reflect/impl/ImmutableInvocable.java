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

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

import org.ldp4j.reflect.model.Invocable;
import org.ldp4j.reflect.model.Parameter;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

abstract class ImmutableInvocable<D extends AccessibleObject & Member & GenericDeclaration> extends Invocable {

	private final D delegate;
	private final boolean constructor;

	ImmutableInvocable(D delegate, boolean constructor) {
		this.delegate = delegate;
		this.constructor = constructor;
	}

	protected final D delegate() {
		return this.delegate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setAccessible(boolean flag) throws SecurityException {
		this.delegate.setAccessible(flag);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isAccessible() {
		return this.delegate.isAccessible();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return this.delegate.getAnnotation(annotationClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return this.delegate.isAnnotationPresent(annotationClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Annotation[] getAnnotations() {
		return this.delegate.getAnnotations();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Annotation[] getDeclaredAnnotations() {
		return this.delegate.getDeclaredAnnotations();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Class<?> getDeclaringClass() {
		return this.delegate.getDeclaringClass();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getName() {
		return this.delegate.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getModifiers() {
		return this.delegate.getModifiers();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isSynthetic() {
		return this.delegate.isSynthetic();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final TypeVariable<?>[] getTypeParameters() {
		return this.delegate.getTypeParameters();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isMethod() {
		return !this.constructor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isConstructor() {
		return this.constructor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final <D1 extends AccessibleObject & Member & GenericDeclaration> D1 as(Class<? extends D1> clazz) {
		checkNotNull(clazz,"Class cannot be null");
		return clazz.cast(this.delegate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isOverridable() {
		return
			isMethod() &&
			!Modifier.isFinal(this.delegate.getModifiers()) &&
			!Modifier.isFinal(getModifiers()) &&
			!Modifier.isPrivate(getModifiers());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return
			Objects.
				hashCode(
					this.delegate,
					this.constructor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj==null) {
			return false;
		} else if(obj instanceof ImmutableParameter<?>) {
			ImmutableInvocable<?> that=(ImmutableInvocable<?>)obj;
			return
				Objects.equal(this.delegate,that.delegate) &&
				Objects.equal(this.constructor,that.constructor);
		} else {
			return false;
		}
	}

	protected static List<Class<? extends Throwable>> unrollExceptions(Class<? extends Throwable>[] exceptionTypes) {
		return ImmutableList.copyOf(exceptionTypes);
	}

	protected static <D extends AccessibleObject & Member & GenericDeclaration> List<Parameter<D>> unrollParameters(
			D invocable,
			Class<?>[] parameterTypes,
			Type[] genericParameterTypes,
			Annotation[][] parameterAnnotations) {
		Builder<Parameter<D>> builder=ImmutableList.builder();
		for(int i=0;i<parameterTypes.length;i++) {
			builder.add(
				ImmutableParameter.
					of(
						invocable,
						i,
						parameterTypes[i],
						genericParameterTypes[i],
						parameterAnnotations[i]
					)
				);
		}
		return builder.build();
	}

}
