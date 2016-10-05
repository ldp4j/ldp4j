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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.session;

import java.net.URI;
import java.util.Objects;

import org.ldp4j.application.spi.ResourceSnapshotResolver;
import org.ldp4j.application.spi.RuntimeDelegate;

/**
 * Utility class for resolving the endpoints (URI) where the resources managed
 * by the LDP4j framework are published, and for discovering which resources are
 * addressed by a given URI.
 */
public final class SnapshotResolver {

	/**
	 * Builder class for creating {@code SnapshotResolver} instances.
	 */
	public static final class Builder {

		private ReadSession session;
		private URI canonicalBase;

		private Builder() {
		}

		/**
		 * Define the canonical base URI to be used for the resolution of
		 * endpoints.
		 *
		 * @param base
		 *            the canonical base URI.
		 * @return this builder.
		 */
		public Builder withCanonicalBase(URI base) {
			this.canonicalBase = base;
			return this;
		}

		/**
		 * Define the session to be used for the resolution of resource
		 * snapshots.
		 *
		 * @param session
		 *            the session
		 * @return this builder.
		 */
		public Builder withReadSession(ReadSession session) {
			this.session = session;
			return this;
		}

		/**
		 * Return a configured snapshot resolver instance.
		 *
		 * @return a configured snapshot resolver instance
		 * @throws NullPointerException
		 *             if the canonical base URI or the session are null.
		 * @throws IllegalArgumentException
		 *             if the canonical base URI is not hierarchical and
		 *             absolute.
		 */
		public SnapshotResolver build() {
			return
				new SnapshotResolver(
					RuntimeDelegate.
						getInstance().
							createResourceResolver(
								getCanonicalBase(),
								Objects.requireNonNull(this.session,"Session cannot be null")));
		}

		private URI getCanonicalBase() {
			Objects.requireNonNull(this.canonicalBase,"Canonical base cannot be null");
			if(this.canonicalBase.isOpaque()) {
				throw new IllegalArgumentException("Canonical base '"+this.canonicalBase+"' is not hierarchical");
			}
			if(!this.canonicalBase.isAbsolute()) {
				throw new IllegalArgumentException("Canonical base '"+this.canonicalBase+"' is not absolute");
			}
			return this.canonicalBase;
		}

	}

	private final ResourceSnapshotResolver resolver;

	private SnapshotResolver(ResourceSnapshotResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * Return the URI used to publish a resource.
	 *
	 * @param snapshot
	 *            the snapshot of the resource that is to be resolved.
	 * @throws SnapshotResolutionException
	 *             if the snapshot cannot be resolved to a URI.
	 * @return the URI used to published the resource that represents the
	 *         snapshot.
	 */
	public URI toURI(ResourceSnapshot snapshot) {
		return this.resolver.resolve(snapshot);
	}

	/**
	 * Return a snapshot of the resource published at a given URI.
	 *
	 * @param endpoint
	 *            the URI that is to be resolved.
	 * @throws SnapshotResolutionException
	 *             if the URI cannot be resolved to a resource snapshot.
	 * @return a snapshot of the resource that is published at the specified
	 *         endpoint.
	 */
	public ResourceSnapshot fromURI(URI endpoint) {
		return this.resolver.resolve(endpoint);
	}

	/**
	 * Create a {@code Builder} instance.
	 * @return a {@code Builder} instance.
	 */
	public static Builder builder() {
		return new Builder();
	}

}
