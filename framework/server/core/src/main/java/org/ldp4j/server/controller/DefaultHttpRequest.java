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

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.ldp4j.application.engine.context.HttpRequest;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

final class DefaultHttpRequest implements HttpRequest {

	private static final class HeaderImpl implements Header {

		private final String rawValue;
		private final String name;

		private HeaderImpl(String rawValue, String name) {
			this.rawValue = rawValue;
			this.name = name;
		}

		@Override
		public String name() {
			return this.name;
		}

		@Override
		public String rawValue() {
			return this.rawValue;
		}

		@Override
		public List<Element> elements() {
			return Collections.emptyList();
		}

		@Override
		public String toString() {
			return
				Objects.
					toStringHelper(Header.class).
						add("name",this.name).
						add("rawValue",this.rawValue).
						toString();
		}

	}

	private final String entity;
	private final List<Header> headers;
	private final HttpMethod method;
	private final String absolutePath;
	private final String host;

	private DefaultHttpRequest(HttpMethod method, String absolutePath, String host, List<Header> headers, String entity) {
		this.method=method;
		this.absolutePath = absolutePath;
		this.host = host;
		this.headers = headers;
		this.entity = entity;
	}

	@Override
	public HttpMethod method() {
		return this.method;
	}

	@Override
	public String absolutePath() {
		return this.absolutePath;
	}

	@Override
	public String host() {
		return this.host;
	}

	@Override
	public ProtocolVersion version() {
		return ProtocolVersion.HTTP_1_1;
	}

	@Override
	public List<Header> headers() {
		return this.headers;
	}

	@Override
	public String body() {
		return this.entity;
	}

	@Override
	public String toString() {
		return
			Objects.
				toStringHelper(HttpRequest.class).
					omitNullValues().
					add("method",this.method).
					add("absolutePath",this.absolutePath).
					add("host",this.host).
					add("version",version()).
					add("headers",this.headers).
					add("entity",this.entity).
					toString();
	}

	static final HttpRequest create(HttpMethod method, UriInfo uriInfo, HttpHeaders headers, String entity) {
		return new DefaultHttpRequest(method,uriInfo.getPath(false),uriInfo.getAbsolutePath().getAuthority(),transformHeaders(headers),entity);
	}

	private static List<Header> transformHeaders(HttpHeaders headers) {
		List<Header> result=Lists.newArrayList();
		for(Entry<String, List<String>> header:headers.getRequestHeaders().entrySet()) {
			for(String value:header.getValue()) {
				result.add(new HeaderImpl(value, header.getKey()));
			}
		}
		return ImmutableList.copyOf(result);
	}

}