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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.entity;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.ldp4j.application.entity.spi.ObjectFactory;
import org.ldp4j.application.entity.spi.ObjectTransformationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public final class ObjectUtil {

	private static final class NullObjectFactory<T> implements ObjectFactory<T> {

		private final Class<? extends T> valueClass;

		private NullObjectFactory(Class<? extends T> valueClass) {
			this.valueClass = valueClass;
		}

		@Override
		public Class<? extends T> targetClass() {
			return this.valueClass;
		}

		@Override
		public T fromString(String rawValue) {
			throw new ObjectTransformationException("Unsupported value type '"+targetClass().getName()+"'",null,this.valueClass);
		}

		@Override
		public String toString(T value) {
			throw new ObjectTransformationException("Unsupported value type '"+targetClass().getName()+"'",null,this.valueClass);
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ObjectUtil.class);

	private static final Map<Class<?>,ObjectFactory<?>> FACTORY_CACHE=Maps.newIdentityHashMap();
	private static final Set<Class<?>> PRELOADED_FACTORIES=Sets.newIdentityHashSet();

	private ObjectUtil() {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static synchronized <T> ObjectFactory<T> findObjectFactory(final Class<? extends T> valueClass) {
		ObjectFactory<?> rawResult=FACTORY_CACHE.get(valueClass);
		if(rawResult==null) {
			for(ObjectFactory candidate:ServiceLoader.load(ObjectFactory.class)) {
				if(PRELOADED_FACTORIES.contains(candidate.getClass())) {
					if(LOGGER.isTraceEnabled()) {
						LOGGER.trace("Discarding cached factory '{}' for value class '{}'",candidate.getClass().getName(),candidate.targetClass().getName());
					}
					continue;
				}
				if(FACTORY_CACHE.containsKey(candidate.targetClass())) {
					if(LOGGER.isWarnEnabled()) {
						LOGGER.warn("Discarding clashing factory '{}' for value class '{}'",candidate.getClass().getName(),candidate.targetClass().getName());
					}
					continue;
				}
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Caching factory '{}' for value class '{}'",candidate.getClass().getName(),candidate.targetClass().getName());
				}
				FACTORY_CACHE.put(candidate.targetClass(),candidate);
				PRELOADED_FACTORIES.add(candidate.getClass());
				if(candidate.targetClass()==valueClass) {
					rawResult=candidate;
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("Found factory '{}' for value class '{}'",candidate.getClass().getName(),valueClass.getName());
					}
					break;
				}
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Discarding factory '{}': target class '{}' does not match value class '{}'",candidate.getClass().getName(),candidate.targetClass().getName(),valueClass.getName());
				}
			}
		}
		if(rawResult==null) {
			rawResult=new NullObjectFactory<T>(valueClass);
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn("No factory found for value class '{}'",valueClass.getName());
			}
		}
		return (ObjectFactory<T>)rawResult;
	}

	public static <T> T fromString(Class<T> clazz, String str) {
		ObjectFactory<T> valueFactory=ObjectUtil.findObjectFactory(clazz);
		return valueFactory.fromString(str);
	}

	public static String toString(Object value) {
		ObjectFactory<Object> valueFactory = ObjectUtil.findObjectFactory(value.getClass());
		return valueFactory.toString(value);
	}

}
