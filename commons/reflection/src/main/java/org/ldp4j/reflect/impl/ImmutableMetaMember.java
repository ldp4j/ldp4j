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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;

import org.ldp4j.reflect.meta.MetaClass;
import org.ldp4j.reflect.meta.MetaMember;
import org.ldp4j.reflect.model.Modifiers;

import com.google.common.base.Objects;

abstract class ImmutableMetaMember<C,T extends AccessibleObject & Member> extends ImmutableMetaAccessibleObject<T> implements MetaMember<C,T> {

	private final MetaClass<C> declaringClass;

	protected ImmutableMetaMember(MetaClass<C> declaringClass, T memberObject) {
		super(memberObject);
		this.declaringClass = declaringClass;
	}

	@Override
	public final MetaClass<C> getDeclaringClass() {
		return this.declaringClass;
	}

	@Override
	public final Modifiers getModifiers() {
		return Modifiers.of(get());
	}

	@Override
	public final String getName() {
		return get().getName();
	}

	@Override
	public final boolean isSynthetic() {
		return get().isSynthetic();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ Objects.hashCode(this.declaringClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if(super.equals(obj) && obj instanceof ImmutableMetaMember<?,?>){
			ImmutableMetaMember<?,?> that=(ImmutableMetaMember<?,?>)obj;
			return Objects.equal(this.declaringClass,that.declaringClass);
		} else {
			return false;
		}
	}

}