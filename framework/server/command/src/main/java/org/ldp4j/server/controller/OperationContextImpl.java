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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.domain.LDP;
import org.ldp4j.application.domain.RDF;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.endpoint.EntityTag;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.template.BasicContainerTemplate;
import org.ldp4j.application.template.ContainerTemplate;
import org.ldp4j.application.template.DirectContainerTemplate;
import org.ldp4j.application.template.IndirectContainerTemplate;
import org.ldp4j.application.template.MembershipAwareContainerTemplate;
import org.ldp4j.application.template.ResourceTemplate;
import org.ldp4j.application.template.TemplateVisitor;
import org.ldp4j.application.vocabulary.Term;
import org.ldp4j.server.ImmutableContext;
import org.ldp4j.server.ResourceResolver;
import org.ldp4j.server.api.utils.VariantHelper;
import org.ldp4j.server.resources.ResourceType;
import org.ldp4j.server.spi.ContentTransformationException;
import org.ldp4j.server.spi.IMediaTypeProvider;
import org.ldp4j.server.spi.IMediaTypeProvider.Marshaller;
import org.ldp4j.server.spi.IMediaTypeProvider.Unmarshaller;
import org.ldp4j.server.spi.RuntimeInstance;
import org.ldp4j.server.utils.VariantUtils;


final class OperationContextImpl implements OperationContext {

	private final class OperationContextResourceResolver implements ResourceResolver {

		@Override
		public URI resolveResource(ResourceId id) {
			String path = applicationContext.findResourceEndpoint(id).path();
			URI uri = base().resolve(path);
			return uri;
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

	}

	private final Operation operation;
	private final UriInfo uriInfo;
	private final HttpHeaders headers;
	private final Request request;
	private final Endpoint endpoint;
	private final ApplicationContext applicationContext;

	private String entity;
	private DataSet dataSet;
	private DataSet metadata;

	private ResourceType resourceType;

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

	private DataSet populateMetadata(DataSet resource) {
		if(this.metadata==null) {
			Resource applicationResource = this.applicationContext.resolveResource(endpoint);
			ResourceTemplate template = this.applicationContext.resourceTemplate(applicationResource);
			this.metadata = getMetadata(template);
		}
		DataSet dataSet=DataSetFactory.createDataSet(this.endpoint.resourceId().name());
		DataSetUtils.merge(this.metadata, dataSet);
		DataSetUtils.merge(resource, dataSet);
		return dataSet;
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
		EntityTag entityTag=endpoint.entityTag(); 
		Date lastModified=endpoint.lastModified();
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
					newUnmarshaller(ImmutableContext.newInstance(base().resolve(endpoint.path()),resourceResolver()));
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
			throw new NotAcceptableException(endpoint,this);
		}
		return variant;
	}

