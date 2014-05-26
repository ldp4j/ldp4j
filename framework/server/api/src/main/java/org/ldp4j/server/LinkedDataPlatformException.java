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
package org.ldp4j.server;

/**
 * The class LinkedDataPlatformException and its subclasses are a form of
 * checked exceptions that indicate failure conditions during the operation of a
 * <i>Linked Data Platform</i> application that the <i>LDP4j Linked Data 
 * Platform Server</i> will have to handle.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public class LinkedDataPlatformException extends Exception {

	/** @serialField The unique serial version identifier. */
	private static final long serialVersionUID = -4610906206610068587L;

	/**
	 * Create a new exception.
	 */
	public LinkedDataPlatformException() {
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
	public LinkedDataPlatformException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new exception with the specified message.
	 *
	 * @param message the message
	 */
	public LinkedDataPlatformException(String message) {
		super(message);
	}

	/**
	 * Create a new exception with the specified cause.
	 *
	 * @param cause the cause
	 */
	public LinkedDataPlatformException(Throwable cause) {
		super(cause);
	}

}