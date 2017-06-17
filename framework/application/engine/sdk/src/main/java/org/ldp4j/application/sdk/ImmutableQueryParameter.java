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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;

import org.ldp4j.application.ext.Parameter;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

/**
 * An immutable {@code Parameter} implementation.
 */
final class ImmutableQueryParameter implements Parameter {

	private static final long serialVersionUID = 5325850847736543804L;

	private final String name;
	private final ImmutableList<String> rawValues;

	private ImmutableQueryParameter(String name, ImmutableList<String> rawValues) {
		this.name=name;
		this.rawValues=rawValues;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String name() {
		return this.name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMultivalued() {
		return this.rawValues.size()>1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int cardinality() {
		return this.rawValues.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String rawValue() {
		return this.rawValues.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> rawValues() {
		return this.rawValues;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T rawValueAs(Class<? extends T> clazz) {
		return ObjectUtil.fromString(clazz, rawValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> rawValuesAs(Class<? extends T> clazz) {
		Builder<T> builder = ImmutableList.<T>builder();
		for(String rawValue:this.rawValues) {
			builder.add(ObjectUtil.fromString(clazz,rawValue));
		}
		return builder.build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("name",this.name).
					add("rawValues",this.rawValues).
					toString();
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