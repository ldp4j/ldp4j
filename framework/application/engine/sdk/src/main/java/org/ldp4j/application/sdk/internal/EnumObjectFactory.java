/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014-2016 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-sdk:0.2.2
 *   Bundle      : ldp4j-application-engine-sdk-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.sdk.internal;

import org.ldp4j.application.sdk.spi.ObjectFactory;
import org.ldp4j.application.sdk.spi.ObjectParseException;

/**
 * Object factory for parsing and formating enum types relying on the standard
 * Enum class facilities for parsing and formatting
 */
public final class EnumObjectFactory<E extends Enum<E>> implements ObjectFactory<E> {

	private final Class<E> targetClass;

	private EnumObjectFactory(Class<E> targetClass) {
		this.targetClass = targetClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends E> targetClass() {
		return this.targetClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E fromString(final String rawValue) {
		try {
			return Enum.valueOf(this.targetClass, rawValue);
		} catch (Exception e) {
			throw new ObjectParseException(e,targetClass(),rawValue);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString(E value) {
		return value.toString();
	}

	public static <S extends Enum<S>> ObjectFactory<S> create(Class<S> enumClass) {
		return new EnumObjectFactory<S>(enumClass);
	}

}