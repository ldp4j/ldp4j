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

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;

import org.ldp4j.reflect.meta.MetaGenericDeclaration;
import org.ldp4j.reflect.meta.MetaTypeVariable;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * Immutable meta generic declaration implementation. Derived
 * attributes/properties are calculated on demand.
 *
 * @author Miguel Esteban Guti&eacute;rrez
 */
final class ImmutableMetaGenericDeclaration<D extends GenericDeclaration> implements MetaGenericDeclaration<D> {

	private final D genericDeclaration;
	private final Type type;

	private ImmutableMetaGenericDeclaration(D genericDeclaration, Type type) {
		this.genericDeclaration = genericDeclaration;
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public D get() {
		return this.genericDeclaration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MetaTypeVariable<D>> getTypeParameters() {
		Builder<MetaTypeVariable<D>> builder=ImmutableList.builder();
		TypeVariable<?>[] typeParameters = this.genericDeclaration.getTypeParameters();
		Type[] actualTypeArguments = getActualArguments(this.type,typeParameters.length);
		for(int i=0;i<typeParameters.length;i++) {
			builder.add(ImmutableMetaTypeVariable.of(this, cast(typeParameters[i],this.get()), actualTypeArguments[i]));
		}
		return builder.build();
	}

	static <D extends GenericDeclaration> ImmutableMetaGenericDeclaration<D> of(D declaration, Type type) {
		return new ImmutableMetaGenericDeclaration<D>(declaration, type);
	}

	private static Type[] getActualArguments(Type type, int length) {
		Type[] actualTypeArguments=null;
		if(type instanceof ParameterizedType) {
			ParameterizedType pt=(ParameterizedType)type;
			actualTypeArguments = pt.getActualTypeArguments();
		} else {
			actualTypeArguments=new Type[length];
			Arrays.fill(actualTypeArguments, null);
		}
		return actualTypeArguments;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.genericDeclaration,this.type);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null) {
			return false;
		} else if(obj instanceof ImmutableMetaGenericDeclaration<?>) {
			ImmutableMetaGenericDeclaration<?> that=(ImmutableMetaGenericDeclaration<?>)obj;
			return
				Objects.equal(this.genericDeclaration,that.genericDeclaration) &&
				Objects.equal(this.type,that.type);
		} else {
			return false;
		}
	}

	private static <D extends GenericDeclaration> TypeVariable<D> cast(TypeVariable<?> typeVariable, D genericDeclaration) {
		checkArgument(typeVariable.getGenericDeclaration()==genericDeclaration);
		@SuppressWarnings("unchecked") // Guarded by previous check, altough it is safe by construction
		TypeVariable<D> result = (TypeVariable<D>)typeVariable;
		return result;
	}

}