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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-sdk:0.2.2
 *   Bundle      : ldp4j-application-engine-sdk-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.sdk;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Set;

import org.ldp4j.application.ext.Parameter;
import org.ldp4j.application.ext.Query;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

/**
 * An immutable {@code Query} implementation.
 */
final class ImmutableQuery implements Query {

	private static final long serialVersionUID = 663489687029161424L;

	private final ImmutableMap<String, ImmutableQueryParameter> parameters;

	private ImmutableQuery(Map<String, ImmutableQueryParameter> params) {
		this.parameters=ImmutableMap.copyOf(params);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return this.parameters.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return this.parameters.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasParameter(String paramName) {
		return this.parameters.containsKey(paramName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Parameter getParameter(String paramName) {
		checkNotNull(paramName,"Parameter name cannot be null");
		return this.parameters.get(paramName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("parameters",this.parameters.values()).
					toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> parameterNames() {
		return this.parameters.keySet();
	}

	static ImmutableQuery create(Map<String,ImmutableQueryParameter> parameters) {
		return new ImmutableQuery(parameters);
	}
}