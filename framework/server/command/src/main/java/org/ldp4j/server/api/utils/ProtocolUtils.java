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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.api.utils;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant;

import org.ldp4j.server.api.EndpointHelper;
import org.ldp4j.server.resources.Resource;
import org.ldp4j.server.resources.ResourceType;

public final class ProtocolUtils {

	private ProtocolUtils() {
	}

	/**
	 * Get a text/plain representation that indicates the acceptable media types
	 * for the specified resource. The current implementation <b>only</b> takes
	 * care of the media type, nor language, nor encodings are taken into
	 * consideration for generating the acceptable content.
	 * 
	 * @param variants
	 *            The acceptable variants
	 * @param resourceLocation
	 *            The location of the resource
	 * @return A content instance that outlines the acceptable media types and
	 *         the locations from where the resource contents for those media
	 *         types can be retrieved.
	 */
	public static String getAcceptableContent(
			List<Variant> variants,
			URI resourceLocation) {
		StringBuilder builder = new StringBuilder();
		for (Variant variant : variants) {
			builder.append(variant.getMediaType()).append(" : %1$s%n");
		}
		return 
			String.format(
				builder.toString(),
				resourceLocation
			);
	}

	public static String createLink(Object uriRef, Object rel) {
		return String.format("<%s>; rel='%s'",uriRef,rel);
	}

	public static void populateEndorsedHeaders(Resource resource, ResponseBuilder builder) {
		builder.header(HttpHeaders.LAST_MODIFIED,resource.lastModified());
		builder.header(HttpHeaders.ETAG, resource.entityTag());
	}

	public static void  populateSpecificHeaders(Resource resource, ResponseBuilder builder) {
		ResourceType type = resource.type();
		// LDP 1.0 - 5.2.1.4 : "LDP servers exposing LDPCs must advertise
		// their LDP support by exposing a HTTP Link header with a target
		// URI matching the type of container (see below) the server
		// supports, and a link relation type of type (that is, rel='type')
		// in all responses to requests made to the LDPC's HTTP Request-URI"
		builder.header(EndpointHelper.LINK_HEADER,createLink(type, "type"));
		if(type.isContainer()) {
			// LDP 1.0 - 5.2.1.4 : "LDP servers may provide additional HTTP
			// Link: rel='type' headers"
			builder.header(EndpointHelper.LINK_HEADER,createLink(ResourceType.RESOURCE,"type"));
		}
	}

}