	@Override
	public InteractionModel interactionModel() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}
	
	@Override
	public URI resolve(Resource newResource) {
		Endpoint endpoint = this.applicationContext.findResourceEndpoint(newResource.id());
		return base().resolve(endpoint.path());
	}

	@Override
	public ResourceType resourceType() {
		if(this.resourceType==null) {
			Resource resource = this.applicationContext.resolveResource(endpoint);
			ResourceTemplate template = this.applicationContext.resourceTemplate(resource);
			this.resourceType=getResourceType(template);
		}
		return this.resourceType;
	}

	@Override
	public Capabilities endpointCapabilities() {
		return this.applicationContext.endpointCapabilities(this.endpoint);
	}

	@Override
	public String serializeResource(DataSet resource, MediaType mediaType) {
		DataSet representation=populateMetadata(resource);
	
		IMediaTypeProvider provider = 
			RuntimeInstance.
				getInstance().
					getMediaTypeProvider(mediaType);
	
		if(provider==null) {
			throw new UnsupportedContentException(endpoint,this,contentVariant());
		}
		
		Marshaller marshaller = 
			provider.
				newMarshaller(ImmutableContext.newInstance(base().resolve(endpoint.path()),resourceResolver()));
		try {
			return marshaller.marshall(representation, mediaType);
		} catch (ContentTransformationException e) {
			throw new ContentProcessingException("Resource representation cannot be parsed as '"+mediaType+"' ",endpoint,this);
		}
	}

	private static final class Context {
		
		private final DataSet dataSet;
	
		private Context(DataSet dataSet) {
			this.dataSet = dataSet;
		}
		
		public URI property(Term term) {
			return term.as(URI.class);
		}
		
		public Value reference(Term term) {
			return dataSet.individual(term.as(URI.class), ExternalIndividual.class);
		}
		
		public Value value(Object value) {
			return DataSetUtils.newLiteral(value);
		}

	}

	private DataSet getMetadata(ResourceTemplate template) {
		final DataSet dataSet=
			DataSetFactory.
				createDataSet(this.endpoint.resourceId().name());
		final Context ctx=new Context(dataSet);
		ManagedIndividualId id=
			ManagedIndividualId.
				createId(
					this.endpoint.resourceId().name(), 
					this.endpoint.resourceId().templateId());
		final Individual<?,?> individual=
			dataSet.individual(id,ManagedIndividual.class);
		template.accept(
			new TemplateVisitor() {
				@Override
				public void visitResourceTemplate(ResourceTemplate template) {
					individual.
						addValue(
							ctx.property(RDF.TYPE), 
							ctx.reference(LDP.RESOURCE));
				}
				@Override
				public void visitContainerTemplate(ContainerTemplate template) {
					visitResourceTemplate(template);
					individual.
						addValue(
							ctx.property(RDF.TYPE), 
							ctx.reference(LDP.CONTAINER));
//					for(Resource member:members) {
//						individual.addValue(
//							ctx.property(LDP.CONTAINS), 
//							ctx.resourceSurrogate(member));
//					}
				}
				@Override
				public void visitBasicContainerTemplate(BasicContainerTemplate template) {
					visitContainerTemplate(template);
					individual.
						addValue(
							ctx.property(RDF.TYPE), 
							ctx.reference(LDP.BASIC_CONTAINER));
				}
				@Override
				public void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
					visitContainerTemplate(template);
					individual.
						addValue(
							template.membershipRelation().toURI(), 
							ctx.value(template.membershipPredicate()));
				}
				@Override
				public void visitDirectContainerTemplate(DirectContainerTemplate template) {
					visitMembershipAwareContainerTemplate(template);
					individual.
						addValue(
							ctx.property(RDF.TYPE), 
							ctx.reference(LDP.DIRECT_CONTAINER));
				}
				@Override
				public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
					visitMembershipAwareContainerTemplate(template);
					individual.
						addValue(
							ctx.property(RDF.TYPE), 
							ctx.reference(LDP.INDIRECT_CONTAINER)).
						addValue(
							ctx.property(LDP.INSERTED_CONTENT_RELATION), 
							ctx.value(template.insertedContentRelation()));
				}
			}
		);
		return dataSet;
	}

	private ResourceType getResourceType(ResourceTemplate template) {
		final AtomicReference<ResourceType> resourceType=new AtomicReference<ResourceType>();
		template.accept(
			new TemplateVisitor() {
				@Override
				public void visitResourceTemplate(ResourceTemplate template) {
					resourceType.set(ResourceType.RESOURCE);
				}
				@Override
				public void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
					resourceType.set(ResourceType.CONTAINER);
				}
				@Override
				public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
					resourceType.set(ResourceType.INDIRECT_CONTAINER);
				}
				@Override
				public void visitDirectContainerTemplate(DirectContainerTemplate template) {
					resourceType.set(ResourceType.DIRECT_CONTAINER);
				}
				@Override
				public void visitContainerTemplate(ContainerTemplate template) {
					resourceType.set(ResourceType.CONTAINER);
				}
				@Override
				public void visitBasicContainerTemplate(BasicContainerTemplate template) {
					resourceType.set(ResourceType.BASIC_CONTAINER);
				}
			}
		);
		return resourceType.get();
	}

	private ResourceResolver resourceResolver() {
		return new OperationContextResourceResolver();
	}

}