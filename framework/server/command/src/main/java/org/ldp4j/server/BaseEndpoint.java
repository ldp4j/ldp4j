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
package org.ldp4j.server;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.ldp4j.server.ImmutableContext;
import org.ldp4j.server.blueprint.ComponentRegistry;
import org.ldp4j.server.resources.Resource;
import org.ldp4j.server.resources.ResourceType;
import org.ldp4j.server.spi.ContentTransformationException;
import org.ldp4j.server.spi.RuntimeInstance;
import org.ldp4j.server.utils.ProtocolUtils;
import org.ldp4j.server.utils.VariantUtils;

import com.google.common.base.Throwables;


class BaseEndpoint<R extends Resource> implements Endpoint {
	
	private static final int    METHOD_NOT_ALLOWED    = 405;
	
	private static final String ENTITY_TAG_HEADER     = "ETag";
	private static final String LAST_MODIFIED_HEADER  = "Last-Modified";
	private static final String CONTENT_LENGTH_HEADER = "Content-Length";
	private static final String LINK_HEADER           = "Link";
	private static final String ACCEPT_POST_HEADER    = "Accept-Post";
	private static final String ALLOW_HEADER          = "Allow";

	private final Configuration<R> configuration;
	private final ComponentRegistry registry;

	private final URI path;
	
	BaseEndpoint(URI path, Configuration<R> configuration, ComponentRegistry registry) {
		this.path = path;
		this.configuration = configuration;
		this.registry = registry;
	}
	
	protected Configuration<R> getConfiguration() {
		return configuration;
	}
	
	private Response doRetrieveResource(UriInfo uriInfo, Request request, boolean includeBody) {
		// First, evaluate if we can proceed with the operation
		ResponseBuilder builder = 
			request.
				evaluatePreconditions(
					getConfiguration().getResource().lastModified(),
					getConfiguration().getResource().entityTag());
		if(builder==null) {
			builder=Response.serverError();
		}
		Entity entity=
			getConfiguration().
				getResource().
					entity();

		// Second, evaluate if we can serve the contents required
		List<Variant> variants= 
			VariantUtils.
				createVariants(
					RuntimeInstance.
						getInstance().
							getSupportedMediaTypes());

		Variant variant = request.selectVariant(variants);

		// Third, determine response status and response content type
		String body=null;
		Status status=null;
		if(variant==null) {
			body=ProtocolUtils.getAcceptableContent(variants, uriInfo.getAbsolutePath());
			status = Status.NOT_ACCEPTABLE;
			builder.
				variants(variants).
				type(MediaType.TEXT_PLAIN);
		} else {
			try {
				URI fullPath = uriInfo.getAbsolutePath();
				List<String> matchedURIs = uriInfo.getMatchedURIs(true);
				StringBuilder tmp=new StringBuilder();
				tmp.append("..");
				for(Iterator<String> it=matchedURIs.iterator();it.hasNext();) {
					tmp.append("..");
					it.next();
					if(it.hasNext()) {
						tmp.append("/");
					}
				}
				URI parentRelative=URI.create(tmp.toString());
				URI base=fullPath.resolve(parentRelative);
				body=entity.serialize(variant,ImmutableContext.newInstance(base, registry.getComponent(ResourceIndex.class)));
				status=entity.isEmpty()?Status.NO_CONTENT:Status.OK;
				builder.type(variant.getMediaType());
			} catch (ContentTransformationException e) {
				status=Status.INTERNAL_SERVER_ERROR;
				body=Throwables.getStackTraceAsString(e);
				builder.type(MediaType.TEXT_PLAIN);
			}
		}
		
		// Fourth, add protocol endorsed headers
		populateProtocolEndorsedHeaders(builder);
		
		// Fifth, add protocol specific headers
		populateProtocolSpecificHeaders(builder);
		
		// Sixth, set status and attach response entity as required.
		builder.
			status(status.getStatusCode()).
			header(CONTENT_LENGTH_HEADER, body.length());
		if(includeBody) {
			builder.entity(body);
		}
	
		return builder.build();
	}

	private void populateProtocolEndorsedHeaders(ResponseBuilder builder) {
		builder.header(LAST_MODIFIED_HEADER,getConfiguration().getResource().lastModified());
		builder.header(ENTITY_TAG_HEADER, getConfiguration().getResource().entityTag());
	}

	private void populateProtocolSpecificHeaders(ResponseBuilder builder) {
		ResourceType type = getConfiguration().getResource().type();
		// LDP 1.0 - 5.2.1.4 : "LDP servers exposing LDPCs must advertise
		// their LDP support by exposing a HTTP Link header with a target
		// URI matching the type of container (see below) the server
		// supports, and a link relation type of type (that is, rel='type')
		// in all responses to requests made to the LDPC's HTTP Request-URI"
		builder.header(LINK_HEADER,ProtocolUtils.createLink(type, "type"));
		if(type.isContainer()) {
			// LDP 1.0 - 5.2.1.4 : "LDP servers may provide additional HTTP
			// Link: rel='type' headers"
			builder.header(LINK_HEADER,ProtocolUtils.createLink(ResourceType.RESOURCE,"type"));
		}
	}

