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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.2
 *   Bundle      : ldp4j-application-kernel-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.constraints;

import java.util.List;

import com.google.common.collect.ImmutableList;

final class HeaderHelper {

	private static final ImmutableList<String> GENERAL_HEADERS=
		ImmutableList.
			<String>builder().
				add("Cache-Control").
				add("Connection").
				add("Date").
				add("Pragma").
				add("Trailer").
				add("Transfer-Encoding").
				add("Upgrade").
				add("Via").
				add("Warning").
				build();

	private static final ImmutableList<String> REQUEST_HEADERS=
		ImmutableList.
			<String>builder().
				add("Accept").
				add("Accept-Charset").
				add("Accept-Encoding").
				add("Accept-Language").
				add("Authorization").
				add("Expect").
				add("From").
				add("Host").
				add("If-Match").
				add("If-Modified-Since").
				add("If-None-Match").
				add("If-Range").
				add("If-Unmodified-Since").
				add("Max-Forwards").
				add("Proxy-Authorization").
				add("Range").
				add("Referer").
				add("TE").
				add("User-Agent").
				build();

	private static final ImmutableList<String> ENTITY_HEADERS=
		ImmutableList.
			<String>builder().
				add("Allow").
				add("Content-Encoding").
				add("Content-Language").
				add("Content-Length").
				add("Content-Location").
				add("Content-MD5").
				add("Content-Range").
				add("Content-Type").
				add("Expires").
				add("Last-Modified").
				build();

	private HeaderHelper() {
	}

	static String getHeaderType(String name) {
		if(matches(name, GENERAL_HEADERS)) {
			return "GeneralHeader";
		} else if(matches(name, REQUEST_HEADERS)) {
			return "RequestHeader";
		} else if(matches(name, ENTITY_HEADERS)) {
			return "EntityHeader";
		}
		return "MessageHeader";
	}

	private static boolean matches(String name, List<String> headers) {
		boolean result=false;
		for(String header:headers) {
			if(header.equalsIgnoreCase(name)) {
				result=true;
				break;
			}
		}
		return result;
	}

}