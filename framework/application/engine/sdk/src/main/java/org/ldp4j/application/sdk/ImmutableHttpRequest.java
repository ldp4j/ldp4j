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

import java.util.Date;
import java.util.List;

import org.ldp4j.application.engine.context.HttpRequest;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

final class ImmutableHttpRequest implements HttpRequest {

	private static final long serialVersionUID = 8395472376889237517L;

	private final HttpMethod method;
	private final ProtocolVersion protocolVersion;
	private final String host;
	private final String absolutePath;
	private final String body;
	private final ImmutableList<Header> headers;
	private final Date serverDate;
	private final Date clientDate;

	ImmutableHttpRequest(HttpMethod method, ProtocolVersion protocolVersion, String absolutePath, String host, List<ImmutableHeader> headers, String body, Date serverDate, Date clientaDate) { // NOSONAR
		this.protocolVersion = protocolVersion;
		this.method=method;
		this.absolutePath = absolutePath;
		this.host = host;
		this.headers = ImmutableList.<Header>copyOf(headers);
		this.body = body;
		this.serverDate = serverDate;
		this.clientDate = clientaDate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpMethod method() {
		return this.method;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String absolutePath() {
		return this.absolutePath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String host() {
		return this.host;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProtocolVersion protocolVersion() {
		return this.protocolVersion;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImmutableList<Header> headers() {
		return this.headers;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String body() {
		return this.body;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date serverDate() {
		return DateUtil.copy(this.serverDate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date clientDate() {
		return DateUtil.copy(this.clientDate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(HttpRequest.class).
					omitNullValues().
					add("method",this.method).
					add("absolutePath",this.absolutePath).
					add("host",this.host).
					add("protocolVersion",this.protocolVersion).
					add("serverDate",this.serverDate).
					add("clientDate",this.clientDate).
					add("headers",this.headers).
					add("body",this.body).
					toString();
	}

}