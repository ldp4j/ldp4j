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
 *   Artifact    : org.ldp4j.framework:ldp4j-conformance-fixture:0.2.2
 *   Bundle      : ldp4j-conformance-fixture-0.2.2.war
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.conformance.fixture;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.session.ResourceSnapshot;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class InMemoryResourceHandler implements ResourceHandler {

	private static final Multimap<String,ResourceHandler> loadedHandlers=LinkedListMultimap.<String, ResourceHandler>create();
	private final String handlerName;
	private final Map<Name<?>, DataSet> resources;

	protected InMemoryResourceHandler(String handlerName) {
		this.handlerName=handlerName;
		this.resources=new LinkedHashMap<Name<?>,DataSet>();
	}

	protected String getHandlerName() {
		return handlerName;
	}

	@Override
	public DataSet get(ResourceSnapshot resource) {
		DataSet dataSet = this.resources.get(resource.name());
		if(dataSet==null) {
			throw new IllegalStateException("Unknown resource '"+resource.name()+"'");
		}
		return dataSet;
	}

	public void add(Name<?> name, DataSet data) {
		this.resources.put(name,data);
	}

	public void update(Name<?> name, DataSet data) {
		remove(name);
		add(name,data);
	}

	public void clear() {
		this.resources.clear();
	}

	public int size() {
		return this.resources.size();
	}

	public void remove(Name<?> name) {
		this.resources.remove(name);
	}

	public boolean hasResource(Name<?> resourceName) {
		return this.resources.containsKey(resourceName);
	}

	@Override
	public String toString() {
		return getHandlerName();
	}

	public static <T extends ResourceHandler> List<T> getInstances(Class<? extends T> handlerClass) {
		List<T> result = new ArrayList<T>();
		for(ResourceHandler handler:loadedHandlers.get(handlerClass.getCanonicalName())) {
			result.add(handlerClass.cast(handler));
		}
		return result;
	}

}