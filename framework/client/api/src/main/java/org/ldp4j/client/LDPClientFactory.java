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

import java.net.URL;

import org.ldp4j.client.spi.RuntimeInstance;

/**
 * LDP4j Client front-end for the creation of proxies to <i>Linked
 * Data Platform Containers</i> and <i>Resources</i>.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public final class LDPClientFactory {

	private LDPClientFactory() {
	}
	
	/**
	 * Create a proxy for the specified <i>Linked Data Platform Container</i>.
	 * 
	 * @param target
	 *            The identity of the container.
	 * @return A proxy to the specified container.
	 * @throws IllegalArgumentException
	 *             if the target is <code>null</code>.
	 * @see ILDPContainer
	 */
	public static ILDPContainer createContainer(URL target) {
		if(target==null) {
			throw new IllegalArgumentException("Object 'target' cannot be null");
		}
		return RuntimeInstance.getInstance().createContainer(target);
	}
	
	/**
	 * Create a proxy for the specified <i>Linked Data Platform Resource</i>.
	 * 
	 * @param target
	 *            The identity of the resource.
	 * @return A proxy to the specified resource.
	 * @throws IllegalArgumentException
	 *             if the target is <code>null</code>.
	 * @see ILDPContainer
	 */
	public static ILDPResource createResource(URL target) {
		if(target==null) {
			throw new IllegalArgumentException("Object 'target' cannot be null");
		}
		return RuntimeInstance.getInstance().createResource(target);
	}

}
