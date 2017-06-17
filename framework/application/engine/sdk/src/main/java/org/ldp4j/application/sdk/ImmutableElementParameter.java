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

import java.util.Objects;

import org.ldp4j.application.engine.context.HttpRequest.Header.Element.Parameter;

import com.google.common.base.MoreObjects;

public final class ImmutableElementParameter implements Parameter {

	private static final long serialVersionUID = -7511803578477277362L;

	private final String name;
	private final String value;

	ImmutableElementParameter(String name, String value) {
		this.name = name;
		this.value = value;
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
	public String value() {
		return this.value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return
			Objects.
				hash(this.name,this.value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof Parameter) {
			Parameter that=(Parameter)obj;
			result=
				Objects.equals(this.name,that.name()) &&
				Objects.equals(this.value,that.value());
		}
		return result;
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
					add("value",this.value).
					toString();
	}

}
