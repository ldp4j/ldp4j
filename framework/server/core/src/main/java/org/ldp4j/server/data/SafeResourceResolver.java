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
package org.ldp4j.server.data;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.server.spi.ContentTransformationException;
import org.ldp4j.server.spi.IMediaTypeProvider;
import org.ldp4j.server.spi.IMediaTypeProvider.Unmarshaller;
import org.ldp4j.server.spi.RuntimeInstance;

final class SafeResourceResolver implements ResourceResolver {
	
	static final class Resolution {
		
		private final URI path;
		private final URIDescriptor descriptor;
		private final URI realPath;

		Resolution(URI path, URIDescriptor descriptor, URI realPath) {
			this.path = path;
			this.descriptor = descriptor;
			this.realPath = realPath;
		}
		
		@Override
		public String toString() {
			return String.format("<%s> [<%s>] : %s",path,realPath,descriptor);
		}
		
	}

	private Iterator<Resolution> resolutions;
	private ResourceResolver resolver;

	private SafeResourceResolver() {
		this.resolver=new NullResourceResolver();
	}
	
	@Override
	public URI resolveResource(ResourceId id) {
		return this.resolver.resolveResource(id);
	}

	@Override
	public ResourceId resolveLocation(URI path) {
		if(!resolutions.hasNext()) {
			throw new IllegalStateException("Unexpected resolution <"+path+">");
		}
		Resolution resolution=resolutions.next();
		if(!resolution.path.equals(path)) {
			throw new IllegalStateException("Invalid resolution: expected <"+resolution.path+"> but requested <"+path+">");
		}	
		ResourceId result=null;
		if(resolution.descriptor.isResolvable()) {
			result=this.resolver.resolveLocation(path);
		}
		return result;
	}

	void setResourceResolver(ResourceResolver resolver) {
		if(resolver!=null) {
			this.resolver = resolver;
		}
	}
	
	void setResolutions(Iterable<Resolution> resolutions) {
		this.resolutions=resolutions.iterator();
	}

	static SafeResourceResolver.SafeResourceResolverBuilder builder() {
		return new SafeResourceResolverBuilder();
	}
	
	static final class SafeResourceResolverBuilder {
		
		private static final class URITrackingResourceResolver implements ResourceResolver {

			private final List<URI> uris=new ArrayList<URI>();

			@Override
			public URI resolveResource(ResourceId id) {
				return null;
			}

			@Override
			public ResourceId resolveLocation(URI path) {
				uris.add(path);
				return null;
			}

			public List<URI> getURIs() {
				return Collections.unmodifiableList(uris);
			}
			
		}

		private URI application;
		private URI endpoint;
		private URI alternative;
		private MediaType type;
		private String entity;
		private IMediaTypeProvider provider;

		private SafeResourceResolverBuilder() {
		}
		
		SafeResourceResolver.SafeResourceResolverBuilder withApplication(URI application) {
			this.application = application;
			return this;
		}
		
		SafeResourceResolver.SafeResourceResolverBuilder withEndpoint(URI endpoint) {
			this.endpoint = endpoint;
			return this;
		}
		
		SafeResourceResolverBuilder withAlternative(URI alternative) {
			this.alternative=alternative;
			return this;
		}

		SafeResourceResolverBuilder withEntity(String entity, MediaType type) {
			this.entity=entity;
			this.type=type;
			return this;
		}
		
		SafeResourceResolver build() throws ContentTransformationException {
			List<Resolution> resolutions = this.init();
			SafeResourceResolver result = new SafeResourceResolver();
			result.setResolutions(resolutions);
			return result;
		}
		
		private List<Resolution> init() throws ContentTransformationException {
			URIResolver resolver=URIResolver.newInstance(this.endpoint,this.alternative);
			URIDescriber describer=URIDescriber.newInstance(this.application,this.endpoint);
			List<URI> endpointExternals = externals(this.endpoint);
			List<URI> alternativeExternals = externals(this.alternative);
			List<Resolution> descriptors=new ArrayList<Resolution>();
			for(int i=0;i<endpointExternals.size();i++) {
				URI c1=endpointExternals.get(i);
				URI c2=alternativeExternals.get(i);
				URI uri = resolver.resolve(c1, c2);
				URIDescriptor descriptor = describer.describe(uri);
				descriptors.add(new Resolution(c1, descriptor, uri));
			}
			return descriptors;
		}

		private List<URI> externals(URI base) throws ContentTransformationException {
			URITrackingResourceResolver index = new URITrackingResourceResolver();
			Unmarshaller unmarshaller=
				provider().
					newUnmarshaller(
						ImmutableContext.
							newInstance(
								base,
								index
							)
						);
			unmarshaller.unmarshall(this.entity, this.type);
			return index.getURIs();
		}
		
		private IMediaTypeProvider provider() {
			if(this.provider==null) {
				this.provider = 
					RuntimeInstance.
						getInstance().
						getMediaTypeProvider(this.type);
			}
			return this.provider;
		}

	}
	
}