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
package org.ldp4j.server.frontend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetFactory;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.endpoint.EntityTag;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.server.api.Entity;
import org.ldp4j.server.api.MissingContentException;
import org.ldp4j.server.api.MissingContentTypeException;
import org.ldp4j.server.api.NotAcceptableException;
import org.ldp4j.server.api.PreconditionFailedException;
import org.ldp4j.server.api.ResourceIndex;
import org.ldp4j.server.api.UnsupportedContentException;
import org.ldp4j.server.api.utils.VariantHelper;
import org.ldp4j.server.blueprint.ComponentRegistry;
import org.ldp4j.server.resources.ResourceType;
import org.ldp4j.server.resources.impl.EntityFactory;
import org.ldp4j.server.resources.impl.ResourceImpl;

public final class OperationContext {

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
	
	private OperationContext(ApplicationContext applicationContext, Endpoint endpoint, UriInfo uriInfo, HttpHeaders headers, Request request, String entity, DataSet dataSet) {
		this.applicationContext = applicationContext;
		this.endpoint = endpoint;
		this.adapter = new ResourceAdapter(this.applicationContext, this.endpoint);
		this.uriInfo=uriInfo;
		this.headers=headers;
		this.request=request;
		this.entity = entity;
		this.dataSet = dataSet;
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

	public UriInfo uriInfo() {
		return this.uriInfo;
	}

	public HttpHeaders headers() {
		return this.headers;
	}

	public Request request() {
		return this.request;
	}

	public String entity() {
		return this.entity;
	}

	public DataSet dataSet() {
		return this.dataSet;
	}

	public OperationContext checkContents(List<Variant> supportedVariant) {
		if(entity()==null || entity().isEmpty()) {
			throw new MissingContentException(this.adapter.getResource(),supportedVariant);
		}
		if(headers().getMediaType()==null) {
			throw new MissingContentTypeException(this.adapter.getResource(),supportedVariant);
		}
		if(!VariantHelper.
				forVariants(supportedVariant).
					isSupported(contentVariant())) {
			throw new UnsupportedContentException(this.adapter.getResource(),supportedVariant);
		}
		return this;
	}

	public OperationContext evaluatePreconditions(EntityTag entityTag, Date lastModified) {
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

	public Variant expectedVariant(List<Variant> variants) {
		Variant variant = request.selectVariant(variants);
		if(variant==null) {
			throw new NotAcceptableException(this.adapter.getResource(),variants);
		}
		return variant;
	}

	public InteractionModel interactionModel() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}
	
	public URI resolve(Resource newResource) {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	public static OperationContextBuilder builder(ApplicationContext applicationContext, Endpoint endpoint) {
		return new OperationContextBuilder(applicationContext,endpoint);
	}
	
	public static final class OperationContextBuilder {

		private UriInfo uriInfo;
		private Request request;
		private HttpHeaders headers;
		private String entity;
		private final ApplicationContext applicationContext;
		private final Endpoint endpoint;

		private OperationContextBuilder(ApplicationContext applicationContext, Endpoint endpoint) {
			this.applicationContext=checkNotNull(applicationContext,"Uri info cannot be null");
			this.endpoint=checkNotNull(endpoint,"Request cannot be null");
		}
		
		public OperationContextBuilder withUriInfo(UriInfo uriInfo) {
			this.uriInfo = uriInfo;
			return this;
		}

		public OperationContextBuilder withHeaders(HttpHeaders headers) {
			this.headers = headers;
			return this;
		}
		
		public OperationContextBuilder withRequest(Request request) {
			this.request = request;
			return this;
		}

		public OperationContextBuilder withEntity(String entity) {
			this.entity = entity;
			return this;
		}
		
		public OperationContext build() {
			checkNotNull(uriInfo,"Uri info cannot be null");
			checkNotNull(request,"Request cannot be null");
			checkNotNull(headers,"Http headers cannot be null");
			DataSet dataSet=null;
			if(this.entity!=null) {
				// TODO: create data set
			}
			return new OperationContext(this.applicationContext,this.endpoint,this.uriInfo,this.headers,this.request,this.entity,dataSet);
		}
		
	}

	public ResourceType resourceType() {
		return this.adapter.getResource().type();
	}
	
	public Entity createEntity(DataSet resource) {
		DataSet dataSet=DataSetFactory.createDataSet(this.endpoint.resourceId().name());
		DataSetUtils.merge(((ResourceImpl)this.adapter.getResource()).metadata(), dataSet);
		DataSetUtils.merge(resource, dataSet);
		return EntityFactory.createEntity(dataSet);
	}

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

	public ResourceIndex resourceIndex() {
		return new ResourceIndex() {
			@Override
			public URI resolveResource(ResourceId id) {
				return URI.create(applicationContext.findResourceEndpoint(id).path());
			}
			
			@Override
			public ResourceId resolveLocation(URI path) {
				throw new UnsupportedOperationException("Method not implemented yet");
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
		};
	}

}