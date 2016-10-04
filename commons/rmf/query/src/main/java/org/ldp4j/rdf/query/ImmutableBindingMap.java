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
import java.util.Objects;
import java.util.Set;

import org.ldp4j.rdf.Node;

final class ImmutableBindingMap {

	private final Map<String, Class<? extends Node>> bindings;

	private ImmutableBindingMap(Map<String, Class<? extends Node>> map) {
		this.bindings=Collections.unmodifiableMap(map);
	}

	public ImmutableBindingMap() {
		this(Collections.<String, Class<? extends Node>>emptyMap());
	}

	public Set<String> getBindingNames() {
		return bindings.keySet();
	}

	public <T extends Node> boolean isCompatibleValue(String binding, T value) {
		Objects.requireNonNull(binding,"Binding cannot be null");
		Objects.requireNonNull(value, "Value cannot be null");
		if(!this.bindings.containsKey(binding)) {
			throw new IllegalArgumentException("Unknown binding '"+binding+"'");
		}
		boolean result=false;
		Class<? extends Node> clazz = this.bindings.get(binding);
		if(clazz!=null) {
			result=clazz.isInstance(value);
		}
		return result;
	}

	public ImmutableBindingMap addBinding(String bindingName, Class<? extends Node> clazz) {
		Map<String, Class<? extends Node>> tmp=new HashMap<String, Class<? extends Node>>();
		tmp.putAll(this.bindings);
		tmp.put(bindingName, clazz);
		return new ImmutableBindingMap(tmp);
	}

}
