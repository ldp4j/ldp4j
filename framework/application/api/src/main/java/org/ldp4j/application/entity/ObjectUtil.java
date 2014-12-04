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

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
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

		private final Class<? extends T> targetClass;

		private NullObjectFactory(Class<? extends T> valueClass) {
			this.targetClass = valueClass;
		}

		@Override
		public Class<? extends T> targetClass() {
			return this.targetClass;
		}

		@Override
		public T fromString(String rawValue) {
			throw new ObjectTransformationException("Unsupported value type '"+prettyPrint(this.targetClass)+"'",null,this.targetClass);
		}

		@Override
		public String toString(T value) {
			throw new ObjectTransformationException("Unsupported value type '"+prettyPrint(this.targetClass)+"'",null,this.targetClass);
		}

	}

	private static final class EnumObjectFactory<S extends Enum<S>> implements ObjectFactory<S> {

		private final Class<S> targetClass;

		private EnumObjectFactory(Class<S> targetClass) {
			this.targetClass = targetClass;
		}

		@Override
		public Class<? extends S> targetClass() {
			return this.targetClass;
		}

		@Override
		public S fromString(final String rawValue) {
			return Enum.valueOf(targetClass, rawValue);
		}

		@Override
		public String toString(S value) {
			return value.toString();
		}

		public static <S extends Enum<S>> ObjectFactory<?> create(Class<S> enumClass) {
			return new EnumObjectFactory<S>(enumClass);
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
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("No cached factory found for value class '{}'",prettyPrint(valueClass));
			}
			ServiceLoader<ObjectFactory> loader = ServiceLoader.load(ObjectFactory.class);
			Iterator<ObjectFactory> it = loader.iterator();
			do {
				try {
					ObjectFactory candidate=it.next();
					Class<? extends ObjectFactory> candidateClass = candidate.getClass();
					if(PRELOADED_FACTORIES.contains(candidateClass)) {
						if(LOGGER.isTraceEnabled()) {
							LOGGER.trace("Discarded cached factory '{}' for value class '{}'",prettyPrint(candidateClass),prettyPrint(valueClass));
						}
					} else {
						Class<?> targetClass = candidate.targetClass();
						if(FACTORY_CACHE.containsKey(targetClass)) {
							if(LOGGER.isWarnEnabled()) {
								LOGGER.warn("Discarded clashing factory '{}' for value class '{}'",prettyPrint(candidateClass),prettyPrint(valueClass));
							}
						} else {
							FACTORY_CACHE.put(targetClass,candidate);
							PRELOADED_FACTORIES.add(candidateClass);
							if(LOGGER.isTraceEnabled()) {
								LOGGER.trace("Cached factory '{}' for value class '{}'",prettyPrint(candidateClass),prettyPrint(targetClass));
							}
							if(targetClass==valueClass) {
								rawResult=candidate;
								if(LOGGER.isDebugEnabled()) {
									LOGGER.debug("Found factory '{}' for value class '{}'",prettyPrint(candidateClass),prettyPrint(valueClass));
								}
							} else {
								if(LOGGER.isTraceEnabled()) {
									LOGGER.trace("Discarded factory '{}': target class '{}' does not match value class '{}'",prettyPrint(candidateClass),prettyPrint(targetClass),prettyPrint(valueClass));
								}
							}
						}
					}
				} catch (ServiceConfigurationError e) {
					LOGGER.error("ObjectFactory configuration failure. Full stacktrace follows",e);
				}
			} while(rawResult==null && it.hasNext());
		}
		if(rawResult==null) {
			if(valueClass.isEnum()) {
				rawResult=EnumObjectFactory.create(valueClass.asSubclass(Enum.class));
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("No factory found for enum value class '{}', using default enum object factory",prettyPrint(valueClass));
				}
			} else {
				rawResult=new NullObjectFactory<T>(valueClass);
				if(LOGGER.isWarnEnabled()) {
					LOGGER.warn("No factory found for value class '{}'",prettyPrint(valueClass));
				}
			}
		}
		return (ObjectFactory<T>)rawResult;
	}

	private static String prettyPrint(Class<?> clazz) {
		return clazz.getCanonicalName();
	}

	public static <T> boolean isSupported(Class<T> clazz) {
		ObjectFactory<T> valueFactory=ObjectUtil.findObjectFactory(clazz);
		return !NullObjectFactory.class.isInstance(valueFactory);
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
