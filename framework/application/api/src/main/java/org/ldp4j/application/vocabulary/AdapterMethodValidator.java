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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.vocabulary;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


final class AdapterMethodValidator {

	private interface TypeValidator {

		boolean isValid(Class<?> parameterType);

		Class<?> getTargetClass();

	}

	private static final class SubclassTypeValidator implements TypeValidator {

		private final Class<?> subClass;

		private SubclassTypeValidator(Class<?> termClass) {
			this.subClass = termClass;
		}

		@Override
		public boolean isValid(Class<?> parameterType) {
			return parameterType.isAssignableFrom(this.subClass);
		}

		@Override
		public Class<?> getTargetClass() {
			return this.subClass;
		}

	}

	private static final class InstanceTypeValidator implements TypeValidator {

		private final Object instance;

		private InstanceTypeValidator(Object term) {
			this.instance = term;
		}

		@Override
		public boolean isValid(Class<?> parameterType) {
			return parameterType.isInstance(instance);
		}

		@Override
		public Class<?> getTargetClass() {
			return this.instance.getClass();
		}

	}

	private final Class<?> returnType;
	private final AdapterMethodValidator.TypeValidator parameterValidator;

	private AdapterMethodValidator(Class<?> returnType, TypeValidator parameterValidator) {
		this.parameterValidator = parameterValidator;
		this.returnType = returnType;
	}

	boolean isValid(Method method) {
		return
			TypeAdapter.ADAPTER_NAME_CONVENTION.equals(method.getName()) &&
			hasValidSignature(method) &&
			isVisible(method);
	}

	Class<?> getTargetClass() {
		return this.parameterValidator.getTargetClass();
	}

	private boolean hasValidSignature(Method method) {
		return
			this.returnType.isAssignableFrom(method.getReturnType()) &&
			method.getParameterTypes().length==1 &&
			this.parameterValidator.isValid(method.getParameterTypes()[0]);
	}

	private boolean isVisible(Method method) {
		int modifiers = method.getModifiers();
		return
			Modifier.isStatic(modifiers) &&
			Modifier.isPublic(modifiers);
	}

	static AdapterMethodValidator newInstance(Class<?> returnType, Class<?> parameterType) {
		return new AdapterMethodValidator(returnType, new SubclassTypeValidator(parameterType));
	}

	static AdapterMethodValidator newInstance(Class<?> returnType, Object value) {
		return new AdapterMethodValidator(returnType, new InstanceTypeValidator(value));
	}

}