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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.util.List;

/**
 * An interface for facilitating the introspection of invokable entities, that
 * is, {@code Constructor} and {@code Method} entities.
 *
 * @author Miguel Esteban Guti&eacute;rrez
 *
 * @param <T>
 *            the type that owns this method or constructor.
 * @param <R>
 *            the return type of (or supertype thereof) the method or the
 *            declaring type of the constructor.
 * @see java.lang.reflect.Constructor
 * @see java.lang.reflect.Method
 */
public interface MetaInvocable<C,T extends AccessibleObject & Member & GenericDeclaration,R>
	extends
		MetaMember<C,T>,
		MetaGenericDeclaration<T> {

	/**
	 * Returns the return type of this {@code InvocableDeclaration}.
	 * @see java.lang.reflect.Method#getReturnType()
	 * @see java.lang.reflect.Constructor#getDeclaringClass()
	 */
	MetaClass<? extends R> getReturnType();

	/**
	 * Returns all declared parameters of this {@code InvokableDeclaration}. Note that if
	 * this is a constructor of a non-static inner class, unlike
	 * {@link Constructor#getParameterTypes}, the hidden {@code this} parameter
	 * of the enclosing class is excluded from the returned parameters.
	 */
	List<MetaParameter<T>> getParameters();

	/**
	 * Returns all declared exception types of this {@code InvokableDeclaration}.
	 */
	List<MetaClass<? extends Throwable>> getExceptionTypes();

	/**
	 * Returns {@code true} if this is an overridable method. Constructors,
	 * private, static, or final methods, or methods declared by final classes
	 * are not overridable.
	 */
	boolean isOverridable();

	/**
	 * Returns {@code true} if this was declared to take a variable number of
	 * arguments.
	 * @see java.lang.reflect.Method#isVarArgs();
	 */
	boolean isVarArgs();

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
	R invoke(C receiver, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	/**
	 * Explicitly specifies the return type of this {@code InvokableDeclaration}. For
	 * example:
	 *
	 * <pre>
	 * {@code
	 * Method factoryMethod = Person.class.getMethod("create");
	 * Invokable<?, Person> factory =
	 *    Invokable.
	 *      of(getNameMethod).
	 *        returning(Person.class);
	 * }
	 * </pre>
	 */
	<R1 extends R> MetaInvocable<C, T, R1> returning(Class<R1> returnType);

	/**
	 * Explicitly specifies the return type of this {@code InvokableDeclaration}.
	 */
	<R1 extends R> MetaInvocable<C, T, R1> returning(MetaClass<R1> returnType);

}