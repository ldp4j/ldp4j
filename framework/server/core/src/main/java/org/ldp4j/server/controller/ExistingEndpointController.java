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

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.domain.LDP;
import org.ldp4j.application.engine.context.ApplicationContext;
import org.ldp4j.application.engine.context.ApplicationExecutionException;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.PublicBasicContainer;
import org.ldp4j.application.engine.context.PublicContainer;
import org.ldp4j.application.engine.context.PublicDirectContainer;
import org.ldp4j.application.engine.context.PublicIndirectContainer;
import org.ldp4j.application.engine.context.PublicRDFSource;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.application.engine.context.PublicResourceVisitor;
import org.ldp4j.application.engine.context.UnsupportedInteractionModelException;
import org.ldp4j.application.ext.ContentProcessingException;
import org.ldp4j.application.ext.InconsistentContentException;
import org.ldp4j.application.ext.InvalidContentException;
import org.ldp4j.server.utils.VariantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

final class ExistingEndpointController extends AbstractEndpointController {


	private static final String ACCEPT_POST_HEADER = "Accept-Post";

	/**
	 * The status code to signal that the content could not be understood by the
	 * application.
	 *
	 * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.2">RFC 4918:
	 *      HTTP Extensions for Web Distributed Authoring and Versioning
	 *      (WebDAV)</a>
	 */
	private static final int UNPROCESSABLE_ENTITY_STATUS_CODE = 422;

	private static final String CONTENT_LENGTH_HEADER = "Content-Length";

	private static final Logger LOGGER=LoggerFactory.getLogger(ExistingEndpointController.class);

	ExistingEndpointController(ApplicationContext applicationContext, PublicResource resource) {
		super(applicationContext,resource);
	}

	private void addRequiredHeaders(OperationContext context, ResponseBuilder builder) {
		EndpointControllerUtils.
			populateProtocolEndorsedHeaders(builder, context.resource());
		EndpointControllerUtils.
			populateProtocolSpecificHeaders(builder, context.resource());
	}

	private void addOptionsMandatoryHeaders(OperationContext context, ResponseBuilder builder) {
		addRequiredHeaders(context,builder);
		EndpointControllerUtils.
			populateAllowedHeaders(builder, context.resource().capabilities());
		addAcceptPostHeaders(context, builder);
	}


	private void addAcceptPostHeaders(OperationContext context, ResponseBuilder builder) {
		List<Variant> acceptPostVariants=
			context.
			resource().
				accept(new PublicResourceVisitor<List<Variant>>() {
					@Override
					public List<Variant> visitRDFSource(PublicRDFSource resource) {
						return Collections.emptyList();
					}
					@Override
					public List<Variant> visitBasicContainer(PublicBasicContainer resource) {
						return VariantUtils.defaultVariants();
					}
					@Override
					public List<Variant> visitDirectContainer(PublicDirectContainer resource) {
						return VariantUtils.defaultVariants();
					}
					@Override
					public List<Variant> visitIndirectContainer(PublicIndirectContainer resource) {
						return VariantUtils.defaultVariants();
					}
				}
			);
		/**
		 * 5.2.3.14
		 */
		for(Variant variant:acceptPostVariants) {
			builder.header(ACCEPT_POST_HEADER,variant.getMediaType());
		}
	}

	private Response doGet(OperationContext context, boolean includeEntity) {
		// 1. Validate output expectations
		Variant variant=context.expectedVariant();

		// 2. Verify that we can carry out the operation
		context.
			checkOperationSupport().
			checkPreconditions();

		ResponseBuilder builder=Response.serverError();
		String body=null;
		Status status=null;

		// 3. Determine the body and status of the response
		try {
			// 3.1. retrieve the resource
			PublicResource resource=context.resource();
			ContentPreferences preferences=
					context.contentPreferences();
			boolean hasPreferences=preferences!=null;
			if(!hasPreferences) {
				preferences=ContentPreferences.defaultPreferences();
			}
			if(LOGGER.isDebugEnabled()) {
				if(hasPreferences) {
					LOGGER.debug("Using preferences: {}",preferences);
				} else {
					LOGGER.debug("No preferences specified");
				}
			}
			// 3.2. prepare the associated entity
			DataSet entity=resource.entity(preferences);
			LOGGER.trace("Data set to serialize: \n {}",entity);
			// 3.3. serialize the entity
			body=context.serialize(entity,variant.getMediaType());
			status=Status.OK;
			builder.variant(variant);
			if(hasPreferences) {
				builder.header(ContentPreferencesUtils.PREFERENCE_APPLIED_HEADER,ContentPreferencesUtils.asPreferenceAppliedHeader(preferences));
			}
		} catch (ApplicationExecutionException e) {
			status=Status.INTERNAL_SERVER_ERROR;
			body=Throwables.getStackTraceAsString(e);
			builder.
				type(MediaType.TEXT_PLAIN).
				language(Locale.ENGLISH);
		}

		// 4. Add the required headers
		addOptionsMandatoryHeaders(context, builder);

		// 5. Complete the response
		builder.
			status(status.getStatusCode()).
			header(ExistingEndpointController.CONTENT_LENGTH_HEADER, body.length());
		if(includeEntity) {
			builder.entity(body);
		}
		return builder.build();
	}

