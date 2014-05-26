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
package org.ldp4j.server.core;

import java.util.Collection;

import org.ldp4j.server.Format;
import org.ldp4j.server.IContent;
import org.ldp4j.server.IResource;
import org.ldp4j.server.LinkedDataPlatformException;



/**
 * <b></b>A service provider interface for creating <i>Linked Data Platform
 * Resource</i> that can be exposed via the <b>LDP4j Linked Data
 * Server</b>.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public interface ILinkedDataPlatformResourceHandler {

	/**
	 * Gets the identifier of the associated container.
	 *
	 * @return the identifier of the associated container
	 */
	String getContainerId();

	/**
	 * Retrieve the specified <i>Linked Data Platform Resource</i>.
	 * 
	 * @param id
	 *            The identifier of the resource that is to be retrieved.
	 * @return The resource that matches the specified identifier or null if the
	 *         specified resource does not exist.
	 * @throws LinkedDataPlatformException
	 *             if an unexpected failure precludes the retrieval of the
	 *             resource.
	 */
	IResource getResource(String id) throws LinkedDataPlatformException;

	/**
	 * Update the contents of the specified <i>Linked Data Platform
	 * Resource</i>.
	 * 
	 * @param id
	 *            The identifier of the resource that is to be updated.
	 * @param content
	 *            The new contents for the specified resource.
	 * @param format
	 *            The format in which the contents are serialized.
	 * @return The updated resource or null if the specified resource does not
	 *         exist.
	 * @throws LinkedDataPlatformException
	 *             if an unexpected failure precludes the update of the
	 *             resource.
	 */
	IResource updateResource(String resourceId, IContent content, Format format) throws LinkedDataPlatformException;
	

	/**
	 * Retrieve the identifiers of the available <i>Linked Data Platform
	 * Resources</i>.
	 * 
	 * @return A collection with the identifiers of the available resources.
	 * @throws LinkedDataPlatformException
	 *             if an unexpected failure precludes the retrieval of the
	 *             identifiers.
	 */
	Collection<String> getResourceList() throws LinkedDataPlatformException;

}