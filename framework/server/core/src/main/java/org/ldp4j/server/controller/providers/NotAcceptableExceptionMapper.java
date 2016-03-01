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
import java.util.Locale;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ldp4j.server.controller.EndpointControllerUtils;
import org.ldp4j.server.controller.NotAcceptableException;
import org.ldp4j.server.utils.VariantUtils;

@Provider
public class NotAcceptableExceptionMapper implements ExceptionMapper<NotAcceptableException> {

	@Override
	public Response toResponse(NotAcceptableException throwable) {
		List<Variant> variants = VariantUtils.defaultVariants();
		String message=
			EndpointControllerUtils.
				getAcceptableContent(
					variants,
					throwable.resourceLocation(),
					throwable.supportedCharsets());
		ResponseBuilder builder=
			Response.
				status(Status.NOT_ACCEPTABLE).
				variants(variants).
				language(Locale.ENGLISH).
				type(MediaType.TEXT_PLAIN).
				entity(message);
		addAcceptedCharsetVariants(builder,throwable.supportedCharsets());
		EndpointControllerUtils.populateProtocolEndorsedHeaders(builder,throwable.resourceLastModified(),throwable.resourceEntityTag());
		EndpointControllerUtils.populateProtocolSpecificHeaders(builder,throwable.resourceClass());
		return builder.build();
	}

	private void addAcceptedCharsetVariants(ResponseBuilder builder, Iterable<String> supportedCharsets) {
		builder.header(HttpHeaders.VARY,HttpHeaders.ACCEPT_CHARSET);
		for(String supportedCharset:supportedCharsets) {
			builder.header(HttpHeaders.ACCEPT_CHARSET,supportedCharset);
		}
	}

}
