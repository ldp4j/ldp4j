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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

abstract class AbstractQueryTemplate implements QueryTemplate {


	private final Set<String> requiredNamedGraphs;
	private String defaultGraphName;

	protected AbstractQueryTemplate() {
		this.requiredNamedGraphs=new HashSet<String>();
	}

	protected final void setDefaultGraph(String defaultGraphName) {
		this.defaultGraphName = defaultGraphName;
	}

	protected final void setNamedGraphs(Set<String> requiredNamedGraphs) {
		Objects.requireNonNull(requiredNamedGraphs, "Required named graphs cannot be null");
		this.requiredNamedGraphs.clear();
		this.requiredNamedGraphs.addAll(requiredNamedGraphs);
	}

	@Override
	public final boolean requiresNamedDefaultGraph() {
		return defaultGraphName!=null;
	}


	@Override
	public final String getDefaultGraphName() {
		return defaultGraphName;
	}

	@Override
	public final Set<String> getRequiredNamedGraphs() {
		return Collections.unmodifiableSet(requiredNamedGraphs);
	}

	@Override
	public final String instantiate(Map<String,String> providedNamedGraphs) {
		Map<String,String> provided=new HashMap<String, String>(providedNamedGraphs);
		Set<String> required = getRequiredNamedGraphs();
		failIfSubset(required, provided.keySet(),"Some required named graphs are not covered");
		failIfSubset(provided.keySet(), required, "Unknown named graphs are provided");
		for(String graphName:provided.values()) {
			validate(graphName);
		}
		return fillInQuery(provided);
	}

	private void validate(String graphName) {
		try {
			URI uri = new URI(graphName);
			if(!uri.isOpaque() && !uri.isAbsolute()) {
				throw new IllegalArgumentException("Relative URIs are disallowed");
			}
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid graph names provided",e);
		}
	}

	private void failIfSubset(Set<String> source, Set<String> target, String message) {
		Set<String> tmp=new HashSet<String>(source);
		tmp.removeAll(target);
		if(!tmp.isEmpty()) {
			throw new IllegalArgumentException(message+": "+tmp);
		}
	}

	protected abstract String fillInQuery(Map<String,String> namedGraphs);

}
