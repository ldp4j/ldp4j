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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import org.ldp4j.application.endpoint.Endpoint;

public class OperationContextException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5924666884556832771L;

	private final OperationContext operationContext;

	private final Endpoint endpoint;

	public OperationContextException(Endpoint endpoint, OperationContext operationContext) {
		super();
		this.endpoint = endpoint;
		this.operationContext = operationContext;
	}

	public OperationContextException(String message, Throwable cause, Endpoint endpoint, OperationContext operationContext) {
		super(message, cause);
		this.endpoint = endpoint;
		this.operationContext = operationContext;
	}

	public OperationContextException(String message, Endpoint endpoint, OperationContext operationContext) {
		super(message);
		this.endpoint = endpoint;
		this.operationContext = operationContext;
	}

	public OperationContextException(Throwable cause, Endpoint endpoint, OperationContext operationContext) {
		super(cause);
		this.endpoint = endpoint;
		this.operationContext = operationContext;
	}

	public OperationContext getOperationContext() {
		return operationContext;
	}

	public Endpoint getEndpoint() {
		return endpoint;
	}

}
