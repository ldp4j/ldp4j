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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-query:0.2.2
 *   Bundle      : rmf-query-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.query;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ldp4j.rdf.io.Module;

public final class ImmutableDataSet implements DataSet {

	
	private final Module<?> defaultGraph;
	private final Map<String,Module<?>> namedGraphs;

	private ImmutableDataSet(Module<?> defaultGraph, Map<String,Module<?>> namedGraphs) {
		this.defaultGraph= defaultGraph;
		this.namedGraphs=new HashMap<String,Module<?>>(namedGraphs);
	}
	
	private ImmutableDataSet(ImmutableDataSet dataSet, Module<?> module) {
		this(module,dataSet.namedGraphs);
	}

	private ImmutableDataSet(ImmutableDataSet dataSet, String graphName, Module<?> graphModule) {
		this(dataSet.defaultGraph,dataSet.namedGraphs);
		this.namedGraphs.put(graphName, graphModule);
	}

	public ImmutableDataSet withDefaultGraph(Module<?> module) {
		return new ImmutableDataSet(this,module);
	}
	
	public ImmutableDataSet withNamedGraph(String name, Module<?> module) {
		return new ImmutableDataSet(this,name,module);
	}

	@Override
	public Module<?> getDefaultGraph() {
		return defaultGraph;
	}

	@Override
	public Module<?> getNamedGraph(String graphName) {
		return namedGraphs.get(graphName);
	}
	
	public static ImmutableDataSet newInstance() {
		return new ImmutableDataSet(null,Collections.<String,Module<?>>emptyMap());
	}
	
}