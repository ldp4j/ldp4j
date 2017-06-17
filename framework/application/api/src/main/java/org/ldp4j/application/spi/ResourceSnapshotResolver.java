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
package org.ldp4j.application.spi;

import java.net.URI;

import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SnapshotResolutionException;

/**
 * Utility class for resolving the endpoints (URI) where the resources managed
 * by the LDP4j framework are published, and for discovering which resources are
 * addressed by a given URI.
 */
public interface ResourceSnapshotResolver {

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
	ResourceSnapshot resolve(URI endpoint);

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
	URI resolve(ResourceSnapshot resource);

}