	public Response options(OperationContext context) {
		ResponseBuilder builder=
			Response.
				ok();
		addOptionsMandatoryHeaders(context, builder);
		return builder.build();
	}

	public Response head(OperationContext context) {
		return doGet(context, false);
	}

	public Response getResource(OperationContext context) {
		return doGet(context, true);
	}

	@Override
	public Response deleteResource(OperationContext context) {
		// 1. Verify that we can carry out the operation
		context.
			checkOperationSupport().
			checkPreconditions();

		ResponseBuilder builder=
			Response.serverError();

		Status status=Status.INTERNAL_SERVER_ERROR;

		// 2. Execute operation and determine response body and status
		try {
			context.resource().delete();
			status=Status.NO_CONTENT;
			// TODO: This could be improved by returning an OK with an
			// additional description of all the resources that were deleted
			// as a side effect.
		} catch (ApplicationExecutionException e) {
			String body=Throwables.getStackTraceAsString(e);
			builder.
				type(MediaType.TEXT_PLAIN).
				language(Locale.ENGLISH).
				header(ExistingEndpointController.CONTENT_LENGTH_HEADER, body.length()).
				entity(body);
			// 2.a. Add response headers
			addRequiredHeaders(context, builder);
		}

		// 3. Complete response
		builder.status(status.getStatusCode());

		return builder.build();
	}

	@Override
	public Response modifyResource(OperationContext context) {
		// 1. Verify that we can carry out the operation
		context.
			checkOperationSupport().
			checkContents().
			checkPreconditions();

		ResponseBuilder builder=Response.serverError();
		int statusCode = Status.INTERNAL_SERVER_ERROR.getStatusCode();

		// 2. Execute operation and determine response body and status
		try {
			context.resource().modify(context.dataSet());
			statusCode=Status.NO_CONTENT.getStatusCode();
			// TODO: This could be improved by returning an OK with an
			// additional description of all the resources that were modified
			// (updated, created, deleted) as a side effect.
		} catch (ApplicationExecutionException e) {
			String body=Throwables.getStackTraceAsString(e);
			Throwable rootCause = Throwables.getRootCause(e);
			if(rootCause instanceof ContentProcessingException) {
				if(rootCause instanceof InconsistentContentException) {
					statusCode=Status.CONFLICT.getStatusCode();
					body="Specified values for application-managed properties are not consistent with the actual resource state"+rootCause.getMessage();
				} else if(rootCause instanceof InvalidContentException) {
					statusCode=UNPROCESSABLE_ENTITY_STATUS_CODE;
					body="Could not understand content: "+rootCause.getMessage();
				} else {
					statusCode=Status.BAD_REQUEST.getStatusCode();
					body=Throwables.getStackTraceAsString(rootCause);
				}
				builder.header("Link",EndpointControllerUtils.createLink(context.base(), LDP.CONSTRAINED_BY.qualifiedEntityName()));
			}
			builder.
				type(MediaType.TEXT_PLAIN).
				language(Locale.ENGLISH).
				header(ExistingEndpointController.CONTENT_LENGTH_HEADER, body.length()).
				entity(body);
		}

		// 3. Add the response headers
		addRequiredHeaders(context, builder);

		// 4. set status and attach response entity as required.
		builder.status(statusCode);

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
			Response.serverError();
		addRequiredHeaders(context, builder);
		return builder.build();
	}

	public Response createResource(OperationContext context) {
		// 1. verify that we can carry out the operation
		context.
			checkOperationSupport().
			checkContents().
			checkPreconditions();

		ResponseBuilder builder=Response.serverError();
		String body=null;
		Status status=null;

		// 2. Execute operation and determine response body and status
		try {
			PublicContainer container=context.container();
			PublicResource newResource =
				container.createResource(context.dataSet(), context.creationPreferences());
			URI location = context.resolve(newResource);
			status=Status.CREATED;
			body=location.toString();
			// 2.1 Add Location header with the URI of the just created resource
			builder.header(HttpHeaders.LOCATION, location.toString());
		} catch (UnsupportedInteractionModelException e) {
			status=Status.FORBIDDEN;
			body=e.getMessage();
			builder.language(Locale.ENGLISH);
		} catch (ApplicationExecutionException e) {
			status=Status.INTERNAL_SERVER_ERROR;
			body=Throwables.getStackTraceAsString(e);
			builder.language(Locale.ENGLISH);
		}

		// 3. Add required headers
		addRequiredHeaders(context, builder);

		// 4. Complete response.
		builder.
			status(status.getStatusCode()).
			header(ExistingEndpointController.CONTENT_LENGTH_HEADER, body.length()).
			entity(body).
			type(MediaType.TEXT_PLAIN);

		return builder.build();
	}
}