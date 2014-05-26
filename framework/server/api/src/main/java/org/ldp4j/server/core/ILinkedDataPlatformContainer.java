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
import org.ldp4j.server.LinkedDataPlatformException;




/**
 * A service provider interface for creating <i>Linked Data Platform
 * Containers</i> that can be exposed via the <b>LDP4j Linked Data Platform 
 * Server</b>.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public interface ILinkedDataPlatformContainer {

	/**
	 * Gets the identifier of the container.
	 *
	 * @return the container id
	 */
	String getContainerId();

	/**
	 * Create a <i>Linked Data Platform Resource</i> using the specified
	 * content.
	 * 
	 * @param body
	 *            An RDF(S) serialization of the content for the resource.
	 * @param format The format in which the contents are serialized.
	 * @return The relative path to the created resource.
	 * @throws LinkedDataPlatformException
	 *             if an unexpected failure precludes the creation of the
	 *             resource.
	 */
	String createResource(IContent body, Format format) throws LinkedDataPlatformException;

	/**
	 * Get a summary of the contents of the specified resources managed by the
	 * <i>Linked Data Platform Container</i> using the specified format.
	 * 
	 * @param resources
	 *            The identifiers of the resources whose summary is to be
	 *            generated.
	 * @param format
	 *            The format in which the summary is to be provided.
	 * @return The summary of the container contents for the specified resources
	 *         in the specified format.
	 * @throws LinkedDataPlatformException
	 *             if an unexpected failure precludes the creation of the
	 *             summary.
	 */
	IContent getSummary(Collection<String> resources, Format format) throws LinkedDataPlatformException;

}