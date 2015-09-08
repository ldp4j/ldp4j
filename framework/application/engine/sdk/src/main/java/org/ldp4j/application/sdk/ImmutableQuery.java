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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-sdk:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-engine-sdk-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.sdk;

import java.util.Map;
import java.util.Set;

import org.ldp4j.application.ext.Parameter;
import org.ldp4j.application.ext.Query;

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.ImmutableMap;

final class ImmutableQuery implements Query {

	private final ImmutableMap<String, ImmutableQueryParameter> parameters;

	private ImmutableQuery(Map<String, ImmutableQueryParameter> params) {
		this.parameters=ImmutableMap.copyOf(params);
	}

	@Override
	public boolean hasParameter(String paramName) {
		checkNotNull(paramName,"Parameter name cannot be null");
		return this.parameters.containsKey(paramName);
	}

	@Override
	public Parameter getParameter(String paramName) {
		checkNotNull(paramName,"Parameter name cannot be null");
		Parameter p=this.parameters.get(paramName);
		if(p==null) {
			p=NullQueryParameter.create(paramName);
		}
		return p;
	}

	@Override
	public Set<String> parameterNames() {
		return this.parameters.keySet();
	}

	static ImmutableQuery create(Map<String,ImmutableQueryParameter> parameters) {
		return new ImmutableQuery(parameters);
	}
}