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
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * A JAX-RS proxy to a <b>Linked Data Platform Container</b>.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public interface IRemoteLDPContainer {
	
	/**
	 * Get the URL of the target Linked Data Platform Container.
	 * @return The URL of the target container.
	 */
	URL getTarget();
	
	/**
	 * Request the creation of a <i>Linked Data Platform Resource</i> to the
	 * target <i>Linked Data Platform Container</i> using the specified source
	 * content. <br />
	 * The method follows the guidelines of the <a
	 * href="http://www.w3.org/2012/ldp/hg/ldp.html#http-post-1">HTTP POST
	 * protocol</a> for <i>Linked Data Platform Containers</i>. Thus, it expects
	 * a Turtle based RDF(S) serialization (see the <a
	 * href="http://www.w3.org/TR/turtle/">Turtle W3C Working Draft</a>) and
	 * returns plain text content.
	 * <p>
	 * @param content The source content for the
	 * 		Linked Data Resource formatted using the Turtle syntax.<br />
	 * 
	 * @return The response of the server.
	 *         </p>
	 */
	@POST
	@Consumes("text/turtle")
	Response createResourceFromTurtle(String content);

	/**
	 * Request the creation of a <i>Linked Data Platform Resource</i> to the
	 * target <i>Linked Data Platform Container</i> using the specified source
	 * content. <br />
	 * The method follows the guidelines of the <a
	 * href="http://www.w3.org/2012/ldp/hg/ldp.html#http-post-1">HTTP POST
	 * protocol</a> for <i>Linked Data Platform Containers</i>, but expecting 
	 * an RDF/XML serialization of the contents. 
	 * @param content The source content for the
	 * Linked Data Resource formatted using the RDF/XML syntax.<br />
	 * 
	 * @return The response of the server.
	 *         </p>
	 */
	@POST
	@Consumes("application/rdf+xml")
	Response createResourceFromRDFXML(String content);

	/**
	 * Request the retrieval of the target <i>Linked Data Platform Container</i>
	 * using the specified format.<br />
	 * The method follows the guidelines of the <a
	 * href="http://www.w3.org/TR/ldp/#http-get-1">HTTP GET protocol</a> for
	 * <i>Linked Data Platform Container</i>.
	 * 
	 * @param format
	 *            The expected syntax in which the contents of the <i>Linked
	 *            Data Container</i> will have to be formatted. The value
	 *            <code>text/turtle</code> will be used for requesting the
	 *            seialization using the Turtle syntax, and the value
	 *            <code>application/rdf+xml</code> will be used for requesting
	 *            the serialization using the RDF/XML syntax.
	 * @returns The response of the server.
	 */
	@GET
	@Produces({"text/turtle","application/rdf+xml"})
	Response getResource(@HeaderParam("Accept") String format);

	Response getResource(String format, boolean includeMembers, boolean includeSummary);
}
