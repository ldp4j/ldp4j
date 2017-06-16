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
package org.ldp4j.server.data;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.ldp4j.rdf.Node;
import org.ldp4j.rdf.NodeVisitor;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.URIRef;
import org.ldp4j.server.data.spi.ContentTransformationException;
import org.ldp4j.server.data.spi.MediaTypeProvider;
import org.ldp4j.server.data.spi.RuntimeDelegate;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

final class TripleResolver {

	static final class TripleResolverBuilder {

		private final class ImmutableTripleResolution implements TripleResolution {

			private final Triple triple;
			private final ResourceResolution subjectResolution;
			private final ResourceResolution objectResolution;

			private ImmutableTripleResolution(
					Triple triple,
					ResourceResolution subjectResolution,
					ResourceResolution objectResolution) {
				this.triple = triple;
				this.subjectResolution = subjectResolution;
				this.objectResolution = objectResolution;
			}

			@Override
			public Triple triple() {
				return triple;
			}

			@Override
			public ResourceResolution subjectResolution() {
				return subjectResolution;
			}

			@Override
			public ResourceResolution objectResolution() {
				return objectResolution;
			}

			@Override
			public String toString() {
				return
					MoreObjects.
						toStringHelper(getClass()).
							omitNullValues().
							add("triple",this.triple).
							add("subjectResolution",this.subjectResolution).
							add("objectResolution", this.objectResolution).
							toString();
			}

		}

		private final class ResourceResolutionGenerator extends NodeVisitor<ResourceResolution> {

			private final Node alternativeNode;

			private ResourceResolutionGenerator(Node alternativeNode) {
				this.alternativeNode = alternativeNode;
			}

			@Override
			public ResourceResolution visitURIRef(URIRef endpointURIRef, ResourceResolution defaultResult) {
				URIRef alternativeURIRef=(URIRef)alternativeNode;
				URI uri = resolver.resolve(endpointURIRef.getIdentity(), alternativeURIRef.getIdentity());
				URIDescriptor descriptor = describer.describe(uri);
				return ResourceResolutionFactory.customResolution(uri, descriptor);
			}

		}

		private MediaTypeProvider unmarshaller;
		private URIResolver resolver;
		private URIDescriber describer;
		private TripleResolver result;

		private TripleResolverBuilder() {
			this.result=new TripleResolver();
		}

		TripleResolverBuilder withApplication(URI application) {
			this.result.setApplication(application);
			return this;
		}

		TripleResolverBuilder withEndpoint(URI endpoint) {
			this.result.setEndpoint(endpoint);
			return this;
		}

		TripleResolverBuilder withAlternative(URI alternative) {
			this.result.setAlternative(alternative);
			return this;
		}

		TripleResolverBuilder withEntity(String entity, MediaType type) {
			this.result.setEntity(entity,type);
			return this;
		}

		TripleResolver build() throws ContentTransformationException {
			this.resolver=URIResolver.newInstance(this.result.endpoint(),this.result.alternative());
			this.describer=URIDescriber.newInstance(this.result.application(),this.result.endpoint());
			this.result.setTripleResolutions(createResolutions());
			return new TripleResolver(this.result);
		}

		private List<TripleResolution> createResolutions() throws ContentTransformationException {
			List<Triple> endpointTriples=triples(this.result.endpoint());
			List<Triple> alternativeTriples=triples(this.result.alternative());
			Builder<TripleResolution> builder = ImmutableList.builder();
			for(int i=0;i<endpointTriples.size();i++) {
				builder.add(resolveTriple(endpointTriples.get(i), alternativeTriples.get(i)));
			}
			return builder.build();
		}

		private TripleResolution resolveTriple(Triple tEndpoint, Triple tAlternative) {
			return
				new ImmutableTripleResolution(
					tEndpoint,
					resolveResource(tEndpoint.getSubject(),tAlternative.getSubject()),
					resolveResource(tEndpoint.getObject(), tAlternative.getObject())
				);
		}

		private ResourceResolution resolveResource(Node nEndpoint,Node nAlternative) {
			return
				nEndpoint.
					accept(
						new ResourceResolutionGenerator(nAlternative),
						ResourceResolutionFactory.nullResolution()
					);
		}

		private List<Triple> triples(URI base) throws ContentTransformationException {
			return
				ImmutableList.
					copyOf(
						mediaTypeProvider().
							unmarshallContent(
								ImmutableContext.newInstance(base),
								this.result.entity(),
								this.result.mediaType()));
		}

		private MediaTypeProvider mediaTypeProvider() {
			if(this.unmarshaller==null) {
				this.unmarshaller=
					RuntimeDelegate.
						getInstance().
							getMediaTypeProvider(this.result.mediaType());
			}
			return this.unmarshaller;
		}
	}

	private URI application;
	private URI endpoint;
	private URI alternative;
	private MediaType type;
	private String entity;
	private List<TripleResolution> resolutions;

	private TripleResolver() {
	}

	private TripleResolver(TripleResolver other) {
		setApplication(other.application);
		setEndpoint(other.endpoint);
		setAlternative(other.alternative);
		setEntity(other.entity,other.type);
		setTripleResolutions(other.resolutions);
	}

	private void setEntity(String entity, MediaType type) {
		this.entity=entity;
		this.type=type;
	}

	private void setAlternative(URI alternative) {
		this.alternative=alternative;
	}

	private void setEndpoint(URI endpoint) {
		this.endpoint = endpoint;
	}

	private void setApplication(URI application) {
		this.application = application;
	}

	private void setTripleResolutions(List<TripleResolution> resolutions) {
		this.resolutions = resolutions;
	}

	URI application() {
		return this.application;
	}

	URI endpoint() {
		return this.endpoint;
	}

	URI alternative() {
		return this.alternative;
	}

	String entity() {
		return this.entity;
	}

	MediaType mediaType() {
		return this.type;
	}

	List<TripleResolution> tripleResolutions() {
		return this.resolutions;
	}

	static TripleResolverBuilder builder() {
		return new TripleResolverBuilder();
	}

}
