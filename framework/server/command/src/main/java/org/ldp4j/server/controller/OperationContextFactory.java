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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.endpoint.Endpoint;

public final class OperationContextFactory {
	
	public static final class OperationContextBuilder {
	
		// Mandatory
		private final Operation operation;
		private ApplicationContext applicationContext;
		private UriInfo uriInfo;
		private Request request;
		private HttpHeaders headers;
	
		// Optional
		private Endpoint endpoint;
		private String entity;
	
		private OperationContextBuilder(Operation operation, ApplicationContext applicationContext) {
			this.operation = operation;
			this.applicationContext = applicationContext;
		}
		
		public OperationContextBuilder withUriInfo(UriInfo uriInfo) {
			this.uriInfo = uriInfo;
			return this;
		}
	
		public OperationContextBuilder withHeaders(HttpHeaders headers) {
			this.headers = headers;
			return this;
		}
		
		public OperationContextBuilder withRequest(Request request) {
			this.request = request;
			return this;
		}
	
		public OperationContextBuilder withEntity(String entity) {
			this.entity = entity;
			return this;
		}
	
		public OperationContextBuilder withEndpoint(Endpoint endpoint) {
			this.endpoint = endpoint;
			return this;
		}
		
		public OperationContext build() {
			checkNotNull(this.operation,"Operation cannot be null");
			checkNotNull(this.applicationContext,"Application context cannot be null");
			checkNotNull(this.uriInfo,"Uri info cannot be null");
			checkNotNull(this.request,"Request cannot be null");
			checkNotNull(this.headers,"Http headers cannot be null");
			return new OperationContextImpl(this.applicationContext,this.endpoint,this.uriInfo,this.headers,this.request,this.entity,this.operation);
		}
	
		public OperationContextBuilder withApplicationContext(ApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
			return this;
		}
		
	}

	private final ApplicationContext applicationContext;

	private OperationContextFactory(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	public OperationContextBuilder forOperation(Operation operation) {
		return new OperationContextBuilder(operation,this.applicationContext);
	}

	public static OperationContextFactory newInstance(ApplicationContext applicationContext) {
		return new OperationContextFactory(applicationContext);
	}
	
}