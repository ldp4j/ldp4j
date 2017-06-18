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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.ext;

import org.ldp4j.application.ApplicationApiRuntimeException;

/**
 * Base unchecked exception for the LDP4j Application exceptions.
 */
public class ApplicationRuntimeException extends ApplicationApiRuntimeException {

	private static final long serialVersionUID = -8352522362783716805L;

	/**
	 * Create a new instance with a message and a cause.
	 *
	 * @param message
	 *            the description of the failure.
	 * @param cause
	 *            the underlying cause of the failure.
	 */
	public ApplicationRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new instance with a message.
	 *
	 * @param message
	 *            the description of the failure.
	 */
	public ApplicationRuntimeException(String message) {
		this(message,null);
	}

	/**
	 * Create a new instance with a cause and a default message.
	 *
	 * @param cause
	 *            the underlying cause of the failure.
	 */
	public ApplicationRuntimeException(Throwable cause) {
		this("Unexpected application runtime exception",cause);
	}

}
