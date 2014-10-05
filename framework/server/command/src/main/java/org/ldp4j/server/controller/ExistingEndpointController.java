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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.ApplicationExecutionException;
import org.ldp4j.application.Capabilities;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.server.api.Entity;
import org.ldp4j.server.api.ImmutableContext;
import org.ldp4j.server.spi.ContentTransformationException;

import com.google.common.base.Throwables;

final class ExistingEndpointController extends AbstractEndpointController {

	private static final String CONTENT_LENGTH_HEADER = "Content-Length";

	public ExistingEndpointController(Endpoint endpoint) {
		super(endpoint);
	}

	public Response options(OperationContext context) {
		Capabilities capabilities=context.endpointCapabilities(); 
		ResponseBuilder builder=
			Response.
				ok();
		EndpointControllerUtils.populateAllowedHeaders(builder, capabilities);
		EndpointControllerUtils.populateProtocolEndorsedHeaders(builder, endpoint());
		EndpointControllerUtils.populateProtocolSpecificHeaders(builder, context.resourceType());
		return builder.build();
	}

	public Response head(OperationContext context) {
		return doGet(context, false);
	}

	public Response getResource(OperationContext context) {
		return doGet(context, true);
	}
	
	public Response deleteResource(OperationContext context) {
		// 1. verify that we can carry out the operation
		context.
			checkOperationSupport().
			checkPreconditions();
	
		Response response=null;
		try {
			context.applicationContext().deleteResource(endpoint());
			response=Response.noContent().type(MediaType.TEXT_PLAIN).build();
			// TODO: This could be improved by returning an OK with an
			// additional description of all the resources that were deleted
			// as a side effect.
		} catch (ApplicationExecutionException e) {
			ResponseBuilder builder = 
				Response.
					serverError().
					entity(Throwables.getStackTraceAsString(e));
			EndpointControllerUtils.populateProtocolEndorsedHeaders(builder, endpoint());
			EndpointControllerUtils.populateProtocolSpecificHeaders(builder, context.resourceType());
			response=builder.build();
		}
		return response;
	}

	@Override
	public Response modifyResource(OperationContext context) {
		// 1. verify that we can carry out the operation
		context.
			checkOperationSupport().
			checkContents().
			checkPreconditions();
	
		ResponseBuilder builder=Response.serverError();
		String body=null;
		Status status=null;
	
		// 2. determine the body and status of the response
		try {
			context.applicationContext().modifyResource(endpoint(),context.dataSet());
			status=Status.NO_CONTENT;
			body="";
		} catch (ApplicationExecutionException e) {
			status=Status.INTERNAL_SERVER_ERROR;
			body=Throwables.getStackTraceAsString(e);
			builder.type(MediaType.TEXT_PLAIN);
		}
		// 3. add protocol endorsed headers
		EndpointControllerUtils.populateProtocolEndorsedHeaders(builder, endpoint());
		
		// 4. add protocol specific headers
		EndpointControllerUtils.populateProtocolSpecificHeaders(builder,context.resourceType());
		
		// 5. set status and attach response entity as required.
		builder.
			status(status.getStatusCode()).
			header(ExistingEndpointController.CONTENT_LENGTH_HEADER, body.length());
		builder.entity(body);
		return builder.build();
	}

	@Override
	public Response patchResource(OperationContext context) {
		// Verify that we can carry out the operation
		context.
			checkOperationSupport().
			checkContents().
			checkPreconditions();
	
		// Fail as we do not support PATCH yet
		ResponseBuilder builder = 
			Response.
				status(Status.INTERNAL_SERVER_ERROR);
		EndpointControllerUtils.populateProtocolEndorsedHeaders(builder, endpoint());
		EndpointControllerUtils.populateProtocolSpecificHeaders(builder, context.resourceType());
		return builder.build();
	}

	public Response createResource(OperationContext context) {
		// 1. verify that we can carry out the operation
		context.
			checkOperationSupport().
			checkContents().
			checkPreconditions();

		DataSet dataSet=context.dataSet();
		
		Response response=null;
		try {
			Resource newResource = 
				context.
					applicationContext().createResource(endpoint(),dataSet);
			URI location = context.resolve(newResource);
			response=Response.created(location).entity(location).type(MediaType.TEXT_PLAIN).build();
		} catch (ApplicationExecutionException e) {
			response=Response.serverError().entity(Throwables.getStackTraceAsString(e)).build();
		}
		return response;
	}

	private Response doGet(OperationContext context, boolean includeEntity) {
		// 1. validate output expectations
		Variant variant=context.expectedVariant();
	
		// 2. verify that we can carry out the operation
		context.
			checkOperationSupport().
			checkPreconditions();
	
		ResponseBuilder builder=Response.serverError();
		String body=null;
		Status status=null;
	
		// 3. Determine the body and status of the response
		try {
			// 3.1. retrieve the resource
			DataSet resource = context.applicationContext().getResource(endpoint());
			// 3.2. prepare the associated entity
			Entity entity=
				context.
					createEntity(
						resource);
			// 3.3. serialize the entity
			body=entity.serialize(variant,ImmutableContext.newInstance(context.base().resolve(endpoint().path()),context.resourceIndex()));
			status=entity.isEmpty()?Status.NO_CONTENT:Status.OK;
			builder.type(variant.getMediaType());
		} catch (ContentTransformationException e) {
			status=Status.INTERNAL_SERVER_ERROR;
			body=Throwables.getStackTraceAsString(e);
			builder.type(MediaType.TEXT_PLAIN);
		} catch (ApplicationExecutionException e) {
			status=Status.INTERNAL_SERVER_ERROR;
			body=Throwables.getStackTraceAsString(e);
			builder.type(MediaType.TEXT_PLAIN);
		}
		// 4. add protocol endorsed headers
		EndpointControllerUtils.populateProtocolEndorsedHeaders(builder, endpoint());
		
		// 5. add protocol specific headers
		EndpointControllerUtils.populateProtocolSpecificHeaders(builder,context.resourceType());
		
		// 6. set status and attach response entity as required.
		builder.
			status(status.getStatusCode()).
			header(ExistingEndpointController.CONTENT_LENGTH_HEADER, body.length());
		if(includeEntity) {
			builder.entity(body);
		}
		return builder.build();
	}

}