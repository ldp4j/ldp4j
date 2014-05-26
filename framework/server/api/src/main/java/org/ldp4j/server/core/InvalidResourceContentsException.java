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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.core;

import org.ldp4j.server.LinkedDataPlatformException;



/**
 * A checked exception that will be thrown by <i>LDP4j Containers or Resource 
 * Handlers</i> when the they cannot create or modify a given Linked Data 
 * Resource with the given contents.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 * @see org.ldp4j.server.LinkedDataPlatformException
 */
public class InvalidResourceContentsException extends LinkedDataPlatformException {

	/** @serialField The unique serial version identifier. */
	private static final long serialVersionUID = 4386820186339411410L;

	/**
	 * Create a new exception.
	 */
	public InvalidResourceContentsException() {
		super();
	}

	/**
	 * Create a new exception with the specified message and cause.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public InvalidResourceContentsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new exception with the specified message.
	 *
	 * @param message the message
	 */
	public InvalidResourceContentsException(String message) {
		super(message);
	}

	/**
	 * Create a new exception with the specified cause.
	 *
	 * @param cause the cause
	 */
	public InvalidResourceContentsException(Throwable cause) {
		super(cause);
	}

}