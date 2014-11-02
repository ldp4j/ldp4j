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
package org.ldp4j.server.controller.providers;

import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ldp4j.application.engine.context.Capabilities;
import org.ldp4j.server.controller.EndpointControllerUtils;
import org.ldp4j.server.controller.MethodNotAllowedException;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

@Provider
public class MethodNotAllowedExceptionMapper implements ExceptionMapper<MethodNotAllowedException> {

	@Override
	public Response toResponse(MethodNotAllowedException throwable) {
		String message = String.format("Endpoint '%s' does not support %s. It only supports: %s",throwable.resourceLocation(),throwable.getOperation(),toHttpMethods(throwable.getResource().capabilities()));
		ResponseBuilder builder=
			Response.
				status(Status.METHOD_NOT_ALLOWED).
				language(Locale.ENGLISH).
				type(MediaType.TEXT_PLAIN).
				entity(message);
		EndpointControllerUtils.populateProtocolEndorsedHeaders(builder,throwable.getResource());
		EndpointControllerUtils.populateProtocolSpecificHeaders(builder,throwable.getResource());
		EndpointControllerUtils.populateAllowedHeaders(builder,throwable.getResource().capabilities());
		return builder.build();
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
