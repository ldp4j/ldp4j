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

import java.util.Collection;
import java.util.List;

import org.ldp4j.application.ext.Parameter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.*;

final class ImmutableQueryParameter implements Parameter {

	private final String name;
	private final ImmutableList<String> rawValues;

	private ImmutableQueryParameter(String name, ImmutableList<String> rawValues) {
		this.name=name;
		this.rawValues=rawValues;
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public boolean isMultivalued() {
		return this.rawValues.size()>1;
	}

	@Override
	public int cardinality() {
		return this.rawValues.size();
	}

	@Override
	public String rawValue() {
		return this.rawValues.get(0);
	}

	@Override
	public List<String> rawValues() {
		return this.rawValues;
	}

	@Override
	public <T> T rawValueAs(Class<? extends T> clazz) {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	@Override
	public <T> List<T> rawValuesAs(Class<? extends T> clazz) {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	static ImmutableQueryParameter create(String name, Collection<?> rawValues) {
		checkNotNull(name,"Parameter name cannot be null");
		checkArgument(!name.trim().isEmpty(),"Parameter name cannot be empty");
		checkNotNull(rawValues,"Raw values cannot be null");
		checkArgument(!rawValues.isEmpty(),"No raw values specified");
		Builder<String> strRawValues=ImmutableList.<String>builder();
		for(Object rawValue:rawValues) {
			strRawValues.add(rawValue.toString());
		}
		return new ImmutableQueryParameter(name, strRawValues.build());
	}

	static ImmutableQueryParameter create(String name, Object... rawValues) {
		return create(name, Lists.newArrayList(rawValues));
	}

}