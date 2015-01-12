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
import java.lang.reflect.Type;
import java.util.Map;

import org.ldp4j.reflect.model.Parameter;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

final class ImmutableParameter<D extends AccessibleObject & Member & GenericDeclaration> implements Parameter<D> {

	private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

	private final Map<Class<? extends Annotation>, Annotation> annotations;
	private final D invocable;
	private final int position;
	private final Class<?> rawType;
	private final Type type;

	private ImmutableParameter(D invocable, int position, Class<?> rawType, Type type, Annotation[] annotations) {
		this.invocable = invocable;
		this.position = position;
		this.rawType = rawType;
		this.type = type;
		this.annotations=unroll(annotations);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return getAnnotation(annotationClass)!=null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		checkNotNull(annotationClass,"Annotation cannot be null");
		return annotationClass.cast(this.annotations.get(annotationClass));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Annotation[] getAnnotations() {
		return toArray(this.annotations);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Annotation[] getDeclaredAnnotations() {
		return getAnnotations();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public D getInvocable() {
		return this.invocable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPosition() {
		return this.position;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getRawType() {
		return this.rawType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Type getType() {
		return this.type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return
			Objects.
				hashCode(
					this.invocable,
					this.position,
					this.rawType,
					this.type,
					this.annotations);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj==null) {
			return false;
		} else if(obj instanceof ImmutableParameter<?>) {
			ImmutableParameter<?> that=(ImmutableParameter<?>)obj;
			return
				Objects.equal(this.invocable,that.invocable) &&
				Objects.equal(this.position,that.position) &&
				Objects.equal(this.rawType,that.rawType) &&
				Objects.equal(this.type,that.type) &&
				Objects.equal(this.annotations,that.annotations);
		} else {
			return false;
		}
	}

	private static Annotation[] toArray(Map<Class<? extends Annotation>, Annotation> annotations) {
		return annotations.values().toArray(EMPTY_ANNOTATION_ARRAY);
	}

	private static Map<Class<? extends Annotation>, Annotation> unroll(Annotation[] annotations) {
		Builder<Class<? extends Annotation>, Annotation> builder = ImmutableMap.<Class<? extends Annotation>,Annotation>builder();
		for(Annotation annotation:annotations) {
			builder.put(annotation.annotationType(),annotation);
		}
		return builder.build();
	}

	static <D extends AccessibleObject & Member & GenericDeclaration> ImmutableParameter<D> of(D invocable, int position, Class<?> rawType, Type type, Annotation... annotations) {
		return new ImmutableParameter<D>(invocable, position, rawType, type, annotations);
	}

}