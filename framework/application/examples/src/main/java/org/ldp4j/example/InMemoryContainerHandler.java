/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014-2016 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-examples:0.2.2
 *   Bundle      : ldp4j-application-examples-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.example;

import java.util.Map;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ApplicationRuntimeException;
import org.ldp4j.application.ext.ContainerHandler;

import com.google.common.collect.Maps;

/**
 * An example in-memory container handler.
 */
public abstract class InMemoryContainerHandler extends InMemoryResourceHandler implements ContainerHandler {

	private final Map<Name<String>,NameProvider> nameProviders;

	protected InMemoryContainerHandler(String handlerName) {
		super(handlerName);
		this.nameProviders=Maps.newLinkedHashMap();
	}

	/**
	 * Add a name provider for a container resource.
	 *
	 * @param containerName
	 *            the name of the container.
	 * @param provider
	 *            the name provider.
	 */
	public final void addNameProvider(Name<String> containerName, NameProvider provider) {
		this.nameProviders.put(containerName, provider);
	}

	/**
	 * Return the name provider registered for a given container resource.
	 *
	 * @param containerName
	 *            the name of the container.
	 * @return the provider for the specified container resource.
	 */
	public final NameProvider nameProvider(Name<?> containerName) {
		NameProvider result = this.nameProviders.get(containerName);
		if(result==null) {
			throw new ApplicationRuntimeException("Unknown container '"+containerName+"'");
		}
		return result;
	}

}