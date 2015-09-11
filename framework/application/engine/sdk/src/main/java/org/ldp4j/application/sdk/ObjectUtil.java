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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.ldp4j.application.sdk.internal.EnumObjectFactory;
import org.ldp4j.application.sdk.internal.PrimitiveObjectFactory;
import org.ldp4j.application.sdk.spi.ObjectFactory;
import org.ldp4j.application.sdk.spi.ObjectTransformationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ObjectUtil {

	/**
	 * An object factory which fails to parse/format values of a specific type.
	 */
	private static final class NullObjectFactory<T> implements ObjectFactory<T> {

		private final Class<? extends T> targetClass;

		private NullObjectFactory(Class<? extends T> valueClass) {
			this.targetClass = valueClass;
		}

		private ObjectTransformationException failure() {
			return new ObjectTransformationException("Unsupported value type '"+prettyPrint(targetClass())+"'",null,targetClass());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<? extends T> targetClass() {
			return this.targetClass;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T fromString(String rawValue) {
			throw failure();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString(T value) {
			throw failure();
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ObjectUtil.class);

	private static final Map<Class<?>,ObjectFactory<?>> FACTORY_CACHE=Maps.newIdentityHashMap();
	private static final Set<Class<?>> PRELOADED_FACTORIES=Sets.newIdentityHashSet();

	private ObjectUtil() {
	}

	@SuppressWarnings("unchecked")
	private static synchronized <T> ObjectFactory<T> findObjectFactory(final Class<? extends T> valueClass) {
		ObjectFactory<?> rawResult=FACTORY_CACHE.get(valueClass);
		if(rawResult==null) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("No cached factory found for value class '{}'",prettyPrint(valueClass));
			}
			rawResult=discoverObjectFactory(valueClass);
			if(rawResult==null) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("No factory discovered for value class '{}'",prettyPrint(valueClass));
				}
				rawResult=prepareDefaultObjectFactory(valueClass);
			}
		}
		return (ObjectFactory<T>)rawResult;
	}

	private static <T> ObjectFactory<?> discoverObjectFactory(final Class<? extends T> valueClass) {
		ObjectFactory<?> result=null;
		@SuppressWarnings("rawtypes")
		ServiceLoader<ObjectFactory> loader=ServiceLoader.load(ObjectFactory.class);
		@SuppressWarnings("rawtypes")
		Iterator<ObjectFactory> it = loader.iterator();
		do {
			try {
				result=processObjectFactory(valueClass,(ObjectFactory<?>)it.next());
			} catch (ServiceConfigurationError e) {
				LOGGER.error("ObjectFactory configuration failure. Full stacktrace follows",e);
			}
		} while(result==null && it.hasNext());
		return result;
	}

	private static <T> ObjectFactory<?> processObjectFactory(final Class<? extends T> valueClass, ObjectFactory<?> candidate) {
		ObjectFactory<?> result=null;
		if(PRELOADED_FACTORIES.contains(candidate.getClass())) {
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Discarded cached factory '{}' for value class '{}'",prettyPrint(candidate.getClass()),prettyPrint(valueClass));
			}
		} else {
			Class<?> targetClass = candidate.targetClass();
			if(FACTORY_CACHE.containsKey(targetClass)) {
				if(LOGGER.isWarnEnabled()) {
					LOGGER.warn("Discarded clashing factory '{}' for value class '{}'",prettyPrint(candidate.getClass()),prettyPrint(valueClass));
				}
			} else {
				cacheObjectFactory(candidate);
				if(targetClass==valueClass) {
					result=candidate;
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("Found factory '{}' for value class '{}'",prettyPrint(candidate.getClass()),prettyPrint(valueClass));
					}
				} else {
					if(LOGGER.isTraceEnabled()) {
						LOGGER.trace("Discarded factory '{}': target class '{}' does not match value class '{}'",prettyPrint(candidate.getClass()),prettyPrint(targetClass),prettyPrint(valueClass));
					}
				}
			}
		}
		return result;
	}

	private static void cacheObjectFactory(final ObjectFactory<?> candidate) {
		FACTORY_CACHE.put(candidate.targetClass(),candidate);
		PRELOADED_FACTORIES.add(candidate.getClass());
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("Cached factory '{}' for value class '{}'",prettyPrint(candidate.getClass()),prettyPrint(candidate.targetClass()));
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> ObjectFactory<?> prepareDefaultObjectFactory(final Class<? extends T> valueClass) {
		ObjectFactory<?> result=null;
		if(valueClass.isEnum()) {
			result=EnumObjectFactory.create(valueClass.asSubclass(Enum.class));
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("No factory found for enum value class '{}'",prettyPrint(valueClass));
			}
		} else if(valueClass.isPrimitive()) {
			result=PrimitiveObjectFactory.create(valueClass);
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("No factory found for primitive value class '{}'",prettyPrint(valueClass));
			}
		} else {
			result=findCompatibleSupertypeObjectFactory(valueClass);
			if(result==null) {
				result=new NullObjectFactory<T>(valueClass);
				if(LOGGER.isWarnEnabled()) {
					LOGGER.warn("No factory found for value class '{}'",prettyPrint(valueClass));
				}
			}
		}
		FACTORY_CACHE.put(valueClass,result);
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("Cached default factory '{}' for value class '{}'",prettyPrint(result.getClass()),prettyPrint(valueClass));
		}
		return result;
	}

	private static <T> ObjectFactory<?> findCompatibleSupertypeObjectFactory(final Class<? extends T> valueClass) {
		ObjectFactory<?> result=null;
		for(Entry<Class<?>,ObjectFactory<?>> entry:FACTORY_CACHE.entrySet()) {
			if(entry.getKey().isAssignableFrom(valueClass)) {
				result=entry.getValue();
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("No factory found for value class '{}', using supertype object factory",prettyPrint(valueClass));
				}
				break;
			}
		}
		return result;
	}

	private static String prettyPrint(Class<?> clazz) {
		return clazz.getCanonicalName();
	}

	static <T> boolean isSupported(Class<T> clazz) {
		checkNotNull(clazz,"Class cannot be null");
		return !NullObjectFactory.class.isInstance(ObjectUtil.findObjectFactory(clazz));
	}

	static <T> T fromString(Class<T> clazz, String str) {
		checkNotNull(clazz,"Class cannot be null");
		checkNotNull(str,"Raw value cannot be null");
		return ObjectUtil.findObjectFactory(clazz).fromString(str);
	}

	static String toString(Object value) {
		checkNotNull(value,"Value cannot be null");
		return ObjectUtil.findObjectFactory(value.getClass()).toString(value);
	}

}