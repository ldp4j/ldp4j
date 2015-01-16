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
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;

import org.ldp4j.reflect.meta.MetaClass;
import org.ldp4j.reflect.meta.MetaInvocable;
import org.ldp4j.reflect.meta.MetaParameter;
import org.ldp4j.reflect.model.Parameter;

import com.google.common.base.Objects;

final class ImmutableMetaParameter<T extends AccessibleObject & Member & GenericDeclaration>
	extends ImmutableMetaAnnotatedElement<Parameter<T>>
	implements MetaParameter<T> {

	private MetaInvocable<?,?,?> invocable;

	ImmutableMetaParameter(MetaInvocable<?,T,?> invocable, Parameter<T> parameter) {
		super(parameter);
		this.invocable = invocable;
	}

	@Override
	public int getPosition() {
		return get().getPosition();
	}

	@Override
	public MetaClass<?> getType() {
		return ImmutableMetaClass.of(get().getRawType(),get().getType());
	}

	@Override
	public MetaInvocable<?,?,?> getInvocable() {
		return this.invocable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ Objects.hashCode(this.invocable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if(super.equals(obj) && obj instanceof ImmutableMetaParameter<?>) {
			ImmutableMetaParameter<?> that=(ImmutableMetaParameter<?>)obj;
			return Objects.equal(this.invocable,that.invocable);
		} else {
			return false;
		}
	}

}
