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
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import org.ldp4j.rdf.bean.util.TypeUtils;

public class AnnotatedClass<T> implements Annotated<Class<T>>, Parameterized {

	private final Class<T> clazz;
	private final ParameterizedType genericClass;
	private final ParameterMap parameterMap;
	private final AnnotatedClass<?> context;
	private final AnnotatedClass<?> superClass;
	
	private AnnotatedClass(AnnotatedClass<?> context, Class<T> clazz, ParameterizedType genericClass, ParameterMap parameterMap) {
		this.context = context;
		this.clazz = clazz;
		this.genericClass = genericClass;
		this.parameterMap = parameterMap;
		this.superClass=superClass(this,clazz.getSuperclass(),clazz.getGenericSuperclass());
	}

	private AnnotatedClass(AnnotatedClass<?> context, Class<T> clazz, ParameterizedType genericClass) {
		this(context,clazz,genericClass,new ParameterMap(clazz.getTypeParameters(),genericClass.getActualTypeArguments(),context));
	}
	
	private AnnotatedClass(AnnotatedClass<?> context, Class<T> clazz) {
		this(context,clazz,null,new ParameterMap(clazz.getTypeParameters()));
	}

	private AnnotatedClass(Class<T> clazz) {
		this(null,clazz);
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return clazz.isAnnotationPresent(annotationClass);
	}

	@Override
	public <S extends Annotation> S getAnnotation(Class<S> annotationClass) {
		return clazz.getAnnotation(annotationClass);
	}

	@Override
	public Annotation[] getAnnotations() {
		return clazz.getAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return clazz.getDeclaredAnnotations();
	}

	@Override
	public Class<T> get() {
		return clazz;
	}

	public Type getGenericClass() {
		return genericClass;
	}
	
	public AnnotatedClass<?> getOwnerType() {
		return context;
	}

	public AnnotatedClass<?> getSuperclass() {
		return superClass;
	}

	public AnnotatedMethod getDeclaredMethod(String name, Class<?>... parameterTypes) {
		try {
			return new AnnotatedMethod(this,clazz.getDeclaredMethod(name, parameterTypes));
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	public AnnotatedField getDeclaredField(String name) {
		try {
			return new AnnotatedField(this,clazz.getDeclaredField(name));
		} catch (NoSuchFieldException e) {
			return null;
		}
	}

	public List<AnnotatedMethod> getDeclaredMethods() {
		List<AnnotatedMethod> result=new ArrayList<AnnotatedMethod>();
		for(Method method:clazz.getDeclaredMethods()) {
			result.add(new AnnotatedMethod(this, method));
		}
		return result;
	}

	public List<AnnotatedField> getDeclaredField() {
		List<AnnotatedField> result=new ArrayList<AnnotatedField>();
		for(Field field:clazz.getDeclaredFields()) {
			result.add(new AnnotatedField(this, field));
		}
		return result;
	}

	@Override
	public List<String> parameterNames() {
		return parameterMap.parameterNames();
	}

	@Override
	public TypeVariable<?> getParameter(String name) {
		return parameterMap.getParameter(name);
	}

	@Override
	public Type getParameterArgument(String name) {
		return parameterMap.getParameterArgument(name);
	}

	public String getSimpleName() {
		return get().getSimpleName();
	}

	public String getCanonicalName() {
		return get().getCanonicalName();
	}

	@Override
	public Class<?> getActualParameterArgument(String name) {
		return parameterMap.getActualParameterArgument(name);
	}

	private static <T> AnnotatedClass<T> superClass(AnnotatedClass<?> context, Class<T> clazz, Type type) {
		AnnotatedClass<T> result=null;
		if(clazz!=null && clazz!=Object.class) {
			if(type!=null && type instanceof ParameterizedType) {
				result=new AnnotatedClass<T>(context,clazz,(ParameterizedType)type);
			} else {
				result=new AnnotatedClass<T>(context,clazz);
			}
		}
		return result;
	}

	public static <T> AnnotatedClass<T> forClass(Class<T> clazz) {
		return new AnnotatedClass<T>(clazz);
	}

	@SuppressWarnings("unchecked")
	public static <T> AnnotatedClass<T> forObject(T object) {
		return forClass((Class<T>)object.getClass());
	}

	public String describe() {
		if(context==null) {
			if(superClass==null) {
				return toString(false);
			} else {
				return toString(false)+" extends "+superClass.describe();
			}
		} else {
			if(superClass==null) {
				return toString(true);
			} else {
				return toString(true)+" extends "+superClass.describe();
			}
		}
	}

	private String toString(boolean resolve) {
		StringBuilder builder = new StringBuilder();
		builder.append(clazz.getCanonicalName());
		TypeVariable<?>[] typeParameters = clazz.getTypeParameters();
		if(typeParameters.length>0) {
			builder.append("<");
			for(int i=0;i<typeParameters.length;i++) {
				if(i>0) {
					builder.append(", ");
				}
				TypeVariable<?> typeParameter = typeParameters[i];
				Type parameter=typeParameter;
				if(resolve) {
					parameter=getParameterArgument(typeParameter.getName());
				}
				builder.append(TypeUtils.toString(parameter));
			}
			builder.append(">");
		}
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return toString(false);
	}

}