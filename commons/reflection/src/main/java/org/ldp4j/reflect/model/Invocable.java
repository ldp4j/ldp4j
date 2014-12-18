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
package org.ldp4j.reflect.model;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.List;

public abstract class Invocable extends AccessibleObject implements Member, GenericDeclaration  {

	/**
	 * Returns the return type of this {@code Invocable}.
	 * @see java.lang.reflect.Method#getReturnType()
	 * @see java.lang.reflect.Constructor#getDeclaringClass()
	 */
	public abstract Class<?> getReturnType();

	/**
	 * Returns the generic return type of this {@code Invocable}.
	 * @see java.lang.reflect.Method#getGenericReturnType()
	 */
	public abstract Type getGenericReturnType();

	/**
	 * Returns all declared parameters of this {@code Invocable}. Note that if
	 * this is a constructor of a non-static inner class, unlike
	 * {@link Constructor#getParameterTypes}, the hidden {@code this} parameter
	 * of the enclosing class is excluded from the returned parameters.
	 */
	public abstract List<Parameter<Invocable>> getParameters();

	/**
	 * Returns all declared exception types of this {@code Invocable}.
	 */
	public abstract List<Class<? extends Throwable>> getExceptionTypes();

	/**
	 * Returns all declared generic exception types of this {@code Invocable}.
	 */
	public abstract List<Type> getGenericExceptionTypes();

	public abstract boolean isMethod();

	public abstract boolean isConstructor();

	public abstract <D extends AccessibleObject & Member & GenericDeclaration> D as(Class<? extends D> clazz);

	/**
	 * Returns {@code true} if this is an overridable method. Constructors,
	 * private, static, or final methods, or methods declared by final classes
	 * are not overridable.
	 */
	public abstract boolean isOverridable();

	/**
	 * Returns {@code true} if this was declared to take a variable number of
	 * arguments.
	 * @see java.lang.reflect.Method#isVarArgs();
	 */
	public abstract boolean isVarArgs();

	/**
	 * Invokes with {@code receiver} as 'this' and {@code args} passed to the
	 * underlying method and returns the return value; or calls the underlying
	 * constructor with {@code args} and returns the constructed instance.
	 *
	 * @throws IllegalAccessException
	 *             if this {@code Constructor} object enforces Java language
	 *             access control and the underlying method or constructor is
	 *             inaccessible.
	 * @throws IllegalArgumentException
	 *             if the number of actual and formal parameters differ; if an
	 *             unwrapping conversion for primitive arguments fails; or if,
	 *             after possible unwrapping, a parameter value cannot be
	 *             converted to the corresponding formal parameter type by a
	 *             method invocation conversion.
	 * @throws InvocationTargetException
	 *             if the underlying method or constructor throws an exception.
	 * @see java.lang.reflect.Method#invoke(Object, Object...)
	 * @see java.lang.reflect.Constructor#newInstance(Object...)
	 */
	public abstract Object invoke(Object receiver, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

}
