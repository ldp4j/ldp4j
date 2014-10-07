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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;


import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.endpoint.EndpointLifecycleListener;
import org.ldp4j.application.lifecycle.ApplicationLifecycleListener;
import org.ldp4j.application.lifecycle.ApplicationState;
import org.ldp4j.application.util.ConcurrentHashSet;


public class EndpointControllerFactory {
	
	private final class LocalEndpointLifecycleListener implements EndpointLifecycleListener {
		@Override
		public void endpointCreated(Endpoint endpoint) {
		}
		@Override
		public void endpointDeleted(Endpoint endpoint) {
			EndpointControllerFactory.this.goneEndpoints.add(endpoint.path());
		}
	}

	private final class LocalApplicationLifecycleListener implements ApplicationLifecycleListener {

		@Override
		public void applicationStateChanged(ApplicationState newState) {
			switch(newState) {
			case AVAILABLE:
				applicationContext().
					registerEndpointLifecyleListener(endpointLifecycleListener());
				break;
			case SHUTDOWN:
				applicationContext().
					deregisterEndpointLifecycleListener(endpointLifecycleListener());
				applicationContext().
					deregisterApplicationLifecycleListener(this);
				break;
			case UNAVAILABLE:
				break;
			case UNDEFINED:
				break;
			}
		}

		private EndpointLifecycleListener endpointLifecycleListener() {
			return EndpointControllerFactory.this.endpointLifecyleListener;
		}

		private ApplicationContext applicationContext() {
			return EndpointControllerFactory.this.applicationContext;
		}

	}

	private final Set<String> goneEndpoints;

	private final LocalApplicationLifecycleListener applicationLifecyleListener;

	private final LocalEndpointLifecycleListener endpointLifecyleListener;

	private final ApplicationContext applicationContext;
	
	private EndpointControllerFactory(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.applicationLifecyleListener = new LocalApplicationLifecycleListener();
		this.endpointLifecyleListener = new LocalEndpointLifecycleListener();
		this.goneEndpoints=new ConcurrentHashSet<String>();
		this.applicationContext.registerApplicationLifecycleListener(this.applicationLifecyleListener);
	}
	
	private String normalizePath(String path) {
		String tPath=path;
		if(tPath==null) {
			tPath="";
		} else {
			tPath = tPath.trim();
		}
		return tPath;
	}

	private Endpoint findEndpoint(String path) {
		return this.applicationContext.resolveEndpoint(normalizePath(path));
	}

	private boolean isGone(String path) {
		return this.goneEndpoints.contains(path);
	}

	public EndpointController createController(String path) {
		Endpoint endpoint=findEndpoint(path);
		EndpointController result=null;
		if(endpoint!=null) {
			result=new ExistingEndpointController(endpoint);
		} else if(isGone(path)) {
			result=new GoneEndpointController(endpoint);
		} else {
			result=new NotFoundEndpointController(endpoint);
		}
		return result;
	}
	
	public static EndpointControllerFactory newInstance(ApplicationContext context) {
		checkNotNull(context,"Application context cannot be null");
		return new EndpointControllerFactory(context);
	}

}
