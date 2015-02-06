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

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ldp4j.application.engine.context.ApplicationContextOperation;
import org.ldp4j.application.engine.context.PublicResource;

public abstract class EndpointController {

	public static final class OperationContextBuilder {

		// Mandatory
		private final HttpOperation httpOperation;
		private final PublicResource resource;

		private ApplicationContextOperation applicationContextOperation;
		private UriInfo uriInfo;
		private Request request;
		private HttpHeaders headers;

		// Optional
		private String entity;

		protected OperationContextBuilder(HttpOperation httpOperation, ApplicationContextOperation applicationContextOperation, PublicResource resource) {
			this.httpOperation = httpOperation;
			this.applicationContextOperation = applicationContextOperation;
			this.resource = resource;
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

		public OperationContext build() {
			checkNotNull(this.httpOperation,"Operation cannot be null");
			checkNotNull(this.applicationContextOperation,"Application context cannot be null");
			checkNotNull(this.uriInfo,"Uri info cannot be null");
			checkNotNull(this.request,"Request cannot be null");
			checkNotNull(this.headers,"Http headers cannot be null");
			return new OperationContextImpl(this.applicationContextOperation,this.resource,this.uriInfo,this.headers,this.request,this.entity,this.httpOperation);
		}

	}

	public abstract OperationContextBuilder operationContextBuilder(HttpOperation operation);

	public abstract Response options(OperationContext context);

	public abstract Response head(OperationContext context);

	public abstract Response createResource(OperationContext context);

	public abstract Response getResource(OperationContext context);

	public abstract Response modifyResource(OperationContext context);

	public abstract Response deleteResource(OperationContext context);

	public abstract Response patchResource(OperationContext context);

}