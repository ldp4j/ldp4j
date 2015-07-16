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

import java.util.Locale;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ldp4j.server.controller.EndpointControllerUtils;
import org.ldp4j.server.controller.PreconditionRequiredException;

import com.google.common.net.HttpHeaders;

@Provider
public class PreconditionRequiredExceptionMapper implements ExceptionMapper<PreconditionRequiredException> {

	private static final int PRECONDITION_REQUIRED = 428;
	private UriInfo uriInfo;

	@Context
	public void setUriInfo(UriInfo info) {
		uriInfo = info;
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}

	@Override
	public Response toResponse(PreconditionRequiredException throwable) {
		String message = String.format("No %s header specified.",HttpHeaders.IF_MATCH);
		ResponseBuilder builder=
			Response.
				status(PRECONDITION_REQUIRED).
				language(Locale.ENGLISH).
				type(MediaType.TEXT_PLAIN).
				entity(message);
		EndpointControllerUtils.populateProtocolEndorsedHeaders(builder,throwable.resourceLastModified(),throwable.resourceEntityTag());
		EndpointControllerUtils.populateProtocolSpecificHeaders(builder,throwable.resourceClass());
		return builder.build();
	}

}
