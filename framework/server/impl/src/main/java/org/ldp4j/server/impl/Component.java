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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.impl;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Component implements IComponent {

	/** Internal logger */
	private static final Logger LOGGER=LoggerFactory.getLogger(Component.class);

	/**
	 * The UriInfo of the <b>JAX-RS</b> component. 
	 */
	private UriInfo uriInfo;

	private final ReadWriteLock lock=new ReentrantReadWriteLock();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public UriInfo getUriInfo() {
		lock.readLock().lock();
		try {
			if(uriInfo==null) {
				throw new IllegalStateException("Object 'uriInfo' has not been initialized yet");
			}
			return uriInfo;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUriInfo(UriInfo uriInfo) {
		if(uriInfo==null) {
			throw new IllegalArgumentException("Object 'uriInfo' cannot be null");
		}
		lock.writeLock().lock();
		try {
			this.uriInfo=uriInfo;
		} finally {
			lock.writeLock().unlock();
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Updating component '%s' (%X) UriInfo to '%s' (%X)",getClass().getCanonicalName(),hashCode(),uriInfo.getClass().getCanonicalName(),uriInfo.hashCode()));
		}
		UriInfoProvider.setUriInfo(uriInfo);
	}

}