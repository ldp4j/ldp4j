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
 *   Artifact    : org.ldp4j.framework:ldp4j-client-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-client-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.client.impl.spi;

import java.net.URL;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * A JAX-RS proxy to a <b>Linked Data Platform Resource</b>.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public interface IRemoteLDPResource {
	
	/**
	 * Get the URL of the target Linked Data Platform Resource.
	 * @return The URL of the target resource.
	 */
	URL getTarget();
	
	/**
	 * Request the retrieval of the target <i>Linked Data Platform Resource</i>
	 * using the specified format.<br />
	 * The method follows the guidelines of the <a
	 * href="http://www.w3.org/TR/ldp/#http-get">HTTP GET protocol</a> for
	 * <i>Linked Data Platform Resources</i>.
	 * 
	 * @param format
	 *            The expected syntax in which the contents of the <i>Linked
	 *            Data Resource</i> will have to be formatted. The value
	 *            <code>text/turtle</code> will be used for requesting the
	 *            seialization using the TURTLE syntax, and the value
	 *            <code>application/rdf+xml</code> will be used for requesting
	 *            the serialization using the RDFS/XML syntax.
	 * @returns The response of the server.
	 */
	@GET
	@Produces({"text/turtle","application/rdf+xml"})
	Response getResource(@HeaderParam("Accept") String format);

	/**
	 * Request the update of the target <i>Linked Data Platform Resource</i>
	 * using the specified contents and format.<br />
	 * The method follows the guidelines of the <a
	 * href="http://www.w3.org/TR/ldp/#http-get">HTTP PUT protocol</a> for
	 * <i>Linked Data Platform Resources</i>.
	 * 
	 * @param format
	 *            The expected syntax in which the contents of the <i>Linked
	 *            Data Resource</i> are formatted. The value
	 *            <code>text/turtle</code> should be used for contents serialized
	 *            using the TURTLE syntax, and the value
	 *            <code>application/rdf+xml</code> for contents serialized using 
	 *            the RDFS/XML syntax.
	 * @returns The response of the server.
	 */
	@PUT
	@Consumes({"text/turtle","application/rdf+xml"})
	Response updateResource(String body, @HeaderParam("Accept") String format);

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
	 *         No Entity (204) if the action has been enacted but the response
	 *         does not include an entity; Server Exception (500) if an internal
	 *         failure precludes the deletion of the resource; and Not found
	 *         (404) or Gone (410) if the resource does not exist or existed at
	 *         some point in time but it no longer does. In case of failure, the
	 *         entity will contain a description of the failure.
	 */
	@DELETE
	Response delete();

}