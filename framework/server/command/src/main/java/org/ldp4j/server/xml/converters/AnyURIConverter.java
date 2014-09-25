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


import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class AnyURIConverter {

	private static final Logger LOGGER=LoggerFactory.getLogger(AnyURIConverter.class);
	
	private AnyURIConverter() {
		// Just to avoid the instantiation of the class
	}
	
	public static URI parseAnyURI(final String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			String errorMessage = "Invalid URI '"+uri+"'";
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(errorMessage,e);
			}
			throw new IllegalArgumentException(errorMessage,e);
		}
	}
	
	public static String printAnyURI(final URI uri) {
		if(uri==null) {
			String errorMessage = "Object 'uri' cannot be null";
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(errorMessage);
			}
			throw new IllegalArgumentException(errorMessage);
		}
		return uri.toString();
	}

}