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

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.engine.context.ApplicationContext;
import org.ldp4j.application.engine.context.ApplicationContextOperation;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.CreationPreferences;
import org.ldp4j.application.engine.context.CreationPreferences.InteractionModel;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.engine.context.HttpRequest.HttpMethod;
import org.ldp4j.application.engine.context.PublicContainer;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.sdk.QueryBuilder;
import org.ldp4j.rdf.Namespaces;
import org.ldp4j.server.data.DataTransformator;
import org.ldp4j.server.data.ResourceResolver;
import org.ldp4j.server.data.UnsupportedMediaTypeException;
import org.ldp4j.server.utils.CharsetSelector;
import org.ldp4j.server.utils.VariantHelper;
import org.ldp4j.server.utils.VariantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

final class OperationContextImpl implements OperationContext {

	private final class OperationContextResourceResolver implements ResourceResolver {

		@Override
		public URI resolveResource(ManagedIndividualId id) {
			URI result=null;
			PublicResource resolvedResource=applicationContextOperation.resolveResource(id);
			if(resolvedResource!=null) {
				result=base().resolve(resolvedResource.path());
				LOGGER.trace("Resolved resource {} URI to '{}'",id,result);
			}
			return result;
		}

		@Override
		public ManagedIndividualId resolveLocation(URI path) {
			PublicResource resolvedResource =
				applicationContextOperation.
					resolveResource(base().relativize(path).toString());

			ManagedIndividualId result = null;
			if(resolvedResource!=null) {
				result=resolvedResource.individualId();
				LOGGER.trace("Resolved location '{}' to resource {}",path,result);
			}
			return result;
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(OperationContextImpl.class);

	private final ApplicationContext applicationContext;
	private final String             endpointPath;
	private final HttpMethod         method;
	private final UriInfo            uriInfo;
	private final HttpHeaders        headers;
	private final Request            request;
	private final String             entity;

	private ApplicationContextOperation applicationContextOperation;
	private PublicResource              resource;
	private DataSet                     dataSet;

	private CharsetSelector charsetSelector;

	OperationContextImpl(
		ApplicationContext applicationContext,
		String endpointPath,
		UriInfo uriInfo,
		HttpHeaders headers,
		Request request,
		String entity,
		HttpMethod method) {
		this.applicationContext = applicationContext;
		this.endpointPath = endpointPath;
		this.method = method;
		this.uriInfo=uriInfo;
		this.headers=headers;
		this.request=request;
		this.entity = entity;
		LOGGER.trace("Host.........: {}",headers.getHeaderString(HttpHeaders.HOST));
		LOGGER.trace("Request uri..: {}",this.uriInfo.getRequestUri());
		LOGGER.trace("Base.........: {}",base());
		LOGGER.trace("Endpoint.....: {}",endpoint());
	}

	private Variant contentVariant() {
		List<String> requestHeader=
			headers().
				getRequestHeader(HttpHeaders.CONTENT_ENCODING);

		List<Variant> variants=
			Variant.
				mediaTypes(headers().getMediaType()).
				encodings(requestHeader.toArray(new String[requestHeader.size()])).
				languages(headers().getLanguage()).
				add().
				build();

		return variants.get(0);
	}

	private String slug() {
		List<String> slugs = this.headers.getRequestHeader("Slug");
		String slug=null;
		if(!slugs.isEmpty()) {
			slug=slugs.get(0);
		}
		return slug;
	}

	private InteractionModel interactionModel() {
		InteractionModel result=null;
		for(String linkHeader:this.headers.getRequestHeader(HttpHeaders.LINK)) {
			result= InteractionModelUtils.fromLink(linkHeader);
			if(result!=null) {
				break;
			}
		}
		return result;
	}

	private ResourceResolver resourceResolver() {
		return new OperationContextResourceResolver();
	}

	private URI endpoint() {
		return URI.create(path());
	}

	private String normalizePath(String path) {
		String tPath=path;
		if(tPath==null) {
			tPath="";
		} else {
			tPath = tPath.trim();
		}
		return tPath;
	}

	private boolean isMethodAllowed() {
		boolean allowed;
		switch(this.method) {
			case GET:
			case HEAD:
			case OPTIONS:
				allowed=true;
				break;
			case DELETE:
				allowed=resource().capabilities().isDeletable();
				break;
			case PATCH:
				allowed=resource().capabilities().isPatchable();
				break;
			case POST:
				allowed=resource().capabilities().isFactory();
				break;
			case PUT:
				allowed=resource().capabilities().isModifiable();
				break;
			default:
				allowed=false;
		}
		return allowed;
	}

	private CharsetSelector charsetSelector() {
		if(this.charsetSelector==null) {
			final List<Variant> variants=VariantUtils.defaultVariants();
			final Variant variant = this.request.selectVariant(variants);
			this.charsetSelector=
				CharsetSelector.
					newInstance().
						mediaType(variant==null?null:variant.getMediaType()).
						acceptableCharsets(this.headers.getRequestHeader(HttpHeaders.ACCEPT_CHARSET)).
						supportedCharsets(supportedCharsets());
		}
		return this.charsetSelector;
	}

	UriInfo uriInfo() {
		return this.uriInfo;
	}

	HttpHeaders headers() {
		return this.headers;
	}

	Request request() {
		return this.request;
	}

	String entity() {
		return this.entity;
	}

	@Override
	public URI base() {
		return URI.create(this.uriInfo.getBaseUri().toString().concat("/")).normalize();
	}

	@Override
	public String path() {
		return this.endpointPath;
	}

	@Override
	public Query getQuery() {
		final MultivaluedMap<String,String> queryParameters = this.uriInfo.getQueryParameters();
		final QueryBuilder builder = QueryBuilder.newInstance();
		for(final Entry<String, List<String>> entry:queryParameters.entrySet()) {
			final String parameterName = entry.getKey();
			for(final String rawValue:entry.getValue()) {
				builder.withParameter(parameterName, rawValue);
			}
		}
		return builder.build();
	}

	@Override
	public boolean isResourceQueryable() {
		return resource().capabilities().isQueryable();
	}

	@Override
	public OperationContext checkContents() {
		List<Variant> supportedVariants=VariantUtils.defaultVariants();
		if(entity()==null || entity().isEmpty()) {
			throw new MissingContentException(this);
		}
		if(headers().getMediaType()==null) {
			throw new MissingContentTypeException(this);
		}
		if(!VariantHelper.
				forVariants(supportedVariants).
					isSupported(contentVariant())) {
			throw new UnsupportedContentException(this,contentVariant());
		}
		return this;
	}

	@Override
	public OperationContext checkPreconditions() {
		EntityTag entityTag=this.resource.entityTag();
		Date lastModified=this.resource.lastModified();
		if(HttpMethod.PUT.equals(this.method)) {
			List<String> requestHeader = this.headers.getRequestHeader(HttpHeaders.IF_MATCH);
			if(requestHeader==null || requestHeader.isEmpty()) {
				throw new PreconditionRequiredException(this);
			}
		}
		ResponseBuilder builder =
			request().
				evaluatePreconditions(
					lastModified,
					new javax.ws.rs.core.EntityTag(entityTag.getValue()));
		if(builder!=null) {
			Response response = builder.build();
			throw new PreconditionFailedException(this,response.getStatus());
		}
		return this;
	}

	@Override
	public OperationContext checkOperationSupport() {
		if(!isMethodAllowed()) {
			throw new MethodNotAllowedException(this,this.method);
		}
		return this;
	}

	@Override
	public DataSet dataSet() {
		if(this.dataSet==null) {
			MediaType mediaType=contentVariant().getMediaType();
			try {
				DataTransformator transformator =
					DataTransformator.
						create(base()).
						enableResolution(resourceResolver()).
						mediaType(mediaType);
				if(this.method.equals(HttpMethod.POST)) {
					transformator=transformator.surrogateEndpoint(endpoint());
				} else {
					transformator=transformator.permanentEndpoint(endpoint());
				}
				this.dataSet=transformator.unmarshall(this.entity);
			} catch(UnsupportedMediaTypeException e) {
				throw new UnsupportedContentException(this,contentVariant(),e);
			} catch(IOException e) {
				throw new InvalidRequestContentException("Entity cannot be parsed as '"+mediaType+"' ("+Throwables.getRootCause(e).getMessage()+")",e,this);
			}
		}
		return this.dataSet;
	}

	@Override
	public Variant expectedVariant() {
		List<Variant> variants=VariantUtils.defaultVariants();
		Variant variant=this.request.selectVariant(variants);
		if(variant==null) {
			throw new NotAcceptableException(this);
		}
		String acceptableCharset=acceptedCharset();
		if(acceptableCharset==null) {
			throw new NotAcceptableException(this);
		}
		return
			Variant.
				encodings(variant.getEncoding()).
				languages(variant.getLanguage()).
				mediaTypes(variant.getMediaType().withCharset(acceptableCharset)).
				add().
				build().
				get(0);
	}

	@Override
	public boolean expectsCharset() {
		return charsetSelector().requiresCharset();
	}

	@Override
	public String acceptedCharset() {
		return charsetSelector().select();
	}

	/**
	 * TODO: Add extension point to enable configuring the list of supported
	 * charsets
	 */
	@Override
	public List<Charset> supportedCharsets() {
		return
			ImmutableList.
				<Charset>builder().
					add(StandardCharsets.UTF_8).
					add(StandardCharsets.ISO_8859_1).
					add(StandardCharsets.US_ASCII).
					add(StandardCharsets.UTF_16).
					add(StandardCharsets.UTF_16LE).
					add(StandardCharsets.UTF_16BE).
					build();
	}

	@Override
	public CreationPreferences creationPreferences() {
		return
			CreationPreferences.
				builder().
					withInteractionModel(interactionModel()).
					withPath(slug()).
					build();
	}

	@Override
	public URI resolve(PublicResource resource) {
		return base().resolve(resource.path());
	}

	@Override
	public String serialize(DataSet representation, Namespaces namespaces, MediaType mediaType) {
		try {
			DataTransformator transformator =
				DataTransformator.
					create(base()).
					enableResolution(resourceResolver()).
					mediaType(mediaType).
					namespaces(namespaces).
					permanentEndpoint(endpoint());
			return transformator.marshall(representation);
		} catch(UnsupportedMediaTypeException e) {
			throw new UnsupportedContentException(this,contentVariant(),e);
		} catch(IOException e) {
			throw new ContentProcessingException("Resource representation cannot be parsed as '"+mediaType+"'. Expecting content matching ",e,this,Status.BAD_REQUEST);
		}
	}

	@Override
	public PublicResource resource() {
		if(this.resource==null) {
			this.resource=
				this.applicationContextOperation.
					findResource(
						normalizePath(this.endpointPath));
		}
		return this.resource;
	}

	@Override
	public PublicContainer container() {
		PublicResource tmp = resource();
		checkState(tmp instanceof PublicContainer,"Expected an instance of class %s but got an instance of class %s",PublicContainer.class.getCanonicalName(),tmp.getClass().getCanonicalName());
		return (PublicContainer)tmp;
	}

	@Override
	public ContentPreferences contentPreferences() {
		ContentPreferences result = null;
		List<String> requestHeader = this.headers.getRequestHeader(ContentPreferencesUtils.PREFER_HEADER);
		for(Iterator<String> it=requestHeader.iterator();it.hasNext() && result==null;) {
			String header = it.next();
			try {
				result=ContentPreferencesUtils.fromPreferenceHeader(header);
			} catch (InvalidPreferenceHeaderException e) {
				LOGGER.debug("Ignored prefer header {}",header,e);
			}
		}
		return result;
	}

	@Override
	public Namespaces applicationNamespaces() {
		org.ldp4j.application.ext.Namespaces applicationNamespaces = this.applicationContext.applicationNamespaces();
		Namespaces namespaces = new Namespaces();
		for(String prefix:applicationNamespaces.getDeclaredPrefixes()) {
			namespaces.addPrefix(prefix, applicationNamespaces.getNamespaceURI(prefix));
		}
		return namespaces;
	}

	@Override
	public void startOperation() {
		this.applicationContextOperation=
			this.applicationContext.
				createOperation(
					HttpRequestFactory.
						create(
							this.method,
							this.uriInfo,
							this.headers,
							this.entity));
	}

	@Override
	public void completeOperation() {
		checkState(this.applicationContextOperation!=null,"Operation not started");
		this.applicationContextOperation.dispose();
	}

}