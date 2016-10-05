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

import java.util.LinkedHashMap;
import java.util.Map;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.ext.UnknownResourceException;
import org.ldp4j.application.session.ResourceSnapshot;

/**
 * An example in-memory resource handler.
 */
public abstract class InMemoryResourceHandler implements ResourceHandler {

	private final String handlerName;
	private final Map<Name<?>, DataSet> resources;

	protected InMemoryResourceHandler(String handlerName) {
		this.handlerName=handlerName;
		this.resources=new LinkedHashMap<Name<?>,DataSet>();
	}

	protected final String getHandlerName() {
		return handlerName;
	}

	/**
	 * {@inheritDoc}
	 * @throws UnknownResourceException if the resource is not known.
	 */
	@Override
	public DataSet get(ResourceSnapshot resource) throws UnknownResourceException {
		DataSet dataSet = this.resources.get(resource.name());
		if(dataSet==null) {
			throw new UnknownResourceException("Unknown resource '"+resource.name()+"'");
		}
		return dataSet;
	}

	/**
	 * Create a new resource to the collection of resource managed by the
	 * handler. If the name is already in use, the representation will be
	 * updated.
	 *
	 * @param name
	 *            the name of the new resource.
	 * @param data
	 *            the default representation for the resource.
	 */
	public final void add(Name<?> name, DataSet data) {
		this.resources.put(name,data);
	}

	/**
	 * Update the representation of a resource managed by the handler.
	 *
	 * @param name
	 *            the name of the resource.
	 * @param data
	 *            the new representation.
	 */
	public final void update(Name<?> name, DataSet data) {
		remove(name);
		add(name,data);
	}

	/**
	 * Remove all the resources managed by the handler.
	 */
	public final void clear() {
		this.resources.clear();
	}

	/**
	 * Return the number or resources managed by the handler.
	 *
	 * @return the number of resources managed by the handler.
	 */
	public final int size() {
		return this.resources.size();
	}

	/**
	 * Remove a resource managed by the handler.
	 *
	 * @param name
	 *            the name of the resource to be removed.
	 */
	public final void remove(Name<?> name) {
		this.resources.remove(name);
	}

	/**
	 * Return {@code true} if the handler manages a given resource,
	 * {@code false} otherwise.
	 *
	 * @param resourceName
	 *            the name of the resource
	 * @return whether or not the handler manages the specified resource.
	 */
	public final boolean hasResource(Name<?> resourceName) {
		return this.resources.containsKey(resourceName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getHandlerName();
	}

}