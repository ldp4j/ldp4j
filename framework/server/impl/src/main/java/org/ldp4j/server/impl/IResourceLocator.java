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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.impl;

import java.net.URI;

import javax.ws.rs.core.UriInfo;

/**
 * Identity management utility service interface that allows the <b>ALM iStack
 * Linked Data Platform Server</b> selecting different naming schemes to be used
 * at runtime.

 * @author Miguel Esteban Gutiérrez
 * @since 1.0-S2
 * @version 1.0
 * @category ALM iStack Linked Data Platform Server SDK
 */
public interface IResourceLocator {

	/**
	 * Create an absolute URI for the specified resource within the specified
	 * container.
	 * 
	 * @param context
	 *            The context within which the absolute URI will be generated.
	 * @param containerId
	 *            The identifier of the container to which the resource belongs.
	 * @param resourceId
	 *            The identifier of the created resource.
	 * @return The absolute URI for the resource that belongs to the specified
	 *         container according to the provided context.
	 */
	URI createResourceLocation(UriInfo context, String containerId, String resourceId);

	/**
	 * Create a relative URI for the specified resource within the specified
	 * container.
	 * <p>
	 * <span class="tags"></span>@param containerId The identifier of the
	 * container to which the resource belongs.<br />
	 * 
	 * @param resourceId
	 *            The identifier of the created resource.<br />
	 * @return The relative URI for the resource that belongs to the specified
	 *         container.
	 *         </p>
	 */
	URI createResourceLocation(String containerId, String resourceId);
	
	
	String resolveResourceFromLocation(URI location);

	/**
	 * Create an absolute URI for the specified container.
	 * 
	 * @param context
	 *            The context within which the absolute URI will be generated.
	 * @param containerId
	 *            The identifier of the container.
	 * @return The absolute URI for the container according to the provided context.
	 */
	URI createContainerLocation(UriInfo context, String containerId);

	/**
	 * Create a relative URI for the specified container.
	 * 
	 * @param containerId
	 *            The identifier of the container.
	 * @return The relative URI for the specifiedcontainer.
	 */
	URI createContainerLocation(String containerId);

	String resolveContainerFromLocation(URI location);

}
