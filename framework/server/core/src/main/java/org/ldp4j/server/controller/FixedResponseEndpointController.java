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

import javax.ws.rs.core.Response;

import org.ldp4j.application.engine.context.ApplicationContext;

abstract class FixedResponseEndpointController extends AbstractEndpointController {

	FixedResponseEndpointController(ApplicationContext applicationContext) {
		super(applicationContext,null);
	}

	protected abstract Response defaultResponse(OperationContext context);

	@Override
	public final Response getResource(OperationContext context) {
		return defaultResponse(context);
	}

	@Override
	public final Response patchResource(OperationContext context) {
		return defaultResponse(context);
	}

	@Override
	public final Response createResource(OperationContext context) {
		return defaultResponse(context);
	}

	@Override
	public final Response modifyResource(OperationContext context) {
		return defaultResponse(context);
	}

	@Override
	public final Response options(OperationContext context) {
		return defaultResponse(context);
	}

	@Override
	public final Response head(OperationContext context) {
		return defaultResponse(context);
	}

	@Override
	public Response deleteResource(OperationContext context) {
		return defaultResponse(context);
	}

}