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

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.List;

import org.ldp4j.reflect.Reflection;
import org.ldp4j.reflect.meta.MetaClass;
import org.ldp4j.reflect.meta.MetaInvocable;
import org.ldp4j.reflect.meta.MetaParameter;
import org.ldp4j.reflect.meta.MetaGenericDeclaration;
import org.ldp4j.reflect.meta.MetaTypeVariable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

abstract class ImmutableMetaInvocable<C,T extends AccessibleObject & Member & GenericDeclaration,R>
	extends ImmutableMetaMember<C,T>
	implements MetaInvocable<C,T,R> {

	private final MetaGenericDeclaration<T> genericDeclaration;
	private final MetaClass<R> type;

	protected ImmutableMetaInvocable(MetaClass<C> declaringClass, T invocableObject, MetaClass<R> type) {
		super(declaringClass,invocableObject);
		this.type = type;
		this.genericDeclaration=ImmutableMetaGenericDeclaration.of(invocableObject,type.getType());
	}

	@Override
	public final MetaClass<? extends R> getReturnType() {
		return this.type;
	}

	@Override
	public final List<MetaTypeVariable<T>> getTypeParameters() {
		return this.genericDeclaration.getTypeParameters();
	}

	@Override
	public final <R1 extends R> MetaInvocable<C,T,R1> returning(Class<R1> returnType) {
		return returning(Reflection.of(returnType));
	}

	@Override
	public final <R1 extends R> MetaInvocable<C,T,R1> returning(MetaClass<R1> returnType) {
		checkArgument(returnType.isAssignableFrom(getReturnType()),"Invokable is known to return %s,s not %s",getReturnType(),returnType);
		@SuppressWarnings("unchecked") // guarded by previous check
		MetaInvocable<C,T,R1> specialized = (MetaInvocable<C,T,R1>) this;
		return specialized;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ Objects.hashCode(this.genericDeclaration,this.type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if(super.equals(obj) && obj instanceof ImmutableMetaInvocable<?,?,?>) {
			ImmutableMetaInvocable<?,?,?> that=(ImmutableMetaInvocable<?,?,?>)obj;
			return
				Objects.equal(this.genericDeclaration,that.genericDeclaration) &&
				Objects.equal(this.type,that.type);
		} else {
			return false;
		}
	}

	protected final List<MetaParameter<T>> unrollParameters(
			Class<?>[] parameterTypes,
			Type[] genericParameterTypes,
			Annotation[][] parameterAnnotations) {
		Builder<MetaParameter<T>> builder=ImmutableList.builder();
		for(int i=0;i<parameterTypes.length;i++) {
			ImmutableParameter<T> parameter=
				ImmutableParameter.of(
					get(),
					i,
					parameterTypes[i],
					genericParameterTypes[i],
					parameterAnnotations[i]);
			builder.add(new ImmutableMetaParameter<T>(this, parameter));
		}
		return builder.build();
	}

	protected final List<MetaClass<? extends Throwable>> unrollExceptions(Class<? extends Throwable>[] exceptionTypes) {
		Builder<MetaClass<? extends Throwable>> builder=ImmutableList.builder();
		for(Class<? extends Throwable> exceptionType:exceptionTypes) {
			builder.add(Reflection.of(exceptionType));
		}
		return builder.build();
	}

}
