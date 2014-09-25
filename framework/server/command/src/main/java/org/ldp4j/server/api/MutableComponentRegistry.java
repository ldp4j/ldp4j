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

import org.ldp4j.server.blueprint.Component;
import org.ldp4j.server.blueprint.ComponentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.MutableClassToInstanceMap;

// TODO: When loading with spring the initialization can be carried out, out of spring if fails if hidden.
public final class MutableComponentRegistry extends ComponentRegistry {

	private static final String OBJECT_COMPONENT_CLASS_CANNOT_BE_NULL = "Object 'componentClass' cannot be null";

	private static final Logger LOGGER=LoggerFactory.getLogger(MutableComponentRegistry.class);
	
	private final MutableClassToInstanceMap<Component> map;

	public MutableComponentRegistry() {
		map=MutableClassToInstanceMap.<Component>create();
	}
	
	<T extends Component> T registerComponent(Class<T> componentClass, T instance) {
		checkNotNull(componentClass,OBJECT_COMPONENT_CLASS_CANNOT_BE_NULL);
		checkNotNull(instance,"Object 'instance' cannot be null");
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("Registered {}: {}",componentClass.getCanonicalName(),instance.getClass().getCanonicalName());
		}
		map.putInstance(componentClass, instance);
		return instance;
	}

	@Override
	public <T extends Component> T getComponent(Class<? extends T> componentClass) {
		checkNotNull(componentClass,OBJECT_COMPONENT_CLASS_CANNOT_BE_NULL);
		T instance = map.getInstance(componentClass);
		if(LOGGER.isTraceEnabled()) {
			LOGGER.
				trace(
					"Requested {}: {}",
					componentClass.getCanonicalName(),
					instance==null?
						"instance not found":
						"found instance "+instance.getClass().getCanonicalName()
				);
		}
		return instance;
	}

	@Override
	public ResourceIndex getResourceIndex() {
		return getComponent(ResourceIndex.class);
	}

	@Override
	public EndpointRegistry getEndpointRegistry() {
		return getComponent(EndpointRegistry.class);
	}

}