	private void populateAllowedMethods(ResponseBuilder builder) {
		builder.
			header(ALLOW_HEADER,"GET").
			// LDP 1.0 - 4.2.8.2 : "LDP servers must indicate their support for HTTP
			// Methods by responding to a HTTP OPTIONS request on the
			// LDPR’s URL with the HTTP Method tokens in the HTTP
			// response header Allow."
			header(ALLOW_HEADER,"OPTIONS").
			header(ALLOW_HEADER,"HEAD");
		if(getConfiguration().isModifiable()) {
			builder.header(ALLOW_HEADER,"PUT");
		}
		if(getConfiguration().isPatchable()) {
			builder.header(ALLOW_HEADER,"PATCH");
		}
		if(getConfiguration().isDeletable()) {
			builder.header(ALLOW_HEADER,"DELETE");
		}
		if(getConfiguration().isFactory()) {
			builder.header(ALLOW_HEADER,"POST");
		}
	}

	private Response getDefaultResponse(
			boolean isSupported, 
			String action,
			String capability) {
		int status = 
			isSupported ? 
				Status.INTERNAL_SERVER_ERROR.getStatusCode() : 
				METHOD_NOT_ALLOWED;
		String content =
			isSupported ?
				"DelegatedResourceSnapshot "+action+" mechanism not implemented yet":
				"DelegatedResourceSnapshot is not "+capability ;
		ResponseBuilder responseBuilder=
			Response.
				status(status).
				entity(content).
				type(MediaType.TEXT_PLAIN).
				language(Locale.ENGLISH);
		if(!isSupported) {
			populateAllowedMethods(responseBuilder);
		}
		populateProtocolEndorsedHeaders(responseBuilder);
		populateProtocolSpecificHeaders(responseBuilder);
		return responseBuilder.build();
	}

	
	@Override
	public final URI path() {
		return path;
	}

	@Override
	public final Response getHeader(UriInfo uriInfo, HttpHeaders headers, Request request) {
		return doRetrieveResource(uriInfo, request, false);
	}

	@Override
	public final Response getOptions(UriInfo uriInfo, HttpHeaders headers, Request request) {
		ResponseBuilder builder=
			Response.
				noContent();
		populateAllowedMethods(builder);
		if(getConfiguration().isFactory()) {
			// LDP 1.0 - 7.1.2 : "The Accept-Post HTTP header should appear
			// in the OPTIONS response for any resource that supports the
			// use of the POST method."
			for(MediaType mediaType:RuntimeInstance.getInstance().getSupportedMediaTypes()) {
				builder.header(ACCEPT_POST_HEADER,mediaType.toString());
			}
		}
		return builder.build();
	}

	@Override
	public final Response retrieveResource(UriInfo uriInfo, HttpHeaders headers, Request request) {
		return doRetrieveResource(uriInfo, request, true);
	}

	@Override
	public Response deleteResource(UriInfo uriInfo, HttpHeaders headers, Request request) {
		EndpointHelper helper=
			EndpointHelper.
				newInstance(getConfiguration()).
					withHeaders(headers).
					withRequest(request).
					withUriInfo(uriInfo).
					build();
		helper.checkPreconditions();
		return 
			getDefaultResponse(
				getConfiguration().isDeletable(), 
				"delete", 
				"deletable");
	}

	@Override
	public Response updateResource(UriInfo uriInfo, HttpHeaders headers, Request request, String entity) {
		EndpointHelper helper=
			EndpointHelper.
				newInstance(getConfiguration()).
					withHeaders(headers).
					withRequest(request).
					withUriInfo(uriInfo).
					build();
		helper.
			checkPreconditions().
			checkContents(VariantUtils.defaultVariants()).
			getExpectedVariant(VariantUtils.defaultVariants());
		return 
			getDefaultResponse(
				getConfiguration().isModifiable(), 
				"update", 
				"updateable");
	}

	@Override
	public Response patchResource(UriInfo uriInfo, HttpHeaders headers, Request request, String entity) {
		return 
			getDefaultResponse(
				getConfiguration().isPatchable(), 
				"patch", 
				"patchable");
	}

	@Override
	public Response createResource(UriInfo uriInfo, HttpHeaders headers, Request request, String entity) {
		EndpointHelper helper=
			EndpointHelper.
				newInstance(getConfiguration()).
					withHeaders(headers).
					withRequest(request).
					withUriInfo(uriInfo).
					build();
		helper.
			checkPreconditions().
			getExpectedVariant(VariantUtils.defaultVariants());
		return 
			getDefaultResponse(
				getConfiguration().isFactory(), 
				"creation", 
				"a factory");
	}

}