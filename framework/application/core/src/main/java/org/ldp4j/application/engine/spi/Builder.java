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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.spi;

public abstract class Builder<T,B extends Builder<T,B>> {

	private PersistencyManager persistencyManager;

	private RuntimeDelegate runtimeInstance;

	public final B setRuntimeInstance(RuntimeDelegate runtimeInstance) {
		if(runtimeInstance==null) {
			throw new IllegalArgumentException("RuntimeInstance cannot be null");
		}
		this.runtimeInstance = runtimeInstance;
		return builder();
	}

	public final B withPersistencyManager(PersistencyManager persistencyManager) {
		this.persistencyManager=persistencyManager;
		return builder();
	}

	private final RuntimeDelegate runtimeInstance() {
		if(runtimeInstance!=null) {
			return runtimeInstance;
		} else {
			return RuntimeDelegate.getInstance();
		}
	}

	protected final <S extends Service> S service(Class<? extends S> serviceClass) {
		return runtimeInstance().getServiceRegistry().getService(serviceClass);
	}

	protected final PersistencyManager persistencyManager() {
		if(this.persistencyManager==null) {
			this.persistencyManager=runtimeInstance().getPersistencyManager();
		}
		return this.persistencyManager;
	}

	protected abstract B builder();

	public abstract T build();

}