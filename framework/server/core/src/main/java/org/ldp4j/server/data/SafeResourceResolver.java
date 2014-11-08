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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.MediaType;

import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.server.data.ResolutionContext.ResolutionContextBuilder;
import org.ldp4j.server.spi.ContentTransformationException;
import org.ldp4j.server.spi.IMediaTypeProvider;
import org.ldp4j.server.spi.IMediaTypeProvider.Unmarshaller;
import org.ldp4j.server.spi.RuntimeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

final class SafeResourceResolver implements ResourceResolver {

	static final Logger LOGGER=LoggerFactory.getLogger(SafeResourceResolver.class);

	private ResourceResolver delegate;
	private ResolutionContext resolutionContext;

	private SafeResourceResolver() {
		this.delegate=new NullResourceResolver();
		this.resolutionContext=ResolutionContext.builder().build();
	}

	private void setResolutionContext(ResolutionContext resolutionContext) {
		this.resolutionContext=resolutionContext.withDelegate(this.delegate);
	}

	@Override
	public URI resolveResource(ManagedIndividualId id) {
		return this.delegate.resolveResource(id);
	}

	@Override
	public ManagedIndividualId resolveLocation(URI path) {
		return this.resolutionContext.currentResolver().resolve(path);
	}

	SafeResourceResolver setResourceResolver(ResourceResolver resolver) {
		SafeResourceResolver result = new SafeResourceResolver();
		if(resolver==null) {
			resolver=new NullResourceResolver();
		}
		result.delegate=resolver;
		result.resolutionContext=this.resolutionContext.withDelegate(resolver);
		return result;
	}

	static SafeResourceResolver.SafeResourceResolverBuilder builder() {
		return new SafeResourceResolverBuilder();
	}

	static final class SafeResourceResolverBuilder {

		private static final class URITrackingResourceResolver implements ResourceResolver {

			private final List<URI> uris=new ArrayList<URI>();

			private final Map<URI,ManagedIndividualId> cachedIds;
			private final AtomicInteger counter;

			private ManagedIndividualId getId(URI path) {
				ManagedIndividualId id = cachedIds.get(path);
				if(id==null) {
					Name<Integer> name = NamingScheme.getDefault().name(this.counter.incrementAndGet());
					id=ManagedIndividualId.createId(name, "URITrackingResourceResolver");
					this.cachedIds.put(path, id);
				}
				return id;
			}

			private URITrackingResourceResolver() {
				this.cachedIds=Maps.newLinkedHashMap();
				this.counter=new AtomicInteger();
			}

			@Override
			public URI resolveResource(ManagedIndividualId id) {
				throw new UnsupportedOperationException("Method not supported yet");
			}

			@Override
			public ManagedIndividualId resolveLocation(URI path) {
				this.uris.add(path);
				return getId(path);
			}

			public List<URI> getURIs() {
				return Collections.unmodifiableList(this.uris);
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
			SafeResourceResolver result = new SafeResourceResolver();
			result.setResolutionContext(this.createResolutionContext());
			return result;
		}

		private ResolutionContext createResolutionContext() throws ContentTransformationException {
			URIResolver resolver=URIResolver.newInstance(this.endpoint,this.alternative);
			URIDescriber describer=URIDescriber.newInstance(this.application,this.endpoint);
			List<URI> endpointExternals = externals(this.endpoint);
			List<URI> alternativeExternals = externals(this.alternative);
			ResolutionContextBuilder builder=ResolutionContext.builder();
			for(int i=0;i<endpointExternals.size();i++) {
				URI c1=endpointExternals.get(i);
				URI c2=alternativeExternals.get(i);
				URI uri = resolver.resolve(c1, c2);
				URIDescriptor descriptor = describer.describe(uri);
				builder.withResolution(c1, descriptor, uri);
			}
			return builder.build();
		}

		private List<URI> externals(URI base) throws ContentTransformationException {
			URITrackingResourceResolver resolver = new URITrackingResourceResolver();
			Unmarshaller unmarshaller=
				provider().
					newUnmarshaller(
						ImmutableContext.
							newInstance(
								base,
								resolver
							)
						);
			unmarshaller.unmarshall(this.entity, this.type);
			return resolver.getURIs();
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