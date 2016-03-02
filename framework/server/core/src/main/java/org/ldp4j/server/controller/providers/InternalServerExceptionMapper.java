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
package org.ldp4j.server.controller.providers;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ldp4j.server.controller.EndpointControllerUtils;
import org.ldp4j.server.controller.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

@Provider
public class InternalServerExceptionMapper implements ExceptionMapper<InternalServerException> {

	private static final Logger LOGGER=LoggerFactory.getLogger(InternalServerExceptionMapper.class);

	@Override
	public Response toResponse(InternalServerException throwable) {
		final String body = toString(throwable);
		LOGGER.error(body);
		return
			EndpointControllerUtils.
				prepareErrorResponse(
					throwable,
					body,
					Status.INTERNAL_SERVER_ERROR.getStatusCode());
	}

	private String toString(InternalServerException throwable) {
		StringBuilder builder=new StringBuilder();
		if(throwable.getMessage()!=null) {
			builder.append(throwable.getMessage());
		}
		if(throwable.getCause()!=null) {
			if(builder.length()==0) {
				builder.append(System.lineSeparator());
			}
			builder.append(Throwables.getStackTraceAsString(throwable.getCause()));
		}
		return builder.toString();
	}

}
