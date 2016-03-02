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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-server-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller.providers;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ldp4j.application.engine.context.Capabilities;
import org.ldp4j.server.controller.EndpointControllerUtils;
import org.ldp4j.server.controller.EndpointControllerUtils.ResponseEnricher;
import org.ldp4j.server.controller.MethodNotAllowedException;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

@Provider
public class MethodNotAllowedExceptionMapper implements ExceptionMapper<MethodNotAllowedException> {

	@Override
	public Response toResponse(final MethodNotAllowedException throwable) {
		String body =
			String.format(
				"Endpoint '%s' does not support %s. It only supports: %s",
				throwable.resourceLocation(),
				throwable.getMethod(),
				toHttpMethods(throwable.resourceCapabilities()));

		return
			EndpointControllerUtils.
				prepareErrorResponse(
					throwable,
					body,
					Status.METHOD_NOT_ALLOWED.getStatusCode(),
					new ResponseEnricher() {
						@Override
						protected void enrich(ResponseBuilder builder) {
							EndpointControllerUtils.populateAllowedHeaders(builder,throwable.resourceCapabilities());
						}
					}
				);
	}

	private String toHttpMethods(Capabilities capabilities) {
		List<String> list = Lists.newArrayList("HEAD","GET","OPTIONS");
		if(capabilities.isModifiable()) {
			list.add("PUT");
		}
		if(capabilities.isDeletable()) {
			list.add("DELETE");
		}
		if(capabilities.isFactory()) {
			list.add("POST");
		}
		if(capabilities.isPatchable()) {
			list.add("PATCH");
		}
		return Joiner.on(", ").join(list);
	}

}
