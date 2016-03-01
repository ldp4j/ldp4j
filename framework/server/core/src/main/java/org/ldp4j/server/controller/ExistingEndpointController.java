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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

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
import org.ldp4j.application.engine.context.PublicContainer;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.application.engine.context.UnsupportedInteractionModelException;
import org.ldp4j.application.ext.ApplicationRuntimeException;
import org.ldp4j.application.ext.InconsistentContentException;
import org.ldp4j.application.ext.InvalidContentException;
import org.ldp4j.application.ext.InvalidQueryException;
import org.ldp4j.application.ext.Parameter;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.ext.UnknownResourceException;
import org.ldp4j.application.ext.UnsupportedContentException;
import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.rdf.Namespaces;
import org.ldp4j.server.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

final class ExistingEndpointController implements EndpointController {

	private static final String NO_CONSTRAINT_REPORT_ID_DEFINED_ERROR = "No constraint report identifier defined. Full stacktrace follows";

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

	private String serialize(OperationContext context, Variant variant, DataSet entity, Namespaces namespaces) {
		return context.serialize(entity,namespaces,variant.getMediaType());
	}

	private Variant textResponseVariant() {
		return
			Variant.
				languages(Locale.ENGLISH).
				mediaTypes(MediaType.TEXT_PLAIN_TYPE.withCharset(StandardCharsets.UTF_8.name())).
				add().
				build().
					get(0);
	}

	private Variant errorResponseVariant() {
		return textResponseVariant();
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
		/**
		 * 5.2.3.14
		 */
		for(Variant variant:EndpointControllerUtils.getAcceptPostVariants(context.resource())) {
			builder.header(ACCEPT_POST_HEADER,variant.getMediaType());
		}
	}

	private void addResponseEntity(ResponseBuilder builder, String entity, Variant variant, boolean includeEntity) {
		MediaType mediaType = variant.getMediaType();

		String charsetName=mediaType.getParameters().get(MediaType.CHARSET_PARAMETER);
		Charset charset=StandardCharsets.UTF_8;
		if(charsetName!=null && !charsetName.isEmpty() && Charset.isSupported(charsetName)) {
			charset=Charset.forName(charsetName);
		} else {
			LOGGER.error("Missing of invalid charset information {}",mediaType);
			charsetName=charset.name();
		}

		MediaType target=
			Configuration.includeCharsetInformation()?
				mediaType.withCharset(charsetName):
				new MediaType(mediaType.getType(),mediaType.getSubtype());

		byte[] bytes = entity.getBytes(charset);
		builder.
			type(target).
			header(ExistingEndpointController.CONTENT_LENGTH_HEADER,bytes.length);

		if(variant.getLanguage()!=null) {
			builder.language(variant.getLanguage());
		}

		if(includeEntity) {
			builder.entity(new ByteArrayInputStream(bytes));
		}
	}

	private Response prepareErrorResponse(OperationContext context, Status statusCode, String message, boolean includeEntity) {
		return prepareErrorResponse(context, statusCode.getStatusCode(), message, includeEntity);
	}

	private Response prepareErrorResponse(OperationContext context, int statusCode, String message, boolean includeEntity) {
		ResponseBuilder builder=Response.status(statusCode);
		addResponseEntity(builder, message, errorResponseVariant(), includeEntity);
		addRequiredHeaders(context, builder);
		return builder.build();
	}

	private Response prepareRetrievalResponse(
			OperationContext context,
			Variant variant,
			boolean hasPreferences,
			ContentPreferences preferences,
			boolean includeEntity,
			String entity,
			Query query) {
		ResponseBuilder builder=Response.serverError();
		builder.variant(variant);
		if(hasPreferences) {
			builder.header(ContentPreferencesUtils.PREFERENCE_APPLIED_HEADER,ContentPreferencesUtils.asPreferenceAppliedHeader(preferences));
		}
		addOptionsMandatoryHeaders(context, builder);
		builder.status(Status.OK.getStatusCode());
		addResponseEntity(builder,entity,variant,includeEntity);
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

	private Response prepareConstraintReportRetrievalResponse(OperationContext context, boolean includeEntity, Variant variant, String constraintReportId) {
		try {
			PublicResource resource=context.resource();
			LOGGER.debug("Retrieving constraint report: {}",constraintReportId);

			DataSet report=resource.getConstraintReport(constraintReportId);
			if(report==null) {
				return prepareErrorResponse(context, Status.NOT_FOUND, "Unknown constraint report '"+constraintReportId+"'", includeEntity);
			}

			LOGGER.trace("Constraint report to serialize:\n{}",report);
			String body=serialize(context,variant,report,NamespacesHelper.constraintReportNamespaces(context.applicationNamespaces()));
			ResponseBuilder builder=Response.ok();
			addResponseEntity(builder, body, variant, includeEntity);
			return builder.build();
		} catch (ApplicationExecutionException e) {
			return processExecutionException(context, e);
		} catch (ApplicationContextException e) {
			return processRuntimeException(context, e);
		}
	}

	private Response handleRetrieval(OperationContext context, boolean includeEntity) {
		final Variant variant=context.expectedVariant();
		context.
			checkOperationSupport().
			checkPreconditions();

		switch(RetrievalScenario.forContext(context)) {
			case MIXED_QUERY:
				return prepareErrorResponse(context,Status.BAD_REQUEST,"Mixed queries not allowed",includeEntity);
			case QUERY_NOT_SUPPORTED:
				return prepareErrorResponse(context,Status.FORBIDDEN,"Resource cannot be queried",includeEntity);
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
			ContentPreferences preferences = context.contentPreferences();
			boolean hasPreferences = preferences==null;
			if(hasPreferences) {
				preferences=ContentPreferences.defaultPreferences();
			}
			DataSet entity=
				query.isEmpty()?
					resource.entity(preferences):
					resource.query(query,preferences);

			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace(EndpointControllerUtils.retrievalResultLog(entity));
			}

			String body=
				serialize(
					context,
					variant,
					entity,
					NamespacesHelper.
						resourceNamespaces(context.applicationNamespaces()));

			return prepareRetrievalResponse(context,variant,hasPreferences,preferences,includeEntity,body,query);
		} catch (ApplicationExecutionException e) {
			return processExecutionException(context, e);
		} catch (ApplicationContextException e) {
			return processRuntimeException(context, e);
		}
	}

