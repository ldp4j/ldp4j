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

import java.lang.reflect.Field;

import org.ldp4j.reflect.meta.MetaClass;
import org.ldp4j.reflect.meta.MetaField;

final class ImmutableMetaField<C>
	extends ImmutableMetaMember<C,Field>
	implements MetaField<C> {

	private ImmutableMetaField(Field field, MetaClass<C> declaringClass) {
		super(declaringClass,field);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T get(Object object, Class<? extends T> clazz) throws IllegalArgumentException, IllegalAccessException, ClassCastException {
		return clazz.cast(get().get(object));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaClass<?> getType() {
		return ImmutableMetaClass.of(get().getType(),get().getGenericType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnumConstant() {
		return get().isEnumConstant();
	}

	static <T> ImmutableMetaField<T> of(MetaClass<T> declaringClass, Field field) {
		return new ImmutableMetaField<T>(field, declaringClass);

	}

}