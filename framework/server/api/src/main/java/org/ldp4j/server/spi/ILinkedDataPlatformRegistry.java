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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.spi;

import java.util.List;

import org.ldp4j.server.core.ILinkedDataPlatformContainer;
import org.ldp4j.server.core.ILinkedDataPlatformResourceHandler;


/**
 * Service interface that allows inspection over the <i>Linked Data Platform
 * Containers</i> or <i>Linked Data Platform Resource handlers</i> registered in
 * the <b>LDP4j Linked Data Platform Server</b>.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public interface ILinkedDataPlatformRegistry {

	/**
	 * Retrieve the list of identifiers of the registered containers.
	 * 
	 * @return A list with the identifiers of the registered containers.
	 */
	List<String> getContainerIds();

	/**
	 * Gets the container registered for the specified identifier.
	 * 
	 * @param containerId
	 *            The identifier of the container that is to be retrieved
	 * @return The container which is registered for the specified identifier.
	 * @see org.centeropenmiddleware.almistack.middleware.ldp.server.spi.ILinkedDataPlatformResourceContainer
	 */
	ILinkedDataPlatformContainer getContainer(String containerId);

	/**
	 * Gets the resource handler associated to the specified container.
	 * 
	 * @param containerId
	 *            The identifier of the container to which the resource handler
	 *            is associated.
	 * @return The resource handler.
	 * @see org.ldp4j.server.core.ILinkedDataPlatformResourceHandler
	 */
	ILinkedDataPlatformResourceHandler getResourceHandler(String containerId);


	/**
	 * Gets the type-specific container registered for the specified identifier.
	 * 
	 * @param containerId
	 *            The identifier of the container that is to be retrieved
	 * @param containerClass
	 *            The specific {@link ILinkedDataPlatformContainer} extension
	 *            that is to be retrieved.
	 * @return The container which is registered for the specified identifier if
	 *         it is an instance of the specified extension, and
	 *         <code>null</code> otherwise.
	 * @see org.centeropenmiddleware.almistack.middleware.ldp.server.spi.ILinkedDataPlatformResourceContainer
	 */
	<C extends ILinkedDataPlatformContainer> C getContainer(
			String containerId, Class<C> containerClass);

	/**
	 * Gets the type-specific resource handler associated to the specified
	 * identifier.
	 * 
	 * @param containerId
	 *            The identifier of the container to which the resource handler
	 *            is associated.
	 * @param containerClass
	 *            The specific {@link ILinkedDataPlatformResourceHandler}
	 *            extension that is to be retrieved.
	 * @return The resource handler which is registered for the specified
	 *         identifier if it is an instance of the specified extension, and
	 *         <code>null</code> otherwise.
	 * @see org.ldp4j.server.core.ILinkedDataPlatformResourceHandler
	 */
	<C extends ILinkedDataPlatformResourceHandler> C getResourceHandler(
			String containerId, Class<C> resourceHandlerClass);

}