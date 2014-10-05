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
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.ldp4j.application.ApplicationExecutionException;
import org.ldp4j.application.Capabilities;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.server.api.Entity;
import org.ldp4j.server.api.ImmutableContext;
import org.ldp4j.server.api.spi.ContentTransformationException;
import org.ldp4j.server.api.spi.RuntimeInstance;
import org.ldp4j.server.api.utils.ProtocolUtils;
import org.ldp4j.server.api.utils.VariantUtils;
import org.ldp4j.server.resources.ResourceType;

import com.google.common.base.Throwables;

final class ExistingEndpointController extends AbstractEndpointController {

	static final String ENTITY_TAG_HEADER     = "ETag";
	static final String LAST_MODIFIED_HEADER  = "Last-Modified";
	static final String CONTENT_LENGTH_HEADER = "Content-Length";
	static final String LINK_HEADER           = "Link";
	static final String ACCEPT_POST_HEADER    = "Accept-Post";
	static final String ALLOW_HEADER          = "Allow";

	public ExistingEndpointController(Endpoint endpoint) {
		super(endpoint);
	}

	public Response options(OperationContext context) {
		Capabilities capabilities=context.endpointCapabilities(); 
		ResponseBuilder builder=
				Response.
					ok();
		// LDP 1.0 - 4.2.8.2 : "LDP servers must indicate their support for HTTP
		// Methods by responding to a HTTP OPTIONS request on the
		// LDPR’s URL with the HTTP Method tokens in the HTTP
		// response header Allow."
		addAllowedMethodHeader(builder,"OPTIONS",true);
		addAllowedMethodHeader(builder,"HEAD",true);
		addAllowedMethodHeader(builder,"GET",true);
		addAllowedMethodHeader(builder,"PUT",capabilities.isModifiable());
		addAllowedMethodHeader(builder,"PATCH",capabilities.isPatchable());
		addAllowedMethodHeader(builder,"DELETE",capabilities.isDeletable());
		addAllowedMethodHeader(builder,"POST",capabilities.isFactory());
		if(capabilities.isFactory()) {
			// LDP 1.0 - 7.1.2 : "The Accept-Post HTTP header should appear
			// in the OPTIONS response for any resource that supports the
			// use of the POST method."
			for(MediaType mediaType:RuntimeInstance.getInstance().getSupportedMediaTypes()) {
				builder.header(ExistingEndpointController.ACCEPT_POST_HEADER,mediaType.toString());
			}
		}
		populateProtocolEndorsedHeaders(builder);
		populateProtocolSpecificHeaders(builder, context.resourceType());
		return builder.build();
	}

	public Response head(OperationContext context) {
		return doGet(context, false);
	}

	public Response getResource(OperationContext context) {
		return doGet(context, true);
	}
	
	@Override
	public Response modifyResource(OperationContext context) {
	
		if(!context.endpointCapabilities().isModifiable()) {
			ResponseBuilder builder = 
				Response.
					status(Status.METHOD_NOT_ALLOWED);
			populateProtocolEndorsedHeaders(builder);
			populateProtocolSpecificHeaders(builder, context.resourceType());
			return builder.build();
		}
	
		// 1. enforce valid state
		context.evaluatePreconditions(endpoint().entityTag(),endpoint().lastModified());
	
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
		populateProtocolEndorsedHeaders(builder);
		
		// 4. add protocol specific headers
		populateProtocolSpecificHeaders(builder,context.resourceType());
		
		// 5. set status and attach response entity as required.
		builder.
			status(status.getStatusCode()).
			header(ExistingEndpointController.CONTENT_LENGTH_HEADER, body.length());
		builder.entity(body);
		return builder.build();
	}

