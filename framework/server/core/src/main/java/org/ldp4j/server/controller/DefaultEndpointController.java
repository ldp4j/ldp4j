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

import javax.ws.rs.core.Response;

import org.ldp4j.application.engine.context.PublicResource;

final class DefaultEndpointController implements EndpointController {

	DefaultEndpointController() {
	}

	private interface Operation {
		Response execute(EndpointController delegate, OperationContext context);
	}

	private Response safe(Operation operation, OperationContext context) {
		context.startOperation();
		try {
			return operation.execute(getDelegate(context.resource()),context);
		} finally {
			context.completeOperation();
		}
	}

	private EndpointController getDelegate(PublicResource resource) {
		EndpointController result=new NotFoundEndpointController();
		if(resource!=null) {
			switch(resource.status()) {
				case GONE:
					result=new GoneEndpointController(resource);
					break;
				case PUBLISHED:
					result=new ExistingEndpointController();
					break;
				default:
					throw new IllegalStateException("Unsupported resource status "+resource.status());
			}
		}
		return result;
	}

	@Override
	public Response options(OperationContext context) {
		return safe(
			new Operation() {
				@Override
				public Response execute(EndpointController delegate, OperationContext context) {
					return delegate.options(context);
				}
			},
			context
		);
	}

	@Override
	public Response head(final OperationContext context) {
		return safe(
			new Operation() {
				@Override
				public Response execute(EndpointController delegate, OperationContext context) {
					return delegate.head(context);
				}
			},
			context
		);
	}

	@Override
	public Response createResource(final OperationContext context) {
		return safe(
			new Operation() {
				@Override
				public Response execute(EndpointController delegate, OperationContext context) {
					return delegate.createResource(context);
				}
			},
			context
		);
	}

	@Override
	public Response getResource(final OperationContext context) {
		return safe(
			new Operation() {
				@Override
				public Response execute(EndpointController delegate, OperationContext context) {
					return delegate.getResource(context);
				}
			},
			context
		);
	}

	@Override
	public Response modifyResource(final OperationContext context) {
		return safe(
			new Operation() {
				@Override
				public Response execute(EndpointController delegate, OperationContext context) {
					return delegate.modifyResource(context);
				}
			},
			context
		);
	}

	@Override
	public Response deleteResource(final OperationContext context) {
		return safe(
			new Operation() {
				@Override
				public Response execute(EndpointController delegate, OperationContext context) {
					return delegate.deleteResource(context);
				}
			},
			context
		);
	}

	@Override
	public Response patchResource(final OperationContext context) {
		return safe(
			new Operation() {
				@Override
				public Response execute(EndpointController delegate, OperationContext context) {
					return delegate.patchResource(context);
				}
			},
			context
		);
	}

}