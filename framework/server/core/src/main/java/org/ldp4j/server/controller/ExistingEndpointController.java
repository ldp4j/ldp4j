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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.domain.LDP;
import org.ldp4j.application.engine.context.ApplicationContextException;
import org.ldp4j.application.engine.context.ApplicationContextOperation;
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
import org.ldp4j.application.ext.ApplicationRuntimeException;
import org.ldp4j.application.ext.InconsistentContentException;
import org.ldp4j.application.ext.InvalidContentException;
import org.ldp4j.application.ext.UnknownResourceException;
import org.ldp4j.application.ext.UnsupportedContentException;
import org.ldp4j.server.utils.VariantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

final class ExistingEndpointController extends AbstractEndpointController {


	private static final String CONSTRAINT_QUERY_PARAMETER = "ldp:constrainedBy";

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

	ExistingEndpointController(ApplicationContextOperation applicationContextOperation, PublicResource resource) {
		super(applicationContextOperation,resource);
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
		Variant variant=context.expectedVariant();

		context.
			checkOperationSupport().
			checkPreconditions();

		Response response=tryQuery(context,includeEntity,variant);
		if(response!=null) {
			return response;
		}

		try {
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
			DataSet entity=resource.entity(preferences);

			LOGGER.trace("Data set to serialize: \n {}",entity);

			String body=context.serialize(entity,variant.getMediaType());

			ResponseBuilder builder=Response.serverError();
			builder.variant(variant);
			if(hasPreferences) {
				builder.header(ContentPreferencesUtils.PREFERENCE_APPLIED_HEADER,ContentPreferencesUtils.asPreferenceAppliedHeader(preferences));
			}
			addOptionsMandatoryHeaders(context, builder);
			builder.
				status(Status.OK.getStatusCode()).
				header(ExistingEndpointController.CONTENT_LENGTH_HEADER, body.length());
			if(includeEntity) {
				builder.entity(body);
			}
			return builder.build();
		} catch (ApplicationExecutionException e) {
			return processExecutionException(context, e);
		} catch (ApplicationContextException e) {
			return processRuntimeException(context, e);
		}

	}

	private Response tryQuery(OperationContext context, boolean includeEntity, Variant variant) {
		if(!context.isQuery()) {
			return null;
		}
		List<String> allParameters = context.getQueryParameters();
		if(allParameters.contains(CONSTRAINT_QUERY_PARAMETER)) {
			if(allParameters.size()==1) {
				List<String> constraintIds=context.getQueryParameterValues(CONSTRAINT_QUERY_PARAMETER);
				if(constraintIds.size()==1) {
					return processConstraintReportRetrieval(context,includeEntity,variant,constraintIds.get(0));
				} else {
					ResponseBuilder builder=
						Response.
							status(Status.BAD_REQUEST).
							type(MediaType.TEXT_PLAIN).
							language(Locale.ENGLISH).
							entity("Only one constraint identifier allowed");
					addRequiredHeaders(context, builder);
					return builder.build();
				}
			} else {
				ResponseBuilder builder=
						Response.
							status(Status.BAD_REQUEST).
							type(MediaType.TEXT_PLAIN).
							language(Locale.ENGLISH).
							entity("Mixed queries not allowed");
				addRequiredHeaders(context, builder);
				return builder.build();
			}
		} else {
			return processQuery(context,includeEntity,variant);
		}
	}

