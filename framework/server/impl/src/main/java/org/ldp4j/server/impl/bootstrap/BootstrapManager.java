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
package org.ldp4j.server.impl.bootstrap;

import org.ldp4j.server.impl.LinkedDataPlatformRegistry;
import org.ldp4j.server.impl.ResourceManager;
import org.ldp4j.server.spi.ILinkedDataPlatformRegistry;
import org.ldp4j.server.spi.IResourceManager;
import org.ldp4j.server.spi.RuntimeInstance;

public final class BootstrapManager {

	private static final class CoreRuntimeInstance extends RuntimeInstance {
		
		private final LinkedDataPlatformRegistry registry;
		private final IResourceManager manager;
	
		private CoreRuntimeInstance() {
			this.registry = new LinkedDataPlatformRegistry();
			this.manager = new ResourceManager();
		}
	
		@Override
		public ILinkedDataPlatformRegistry getRegistry() {
			return registry;
		}
	
		@Override
		public IResourceManager getManager() {
			return manager;
		}
	
	}

	private static BootstrapManager instance;

	private final CoreRuntimeInstance runtime;


	private BootstrapManager() {
		this.runtime=new CoreRuntimeInstance();
	}
	
	private CoreRuntimeInstance getRuntime() {
		return this.runtime;
	}

	public static synchronized BootstrapManager getInstance() {
		if(instance==null) {
			throw new IllegalStateException("LDP4j implementation has not been initialized yet");
		}
		return instance;
	}

	static synchronized void initialize() {
		if(instance!=null) {
			throw new IllegalStateException("LDP4j implementation already initialized");
		}
		instance=new BootstrapManager();
		RuntimeInstance.setInstance(instance.getRuntime());
	}
	
	static synchronized void shutdown() {
		if(instance!=null) {
			instance=null;
		}
	}

}
