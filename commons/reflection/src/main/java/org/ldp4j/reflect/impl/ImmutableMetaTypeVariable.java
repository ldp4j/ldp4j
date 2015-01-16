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
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

import org.ldp4j.reflect.meta.MetaClass;
import org.ldp4j.reflect.meta.MetaGenericDeclaration;
import org.ldp4j.reflect.meta.MetaTypeVariable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

final class ImmutableMetaTypeVariable<D extends GenericDeclaration> implements MetaTypeVariable<D> {

	private final TypeVariable<D> typeVariable;
	private final MetaGenericDeclaration<D> declaration;
	private final MetaClass<?> actualArgument;

	protected ImmutableMetaTypeVariable(MetaGenericDeclaration<D> declaration, TypeVariable<D> typeVariable, MetaClass<?> actualArgument) {
		this.declaration = declaration;
		this.typeVariable = typeVariable;
		this.actualArgument = actualArgument;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TypeVariable<D> get() {
		return this.typeVariable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaGenericDeclaration<D> getDeclaration() {
		return this.declaration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return this.typeVariable.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MetaClass<?>> getBounds() {
		Builder<MetaClass<?>> builder = ImmutableList.<MetaClass<?>>builder();
		for(Type bound:this.typeVariable.getBounds()) {
			builder.add(ImmutableMetaClass.of(bound));
		}
		return builder.build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDefined() {
		return this.actualArgument!=null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaClass<?> actualArgument() {
		return this.actualArgument;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.typeVariable,this.declaration,this.actualArgument);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj==null) {
			return false;
		} else if(obj instanceof ImmutableMetaTypeVariable<?>) {
			ImmutableMetaTypeVariable<?> that=(ImmutableMetaTypeVariable<?>)obj;
			return
				Objects.equal(this.typeVariable,that.typeVariable) &&
				Objects.equal(this.declaration,that.declaration) &&
				Objects.equal(this.actualArgument,that.actualArgument);
		} else {
			return false;
		}
	}

	static <D extends GenericDeclaration> ImmutableMetaTypeVariable<D> of(
			MetaGenericDeclaration<D> declaration,
			TypeVariable<D> typeVariable,
			Type actualArgument) {
		return
			new ImmutableMetaTypeVariable<D>(
				declaration,
				typeVariable,
				ImmutableMetaClass.of(actualArgument));
	}

}