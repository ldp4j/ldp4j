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
package org.ldp4j.client.impl.sdk;

import java.net.URL;

import org.ldp4j.client.impl.spi.IRemoteLDPResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base JAX-RS proxy implementation to <i>Linked Data Platform Resources</i>.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 * @see org.ldp4j.client.impl.spi.IRemoteLDPResource
 */
public abstract class AbstractRemoteLDPResource implements IRemoteLDPResource {

	private static final Logger LOGGER=LoggerFactory.getLogger(AbstractRemoteLDPResource.class);
	
	private final URL target;
	
	/**
	 * Create an base JAX-RS proxy for the specified <i>Linked Data Platform
	 * Resource</i>.
	 * 
	 * @param target
	 *            The URL of the target resource.
	 */
	public AbstractRemoteLDPResource(URL target) {
		if(target==null) {
			throw new IllegalStateException("Object 'target' cannot be null");
		}
		this.target=target;
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Creating remote LDP resource for target '%s'",target));
		}
	}

	/**
	 * ${inheritDoc}
	 */
	@Override
	public final URL getTarget() {
		return this.target;
	}
	
}