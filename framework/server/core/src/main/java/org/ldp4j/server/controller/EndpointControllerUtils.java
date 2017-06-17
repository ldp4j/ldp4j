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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.engine.context.Capabilities;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.engine.context.PublicBasicContainer;
import org.ldp4j.application.engine.context.PublicDirectContainer;
import org.ldp4j.application.engine.context.PublicIndirectContainer;
import org.ldp4j.application.engine.context.PublicRDFSource;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.application.vocabulary.Term;
import org.ldp4j.server.config.Configuration;
import org.ldp4j.server.utils.VariantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public final class EndpointControllerUtils {

	public abstract static class ResponseEnricher implements Function<ResponseBuilder,ResponseBuilder> {

		@Override
		public final ResponseBuilder apply(ResponseBuilder builder) {
			enrich(builder);
			return builder;
		}

		protected abstract void enrich(ResponseBuilder builder);

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(EndpointControllerUtils.class);

	private static final String NL=System.lineSeparator();

	private EndpointControllerUtils() {
	}

	private static void addAllowedMethodHeader(ResponseBuilder builder, String method, boolean flag) {
		if(flag) {
			builder.header(MoreHttp.ALLOW_HEADER,method);
		}
	}

	private static void addProtocolEndorsedHeaders(ResponseBuilder builder, Date lastModified, EntityTag entityTag) {
		builder.header(MoreHttp.LAST_MODIFIED_HEADER,lastModified);
		builder.header(MoreHttp.ENTITY_TAG_HEADER,entityTag);
	}

	// LDP 1.0 - 5.2.1.4 : "LDP servers exposing LDPCs must advertise
	// their LDP support by exposing a HTTP Link header with a target
	// URI matching the type of container (see below) the server
	// supports, and a link relation type of type (that is, rel='type')
	// in all responses to requests made to the LDPC's HTTP Request-URI"
	// LDP 1.0 - 5.2.1.4 : "LDP servers may provide additional HTTP
	// Link: rel='type' headers"
	private static void addProtocolSpecificHeaders(ResponseBuilder builder, Class<? extends PublicResource> clazz) {
		List<Term> types=new ArrayList<Term>();
		if(PublicRDFSource.class.isAssignableFrom(clazz)) {
			types.add(LDP.RESOURCE);
		}
		if(PublicBasicContainer.class.isAssignableFrom(clazz)) {
			types.add(LDP.BASIC_CONTAINER);
		} else if(PublicDirectContainer.class.isAssignableFrom(clazz)) {
			types.add(LDP.DIRECT_CONTAINER);
		} else if(PublicIndirectContainer.class.isAssignableFrom(clazz)) {
			types.add(LDP.INDIRECT_CONTAINER);
		}
		for(Term type:types) {
			builder.header(MoreHttp.LINK_HEADER,MoreHttp.createLink(type, "type"));
		}
	}

	static Variant textResponseVariant() {
		return
			Variant.
				languages(Locale.ENGLISH).
				mediaTypes(MediaType.TEXT_PLAIN_TYPE.withCharset(StandardCharsets.UTF_8.name())).
				add().
				build().
					get(0);
	}

	static Variant errorResponseVariant() {
		return textResponseVariant();
	}

	/**
	 * Get a text/plain representation that indicates the acceptable media types
	 * and charsets for the specified resource. The current implementation
	 * <b>only</b> takes care of the media type, nor language, nor encodings are
	 * taken into consideration for generating the acceptable content.
	 *
	 * @param variants
	 *            The acceptable variants
	 * @param resourceLocation
	 *            The location of the resource
	 * @param supportedCharsets
	 *            The supported charsets
	 * @return A content instance that outlines the acceptable media types
	 *         together with the locations from where the resource contents for
	 *         those media types can be retrieved, and the charsets supported.
	 */
	static String getAcceptableContent(
			List<Variant> variants,
			URI resourceLocation,
			List<String> supportedCharsets) {
		StringBuilder builder = new StringBuilder();
		for (Variant variant : variants) {
			MediaType mediaType = variant.getMediaType();
			builder.append(mediaType).append(" : %1$s%n");
			for(String supportedCharset:supportedCharsets) {
				builder.append(mediaType.withCharset(supportedCharset)).append(" : %1$s%n");
			}
		}
		return String.format(builder.toString(),resourceLocation);
	}

	static String retrievalLog(OperationContext context) {
		StringBuilder builder=new StringBuilder();
		Query query=context.getQuery();
		if(query.isEmpty()) {
			builder.append("Executing resource retrieval:").append(NL);
		} else {
			builder.append("Executing resource query:").append(NL);
			builder.append("  - Parameters:").append(NL);
			for(String parameter:query.parameterNames()) {
			  builder.append("    + ").append(parameter).append(" : ").append(query.getParameter(parameter).rawValues());
			}
		}
		ContentPreferences preferences=context.contentPreferences();
		if(preferences!=null) {
			builder.append("  - Using preferences: ").append(preferences).append(NL);
		} else {
			builder.append("  - Using default preferences: ").append(ContentPreferences.defaultPreferences()).append(NL);
		}
		return builder.toString();
	}

	static String retrievalResultLog(DataSet entity) {
		return String.format("  - Data set to serialize:%n%s",entity);
	}

	static void populateRequiredHeaders(ResponseBuilder builder, OperationContext context) {
		PublicResource resource = context.resource();
		addProtocolEndorsedHeaders(builder,resource.lastModified(),resource.entityTag());
		addProtocolSpecificHeaders(builder,resource.getClass());

	}

	static void populateResponseBody(ResponseBuilder builder, String entity, Variant variant, boolean includeEntity) {
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
			header(MoreHttp.CONTENT_LENGTH_HEADER,bytes.length);

		if(variant.getLanguage()!=null) {
			builder.language(variant.getLanguage());
		}

		if(includeEntity) {
			builder.entity(new ByteArrayInputStream(bytes));
		}
	}

	static String createQueryOfLink(Object uriRef, Query query) {
		String strQuery = toString(query);
		try {
			return MoreHttp.createLink(uriRef, "queryOf")+"; parameters=\""+URLEncoder.encode(strQuery,"UTF-8")+"\"";
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 encoding should always be supported",e);
		}
	}

	static String toString(Query query) {
		List<String> parameters=Lists.newArrayList();
		for(String paramName:query.parameterNames()) {
			for(String rawValue:query.getParameter(paramName).rawValues()) {
				parameters.add(paramName+"="+rawValue);
			}
		}
		StringBuilder builder=new StringBuilder();
		Iterator<String> iterator=parameters.iterator();
		while(iterator.hasNext()) {
			builder.append(iterator.next());
			if(iterator.hasNext()) {
				builder.append("&");
			}
		}
		return builder.toString();
	}

	public static void populateAllowedHeaders(ResponseBuilder builder, Capabilities capabilities) {
		/**
		 * LDP 1.0 - 4.2.8.2 : "LDP servers must indicate their support for HTTP
		 * Methods by responding to a HTTP OPTIONS request on the LDPR’s URL
		 * with the HTTP Method tokens in the HTTP response header Allow."
		 */
		addAllowedMethodHeader(builder,"OPTIONS",true);
		addAllowedMethodHeader(builder,"HEAD",true);
		addAllowedMethodHeader(builder,"GET",true);
		addAllowedMethodHeader(builder,"PUT",capabilities.isModifiable());
		addAllowedMethodHeader(builder,"PATCH",capabilities.isPatchable());
		addAllowedMethodHeader(builder,"DELETE",capabilities.isDeletable());
		addAllowedMethodHeader(builder,"POST",capabilities.isFactory());
		if(capabilities.isFactory()) {
			/**
			 * LDP 1.0 - 5.2.3.13: "LDP servers that support POST must include
			 * an Accept-Post response header on HTTP OPTIONS responses, listing
			 * POST request media type(s) supported by the server.
			 *
			 * LDP 1.0 - 7.1.2 : "The Accept-Post HTTP header should appear in
			 * the OPTIONS response for any resource that supports the use of
			 * the POST method."
			 */
			for(Variant variant:VariantUtils.defaultVariants()) {
				builder.header(MoreHttp.ACCEPT_POST_HEADER,variant.getMediaType());
			}
		}
	}

	public static Response prepareErrorResponse(DiagnosedException throwable, ResponseEnricher... enrichers) {
		Diagnosis diagnosis = throwable.getDiagnosis();
		ResponseBuilder builder=Response.status(diagnosis.statusCode());
		populateResponseBody(builder,diagnosis.diagnostic(), errorResponseVariant(), diagnosis.mandatory());
		addProtocolEndorsedHeaders(builder,throwable.resourceLastModified(),throwable.resourceEntityTag());
		addProtocolSpecificHeaders(builder,throwable.resourceClass());
		for(Function<ResponseBuilder, ResponseBuilder> enricher:enrichers) {
			builder=enricher.apply(builder);
		}
		return builder.build();
	}

}
