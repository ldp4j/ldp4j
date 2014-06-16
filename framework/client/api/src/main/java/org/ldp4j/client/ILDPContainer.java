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
 *   Artifact    : org.ldp4j.framework:ldp4j-client-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-client-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.client;

import java.net.URL;


/**
 * A proxy to a <b>Linked Data Platform Container</b>.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public interface ILDPContainer {

	/**
	 * Get the identity of the container.
	 * 
	 * @return The identity of the container or <code>null</code> if the container
	 *         is not published yet.
	 */
	URL getIdentity();

	/**
	 * Create a <i>Linked Data Platform Resource</i> using the specified format.
	 * 
	 * @param content
	 *            The content of the resource.
	 * @param format
	 *            The syntax in which the content is formatted.
	 * @return The identity of the created resource.
	 * @throws LDPContainerException
	 *             if the content cannot be published.
	 * @see Format
	 */
	URL createResource(IContent content, Format format) throws LDPContainerException;

	/**
	 * Retrieve the description of the <i>Linked Data Platform Container</i> using the specified format.
	 * 
	 * @param format
	 *            The syntax in which the description is be formatted.
	 * @param excludeMembers
	 *            Whether or not to exclude container's member properties.
	 * @param exclude_members_properties
	 *            Whether or not to exclude member specific properties.
	 * @return The description of the container in the specified format.
	 * @throws LDPContainerException
	 *             if the description cannot be retrieved.
	 * @see Format
	 */
	IContent getDescription(Format format, boolean excludeMembers, boolean excludeMemberProperties) throws LDPContainerException;

	/**
	 * Retrieve the member resources of the <i>Linked Data Platform Container</i>.
	 * 
	 * @param format
	 *            The syntax in which the description is be formatted.
	 * @return A list including the location of the member resources that belong to the container
	 * @throws LDPContainerException
	 *             if the resources cannot be retrieved
	 * @see Format
	 */
	IContent searchResources(Format format, int page, int count) throws LDPContainerException;

}