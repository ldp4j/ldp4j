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
package org.ldp4j.reflect.meta;

import java.lang.reflect.Type;
import java.util.List;

import org.ldp4j.reflect.model.Modifiers;

public interface MetaClass<T>
	extends MetaGenericDeclaration<Class<T>>, MetaAnnotatedElement<Class<T>> {

	Type getType();

	Type getResolvedType();

	<U> MetaClass<? extends U> asSubclass(Class<U> clazz);

	T cast(Object object);

	String getCanonicalName();

	List<MetaClass<?>> getClasses();

	MetaClass<?> getComponentType();

	MetaConstructor<T> getConstructor(Class<?>... parameterClasses);

	List<MetaConstructor<T>> getConstructors();

	MetaConstructor<T> getDeclaredConstructor(Class<?>... parameterClasses);

	List<MetaConstructor<T>> getDeclaredConstructors();

	MetaField<T> getDeclaredField(String name);

	List<MetaField<T>> getDeclaredFields();

	MetaMethod<T,?> getDeclaredMethod(String name, Class<?>... parameterClasses);

	List<MetaMethod<T,?>> getDeclaredMethods();

	MetaClass<?> getDeclaringClass();

	MetaClass<?> getEnclosingClass();

	MetaConstructor<?> getEnclosingConstructor();

	MetaMethod<?,?> getEnclosingMethod();

	List<T> getEnumConstants();

	MetaField<T> getField(String name);

	List<MetaField<T>> getFields();

	List<MetaClass<?>> getInterfaces();

	MetaMethod<T,?> getMethod(String name, Class<?>... parameterClasses);

	List<MetaMethod<T,?>> getMethods();

	Modifiers getModifiers();

	String getName();

	Package getPackage();

	String getSimpleName();

	MetaClass<?> getSuperclass();

	boolean isAnnotation();

	boolean isAnonymousClass();

	boolean isArray();

	boolean isAssignableFrom(Class<?> clazz);

	boolean isAssignableFrom(MetaClass<?> clazz);

	boolean isEnum();

	boolean isInstance(Object object);

	boolean isInterface();

	boolean isLocalClass();

	boolean isMemberClass();

	boolean isPrimitive();

	boolean isSynthetic();

}