	private Response processQuery(OperationContext context, boolean includeEntity, Variant variant) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Executing query: ");
			for(String parameter:context.getQueryParameters()) {
				LOGGER.debug("  - {} : {}",parameter,context.getQueryParameterValues(parameter));
			}
		}
		try {
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
			// TODO: Update with actual query functionality when available
			DataSet entity=resource.entity(preferences);

			LOGGER.trace("Data set to serialize: \n {}",entity);

			String body=context.serialize(entity,variant.getMediaType());

			ResponseBuilder builder=Response.serverError();
			builder.variant(variant);
			if(hasPreferences) {
				builder.header(ContentPreferencesUtils.PREFERENCE_APPLIED_HEADER,ContentPreferencesUtils.asPreferenceAppliedHeader(preferences));
			}
			addOptionsMandatoryHeaders(context, builder);
			builder.
				status(Status.OK.getStatusCode()).
				header(ExistingEndpointController.CONTENT_LENGTH_HEADER, body.length());
			if(includeEntity) {
				builder.entity(body);
			}
			return builder.build();
		} catch (ApplicationExecutionException e) {
			return processExecutionException(context, e);
		} catch (ApplicationContextException e) {
			return processRuntimeException(context, e);
		}
	}

	private Response processConstraintReportRetrieval(OperationContext context, boolean includeEntity, Variant variant, String constraintReportId) {
		try {
			PublicResource resource=context.resource();
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Retrieving constraints: "+constraintReportId);
			}

			DataSet report=resource.getConstraintReport(constraintReportId);
			if(report==null) {
				ResponseBuilder builder=
					Response.
						status(Status.BAD_REQUEST).
						type(MediaType.TEXT_PLAIN).
						language(Locale.ENGLISH).
						entity("Unknown constraints '"+constraintReportId+"'");
				addRequiredHeaders(context, builder);
				return builder.build();
			}

			LOGGER.trace("Constraints to serialize: \n {}",report);

			String body=context.serialize(report,variant.getMediaType());

			ResponseBuilder builder=Response.serverError();
			builder.variant(variant);
			builder.
				status(Status.OK.getStatusCode()).
				header(ExistingEndpointController.CONTENT_LENGTH_HEADER, body.length());
			if(includeEntity) {
				builder.entity(body);
			}
			return builder.build();
		} catch (ApplicationExecutionException e) {
			return processExecutionException(context, e);
		} catch (ApplicationContextException e) {
			return processRuntimeException(context, e);
		}
	}

	private Response processRuntimeException(OperationContext context, ApplicationContextException exception) {
		ResponseBuilder builder=
			Response.
				serverError().
				type(MediaType.TEXT_PLAIN).
				language(Locale.ENGLISH).
				entity(Throwables.getStackTraceAsString(exception));
		addRequiredHeaders(context, builder);
		return builder.build();
	}

	private Response processExecutionException(OperationContext context, ApplicationExecutionException exception) {
		ResponseBuilder builder=
			Response.serverError();

		int statusCode=0;

		String body=null;
		Throwable rootCause = exception.getCause();
		if(rootCause instanceof InvalidContentException) {
			InvalidContentException ice=(InvalidContentException)rootCause;
			if(rootCause instanceof InconsistentContentException) {
				statusCode=Status.CONFLICT.getStatusCode();
				body="Specified values for application-managed properties are not consistent with the actual resource state "+rootCause.getMessage();
			} else if(rootCause instanceof UnsupportedContentException) {
				statusCode=UNPROCESSABLE_ENTITY_STATUS_CODE;
				body="Could not understand content: "+rootCause.getMessage();
			} else {
				statusCode=Status.BAD_REQUEST.getStatusCode();
				body=Throwables.getStackTraceAsString(rootCause);
			}
			builder.header("Link",EndpointControllerUtils.createLink(context.base()+context.path()+"?ldp:constrainedBy="+ice.getConstraintsId(), LDP.CONSTRAINED_BY.qualifiedEntityName()));
		} else if (rootCause instanceof UnknownResourceException) {
			statusCode=Status.NOT_FOUND.getStatusCode();
			body="Resource not found";
		} else if (rootCause instanceof ApplicationRuntimeException) {
			statusCode=Status.INTERNAL_SERVER_ERROR.getStatusCode();
			body=Throwables.getStackTraceAsString(rootCause);
		}
		builder.
			type(MediaType.TEXT_PLAIN).
			language(Locale.ENGLISH).
			header(ExistingEndpointController.CONTENT_LENGTH_HEADER, body.length()).
			entity(body);
		addRequiredHeaders(context, builder);
		builder.status(statusCode);
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
		context.
			checkOperationSupport().
			checkPreconditions();
		try {
			context.resource().delete();
			// TODO: This could be improved by returning an OK with an
			// additional description of all the resources that were deleted
			// as a side effect.
			return Response.noContent().build();
		} catch (ApplicationExecutionException e) {
			return processExecutionException(context, e);
		} catch (ApplicationContextException e) {
			return processRuntimeException(context, e);
		}
	}

	@Override
	public Response modifyResource(OperationContext context) {
		context.
			checkOperationSupport().
			checkContents().
			checkPreconditions();
		try {
			context.resource().modify(context.dataSet());
			// TODO: This could be improved by returning an OK with an
			// additional description of all the resources that were modified
			// (updated, created, deleted) as a side effect.
			ResponseBuilder builder=Response.noContent();
			addRequiredHeaders(context, builder);
			return builder.build();
		} catch (ApplicationExecutionException e) {
			return processExecutionException(context, e);
		} catch (ApplicationContextException e) {
			return processRuntimeException(context, e);
		}

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
		context.
			checkOperationSupport().
			checkContents().
			checkPreconditions();

		try {
			PublicContainer container=context.container();
			PublicResource newResource =
				container.createResource(context.dataSet(), context.creationPreferences());
			URI location = context.resolve(newResource);
			ResponseBuilder builder=
				Response.
					created(location).
					type(MediaType.TEXT_PLAIN).
					entity(location.toString());
			addRequiredHeaders(context, builder);
			return builder.build();
		} catch (UnsupportedInteractionModelException e) {
			ResponseBuilder builder=
				Response.
					status(Status.FORBIDDEN).
					type(MediaType.TEXT_PLAIN).
					language(Locale.ENGLISH).
					entity(e.getMessage());
			addRequiredHeaders(context, builder);
			return builder.build();
		} catch (ApplicationExecutionException e) {
			return processExecutionException(context, e);
		} catch (ApplicationContextException e) {
			return processRuntimeException(context, e);
		}

	}
}