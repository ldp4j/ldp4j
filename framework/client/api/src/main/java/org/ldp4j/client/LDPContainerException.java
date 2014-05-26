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
 *   Artifact    : org.ldp4j.framework:ldp4j-client-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-client-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.client;

/**
 * The class LDPContainerException and its subclasses are a form of <i>Linked
 * Data Platform</i> exception. These exceptions indicate failure conditions
 * during the operation of a <i>Linked Data Platform Container</i> that a
 * reasonable application might want to catch.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 * @see org.ldp4j.client.LDPClientException
 */
public class LDPContainerException extends LDPClientException {

	/** @serialField The unique serial version identifier. */
	private static final long serialVersionUID = -6536265313482795837L;

	/**
	 * Create a new container exception.
	 */
	public LDPContainerException() {
		super();
	}

	/**
	 * Create a new container exception with the specified message 
	 * and cause.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public LDPContainerException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new container exception with the specified message.
	 *
	 * @param message the message
	 */
	public LDPContainerException(String message) {
		super(message);
	}

	/**
	 * Create a new container exception with the specified cause.
	 *
	 * @param cause the cause
	 */
	public LDPContainerException(Throwable cause) {
		super(cause);
	}

}