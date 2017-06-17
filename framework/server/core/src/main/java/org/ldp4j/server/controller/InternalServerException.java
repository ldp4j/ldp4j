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

import javax.ws.rs.core.Response.Status;

import com.google.common.base.Throwables;


public class InternalServerException extends DiagnosedException {

	private static final long serialVersionUID = -7935564305394686915L;

	public InternalServerException(OperationContext context, Throwable cause) {
		this(context,null,cause);
	}

	public InternalServerException(OperationContext context, String message, Throwable cause) {
		super(
			context,
			cause,
			Diagnosis.
				create().
				statusCode(Status.INTERNAL_SERVER_ERROR).
				diagnostic(toString(message,cause)).
				mandatory(true)
		);
	}

	private static String toString(String message, Throwable cause) {
		StringBuilder builder=new StringBuilder();
		if(message!=null) {
			builder.append(message);
		}
		if(cause!=null) {
			if(builder.length()>0) {
				builder.append(System.lineSeparator());
			}
			builder.append(Throwables.getStackTraceAsString(cause));
		}
		if(builder.length()==0) {
			builder.append("Unexpected application failure");
		}
		return builder.toString();
	}

}
