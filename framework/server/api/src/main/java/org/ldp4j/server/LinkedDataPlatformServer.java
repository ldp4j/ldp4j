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
package org.ldp4j.server;

import org.ldp4j.server.spi.ILinkedDataPlatformRegistry;
import org.ldp4j.server.spi.IResourceManager;
import org.ldp4j.server.spi.RuntimeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The LinkedDataPlatformServer class allows developers to communicate with the
 * <b>LDP4j Linked Data Platform Server</b>, regaining access to its
 * internal services if required for implementing <i>LDP4j
 * Containers</i> or <i>LDP4j Resource handlers</i> .
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public final class LinkedDataPlatformServer {

	/**
	 * Internal logger.
	 */
	private static final Logger LOGGER=LoggerFactory.getLogger(LinkedDataPlatformServer.class);
	
	private LinkedDataPlatformServer() {
	}
	
	/**
	 * Retrieve the registry of the <b>LDP4j Linked Data Platform Server</b>.
	 * 
	 * @return The registry implementation of the <b>LDP4j Linked Data
	 *         Platform Server</b>.
	 * @throws IllegalStateException
	 *             if no registry implementation is available.
	 * @see org.ldp4j.server.spi.ILinkedDataPlatformRegistry
	 */
	public static ILinkedDataPlatformRegistry getRegistry() {
		ILinkedDataPlatformRegistry registry=RuntimeInstance.getInstance().getRegistry();
		if(registry==null) {
			String errorMessage = "No Linked Data Platform Registry implementation available";
			if(LOGGER.isErrorEnabled()) {
				LOGGER.error(errorMessage);
			}
			throw new IllegalStateException(errorMessage);
		}
		return registry;
	}

	/**
	 * Retrieve the resource manager of the <b>LDP4j Linked Data Platform
	 * Server</b>.
	 * 
	 * @return The resource manager implementation of the <b>LDP4j Linked Data
	 *         Platform Server</b>.
	 * @throws IllegalStateException
	 *             if no resource manager implementation is available.
	 * @see org.ldp4j.server.spi.ILinkedDataPlatformRegistry
	 */
	public static IResourceManager getManager() {
		IResourceManager manager=RuntimeInstance.getInstance().getManager();
		if(manager==null) {
			String errorMessage = "No Linked Data Platform Resource Manager implementation available";
			if(LOGGER.isErrorEnabled()) {
				LOGGER.error(errorMessage);
			}
			throw new IllegalStateException(errorMessage);
		}
		return manager;
	}
	
}