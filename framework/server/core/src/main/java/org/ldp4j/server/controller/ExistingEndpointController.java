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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-server-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.engine.context.ApplicationContextException;
import org.ldp4j.application.engine.context.ApplicationExecutionException;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.OperationPrecondititionException;
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
import org.ldp4j.application.ext.Parameter;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.ext.UnknownResourceException;
import org.ldp4j.application.ext.UnsupportedContentException;
import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.rdf.Namespaces;
import org.ldp4j.server.utils.VariantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

final class ExistingEndpointController implements EndpointController {


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

	ExistingEndpointController() {
	}

	private void addRequiredHeaders(OperationContext context, ResponseBuilder builder) {
		PublicResource resource = context.resource();
		EndpointControllerUtils.
			populateProtocolEndorsedHeaders(builder,resource.lastModified(),resource.entityTag());
		EndpointControllerUtils.
			populateProtocolSpecificHeaders(builder,resource.getClass());
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

		Response response=null;
		Query query = context.getQuery();
		if(!query.isEmpty()) {
			response=handleQuery(context,includeEntity,variant,query);
		} else {
			response=handleRetrieval(context,includeEntity,variant,query);
		}

		return response;

	}

	private Response handleRetrieval(OperationContext context, boolean includeEntity, Variant variant, Query query) {
		try {
			PublicResource resource=context.resource();
			ContentPreferences preferences=context.contentPreferences();
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
			DataSet entity=
				query.isEmpty()?
					resource.entity(preferences):
					resource.query(query, preferences);

			LOGGER.trace("Data set to serialize: \n {}",entity);

			String body=
				serialize(
					context,
					variant,
					entity,
					NamespacesHelper.
						resourceNamespaces(context.applicationNamespaces()));

			return createReatrievalResponse(context,variant,hasPreferences,preferences, includeEntity, body,query);
		} catch (ApplicationExecutionException e) {
			return processExecutionException(context, e);
		} catch (ApplicationContextException e) {
			return processRuntimeException(context, e);
		}
	}

	private Response createReatrievalResponse(OperationContext context,
			Variant variant, boolean hasPreferences,
			ContentPreferences preferences, boolean includeEntity,
			String entity, Query query) {
		ResponseBuilder builder=Response.serverError();
		builder.variant(variant);
		if(hasPreferences) {
			builder.header(ContentPreferencesUtils.PREFERENCE_APPLIED_HEADER,ContentPreferencesUtils.asPreferenceAppliedHeader(preferences));
		}
		addOptionsMandatoryHeaders(context, builder);
		builder.
			status(Status.OK.getStatusCode()).
			header(ExistingEndpointController.CONTENT_LENGTH_HEADER, entity.length());
		if(includeEntity) {
			builder.entity(entity);
		}
		if(!query.isEmpty()) {
			builder.
				header(
					HttpHeaders.LINK,
					EndpointControllerUtils.
						createQueryOfLink(
							context.base().resolve(context.path()),
							query));
		}
		return builder.build();
	}

	private String serialize(OperationContext context, Variant variant, DataSet entity, Namespaces namespaces) {
		return context.serialize(entity,namespaces,variant.getMediaType());
	}

