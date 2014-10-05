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
package org.ldp4j.server.controller;


import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.Capabilities;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetFactory;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.endpoint.EntityTag;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.server.api.Entity;
import org.ldp4j.server.api.ImmutableContext;
import org.ldp4j.server.api.ResourceIndex;
import org.ldp4j.server.api.utils.VariantHelper;
import org.ldp4j.server.blueprint.ComponentRegistry;
import org.ldp4j.server.resources.ResourceType;
import org.ldp4j.server.resources.impl.EntityFactory;
import org.ldp4j.server.resources.impl.ResourceImpl;
import org.ldp4j.server.spi.ContentTransformationException;
import org.ldp4j.server.spi.IMediaTypeProvider;
import org.ldp4j.server.spi.RuntimeInstance;
import org.ldp4j.server.spi.IMediaTypeProvider.Unmarshaller;
import org.ldp4j.server.utils.VariantUtils;

final class OperationContextImpl implements OperationContext {

	private final class ResourceIndexImpl implements ResourceIndex {

		@Override
		public URI resolveResource(ResourceId id) {
			return URI.create(applicationContext.findResourceEndpoint(id).path());
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
			}
			return result;
		}

		@Override
		public ComponentRegistry getRegistry() {
			throw new UnsupportedOperationException("Method not supported");
		}

		@Override
		public boolean unpublish(ResourceId id) {
			throw new UnsupportedOperationException("Method not supported");
		}

		@Override
		public void publish(ResourceId id, URI path) {
			throw new UnsupportedOperationException("Method not supported");
		}

		@Override
		public boolean isPublished(ResourceId id) {
			throw new UnsupportedOperationException("Method not supported");
		}

		@Override
		public boolean isActive(URI endpoint) {
			throw new UnsupportedOperationException("Method not supported");
		}
	}

	enum InteractionModel {
		RESOURCE,
		BASIC_CONTAINER,
		DIRECT_CONTAINER,
		INDIRECT_CONTAINER
	}

	private final UriInfo uriInfo;
	private final HttpHeaders headers;
	private final Request request;
	private String entity;
	private DataSet dataSet;
	private final Endpoint endpoint;
	private final ApplicationContext applicationContext;
	private final ResourceAdapter adapter;
	
	private final Operation operation;

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
		this.adapter = new ResourceAdapter(this.applicationContext, this.endpoint);
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

	@Override
	public ApplicationContext applicationContext() {
		return this.applicationContext;
	}
	
	@Override
	public URI base() {
		URI fullPath = this.uriInfo.getAbsolutePath();
		List<String> matchedURIs = this.uriInfo.getMatchedURIs(true);
		StringBuilder tmp=new StringBuilder();
		for(Iterator<String> it=matchedURIs.iterator();it.hasNext();) {
			tmp.append("..");
			it.next();
			if(it.hasNext()) {
				tmp.append("/");
			}
		}
		URI parentRelative=URI.create(tmp.toString());
		return fullPath.resolve(parentRelative);
	}

	@Override
	public String path() {
		List<String> matchedURIs = this.uriInfo.getMatchedURIs(true);
		StringBuilder tmp=new StringBuilder();
		for(Iterator<String> it=matchedURIs.iterator();it.hasNext();) {
			tmp.append(it.next());
			if(it.hasNext()) {
				tmp.append("/");
			}
		}
		return tmp.toString();
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
		EntityTag entityTag=endpoint.entityTag(); 
		Date lastModified=endpoint.lastModified();
		ResponseBuilder builder = 
			request().
				evaluatePreconditions(
					lastModified,
					new javax.ws.rs.core.EntityTag(entityTag.getValue()));
		if(builder!=null) {
			Response response = builder.build();
			throw new PreconditionFailedException(this.adapter.getResource(),response.getStatus());
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
			allowed=endpointCapabilities().isDeletable();
			break;
		case PATCH:
			allowed=endpointCapabilities().isPatchable();
			break;
		case POST:
			allowed=endpointCapabilities().isFactory();
			break;
		case PUT:
			allowed=endpointCapabilities().isModifiable();
			break;
		}
		if(!allowed) {
			throw new MethodNotAllowedException(operation,endpoint,resourceType());
		}
		return this;
	}

	@Override
	public DataSet dataSet() {
		if(this.dataSet==null) {
			MediaType mediaType = contentVariant().getMediaType();
			IMediaTypeProvider provider = 
				RuntimeInstance.
					getInstance().
						getMediaTypeProvider(mediaType);
	
			if(provider==null) {
				throw new UnsupportedContentException(endpoint,this,contentVariant());
			}
			
			Unmarshaller unmarshaller = 
				provider.
					newUnmarshaller(ImmutableContext.newInstance(base().resolve(endpoint.path()),resourceIndex()));
			try {
				this.dataSet=unmarshaller.unmarshall(this.entity, mediaType);
			} catch (ContentTransformationException e) {
				throw new ContentProcessingException("Entity cannot be parsed as '"+mediaType+"' ",endpoint,this);
			}
		}
		return this.dataSet;
	}

	@Override
	public Variant expectedVariant() {
		List<Variant> variants=VariantUtils.defaultVariants();
		Variant variant = request.selectVariant(variants);
		if(variant==null) {
			throw new NotAcceptableException(this.adapter.getResource(),variants);
		}
		return variant;
	}

	@Override
	public InteractionModel interactionModel() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}
	
	@Override
	public URI resolve(Resource newResource) {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	@Override
	public ResourceType resourceType() {
		return this.adapter.getResource().type();
	}

	@Override
	public Entity createEntity(DataSet resource) {
		DataSet dataSet=DataSetFactory.createDataSet(this.endpoint.resourceId().name());
		DataSetUtils.merge(((ResourceImpl)this.adapter.getResource()).metadata(), dataSet);
		DataSetUtils.merge(resource, dataSet);
		return EntityFactory.createEntity(dataSet);
	}

	@Override
	public ResourceIndex resourceIndex() {
		return new ResourceIndexImpl();
	}

	@Override
	public Capabilities endpointCapabilities() {
		return this.applicationContext.endpointCapabilities(this.endpoint);
	}

}