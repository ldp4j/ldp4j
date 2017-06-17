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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.ldp4j.application.engine.context.ApplicationContext;
import org.ldp4j.application.engine.context.HttpRequest.HttpMethod;

public final class OperationContextBuilder {

	// Mandatory
	private ApplicationContext applicationContext;
	private String endpointPath;
	private HttpMethod method;
	private UriInfo uriInfo;
	private Request request;
	private HttpHeaders headers;

	// Optional
	private String entity;

	public OperationContextBuilder() {
	}

	public OperationContextBuilder withEndpointPath(String path) {
		this.endpointPath = path;
		return this;
	}

	public OperationContextBuilder withApplicationContext(ApplicationContext application) {
		this.applicationContext = application;
		return this;
	}

	public OperationContextBuilder withOperation(HttpMethod method) {
		this.method=method;
		return this;
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
		checkNotNull(this.applicationContext,"Application context cannot be null");
		checkNotNull(this.endpointPath,"Endpoint path cannot be null");
		checkNotNull(this.method,"Method cannot be null");
		checkNotNull(this.uriInfo,"Uri info cannot be null");
		checkNotNull(this.request,"Request cannot be null");
		checkNotNull(this.headers,"Headers cannot be null");
		return new OperationContextImpl(this.applicationContext,this.endpointPath,this.uriInfo,this.headers,this.request,this.entity,this.method);
	}

}