	private Response handleQuery(OperationContext context, boolean includeEntity, Variant variant, Query query) {
		Response response=null;
		if(query.hasParameter(CONSTRAINT_QUERY_PARAMETER)) {
			if(query.size()==1) {
				Parameter parameter=query.getParameter(CONSTRAINT_QUERY_PARAMETER);
				response=processConstraintReportRetrieval(context,includeEntity,variant,parameter);
			} else {
				ResponseBuilder builder=
						Response.
							status(Status.BAD_REQUEST).
							type(MediaType.TEXT_PLAIN).
							language(Locale.ENGLISH).
							entity("Mixed queries not allowed");
				addRequiredHeaders(context, builder);
				response=builder.build();
			}
		} else if(!context.isResourceQueryable()) {
			ResponseBuilder builder=
					Response.
						status(Status.BAD_REQUEST).
						type(MediaType.TEXT_PLAIN).
						language(Locale.ENGLISH).
						entity("Resource cannot be queried");
			addRequiredHeaders(context, builder);
			response=builder.build();
		} else {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Executing query: ");
				for(String parameter:query.parameterNames()) {
					LOGGER.debug("  - {} : {}",parameter,query.getParameter(parameter).rawValues());
				}
			}
			response=handleRetrieval(context,includeEntity,variant,query);
		}
		return response;
	}

	private Response processConstraintReportRetrieval(OperationContext context, boolean includeEntity, Variant variant, Parameter parameter) {
		Response response=null;
		if(parameter.cardinality()==1) {
			response=handleConstraintReportRetrieval(context,includeEntity,variant,parameter.rawValue());
		} else {
			ResponseBuilder builder=
				Response.
					status(Status.BAD_REQUEST).
					type(MediaType.TEXT_PLAIN).
					language(Locale.ENGLISH).
					entity("Only one constraint identifier allowed");
			addRequiredHeaders(context, builder);
			response=builder.build();
		}
		return response;
	}

	private Response handleConstraintReportRetrieval(OperationContext context, boolean includeEntity, Variant variant, String constraintReportId) {
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

			String body=serialize(context, variant, report, NamespacesHelper.constraintReportNamespaces(context.applicationNamespaces()));

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
		ResponseBuilder builder=Response.serverError();

		int statusCode=0;

		String body=null;
		Throwable rootCause = exception.getCause();
		if(rootCause instanceof InvalidContentException) {
			InvalidContentException ice=(InvalidContentException)rootCause;
			if(rootCause instanceof InconsistentContentException) {
				statusCode=Status.CONFLICT.getStatusCode();
				body="Specified values for application-managed properties are not consistent with the actual resource state: "+rootCause.getMessage();
			} else if(rootCause instanceof UnsupportedContentException) {
				statusCode=UNPROCESSABLE_ENTITY_STATUS_CODE;
				body="Could not understand content: "+rootCause.getMessage();
			} else {
				statusCode=Status.BAD_REQUEST.getStatusCode();
				body=Throwables.getStackTraceAsString(rootCause);
			}
			if(ice.getConstraintsId()==null) {
				LOGGER.error("No constraints identifier defined. Full stacktrace follows",exception);
				throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode());
			}
			builder.header(
				HttpHeaders.LINK,
				EndpointControllerUtils.
					createLink(
						context.base()+context.path()+"?ldp:constrainedBy="+ice.getConstraintsId(),
						LDP.CONSTRAINED_BY.qualifiedEntityName()));
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

	private Response processUnsupportedInteractionModelException(OperationContext context, UnsupportedInteractionModelException e) {
		ResponseBuilder builder=
			Response.
				status(Status.FORBIDDEN).
				type(MediaType.TEXT_PLAIN).
				language(Locale.ENGLISH).
				entity(e.getMessage());
		addRequiredHeaders(context, builder);
		return builder.build();
	}

	private Response processOperationPreconditionException(OperationContext context, OperationPrecondititionException e) {
		ResponseBuilder builder=
			Response.
				status(UNPROCESSABLE_ENTITY_STATUS_CODE).
				type(MediaType.TEXT_PLAIN).
				language(Locale.ENGLISH).
				entity(e.getMessage());
		addRequiredHeaders(context, builder);
		return builder.build();
	}

	@Override
	public Response options(OperationContext context) {
		ResponseBuilder builder=
			Response.
				ok();
		addOptionsMandatoryHeaders(context, builder);
		return builder.build();
	}

	@Override
	public Response head(OperationContext context) {
		return doGet(context, false);
	}

	@Override
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
		ResponseBuilder builder=Response.serverError();
		addRequiredHeaders(context, builder);
		return builder.build();
	}

	@Override
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
		} catch (ApplicationExecutionException e) {
			return processExecutionException(context, e);
		} catch (UnsupportedInteractionModelException e) {
			return processUnsupportedInteractionModelException(context, e);
		} catch (OperationPrecondititionException e) {
			return processOperationPreconditionException(context,e);
		} catch (ApplicationContextException e) {
			return processRuntimeException(context, e);
		}

	}
}