	@Override
	public Response patchResource(OperationContext context) {
		if(!context.endpointCapabilities().isPatchable()) {
			ResponseBuilder builder = 
				Response.
					status(Status.METHOD_NOT_ALLOWED);
			populateProtocolEndorsedHeaders(builder);
			populateProtocolSpecificHeaders(builder, context.resourceType());
			return builder.build();
		}
	
		ResponseBuilder builder = 
			Response.
				status(Status.INTERNAL_SERVER_ERROR);
		populateProtocolEndorsedHeaders(builder);
		populateProtocolSpecificHeaders(builder, context.resourceType());
		return builder.build();
	}

	public Response deleteResource(OperationContext context) {
		if(!context.endpointCapabilities().isDeletable()) {
			ResponseBuilder builder = 
				Response.
					status(Status.METHOD_NOT_ALLOWED);
			populateProtocolEndorsedHeaders(builder);
			populateProtocolSpecificHeaders(builder, context.resourceType());
			return builder.build();
		}
		// Enforce valid state
		context.evaluatePreconditions(endpoint().entityTag(),endpoint().lastModified());
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
			populateProtocolEndorsedHeaders(builder);
			populateProtocolSpecificHeaders(builder, context.resourceType());
			response=builder.build();
		}
		return response;
	}
	
	public Response createResource(OperationContext context) {
		// Validate input request
		final DataSet dataSet=context.dataSet();
	
		// Enforce valid state
		context.evaluatePreconditions(endpoint().entityTag(),endpoint().lastModified());
	
		Response response=null;
		try {
			Resource newResource = context.applicationContext().createResource(endpoint(),dataSet);
			URI location = context.resolve(newResource);
			response=Response.created(location).entity(location).type(MediaType.TEXT_PLAIN).build();
		} catch (ApplicationExecutionException e) {
			response=Response.serverError().entity(Throwables.getStackTraceAsString(e)).build();
		}
		return response;
	}

	private void addAllowedMethodHeader(ResponseBuilder builder, String method, boolean flag) {
		if(flag) {
			builder.header(ExistingEndpointController.ALLOW_HEADER,method);
		}
	}

	private Response doGet(OperationContext context, boolean includeEntity) {
		// 1. validate output expectations
		Variant variant=context.expectedVariant(supportedVariants());

		// 2. enforce valid state
		context.evaluatePreconditions(endpoint().entityTag(),endpoint().lastModified());

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
		populateProtocolEndorsedHeaders(builder);
		
		// 5. add protocol specific headers
		populateProtocolSpecificHeaders(builder,context.resourceType());
		
		// 6. set status and attach response entity as required.
		builder.
			status(status.getStatusCode()).
			header(ExistingEndpointController.CONTENT_LENGTH_HEADER, body.length());
		if(includeEntity) {
			builder.entity(body);
		}
		return builder.build();
	}

	private void populateProtocolEndorsedHeaders(ResponseBuilder builder) {
		builder.header(ExistingEndpointController.LAST_MODIFIED_HEADER,endpoint().lastModified());
		builder.header(ExistingEndpointController.ENTITY_TAG_HEADER,endpoint().entityTag());
	}

	private void populateProtocolSpecificHeaders(ResponseBuilder builder, ResourceType type) {
		// LDP 1.0 - 5.2.1.4 : "LDP servers exposing LDPCs must advertise
		// their LDP support by exposing a HTTP Link header with a target
		// URI matching the type of container (see below) the server
		// supports, and a link relation type of type (that is, rel='type')
		// in all responses to requests made to the LDPC's HTTP Request-URI"
		builder.header(ExistingEndpointController.LINK_HEADER,ProtocolUtils.createLink(type, "type"));
		if(type.isContainer()) {
			// LDP 1.0 - 5.2.1.4 : "LDP servers may provide additional HTTP
			// Link: rel='type' headers"
			builder.header(ExistingEndpointController.LINK_HEADER,ProtocolUtils.createLink(ResourceType.RESOURCE,"type"));
		}
	}

	private List<Variant> supportedVariants() {
		return VariantUtils.defaultVariants();
	}

}