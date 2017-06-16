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

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.ldp4j.application.engine.context.Capabilities;
import org.ldp4j.application.engine.context.HttpRequest.HttpMethod;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class MethodNotAllowedException extends DiagnosedException {

	private static final long serialVersionUID = -3661009844197939466L;

	private final HttpMethod operation;

	public MethodNotAllowedException(OperationContext operationContext, HttpMethod method) {
		super(
			operationContext,
			null,
			Diagnosis.
				create().
					statusCode(Status.METHOD_NOT_ALLOWED).
					diagnostic(
						"Endpoint '%s' does not support %s. It only supports: %s",
						resourceLocation(operationContext),
						method,
						toHttpMethods(resourceCapabilities(operationContext)))
		);
		this.operation = method;
	}

	public HttpMethod getMethod() {
		return operation;
	}

	private static String toHttpMethods(Capabilities capabilities) {
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
