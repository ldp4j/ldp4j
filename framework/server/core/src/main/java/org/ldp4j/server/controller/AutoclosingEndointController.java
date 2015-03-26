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

final class AutoclosingEndointController extends AbstractEndpointController {

	private final AbstractEndpointController delegate;

	AutoclosingEndointController(AbstractEndpointController delegate) {
		super(delegate.getApplicationOperationContext(),delegate.getPublicResource());
		this.delegate = delegate;
	}

	private interface Operation {
		Response execute(AbstractEndpointController delegate);
	}

	private Response safe(Operation operation) {
		try {
			return operation.execute(this.delegate);
		} finally {
			this.delegate.getApplicationOperationContext().dispose();
		}
	}

	@Override
	public Response options(final OperationContext context) {
		return safe(
			new Operation() {
				@Override
				public Response execute(AbstractEndpointController delegate) {
					return delegate.options(context);
				}
			}
		);
	}

	@Override
	public Response head(final OperationContext context) {
		return safe(
			new Operation() {
				@Override
				public Response execute(AbstractEndpointController delegate) {
					return delegate.head(context);
				}
			}
		);
	}

	@Override
	public Response createResource(final OperationContext context) {
		return safe(
			new Operation() {
				@Override
				public Response execute(AbstractEndpointController delegate) {
					return delegate.createResource(context);
				}
			}
		);
	}

	@Override
	public Response getResource(final OperationContext context) {
		return safe(
			new Operation() {
				@Override
				public Response execute(AbstractEndpointController delegate) {
					return delegate.getResource(context);
				}
			}
		);
	}

	@Override
	public Response modifyResource(final OperationContext context) {
		return safe(
			new Operation() {
				@Override
				public Response execute(AbstractEndpointController delegate) {
					return delegate.modifyResource(context);
				}
			}
		);
	}

	@Override
	public Response deleteResource(final OperationContext context) {
		return safe(
			new Operation() {
				@Override
				public Response execute(AbstractEndpointController delegate) {
					return delegate.deleteResource(context);
				}
			}
		);
	}

	@Override
	public Response patchResource(final OperationContext context) {
		return safe(
			new Operation() {
				@Override
				public Response execute(AbstractEndpointController delegate) {
					return delegate.patchResource(context);
				}
			}
		);
	}

}