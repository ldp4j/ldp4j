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
 * Object factory for parsing and formatting Boolean objects.
 */
public class BooleanObjectFactory implements ObjectFactory<Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends Boolean> targetClass() {
		return Boolean.class;
	}

	/**
	 * Returns a {@code Boolean} with a value represented by the specified
	 * string. The {@code Boolean} returned represents:
	 * <ol>
	 * <li>
	 * a {@code true} value if the string argument is not {@code null} and is equal,
	 * ignoring case, to the string {@code "true"}.</li>
	 * <li>
	 * a {@code false} value if the string argument is not {@code null} and is equal,
	 * ignoring case, to the string {@code "false"}.</li>
	 * </ol>
	 *
	 * @param rawValue
	 *            a string.
	 * @return the {@code Boolean} value represented by the string.
	 * @throws ObjectParseException
	 *             iff the specified raw value cannot be converted to a
	 *             {@code Boolean} object.
	 */
	@Override
	public Boolean fromString(final String rawValue) {
		try {
			return PrimitiveObjectFactory.parseBoolean(rawValue);
		} catch (final IllegalArgumentException e) {
			throw new ObjectParseException(e,Boolean.class, rawValue);
		}
	}

	/**
	 * Returns a {@code String} object representing the specified boolean. If
	 * the specified boolean is {@code true}, then the string {@code "true"}
	 * will be returned, otherwise the string {@code "false"} will be returned.
	 *
	 * @param value
	 *            the boolean to be converted
	 * @return the string representation of the specified {@code boolean}
	 */
	@Override
	public String toString(final Boolean value) {
		return value.toString();
	}

}