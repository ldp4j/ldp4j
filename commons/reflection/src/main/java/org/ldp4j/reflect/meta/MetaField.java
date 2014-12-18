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

import java.lang.reflect.Field;

/**
 * An interface for facilitating the introspection of {@code Field} elements.
 *
 * @author Miguel Esteban Guti&eacute;rrez
 *
 * @see java.lang.reflect.Field
 */
public interface MetaField<C> extends MetaAccessibleObject<Field>, MetaMember<C,Field> {

	/**
	 * @throws ClassCastException If the field value cannot be casted to the specified subclass
	 * @throws IllegalAccessException  If the field is not accessible
	 * @throws IllegalArgumentException
	 * @see java.lang.reflect.Field#get(Object)
	 * @see java.lang.reflect.Field#getBoolean(Object)
	 * @see java.lang.reflect.Field#getByte(Object)
	 * @see java.lang.reflect.Field#getChar(Object)
	 * @see java.lang.reflect.Field#getDouble(Object)
	 * @see java.lang.reflect.Field#getFloat(Object)
	 * @see java.lang.reflect.Field#getInt(Object)
	 * @see java.lang.reflect.Field#getLong(Object)
	 * @see java.lang.reflect.Field#getShort(Object)
	 */
	<T> T get(Object object, Class<? extends T> clazz) throws IllegalArgumentException, IllegalAccessException, ClassCastException;

	/**
	 * @see java.lang.reflect.Field#getType()
	 */
	MetaClass<?> getType();

	/**
	 * @see java.lang.reflect.Field#isEnumConstant()
	 */
	boolean isEnumConstant();

}