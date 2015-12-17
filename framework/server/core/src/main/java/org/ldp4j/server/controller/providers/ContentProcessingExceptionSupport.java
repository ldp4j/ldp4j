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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.0
 *   Bundle      : ldp4j-server-core-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller.providers;

import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import org.ldp4j.server.controller.ContentProcessingException;
import org.ldp4j.server.controller.EndpointControllerUtils;
import org.ldp4j.server.utils.VariantUtils;

final class ContentProcessingExceptionSupport {

	private ContentProcessingExceptionSupport() {
	}

	static String getFailureMessage(String message, List<Variant> variants) {
		return
			new StringBuilder().
				append(message).
				append(variants.size()==1?"":"one of: ").
				append(VariantUtils.toString(variants)).
				toString();
	}

	static <T extends ContentProcessingException> Response getFailureResponse(
			Status status,
			String message,
			T throwable) {
		ResponseBuilder builder=
			Response.
				status(status).
				language(Locale.ENGLISH).
				type(MediaType.TEXT_PLAIN).
				entity(
					getFailureMessage(
						message,
						throwable.getSupportedVariants()));
		EndpointControllerUtils.populateProtocolEndorsedHeaders(builder,throwable.resourceLastModified(),throwable.resourceEntityTag());
		EndpointControllerUtils.populateProtocolSpecificHeaders(builder,throwable.resourceClass());
		return builder.build();
	}

}
