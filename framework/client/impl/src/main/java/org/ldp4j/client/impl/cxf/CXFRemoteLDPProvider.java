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
package org.ldp4j.client.impl.cxf;

import java.net.URL;

import org.ldp4j.client.impl.spi.IRemoteLDPContainer;
import org.ldp4j.client.impl.spi.IRemoteLDPProvider;
import org.ldp4j.client.impl.spi.IRemoteLDPResource;

/**
 * CXF-based JAX-RS proxy provider.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 * @see org.ldp4j.client.impl.spi.IRemoteLDPProvider
 */
public final class CXFRemoteLDPProvider implements IRemoteLDPProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IRemoteLDPContainer createContainerProxy(URL target) {
		if(target==null) {
			throw new IllegalArgumentException("Object 'target' cannot be null");
		}
		return new CXFRemoteLDPContainer(target);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IRemoteLDPResource createResourceProxy(URL target) {
		if(target==null) {
			throw new IllegalArgumentException("Object 'target' cannot be null");
		}
		return new CXFRemoteLDPResource(target);
	}

}