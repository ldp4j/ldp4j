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
package org.ldp4j.server.controller;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import org.ldp4j.server.utils.VariantUtils;

public class ContentProcessingException extends DiagnosedException {

	private static final long serialVersionUID = -7271633668400276805L;

	public ContentProcessingException(String message, OperationContext context, Status statusCode) {
		this(message,null,context,statusCode);
	}

	public ContentProcessingException(String message, Throwable cause, OperationContext context, Status statusCode) {
		super(
			context,
			cause,
			Diagnosis.
				create().
					statusCode(statusCode).
					diagnostic(getFailureMessage(message,VariantUtils.defaultVariants())));
	}

	static String getFailureMessage(String message, List<Variant> variants) {
		return
			new StringBuilder().
				append(message).
				append(variants.size()==1?"":"one of: ").
				append(VariantUtils.toString(variants)).
				toString();
	}

	public final List<Variant> getSupportedVariants() {
		return Collections.unmodifiableList(VariantUtils.defaultVariants());
	}

}
