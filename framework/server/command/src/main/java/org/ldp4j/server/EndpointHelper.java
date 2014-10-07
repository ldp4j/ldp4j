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

import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.ldp4j.server.resources.Resource;
import org.ldp4j.server.utils.ProtocolUtils;
import org.ldp4j.server.utils.VariantHelper;

public final class EndpointHelper {
	
	public static final String LINK_HEADER           = "Link";
	@SuppressWarnings("unused")
	private static final String ACCEPT_POST_HEADER    = "Accept-Post";

	private final Configuration<?> configuration;

	private final UriInfo uriInfo;
	private final HttpHeaders headers;
	private final Request request;
	private final String entity;

	private EndpointHelper(Configuration<?> configuration, UriInfo uriInfo, HttpHeaders headers, Request request, String entity) {
		this.configuration = configuration;
		this.uriInfo = uriInfo;
		this.headers = headers;
		this.request = request;
		this.entity = entity;
	}

	private Resource getResource() {
		return configuration.getResource();
	}
	
	private Variant getContentVariant() {
		List<String> requestHeader= 
			getHeaders().
				getRequestHeader(HttpHeaders.CONTENT_ENCODING);
		
		List<Variant> variants=
			Variant.VariantListBuilder.
				newInstance().
				mediaTypes(getHeaders().getMediaType()).
				encodings(requestHeader.toArray(new String[requestHeader.size()])).
				languages(getHeaders().getLanguage()).
				add().
				build();
		
		return variants.get(0);
	}

	public EndpointHelper checkPreconditions() {
		ResponseBuilder builder = 
			getRequest().
				evaluatePreconditions(
					getResource().lastModified(),
					getResource().entityTag());
		if(builder!=null) {
			Response response = builder.build();
			throw new PreconditionFailedException(getResource(),response.getStatus());
		}
		return this;
	}
	
	public EndpointHelper checkContents(List<Variant> supportedVariant) {
		if(getEntity()==null || getEntity().isEmpty()) {
			throw new MissingContentException(getResource(),supportedVariant);
		}
		if(getHeaders().getMediaType()==null) {
			throw new MissingContentTypeException(getResource(),supportedVariant);
		}
		if(!VariantHelper.
				forVariants(supportedVariant).
					isSupported(getContentVariant())) {
			throw new UnsupportedContentException(getResource(),supportedVariant);
		}
		return this;
	}

	public Variant getExpectedVariant(List<Variant> variants) {
		Variant variant = request.selectVariant(variants);
		if(variant==null) {
			throw new NotAcceptableException(getResource(),variants);
		}
		return variant;
	}
	
	public UriInfo getUriInfo() {
		if(uriInfo==null) {
			throw new IllegalStateException("No uriInfo is available");
		}
		return uriInfo;
	}

	public HttpHeaders getHeaders() {
		if(headers==null) {
			throw new IllegalStateException("No headers are available");
		}
		return headers;
	}

	public Request getRequest() {
		if(request==null) {
			throw new IllegalStateException("No request is available");
		}
		return request;
	}

	public String getEntity() {
		return entity;
	}

	public EndpointHelper populateProtocolEndorsedHeaders(ResponseBuilder builder) {
		ProtocolUtils.populateEndorsedHeaders(getResource(), builder);
		return this;
	}
	
	public EndpointHelper populateProtocolSpecificHeaders(ResponseBuilder builder) {
		ProtocolUtils.populateSpecificHeaders(getResource(), builder);
		return this;
	}

	public static EndpointHelperBuilder newInstance(Configuration<?> configuration) {
		return new EndpointHelperBuilder(configuration);
	}
	
	public static final class EndpointHelperBuilder {
		
		private final Configuration<?> configuration;

		private UriInfo info;
		private HttpHeaders headers;
		private Request request;
		private String entity;
		
		private EndpointHelperBuilder(Configuration<?> configuration) {
			this.configuration = configuration;
		}
		
		public EndpointHelperBuilder withUriInfo(UriInfo info) {
			this.info = info;
			return this;
		}

		public EndpointHelperBuilder withHeaders(HttpHeaders headers) {
			this.headers = headers;
			return this;
		}
		
		public EndpointHelperBuilder withRequest(Request request) {
			this.request = request;
			return this;
		}
		
		public EndpointHelperBuilder withEntity(String entity) {
			this.entity = entity;
			return this;
		}
		
		public EndpointHelper build() {
			return new EndpointHelper(configuration,info, headers, request, entity);
		}
	}
	
}