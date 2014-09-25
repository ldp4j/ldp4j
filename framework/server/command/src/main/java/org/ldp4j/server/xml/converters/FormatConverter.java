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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.xml.converters;

import javax.ws.rs.core.MediaType;

/**
 * JAXB 2.0 compliant converter to transforming between {@code String} and
 * {@code javax.ws.rs.core.MediaType} instances.
 * 
 * @since 1.0.0
 * @version 1.0
 * @author Miguel Esteban Guti&eacute;rrez
 * @see <a
 *      href="https://jcp.org/aboutJava/communityprocess/mrel/jsr222/index2.html">JSR
 *      222: JavaTM Architecture for XML Binding (JAXB) 2.0</a>
 * @see javax.ws.rs.core.MediaType
 */
public final class FormatConverter {

	private FormatConverter() {
	}

	/**
	 * Transform the specified String into a valid
	 * {@code javax.ws.rs.core.MediaType}
	 * 
	 * @param value
	 *            The string to transform.
	 * @return A {@code javax.ws.rs.core.MediaType} instance that represents the
	 *         specified text.
	 * @throws IllegalArgumentException
	 *             if the specified string cannot be transformed to a proper
	 *             {@code javax.ws.rs.core.MediaType}.
	 * @see javax.ws.rs.core.MediaType
	 */
	public static MediaType parseFormat(String value) {
		String[] split = value.split("/");
		if(split.length!=2) {
			throw new IllegalArgumentException("Invalid media type '"+value+"'");
		}
		return new MediaType(split[0],split[1]);
	}
	
	/**
	 * Transform a {@code javax.ws.rs.core.MediaType} instance into a plain
	 * string.
	 * 
	 * @param value
	 *            the media type to transform.
	 * @return A string that represents the specified value.
	 */
	public static String printFormat(MediaType value) {
		return value.getType()+"/"+value.getSubtype();
	}
	
}
