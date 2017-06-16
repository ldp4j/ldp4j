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

import org.ldp4j.application.data.constraints.Constraints;

/**
 * This exception may be thrown by an LDP4j Application if the data to be used
 * to update a resource is not consistent with the current representation of the
 * resource, i.e., the properties managed by the application have been modified
 * in any manner.
 */
public class InconsistentContentException extends InvalidContentException {

	private static final long serialVersionUID = -9204136391485005625L;

	/**
	 * Create a new instance with a message, a cause, and constraints.
	 *
	 * @param message
	 *            the description of the failure.
	 * @param cause
	 *            the underlying cause of the failure.
	 * @param constraints
	 *            the constraints that are not satisfied by the input data.
	 */
	public InconsistentContentException(String message, Throwable cause, Constraints constraints) {
		super(message, cause, constraints);
	}

	/**
	 * Create a new instance with a message and constraints.
	 *
	 * @param message
	 *            the description of the failure.
	 * @param constraints
	 *            the constraints that are not satisfied by the input data.
	 */
	public InconsistentContentException(String message, Constraints constraints) {
		this(message,null,constraints);
	}

	/**
	 * Create a new instance with a cause, constraints, and a default message.
	 *
	 * @param cause
	 *            the underlying cause of the failure.
	 * @param constraints
	 *            the constraints that are not satisfied by the input data.
	 */
	public InconsistentContentException(Throwable cause, Constraints constraints) {
		this("Content is not consistent with the current resource state",cause,constraints);
	}

}
