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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-sdk:0.2.2
 *   Bundle      : ldp4j-application-engine-sdk-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.sdk;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;

import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.engine.context.HttpRequest.HttpMethod;
import org.ldp4j.application.engine.context.HttpRequest.ProtocolVersion;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public final class HttpRequestBuilder {

	private final List<ImmutableHeader> headers;
	private HttpMethod method;
	private String absolutePath;
	private String host;
	private String body;
	private Date serverDate;
	private Date clientDate;
	private ProtocolVersion protocolVersion;

	private HttpRequestBuilder() {
		this.headers=Lists.newArrayList();
	}

	public HttpRequestBuilder withMethod(HttpMethod method) {
		this.method = method;
		return this;
	}

	public HttpRequestBuilder withProtocolVersion(ProtocolVersion version) {
		this.protocolVersion = version;
		return this;
	}

	public HttpRequestBuilder withHost(String host) {
		this.host = host;
		return this;
	}

	public HttpRequestBuilder withAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
		return this;
	}

	public HttpRequestBuilder withBody(String entity) {
		this.body = entity;
		return this;
	}

	public HttpRequestBuilder withHeader(String name, String rawValue) {
		ImmutableHeader header =
			new ImmutableHeader(
				checkNotNull(name,"Header name cannot be null"),
				checkNotNull(rawValue,"Header raw value cannot be null"),
				Lists.<ImmutableElement>newArrayList());
		if(!this.headers.contains(header)) {
			this.headers.add(header);
		}
		return this;
	}

	public HttpRequestBuilder withServerDate(Date date) {
		this.serverDate=DateUtil.copy(date);
		return this;
	}

	public HttpRequestBuilder withClientDate(Date date) {
		this.clientDate=DateUtil.copy(date);
		return this;
	}

	public HttpRequest build() {
		return
			new ImmutableHttpRequest(
				checkNotNull(this.method,"Http method cannot be null"),
				Optional.fromNullable(this.protocolVersion).or(ProtocolVersion.HTTP_1_1),
				checkNotNull(this.absolutePath,"Absolute path cannot be null"),
				checkNotNull(this.host,"Host cannot be null"),
				this.headers,
				this.body,
				Optional.fromNullable(this.serverDate).or(new Date()),
				this.clientDate);
	}

	public static HttpRequestBuilder newInstance() {
		return new HttpRequestBuilder();
	}

}