	private Response handleConstraintReportRetrieval(OperationContext context, boolean includeEntity, Variant variant) {
		Parameter parameter=RetrievalScenario.constraintReportId(context);
		if(parameter.cardinality()==1) {
			return prepareConstraintReportRetrievalResponse(context,includeEntity,variant,parameter.rawValue());
		} else {
			return prepareErrorResponse(context,Status.BAD_REQUEST,"Only one constraint report identifier is allowed",includeEntity);
		}
	}

	private Response processInvalidContentException(OperationContext context, InvalidContentException rootCause) {
		ResponseBuilder builder=Response.serverError();
		String body=null;
		if(rootCause instanceof InconsistentContentException) {
			builder.status(Status.CONFLICT);
			body="Specified values for application-managed properties are not consistent with the actual resource state: "+rootCause.getMessage();
		} else if(rootCause instanceof UnsupportedContentException) {
			builder.status(UNPROCESSABLE_ENTITY_STATUS_CODE);
			body="Could not understand content: "+rootCause.getMessage();
		} else {
			builder.status(Status.BAD_REQUEST.getStatusCode());
			body=Throwables.getStackTraceAsString(rootCause);
		}
		builder.header(
			HttpHeaders.LINK,
			EndpointControllerUtils.
				createLink(
					RetrievalScenario.constraintReportLink(context,rootCause.getConstraintsId()),
					LDP.CONSTRAINED_BY.qualifiedEntityName()));
		addResponseEntity(builder,body,errorResponseVariant(),true);
		addRequiredHeaders(context,builder);
		return builder.build();
	}

	private Response processExecutionException(OperationContext context, ApplicationExecutionException exception) {
		final Throwable rootCause = exception.getCause();
		if(rootCause instanceof InvalidContentException) {
			final InvalidContentException ice=(InvalidContentException)exception.getCause();
			if(ice.getConstraintsId()==null) {
				LOGGER.error(NO_CONSTRAINT_REPORT_ID_DEFINED_ERROR,exception);
				return prepareErrorResponse(
						context,
						Status.INTERNAL_SERVER_ERROR,
						NO_CONSTRAINT_REPORT_ID_DEFINED_ERROR+"\n"+Throwables.getStackTraceAsString(exception),
						true);
			}
			return processInvalidContentException(context,ice);
		} else if (rootCause instanceof UnknownResourceException) {
			return prepareErrorResponse(context,Status.NO_CONTENT,"Resource not found",true);
		} else if (rootCause instanceof InvalidQueryException) {
			return prepareErrorResponse(context,Status.BAD_REQUEST,"Invalid query: "+rootCause.getMessage(),true);
		} else if (rootCause instanceof ApplicationRuntimeException) {
			return prepareErrorResponse(context,Status.INTERNAL_SERVER_ERROR,Throwables.getStackTraceAsString(rootCause),true);
		} else {
			return prepareErrorResponse(context,Status.INTERNAL_SERVER_ERROR,Throwables.getStackTraceAsString(exception),true);
		}
	}

	private Response processRuntimeException(OperationContext context, ApplicationContextException exception) {
		return prepareErrorResponse(context,Status.INTERNAL_SERVER_ERROR,Throwables.getStackTraceAsString(exception),true);
	}

	private Response processUnsupportedInteractionModelException(OperationContext context, UnsupportedInteractionModelException e) {
		return prepareErrorResponse(context,Status.FORBIDDEN,e.getMessage(),true);
	}

	private Response processOperationPreconditionException(OperationContext context, OperationPrecondititionException e) {
		return prepareErrorResponse(context,UNPROCESSABLE_ENTITY_STATUS_CODE,e.getMessage(),true);
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
			return processExecutionException(context, e);
		} catch (ApplicationContextException e) {
			return processRuntimeException(context, e);
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
		return prepareErrorResponse(context, Status.METHOD_NOT_ALLOWED,"Patch is not supported",true);
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
			addResponseEntity(builder, location.toString(), textResponseVariant(), true);
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