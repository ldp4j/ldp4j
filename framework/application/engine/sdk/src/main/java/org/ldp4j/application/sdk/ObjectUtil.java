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

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

import org.ldp4j.application.ext.ObjectTransformationException;
import org.ldp4j.application.sdk.internal.EnumObjectFactory;
import org.ldp4j.application.sdk.internal.PrimitiveObjectFactory;
import org.ldp4j.application.sdk.internal.ReflectionObjectFactory;
import org.ldp4j.application.sdk.spi.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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

	private static String prettyPrint(Class<?> clazz) {
		return clazz.getCanonicalName();
	}

	private static Object[] prettyPrint(Class<?>... classes) {
		String[] result = new String[classes.length];
		for(int i=0;i<classes.length;i++) {
			result[i]=prettyPrint(classes[i]);
		}
		return result;
	}

	private static void debug(String message, Class<?>... classes) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(message,prettyPrint(classes));
		}
	}

	private static void warn(String message, Class<?>... classes) {
		if(LOGGER.isWarnEnabled()) {
			LOGGER.warn(message,prettyPrint(classes));
		}
	}

	private static void trace(String message, Class<?>... classes) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(message,prettyPrint(classes));
		}
	}

	@SuppressWarnings("unchecked")
	private static synchronized <T> ObjectFactory<T> findObjectFactory(final Class<? extends T> valueClass) {
		ObjectFactory<?> rawResult=FACTORY_CACHE.get(valueClass);
		if(rawResult==null) {
			debug("No cached factory found for value class '{}'",valueClass);
			rawResult=discoverObjectFactory(valueClass);
			if(rawResult==null) {
				debug("No factory discovered for value class '{}'",valueClass);
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
			trace("Discarded cached factory '{}' for value class '{}'",candidate.getClass(),valueClass);
		} else {
			Class<?> targetClass = candidate.targetClass();
			if(FACTORY_CACHE.containsKey(targetClass)) {
				warn("Discarded clashing factory '{}' for value class '{}'",candidate.getClass(),valueClass);
			} else {
				cacheObjectFactory(candidate);
				if(targetClass==valueClass) {
					result=candidate;
					debug("Found factory '{}' for value class '{}'",candidate.getClass(),valueClass);
				} else {
					trace("Discarded factory '{}': target class '{}' does not match value class '{}'",candidate.getClass(),targetClass,valueClass);
				}
			}
		}
		return result;
	}

	private static void cacheObjectFactory(final ObjectFactory<?> candidate) {
		FACTORY_CACHE.put(candidate.targetClass(),candidate);
		PRELOADED_FACTORIES.add(candidate.getClass());
		trace("Cached factory '{}' for value class '{}'",candidate.getClass(),candidate.targetClass());
	}

	@SuppressWarnings("unchecked")
	private static <T> ObjectFactory<?> prepareDefaultObjectFactory(final Class<? extends T> valueClass) {
		ObjectFactory<?> result=null;
		if(valueClass.isEnum()) {
			result=EnumObjectFactory.create(valueClass.asSubclass(Enum.class));
			debug("No factory found for enum value class '{}'",valueClass);
		} else if(valueClass.isPrimitive()) {
			result=PrimitiveObjectFactory.create(valueClass);
			debug("No factory found for primitive value class '{}'",valueClass);
		} else {
			result=createConventionObjectFactory(valueClass);
		}
		FACTORY_CACHE.put(valueClass,result);
		trace("Cached default factory '{}' for value class '{}'",result.getClass(),valueClass);
		return result;
	}

	private static <T> ObjectFactory<?> createConventionObjectFactory(final Class<? extends T> valueClass) {
		ObjectFactory<?> result=null;
		result=createReflectionObjectFactory(valueClass,"valueOf");
		if(result!=null) {
			return result;
		}
		result=createReflectionObjectFactory(valueClass,"fromString");
		if(result!=null) {
			return result;
		}
		result=findCompatibleSupertypeObjectFactory(valueClass);
		if(result!=null) {
			return result;
		}
		warn("No factory found for value class '{}'",valueClass);
		return new NullObjectFactory<T>(valueClass);
	}

	private static <T> ObjectFactory<T> createReflectionObjectFactory(final Class<? extends T> valueClass, String methodName) {
		ObjectFactory<T> result=null;
		try {
			final Method method = valueClass.getDeclaredMethod(methodName, String.class);
			if(isPublicStatic(method) && hasCompatibleReturnType(method, valueClass)) {
				result=new ReflectionObjectFactory<T>(valueClass, method);
				debug("Created "+methodName+"(String) compatible object factory for class '{}'",valueClass);
			}
		} catch (Exception e) {
			LOGGER.trace("Could not create {}(String) compatible object factory for class '{}'",methodName,prettyPrint(valueClass),e);
		}
		return result;
	}

	private static <T> boolean hasCompatibleReturnType(final Method method, final Class<? extends T> valueClass) {
		return valueClass.isAssignableFrom(method.getReturnType());
	}

	private static boolean isPublicStatic(final Method method) {
		final int modifiers = method.getModifiers();
		return Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers);
	}

	private static <T> ObjectFactory<?> findCompatibleSupertypeObjectFactory(final Class<? extends T> valueClass) {
		ObjectFactory<?> result=null;
		for(Entry<Class<?>,ObjectFactory<?>> entry:FACTORY_CACHE.entrySet()) {
			if(entry.getKey().isAssignableFrom(valueClass)) {
				result=entry.getValue();
				debug("No factory found for value class '{}', using supertype object factory",valueClass);
				break;
			}
		}
		return result;
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