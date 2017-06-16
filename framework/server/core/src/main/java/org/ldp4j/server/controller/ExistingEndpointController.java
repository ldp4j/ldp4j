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

import java.net.URI;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.engine.context.ApplicationContextException;
import org.ldp4j.application.engine.context.ApplicationExecutionException;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.OperationPrecondititionException;
import org.ldp4j.application.engine.context.PublicContainer;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.application.engine.context.UnsupportedInteractionModelException;
import org.ldp4j.application.ext.ApplicationRuntimeException;
import org.ldp4j.application.ext.InvalidContentException;
import org.ldp4j.application.ext.InvalidQueryException;
import org.ldp4j.application.ext.Parameter;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.ext.UnknownResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

final class ExistingEndpointController implements EndpointController {

	private static final class DefaultContentPreferencesSupplier implements Supplier<ContentPreferences> {
		@Override
		public ContentPreferences get() {
			return ContentPreferences.defaultPreferences();
		}
	}

	private static final String NO_CONSTRAINT_REPORT_ID_DEFINED_ERROR = "No constraint report identifier defined. Full stacktrace follows";

	private static final Logger LOGGER=LoggerFactory.getLogger(ExistingEndpointController.class);

	ExistingEndpointController() {
	}

	private void addOptionsMandatoryHeaders(OperationContext context, ResponseBuilder builder) {
		EndpointControllerUtils.populateRequiredHeaders(builder, context);
		EndpointControllerUtils.populateAllowedHeaders(builder, context.resource().capabilities());
	}

	private ResponseBuilder prepareRetrievalResponse(
			OperationContext context,
			Variant variant,
			DataSet entity,
			boolean includeEntity) {
		String body=
			context.serialize(
				entity,
				NamespacesHelper.
					constraintReportNamespaces(
						context.applicationNamespaces()),
				variant.getMediaType());
		ResponseBuilder builder=Response.ok();
		EndpointControllerUtils.
			populateResponseBody(
				builder,
				body,
				variant,
				includeEntity);
		return builder;
	}

