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

/**
 * The set of possible syntaxes in which the content of a <b>Linked Data Platform
 * Resource</b> can be formatted.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 * @see org.ldp4j.client.ILDPResource#getContent(Format)
 */
public enum Format {
	
	/** The Turtle format. */
	Turtle("text/turtle"),
	
	/** The RDF/XML format. */
	RDFXML("application/rdf+xml"),
	;
	
	/** The mime associated to the format. */
	private final String mime;

	/**
	 * Instantiates a new format.
	 *
	 * @param mime the mime
	 */
	Format(String mime) {
		this.mime = mime;
	}

	/**
	 * Get the format defined for a specified mime.
	 * 
	 * @param mime
	 *            the mime whose format is to be retrieved.
	 * @return the format associated to the mime, or {@code null} if there is no
	 *         format associated to the mime.
	 */
	public static Format fromMime(String mime) {
		Format result=null;
		for(Format format:values()) {
			if(format.getMime().equalsIgnoreCase(mime)) {
				result=format;
				break;
			}
		}
		return result;
	}

	
	/**
	 * Gets the mime of the format.
	 *
	 * @return the mime associated to the format.
	 */
	public String getMime() {
		return mime;
	}
}