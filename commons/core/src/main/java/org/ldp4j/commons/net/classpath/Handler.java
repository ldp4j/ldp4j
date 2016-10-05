/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014-2016 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.2.2
 *   Bundle      : ldp4j-commons-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.commons.net.classpath;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Handler extends URLStreamHandler {

	public static final String PROTOCOL_SCHEME = "classpath";

	private static final Logger LOGGER=LoggerFactory.getLogger(Handler.class);

	private final ClassLoader classLoader;

	public Handler() {
		this.classLoader=getClass().getClassLoader();
	}

	public Handler(ClassLoader classLoader) {
		this.classLoader=classLoader;
	}

	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		String resource = extractResource(url);
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(String.format("Creating stream for resource <%s>, using class loader <%s>...",resource,classLoader));
		}
		final URL resourceURL=classLoader.getResource(resource);
		if(resourceURL==null) {
			throw new IOException(String.format("Resource <%s> could not be found in the classpath.",resource));
		}
		return resourceURL.openConnection();
	}

	private String extractResource(final URL url) throws MalformedURLException {
		if(!url.getProtocol().equals(PROTOCOL_SCHEME)) {
			throw new MalformedURLException("Unknown protocol "+url.getProtocol());
		}
		String resource = url.toString().substring(PROTOCOL_SCHEME.length()+1);
		while(resource.startsWith("/")) {
			resource=resource.substring(1);
		}
		return resource;
	}

}