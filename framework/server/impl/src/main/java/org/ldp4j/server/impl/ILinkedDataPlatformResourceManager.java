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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;


/**
 * A <b>JAX-RS</b> resource for managing <i>Linked Data Platform Resources</i>.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0-S2
 * @version 1.0
 * @category ALM iStack Linked Data Platform Server
 * @see <a
 *      href="http://www.w3.org/TR/ldp/#linked-data-platform-resource">Linked Data Platform Resource specification</a>
 */
@Path("/resources")
public interface ILinkedDataPlatformResourceManager extends IComponent{

	/**
	 * Request the retrieval of the specified <i>Linked Data Platform
	 * Resource</i> from the <i>Linked Data Platform Server</i> using the
	 * specified format.<br />
	 * The method follows the guidelines of the <a
	 * href="http://www.w3.org/TR/ldp/#http-get">HTTP GET protocol</a> for
	 * <i>Linked Data Platform Resources</i>.
	 * 
	 * @param containerId
	 *            The identifier of the target <i>Linked Data Container</i>.
	 * @param resourceId
	 *            The identifier of the target <i>Linked Data Resource</i>.
	 * @param format
	 *            The expected format in which the contents of the <i>Linked
	 *            Data Resource</i> will have to be serialized. The value
	 *            <code>text/turtle</code> will be used for requesting the
	 *            seialization using the Turtle syntax, and the value
	 *            <code>application/rdf+xml</code> will be used for requesting
	 *            the serialization using the RDF/XML syntax.
	 * @return An HTTP response with status Ok (200) if the operation succeded,
	 *         Server Exception (500) if the container could not retrieve the
	 *         resource contents, and Not found (404) if the resource does not
	 *         exist. In case of failure, the entity will contain a description
	 *         of the failure.
	 */
	@GET
	@Produces({"text/turtle","application/rdf+xml"})
	@Path("/{containerId}/{resourceId}")
	Response getResource(@PathParam("containerId") String containerId, @PathParam("resourceId") String resourceId, @HeaderParam("Accept") String format);

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
	@PUT
	@Consumes({"text/turtle","application/rdf+xml"})
	@Path("/{containerId}/{resourceId}")
	Response updateResource(@PathParam("containerId") String containerId, @PathParam("resourceId") String resourceId, String body, @HeaderParam("Content-Type") String format);

	/**
	 * Request the deletion specified <i>Linked Data Platform Resource</i> from
	 * the <i>Linked Data Platform Server</i>. <br />
	 * The method follows the guidelines of the <a
	 * href="http://www.w3.org/TR/ldp/#http-delete">HTTP Delete protocol</a> for
	 * <i>Linked Data Platform Resources</i>.
	 * 
	 * @param containerId
	 *            The identifier of the target <i>Linked Data Container</i>.
	 * @param resourceId
	 *            The identifier of the target <i>Linked Data Resource</i>.
	 * @return An HTTP response with status status code OK (200) if the resource
	 *         was deleted and the response includes an entity describing the
	 *         status, Accepted (202) if the action has not yet been enacted, or
	 *         No Content (204) if the action has been enacted but the response
	 *         does not include an entity; Server Exception (500) if an internal
	 *         failure precludes the deletion of the resource; and Not found
	 *         (404) or Gone (410) if the resource does not exist or existed at
	 *         some point in time but it no longer does. In case of failure, the
	 *         entity will contain a description of the failure.
	 */
	@DELETE
	@Path("/{containerId}/{resourceId}")
	Response deleteResource(@PathParam("containerId") String containerId, @PathParam("resourceId") String resourceId);

	/**
	 * Gets the list of available <i>Linked Data Platform Resources</i> for the
	 * specified container.
	 * 
	 * @return The identifiers of the registered <i>Linked Data Platform
	 *         Containers</i>.
	 */
	@GET
	@Path("/{containerId}")
	Response getResourceIdentifiers(@PathParam("containerId") String containerId);

}
