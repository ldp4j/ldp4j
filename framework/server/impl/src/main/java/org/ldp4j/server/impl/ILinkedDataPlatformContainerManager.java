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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


/**
 * A <b>JAX-RS</b> resource for managing <i>Linked Data Platform Containers</i>.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 * @see <a
 *      href="http://www.w3.org/TR/ldp/#linked-data-platform-container">Linked Data Platform Container specification</a>
 */
@Path("/containers")
public interface ILinkedDataPlatformContainerManager extends IComponent {

	/**
	 * Request the creation of a <i>Linked Data Platform Resource</i> to the
	 * specified <i>Linked Data Platform Container</i> with a given source
	 * content. <br />
	 * The method follows the guidelines of the <a
	 * href="http://www.w3.org/2012/ldp/hg/ldp.html#http-post-1">HTTP POST
	 * protocol</a> for <i>Linked Data Platform Containers</i>. Thus, it expects
	 * a Turtle based RDF(S) serialization (see the <a
	 * href="http://www.w3.org/TR/turtle/">Turtle W3C Working Draft</a>) and
	 * returns plain text content.
	 * 
	 * @param containerId
	 *            The identifier of the target <i>Linked Data Container</i>.
	 * @param body
	 *            The source content for the Linked Data Resource. The contents
	 *            should be an RDF(S) Turtle serialization.
	 * @return An HTTP response with status Created (201) if the operation
	 *         succeded, Server Exception (500) if the container could not
	 *         create the resource, and Not found (404) if the container does
	 *         not exist. If the resource could be created, its location will be
	 *         included in the entity of the response as well as in a Location
	 *         header. In case of failure, the entity will contain a description
	 *         of the failure.
	 */
	@POST
	@Consumes({"text/turtle","application/rdf+xml"})
	@Path("/{containerId}")
	Response createResource(@PathParam("containerId") String containerId, String body, @HeaderParam("Content-Type") String contentType);

	/**
	 * Gets the list of available <i>Linked Data Platform Containers</i>.
	 * 
	 * @return The identifiers of the registered <i>Linked Data Platform
	 *         Containers</i>.
	 */
	@GET
	Response getContainerIdentifiers();

	/**
	 * Request the creation of a <i>Linked Data Platform Resource</i> to the
	 * specified <i>Linked Data Platform Container</i> with a given source
	 * content. <br />
	 * The method follows the guidelines of the <a
	 * href="http://www.w3.org/2012/ldp/hg/ldp.html#http-post-1">HTTP POST
	 * protocol</a> for <i>Linked Data Platform Containers</i>. Thus, it expects
	 * a Turtle based RDF(S) serialization (see the <a
	 * href="http://www.w3.org/TR/turtle/">Turtle W3C Working Draft</a>) and
	 * returns plain text content.
	 * 
	 * @param containerId
	 *            The identifier of the target <i>Linked Data Container</i>.
	 * @param body
	 *            The source content for the Linked Data Resource. The contents
	 *            should be an RDF(S) Turtle serialization.
	 * @return An HTTP response with status Created (201) if the operation
	 *         succeded, Server Exception (500) if the container could not
	 *         create the resource, and Not found (404) if the container does
	 *         not exist. If the resource could be created, its location will be
	 *         included in the entity of the response as well as in a Location
	 *         header. In case of failure, the entity will contain a description
	 *         of the failure.
	 */
	@GET
	@Produces({"text/turtle","application/rdf+xml"})
	@Path("/{containerId}")
	Response search(@Context UriInfo uriInfo, @PathParam("containerId") String containerId, @HeaderParam("Accept") String contentType);
}