	private Response prepareResourceRetrievalResponse(
			OperationContext context,
			Variant variant,
			DataSet entity,
			boolean includeEntity) {
		ResponseBuilder builder=prepareRetrievalResponse(context, variant, entity, includeEntity);
		addOptionsMandatoryHeaders(context, builder);

		ContentPreferences preferences = context.contentPreferences();
		if(preferences!=null) {
			builder.
				header(
					ContentPreferencesUtils.PREFERENCE_APPLIED_HEADER,
					ContentPreferencesUtils.asPreferenceAppliedHeader(preferences));
		}

		Query query=context.getQuery();
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

	private Response prepareConstraintReportRetrievalResponse(
			OperationContext context,
			Variant variant,
			DataSet report,
			boolean includeEntity) {
		return
			prepareRetrievalResponse(context,variant,report,includeEntity).
				build();
	}

	private Response handleRetrieval(OperationContext context, boolean includeEntity) {
		final Variant variant=context.expectedVariant();
		context.
			checkOperationSupport().
			checkPreconditions();
		switch(RetrievalScenario.forContext(context)) {
			case MIXED_QUERY:
				throw new MixedQueryNotAllowedException(context,includeEntity);
			case QUERY_NOT_SUPPORTED:
				throw new QueryingNotSupportedException(context,includeEntity);
			case CONSTRAINT_REPORT_RETRIEVAL:
				return handleConstraintReportRetrieval(context,includeEntity,variant);
			default:
				return handleResourceRetrieval(context,includeEntity,variant);
		}
	}

	private Response handleResourceRetrieval(OperationContext context, boolean includeEntity, Variant variant) {
		try {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(EndpointControllerUtils.retrievalLog(context));
			}
			PublicResource resource=context.resource();
			Query query=context.getQuery();
			ContentPreferences preferences =
				Optional.
					fromNullable(context.contentPreferences()).
					or(new DefaultContentPreferencesSupplier());
			DataSet entity=
				query.isEmpty()?
					resource.entity(preferences):
					resource.query(query,preferences);
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace(EndpointControllerUtils.retrievalResultLog(entity));
			}
			return prepareResourceRetrievalResponse(context,variant,entity,includeEntity);
		} catch (ApplicationExecutionException e) {
			throw diagnoseApplicationExecutionException(context, e);
		} catch (ApplicationContextException e) {
			throw new InternalServerException(context,e);
		}
	}

	private Response handleConstraintReportRetrieval(OperationContext context, boolean includeEntity, Variant variant) {
		Parameter parameter=RetrievalScenario.constraintReportId(context);
		if(parameter.cardinality()!=1) {
			throw new InvalidConstraintReportRetrievalException(context,parameter.rawValues(),includeEntity);
		}
		String constraintReportId = parameter.rawValue();
		try {
			LOGGER.debug("Retrieving constraint report: {}",constraintReportId);

			PublicResource resource=context.resource();
			DataSet report=resource.getConstraintReport(constraintReportId);
			if(report==null) {
				throw new UnknownConstraintReportException(context,constraintReportId,includeEntity);
			}

			LOGGER.trace("Constraint report to serialize:\n{}",report);
			return prepareConstraintReportRetrievalResponse(context,variant,report,includeEntity);
		} catch (ApplicationExecutionException e) {
			throw diagnoseApplicationExecutionException(context, e);
		} catch (ApplicationContextException e) {
			throw new InternalServerException(context,e);
		}
	}

	private OperationContextException diagnoseApplicationExecutionException(OperationContext context, ApplicationExecutionException exception) {
		OperationContextException result = null;
		final Throwable rootCause=exception.getCause();
		if(rootCause instanceof InvalidContentException) {
			final InvalidContentException ice = (InvalidContentException)exception.getCause();
			if(ice.getConstraintsId()==null) {
				result=new InternalServerException(context,NO_CONSTRAINT_REPORT_ID_DEFINED_ERROR,ice);
			} else {
				result=new InvalidContentDiagnosedException(context,ice);
			}
		} else if (rootCause instanceof UnknownResourceException) {
			throw new WebApplicationException(Status.NOT_FOUND);
		} else if (rootCause instanceof InvalidQueryException) {
			result=new InvalidQueryDiagnosedException(context, (InvalidQueryException)rootCause);
		} else if (rootCause instanceof ApplicationRuntimeException) {
			result=new InternalServerException(context,rootCause);
		} else {
			result=new InternalServerException(context,exception);
		}
		return result;
	}

	@Override
	public Response options(OperationContext context) {
		ResponseBuilder builder=Response.ok();
		addOptionsMandatoryHeaders(context, builder);
		return builder.build();
	}

	@Override
	public Response head(OperationContext context) {
		return handleRetrieval(context, false);
	}

	@Override
	public Response getResource(OperationContext context) {
		return handleRetrieval(context, true);
	}

	/**
	 * TODO: This could be improved by returning an OK with an additional
	 * description of all the resources that were deleted as a side effect.
	 */
	@Override
	public Response deleteResource(OperationContext context) {
		context.
			checkOperationSupport().
			checkPreconditions();
		try {
			context.resource().delete();
			return Response.noContent().build();
		} catch (ApplicationExecutionException e) {
			throw diagnoseApplicationExecutionException(context, e);
		} catch (ApplicationContextException e) {
			throw new InternalServerException(context,e);
		}
	}

	/**
	 * TODO: This could be improved by returning an OK with an additional
	 * description of all the resources that were modified (updated, created,
	 * deleted) as a side effect.
	 */
	@Override
	public Response modifyResource(OperationContext context) {
		context.
			checkOperationSupport().
			checkContents().
			checkPreconditions();
		try {
			context.resource().modify(context.dataSet());
			ResponseBuilder builder=Response.noContent();
			EndpointControllerUtils.populateRequiredHeaders(builder, context);
			return builder.build();
		} catch (ApplicationExecutionException e) {
			throw diagnoseApplicationExecutionException(context, e);
		} catch (ApplicationContextException e) {
			throw new InternalServerException(context,e);
		}
	}

	@Override
	public Response patchResource(OperationContext context) {
		// Verify that we can carry out the operation
		context.
			checkOperationSupport().
			checkContents().
			checkPreconditions();
		// Fail as we do not support PATCH
		throw new NotImplementedException(context);
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
				container.createResource(context.dataSet(),context.creationPreferences());
			URI location = context.resolve(newResource);
			ResponseBuilder builder=Response.created(location);
			EndpointControllerUtils.populateRequiredHeaders(builder, context);
			EndpointControllerUtils.
				populateResponseBody(
					builder,
					location.toString(),
					EndpointControllerUtils.textResponseVariant(),
					true);
			return builder.build();
		} catch (ApplicationExecutionException e) {
			throw diagnoseApplicationExecutionException(context, e);
		} catch (UnsupportedInteractionModelException e) {
			throw new UnsupportedInteractionModelDiagnosedException(context, e);
		} catch (OperationPrecondititionException e) {
			throw new OperationPrecondititionModelDiagnosedException(context, e);
		} catch (ApplicationContextException e) {
			throw new InternalServerException(context,e);
		}
	}
}