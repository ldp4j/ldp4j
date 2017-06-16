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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.ext;

import org.ldp4j.application.ApplicationApiRuntimeException;

/**
 * This exception is thrown when a parameter raw value cannot be transformed to
 * an instance of a given type.
 *
 * @see Parameter#rawValueAs(Class)
 * @see Parameter#rawValuesAs(Class)
 */
public class ObjectTransformationException extends ApplicationApiRuntimeException {

	private static final long serialVersionUID = 1526607378071322725L;

	private final Class<?> valueClass;

	/**
	 * Create a new instance with a message, cause, and valueClass
	 *
	 * @param message
	 *            the description of the failure
	 * @param cause
	 *            the underlying cause of the exception
	 * @param valueClass
	 *            the type to which the raw value could not be transformed to
	 */
	public ObjectTransformationException(String message, Throwable cause, Class<?> valueClass) {
		super(message, cause);
		this.valueClass = valueClass;
	}

	/**
	 * Returns the {@code Class} of the type to which a raw value could not be
	 * transfomed.
	 *
	 * @return the {@code Class} of the type to which a raw value could not be
	 * transfomed
	 */
	public Class<?> getValueClass() {
		return this.valueClass;
	}

}