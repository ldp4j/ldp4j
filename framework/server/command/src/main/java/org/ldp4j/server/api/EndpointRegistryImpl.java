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
package org.ldp4j.server.api;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.server.blueprint.ComponentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EndpointRegistryImpl implements EndpointRegistry {

	private static final String FIND_ERROR_NOT_FOUND      = "No endpoint available at '%s'";
	private static final String FIND_ERROR_GONE           = "Endpoint at '%s' has already been deregistered";
	private static final String DEREGISTRATION_SUCCESS    = "Deregistered endpoint '%s' (%s)";
	private static final String REGISTRATION_SUCCESS      = "Registered endpoint '%s' (%s)";
	private static final String REGISTRATION_ERROR_IN_USE = "Cannot register endpoint at '%s': location is currently in use";
	private static final String REGISTRATION_ERROR_GONE   = "Cannot register endpoint at '%s': location already gone";

	private static final Logger LOGGER=LoggerFactory.getLogger(EndpointRegistryImpl.class);

	private final ReadWriteLock lock=new ReentrantReadWriteLock();
	private final Map<URI,Endpoint> endpoints=new HashMap<URI,Endpoint>();
	private final Set<URI> gone=new HashSet<URI>();

	private final ComponentRegistry registry;
	
	public EndpointRegistryImpl(ComponentRegistry registry) {
		this.registry = registry;
	}
	
	private String log(String message, Object... args) {
		String result=null;
		if(LOGGER.isDebugEnabled()) {
			result = String.format(message,args);
			LOGGER.debug(result);
		}
		return result;
	}

	@Override
	public ComponentRegistry getRegistry() {
		return registry;
	}

	@Override
	public Endpoint findEndpoint(URI path) {
		lock.readLock().lock();
		try {
			Endpoint endpoint = endpoints.get(path);
			if(endpoint==null) {
				boolean removed = gone.contains(path);
				endpoint=
					removed?
						EndpointFactory.goneEndpoint(path):
						EndpointFactory.notFoundEndpoint(path);
				log(removed?FIND_ERROR_GONE:FIND_ERROR_NOT_FOUND,path);
			}
			return endpoint;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public void registerEndpoint(Endpoint endpoint) throws EndpointRegistrationException {
		checkNotNull(endpoint,"Object 'endpoint' cannot be null");
		URI path=endpoint.path();
		lock.writeLock().lock();
		try {
			if(gone.contains(path)) {
				throw new EndpointRegistrationException(log(REGISTRATION_ERROR_GONE,path));
			}
			if(endpoints.containsKey(path)) {
				throw new EndpointRegistrationException(log(REGISTRATION_ERROR_IN_USE,path));
			}
			endpoints.put(path, endpoint);
			log(REGISTRATION_SUCCESS, path, endpoint);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void deregisterEndpoint(URI path) {
		lock.writeLock().lock();
		try {
			Endpoint endpoint = endpoints.remove(path);
			if(endpoint != null) {
				gone.add(path);
				log(DEREGISTRATION_SUCCESS,path,endpoint);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	
}