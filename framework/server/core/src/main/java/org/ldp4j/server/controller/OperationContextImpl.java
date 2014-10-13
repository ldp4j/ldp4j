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
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.PublicContainer;
import org.ldp4j.application.PublicResource;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.endpoint.EntityTag;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.server.Context;
import org.ldp4j.server.ImmutableContext;
import org.ldp4j.server.ResourceResolver;
import org.ldp4j.server.spi.ContentTransformationException;
import org.ldp4j.server.spi.IMediaTypeProvider;
import org.ldp4j.server.spi.IMediaTypeProvider.Marshaller;
import org.ldp4j.server.spi.IMediaTypeProvider.Unmarshaller;
import org.ldp4j.server.spi.RuntimeInstance;
import org.ldp4j.server.utils.VariantHelper;
import org.ldp4j.server.utils.VariantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final class OperationContextImpl implements OperationContext {

	private static final Logger LOGGER=LoggerFactory.getLogger(OperationContextImpl.class);

	private final class OperationContextResourceResolver implements ResourceResolver {

		@Override
		public URI resolveResource(ResourceId id) {
			Endpoint resourceEndpoint = applicationContext.resolveResource(id);
			URI result = null;
			if(resourceEndpoint==null) {
				throw new IllegalStateException("Could not resolve resource "+id);
			}
			result=base().resolve(resourceEndpoint.path());
			LOGGER.trace("Resolved resource {} URI to '{}'",id,result);
			return result;
		}

		@Override
		public ResourceId resolveLocation(URI path) {
			Endpoint resolveEndpoint = 
				applicationContext.
					resolveEndpoint(
						base().relativize(path).toString());
			ResourceId result=null;
			if(resolveEndpoint!=null) {
				result=resolveEndpoint.resourceId();
				LOGGER.trace("Resolved path '{}' to resource {}",path,result);
			}
			return result;
		}

	}

	private final Operation          operation;
	private final UriInfo            uriInfo;
	private final HttpHeaders        headers;
	private final Request            request;
	private final Endpoint           endpoint;
	private final ApplicationContext applicationContext;

	private String  entity;
	private DataSet dataSet;

	private PublicResource resource;

	OperationContextImpl(
		ApplicationContext applicationContext, 
		Endpoint endpoint, 
		UriInfo uriInfo, 
		HttpHeaders headers, 
		Request request, 
		String entity, 
		Operation operation) {
		this.applicationContext = applicationContext;
		this.endpoint = endpoint;
		this.operation = operation;
		this.uriInfo=uriInfo;
		this.headers=headers;
		this.request=request;
		this.entity = entity;
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

	private Variant contentVariant() {
		List<String> requestHeader= 
			headers().
				getRequestHeader(HttpHeaders.CONTENT_ENCODING);
		
		List<Variant> variants=
			Variant.VariantListBuilder.
				newInstance().
				mediaTypes(headers().getMediaType()).
				encodings(requestHeader.toArray(new String[requestHeader.size()])).
				languages(headers().getLanguage()).
				add().
				build();
		
		return variants.get(0);
	}

	private Context createContext() {
		ResourceResolver resolver = resourceResolver();
		URI endpoint = base().resolve(this.endpoint.path());
		if(Operation.POST.equals(this.operation)) {
			try {
				URI alternative=
					new URI(
						endpoint.getScheme(),
						endpoint.getUserInfo(),
						"ldp4j".concat(endpoint.getHost()),
						endpoint.getPort(),
						endpoint.getPath(),
						endpoint.getFragment(),
						endpoint.getQuery()
					);
				resolver=
					SafeResourceResolver.
						builder().
							withApplication(base()).
							withEndpoint(endpoint).
							withAlternative(alternative).
							withEntity(entity(), contentVariant().getMediaType()).
							build();
			} catch (URISyntaxException e) {
				AssertionError assertionError = new AssertionError("Alternative URI creation failed");
				assertionError.initCause(e);
				throw assertionError;
			} catch (ContentTransformationException e) {
				throw new ContentProcessingException("Could not create safe resolver", this.endpoint, this);
			}
		}
		return ImmutableContext.newInstance(endpoint,resolver);
	}

	@Override
	public ApplicationContext applicationContext() {
		return this.applicationContext;
	}
	
	@Override
	public URI base() {
		String path = uriInfo.getPath();
		String prefix = "/"+path.substring(0,path.indexOf('/')+1);
		return URI.create(uriInfo.getBaseUri().toString().concat(prefix));
	}

	@Override
	public String path() {
		String path = uriInfo.getPath();
		return path.substring(path.indexOf('/')+1);
	}

	@Override
	public OperationContext checkContents() {
		List<Variant> supportedVariants=VariantUtils.defaultVariants();
		if(entity()==null || entity().isEmpty()) {
			throw new MissingContentException(endpoint,this);
		}
		if(headers().getMediaType()==null) {
			throw new MissingContentTypeException(endpoint,this);
		}
		if(!VariantHelper.
				forVariants(supportedVariants).
					isSupported(contentVariant())) {
			throw new UnsupportedContentException(endpoint,this,contentVariant());
		}
		return this;
	}

	@Override
	public OperationContext checkPreconditions() {
		EntityTag entityTag=this.endpoint.entityTag(); 
		Date lastModified=this.endpoint.lastModified();
		if(Operation.PUT.equals(this.operation)) {
			List<String> requestHeader = this.headers.getRequestHeader(HttpHeaders.IF_MATCH);
			if((requestHeader==null || requestHeader.isEmpty())) {
				throw new PreconditionRequiredException(this,this.endpoint);
			}
		}
		ResponseBuilder builder = 
			request().
				evaluatePreconditions(
					lastModified,
					new javax.ws.rs.core.EntityTag(entityTag.getValue()));
		if(builder!=null) {
			Response response = builder.build();
			throw new PreconditionFailedException(endpoint,this,response.getStatus());
		}
		return this;
	}

	@Override
	public OperationContext checkOperationSupport() {
		boolean allowed=false;
		switch(operation) {
		case GET:
			allowed=true;
			break;
		case HEAD:
			allowed=true;
			break;
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
		}
		if(!allowed) {
			throw new MethodNotAllowedException(this,endpoint);
		}
		return this;
	}

	@Override
	public DataSet dataSet() {
		if(this.dataSet==null) {
			LOGGER.trace("Raw entity to unmarshall: \n{}",this.entity);
			
			MediaType mediaType = contentVariant().getMediaType();
			IMediaTypeProvider provider = 
				RuntimeInstance.
					getInstance().
						getMediaTypeProvider(mediaType);
	
			if(provider==null) {
				throw new UnsupportedContentException(endpoint,this,contentVariant());
			}
			
			Context context = createContext();
			Unmarshaller unmarshaller = 
				provider.
					newUnmarshaller(context);
			try {
				LOGGER.trace("Unmarshalling using base '{}'",context.getBase());
				this.dataSet=unmarshaller.unmarshall(this.entity, mediaType);
			} catch (ContentTransformationException e) {
				throw new ContentProcessingException("Entity cannot be parsed as '"+mediaType+"' ",endpoint,this);
			}
			
			LOGGER.trace("Unmarshalled data set: \n{}",this.dataSet);
		}
		return this.dataSet;
	}

	@Override
	public Variant expectedVariant() {
		List<Variant> variants=VariantUtils.defaultVariants();
		Variant variant = request.selectVariant(variants);
		if(variant==null) {
			throw new NotAcceptableException(endpoint,this);
		}
		return variant;
	}

	@Override
	public InteractionModel interactionModel() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}
	
	@Override
	public URI resolve(PublicResource newResource) {
		return base().resolve(newResource.path());
	}

	@Override
	public String serializeResource(DataSet representation, MediaType mediaType) {
		IMediaTypeProvider provider = 
			RuntimeInstance.
				getInstance().
					getMediaTypeProvider(mediaType);
	
		if(provider==null) {
			throw new UnsupportedContentException(endpoint,this,contentVariant());
		}
		
		Context context = createContext();
		Marshaller marshaller = 
			provider.
				newMarshaller(context);
		try {
			LOGGER.trace("Marshalling using base '{}'",context.getBase());
			String rawEntity = marshaller.marshall(representation, mediaType);
			LOGGER.trace("Marshalled entity: \n{}",rawEntity);
			return rawEntity;
		} catch (ContentTransformationException e) {
			throw new ContentProcessingException("Resource representation cannot be parsed as '"+mediaType+"' ",endpoint,this);
		}
	}

	private ResourceResolver resourceResolver() {
		return new OperationContextResourceResolver();
	}

	@Override
	public PublicResource resource() {
		if(this.resource==null) {
			this.resource=this.applicationContext.findResource(this.endpoint);
		}
		return this.resource;
	}

	@Override
	public PublicContainer container() {
		PublicResource tmp = resource();
		if(!(tmp instanceof PublicContainer)) {
			throw new IllegalStateException("Expected an instance of class "+PublicContainer.class.getCanonicalName()+" but got an instance of class "+tmp.getClass().getCanonicalName());
		}
		return (PublicContainer)tmp;
	}

}