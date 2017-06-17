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
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ldp4j.rdf.bean.util.TypeUtils;

public class AnnotatedMethod extends AnnotatedMember<Method> implements Parameterized {

	AnnotatedMethod(AnnotatedClass<?> metaClass, Method method) {
		super(metaClass,method);
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

	public Object invoke(Object object, Object... arguments) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return get().invoke(object, arguments);
	}

	public Class<?> getReturnType() {
		return get().getReturnType();
	}
	
	public Type getGenericReturnType() {
		return get().getGenericReturnType();
	}

	public List<Class<?>> getParameterTypes() {
		return Arrays.asList(get().getParameterTypes());
	}

	public Class<?> getRawReturnType() {
		return 
			resolveType(
				this,
				getGenericReturnType(),
				getReturnType());
	}

	public List<Class<?>> getRawParameterTypes() {
		List<Class<?>> result=new ArrayList<Class<?>>();
		Class<?>[] parameters=get().getParameterTypes();
		Type[] genericParameters=get().getGenericParameterTypes();
		for(int i=0;i<parameters.length;i++) {
			result.add(
				resolveType(
					this,
					genericParameters[i], 
					parameters[i]));
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		dumpQualifiers(builder);
		dumpReturnType(builder);
		builder.append(getDeclaringClass());
		builder.append(".");
		dumpMethodTypeVariables(builder);
		builder.append(get().getName());
		builder.append("(");
		dumpSignature(builder);
		builder.append(")");
		dumpExceptions(builder);
		return builder.toString();
	}

	private void dumpReturnType(StringBuilder builder) {
		builder.append(describe(get().getReturnType(),get().getGenericReturnType(),getRawReturnType())).append(" ");
	}

	private void dumpQualifiers(StringBuilder builder) {
		builder.append(isPublic()?"public ":isPrivate()?"private ":isProtected()?"protected ":"");
		builder.append(isFinal()?"final ":isAbstract()?"abstract ":"");
		builder.append(isStatic()?"static ":"");
	}

	private void dumpMethodTypeVariables(StringBuilder builder) {
		TypeVariable<Method>[] methodTypeParameters = get().getTypeParameters();
		if(methodTypeParameters.length>0) {
			builder.append("<");
			for(int i=0;i<methodTypeParameters.length;i++) {
				if(i>0) {
					builder.append(", ");
				}
				builder.append(TypeUtils.toString(methodTypeParameters[i]));
			}
			builder.append(">");
		}
	}

	private void dumpExceptions(StringBuilder builder) {
		Class<?>[] exceptionTypes = get().getExceptionTypes();
		Type[] genericExceptionTypes = get().getGenericExceptionTypes();
		if(exceptionTypes.length>0) {
			builder.append("throws ");
			for(int i=0;i<exceptionTypes.length;i++) {
				String exception=describe(exceptionTypes[i],genericExceptionTypes[i],exceptionTypes[i]);
				if(i>0) {
					builder.append(", ");
				}
				builder.append(exception);
			}
		}
	}

	private void dumpSignature(StringBuilder builder) {
		List<Class<?>> rawParameterTypes = getRawParameterTypes();
		List<Class<?>> parameterTypes=getParameterTypes();
		List<Type> genericParameterTypes=Arrays.asList(get().getGenericParameterTypes());
		for(int i=0;i<parameterTypes.size();i++) {
			String parameter=describe(parameterTypes.get(i),genericParameterTypes.get(i),rawParameterTypes.get(i));
			if(i>0) {
				builder.append(", ");
			}
			builder.append(parameter);
		}
	}

	private String describe(Class<?> returnType, Type genericReturnType, Class<?> rawReturnType) {
		if(genericReturnType==returnType) {
			return returnType.getCanonicalName();
		} else if(genericReturnType instanceof TypeVariable<?>) {
			return ((TypeVariable<?>)genericReturnType).getName();
		} else if(genericReturnType instanceof GenericArrayType) {
			GenericArrayType array = (GenericArrayType)genericReturnType;
			if(array.getGenericComponentType() instanceof  TypeVariable<?>) {
				return ((TypeVariable<?>)array.getGenericComponentType()).getName().concat("[]");
			}
		}
		return TypeUtils.toString(genericReturnType)+" {"+rawReturnType.getCanonicalName()+"}";
	}

	@Override
	public List<String> parameterNames() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	@Override
	public TypeVariable<?> getParameter(String name) {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	@Override
	public Type getParameterArgument(String name) {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	@Override
	public Class<?> getActualParameterArgument(String name) {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

}