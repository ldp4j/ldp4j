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

import com.google.common.base.MoreObjects;

final class ResourceResolutionFactory {

	private static final class CustomResourceResolution implements ResourceResolution {

		private final URI uri;
		private final URIDescriptor descriptor;

		CustomResourceResolution(URI uri, URIDescriptor descriptor) {
			this.uri = uri;
			this.descriptor = descriptor;
		}

		@Override
		public boolean isResolvable() {
			return descriptor.isResolvable();
		}

		@Override
		public boolean isTransient() {
			return this.descriptor.isTransient();
		}

		@Override
		public URI realURI() {
			return uri;
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						omitNullValues().
						add("resolvable",descriptor.isResolvable()).
						add("transient",descriptor.isTransient()).
						add("uri","<"+uri+">").
						toString();
		}

	}

	private static final class NullResourceResolution implements ResourceResolution {

		@Override
		public boolean isResolvable() {
			return false;
		}

		@Override
		public boolean isTransient() {
			return false;
		}

		@Override
		public URI realURI() {
			return null;
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						toString();
		}

	}

	private ResourceResolutionFactory() {
	}

	static ResourceResolution nullResolution() {
		return new NullResourceResolution();
	}

	static ResourceResolution customResolution(URI path, URIDescriptor descriptor) {
		return new CustomResourceResolution(path, descriptor);
	}

}
