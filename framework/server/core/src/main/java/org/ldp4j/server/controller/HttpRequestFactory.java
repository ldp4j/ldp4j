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

import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.engine.context.HttpRequest.HttpMethod;
import org.ldp4j.application.sdk.HttpRequestBuilder;

final class HttpRequestFactory {

	private HttpRequestFactory() {
	}

	static HttpRequest create(HttpMethod method, UriInfo uriInfo, HttpHeaders headers, String entity) {
		HttpRequestBuilder builder=
			HttpRequestBuilder.
				newInstance().
					withMethod(method).
					withServerDate(new Date()).
					withClientDate(headers.getDate()).
					withBody(entity).
					withAbsolutePath(uriInfo.getAbsolutePath().getPath()).
					withHost(uriInfo.getAbsolutePath().getAuthority());
		createHeaders(builder,headers);
		return builder.build();
	}

	private static void createHeaders(HttpRequestBuilder builder, HttpHeaders headers) {
		for(Entry<String, List<String>> entry:headers.getRequestHeaders().entrySet()) {
			for(String rawValue:entry.getValue()) {
				if(rawValue!=null) {
					builder.withHeader(entry.getKey(), rawValue);
				}
			}
		}
	}

}