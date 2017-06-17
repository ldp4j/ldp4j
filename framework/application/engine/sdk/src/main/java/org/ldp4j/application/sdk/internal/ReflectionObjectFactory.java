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
package org.ldp4j.application.sdk.internal;

import java.lang.reflect.Method;

import org.ldp4j.application.sdk.spi.ObjectFactory;
import org.ldp4j.application.sdk.spi.ObjectParseException;

public final class ReflectionObjectFactory<T> implements ObjectFactory<T> {

	private final Class<? extends T> valueClass;
	private final Method method;

	public ReflectionObjectFactory(Class<? extends T> valueClass, Method method) {
		this.valueClass = valueClass;
		this.method = method;
		this.method.setAccessible(true);
	}

	@Override
	public Class<? extends T> targetClass() {
		return this.valueClass;
	}

	@Override
	public T fromString(String rawValue) {
		try {
			return targetClass().cast(this.method.invoke(null, rawValue));
		} catch (Exception e) {
			throw new ObjectParseException(e,targetClass(),rawValue);
		}
	}

	@Override
	public String toString(T value) {
		return value.toString();
	}

}