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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-application:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-application-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.spi.ResourceHandlerRegistry;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

final class InMemoryResourceHandlerRegistry implements ResourceHandlerRegistry {

	private ClassToInstanceMap<ResourceHandler> map=MutableClassToInstanceMap.<ResourceHandler>create();

	InMemoryResourceHandlerRegistry() {
	}
	
	@Override
	public <T extends ResourceHandler> void registerHandler(Class<? extends T> handlerClass, T instance) {
		checkNotNull(handlerClass,"ResourceSnapshot handler class cannot be null");
		checkNotNull(instance,"ResourceSnapshot handler cannot be null");
		map.put(handlerClass, instance);
	}

	@Override
	public <T extends ResourceHandler> T getHandler(Class<? extends T> handlerClass) {
		checkNotNull(handlerClass,"ResourceSnapshot handler class cannot be null");
		return map.getInstance(handlerClass);
	}

}