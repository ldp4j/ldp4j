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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.ldp4j.application.sdk.spi.ObjectFactory;
import org.ldp4j.application.sdk.spi.ObjectParseException;

public final class PrimitiveObjectFactory<T> implements ObjectFactory<T> {

	private static final String FALSE = "false";
	private static final String TRUE = "true";

	private final Class<? extends T> valueClass;

	private PrimitiveObjectFactory(final Class<? extends T> valueClass) {
		this.valueClass = valueClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends T> targetClass() {
		return this.valueClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T fromString(final String rawValue) { // NOSONAR
		try {
			Object result = null;
			if (byte.class.equals(this.valueClass)) {
				result = Byte.decode(rawValue);
			} else if (short.class.equals(this.valueClass)) {
				result = Short.decode(rawValue);
			} else if (int.class.equals(this.valueClass)) {
				result = Integer.decode(rawValue);
			} else if (long.class.equals(this.valueClass)) {
				result = Long.decode(rawValue);
			} else if (double.class.equals(this.valueClass)) {
				result = Double.valueOf(rawValue);
			} else if (float.class.equals(this.valueClass)) {
				result = Float.valueOf(rawValue);
			} else if (boolean.class.equals(this.valueClass)) {
				result = parseBoolean(rawValue);
			} else { // Must be char
				result = parseCharacter(rawValue);
			}
			return (T) result;
		} catch (final Exception e) {
			throw new ObjectParseException(e, this.valueClass, rawValue);
		}
	}

	static Character parseCharacter(final String rawValue) {
		if (rawValue.length() != 1) {
			throw new IllegalArgumentException("Raw value has more than one character");
		}
		return Character.valueOf(rawValue.charAt(0));
	}

	static boolean parseBoolean(final String rawValue) {
		if (TRUE.equalsIgnoreCase(rawValue)) {
			return true;
		} else if (FALSE.equalsIgnoreCase(rawValue)) {
			return false;
		}
		throw new IllegalArgumentException("Not a standard boolean representation");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString(final T value) {
		return value.toString();
	}

	/**
	 * Create a primitive object factory for the specified primitive class
	 *
	 * @param <T>
	 *            the primitive class type
	 * @param valueClass
	 *            the primitive class to use
	 * @return an object factory
	 * @throws NullPointerException
	 *             if the specified class is {@code null}
	 * @throws IllegalArgumentException
	 *             if the specified class is not a primitive class
	 */
	public static <T> PrimitiveObjectFactory<T> create(final Class<? extends T> valueClass) {
		checkNotNull(valueClass, "Value class cannot be null");
		checkArgument(valueClass.isPrimitive(), "Value class '" + valueClass.getName() + "' is not primitive");
		return new PrimitiveObjectFactory<T>(valueClass);
	}
}