/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014-2016 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-bean:0.2.2
 *   Bundle      : rmf-bean-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.bean.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;


public class AnnotatedField extends AnnotatedMember<Field> {

	AnnotatedField(AnnotatedClass<?> metaClass, Field field) {
		super(metaClass,field);
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return get().isAnnotationPresent(annotationClass);
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return get().getAnnotation(annotationClass);
	}

	@Override
	public Annotation[] getAnnotations() {
		return get().getAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return get().getDeclaredAnnotations();
	}

	public Class<?> getType() {
		return get().getType();
	}

	public Type getGenericType() {
		return get().getGenericType();
	}

	public Class<?> getRawType() {
		return resolveType(getDeclaringClass(),getGenericType(),getType());
	}

	public boolean isAccessible() {
		return get().isAccessible();
	}

	public Object getValue(Object object) throws IllegalArgumentException, IllegalAccessException {
		return get().get(object);
	}

	public void setValue(Object object, Object value) throws IllegalArgumentException, IllegalAccessException {
		get().set(object, value);
	}

}