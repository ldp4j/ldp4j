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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-bean:0.2.2
 *   Bundle      : rmf-bean-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.bean.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

final class TypeSupport {

	private static final Map<Class<?>,Class<?>> WRAPPER_TO_PRIMITIVE;
	private static final Map<Class<?>,Class<?>> PRIMITIVE_TO_WRAPPER;

	static {
		WRAPPER_TO_PRIMITIVE=new HashMap<Class<?>, Class<?>>();
		WRAPPER_TO_PRIMITIVE.put(Character.class, Character.TYPE);
		WRAPPER_TO_PRIMITIVE.put(Byte.class, Byte.TYPE);
		WRAPPER_TO_PRIMITIVE.put(Short.class,Short.TYPE);
		WRAPPER_TO_PRIMITIVE.put(Integer.class, Integer.TYPE);
		WRAPPER_TO_PRIMITIVE.put(Long.class, Long.TYPE);
		WRAPPER_TO_PRIMITIVE.put(Double.class, Double.TYPE);
		WRAPPER_TO_PRIMITIVE.put(Float.class,Float.TYPE);
		WRAPPER_TO_PRIMITIVE.put(Boolean.class, Boolean.TYPE);
		PRIMITIVE_TO_WRAPPER=new HashMap<Class<?>, Class<?>>();
		for(Entry<Class<?>,Class<?>> entry:WRAPPER_TO_PRIMITIVE.entrySet()) {
			PRIMITIVE_TO_WRAPPER.put(entry.getValue(), entry.getKey());
		}
	}

	private TypeSupport() {
	}
	
	public static boolean isPrimitive(Class<?> clazz) {
		return PRIMITIVE_TO_WRAPPER.containsKey(clazz);
	}

	public static boolean isWrapper(Class<?> clazz) {
		return WRAPPER_TO_PRIMITIVE.containsKey(clazz);
	}

	public static boolean isLiteral(Class<?> clazz) {
		return 
			isPrimitive(clazz) || 
			isWrapper(clazz) ||
			CharSequence.class.isAssignableFrom(clazz);
	}

	public static boolean isAggregation(Class<?> clazz) {
		return List.class==clazz || Set.class==clazz;
	}

	public static boolean isRepeatable(Class<?> clazz) {
		return isAggregation(clazz) && !Set.class.isAssignableFrom(clazz);
	}

	public static Type getAggregatedType(Type type) {
		// TODO: Add proper type checking
		assert type instanceof ParameterizedType : "Invalid aggregation type: expected a Set or List but got '"+type+"'";
		ParameterizedType pt=(ParameterizedType)type;
		Type[] actualTypeArguments = pt.getActualTypeArguments();
		assert actualTypeArguments.length==1 : "Invalid number of type arguments: expected 1, but got "+actualTypeArguments.length;
		return actualTypeArguments[0];
	}

}