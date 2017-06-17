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

import java.nio.charset.Charset;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.ldp4j.server.utils.VariantUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class NotAcceptableException extends DiagnosedException {

	private static final long serialVersionUID = 6897847237787548607L;

	private final List<String> supportedCharsets;

	private NotAcceptableException(OperationContext context, List<String> supportedCharsets) {
		super(
			context,
			null,
			Diagnosis.
				create().
					statusCode(Status.NOT_ACCEPTABLE).
					diagnostic(
						EndpointControllerUtils.
							getAcceptableContent(
								VariantUtils.defaultVariants(),
								resourceLocation(context),
								supportedCharsets))
		);
		this.supportedCharsets=supportedCharsets;
	}

	public NotAcceptableException(OperationContext context) {
		this(context,getNames(context.supportedCharsets()));
	}

	public List<String> supportedCharsets() {
		return this.supportedCharsets;
	}

	private static List<String> getNames(List<Charset> supportedCharsets) {
		Builder<String> builder=ImmutableList.<String>builder();
		for(Charset supportedCharset:supportedCharsets) {
			builder.add(supportedCharset.name());
		}
		return builder.build();
	}

}