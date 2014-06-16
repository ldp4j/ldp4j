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
 *   Artifact    : org.ldp4j.framework:ldp4j-client-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-client-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.client.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ReflectPermission;
import java.net.URL;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.client.ILDPContainer;
import org.ldp4j.client.ILDPResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * RuntimeInstance is an abstract factory class that provides various methods for
 * the creation of objects that implement the LDP4j Client SPI. These methods are 
 * designed for use by other LDP4j Client API classes and are not intended to be 
 * called directly by applications. RuntimeInstance allows the standard LDP4j 
 * Client API classes to use different LDP Client SPI implementations without 
 * any code changes. <br />
 * <br/>
 * 
 * An implementation of LDP Client SPI MUST provide a concrete subclass of RuntimeInstance.
 * Using the supplied RuntimeInstance this can be provided to LDP4j Client SPI 
 * in one of two ways:<br />
 *	<ol>
 * 		<li>An instance of RuntimeInstance can be instantiated and injected using its
 * static method <code>setInstance</code>. In this case the implementation is responsible
 * for creating the instance; this option is intended for use with
 * implementations based on IoC frameworks.</li>
 * 		<li>The class to be used can be configured, (see below). In this case LDP Client SPI is
 * responsible for instantiating an instance of the class and the configured
 * class MUST have a public constructor which takes no arguments.</li>
 * 	</ol>
 * 
 * Note that an implementation MAY supply an alternate implementation of the
 * RuntimeInstance API class (provided it passes the TCK signature test and
 * behaves according to the specification) that supports alternate means of
 * locating a concrete subclass. <br />
 * <br/>
 *
 * A LDP4j Client SPI implementation may rely on a particular implementation of
 * RuntimeInstance being used – applications SHOULD NOT override the supplied
 * RuntimeInstance instance with an application-supplied alternative and doing so
 * may cause unexpected problems.
 * <br/>
 *
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public abstract class RuntimeInstance {
	
	/** The internal logger */
	private static final Logger LOGGER=LoggerFactory.getLogger(RuntimeInstance.class);

	/** The Constant LDP4J_CLIENT_API_SPI_RUNTIMEINSTANCE_FINDER. */
	public static final String LDP4J_CLIENT_API_SPI_RUNTIMEINSTANCE_FINDER = "org.ldp4j.client.spi.runtimeinstance.finder";

	/** Name of the configuration file where the. {@link RuntimeInstance#LDP4J_CLIENT_API_PROPERTY} property that identifies the {@link RuntimeInstance} implementation to be returned from {@link RuntimeInstance#getInstance()} can be defined. */
	public static final String LDP4J_CLIENT_API_CFG = "ldp4j-client.properties";

	/**
	 * Name of the property identifying the {@link RuntimeInstance} implementation
	 * to be returned from {@link RuntimeInstance#getInstance()}.
	 */
	public static final String LDP4J_CLIENT_API_PROPERTY = "org.ldp4j.client.spi.RuntimeInstance";

	/** The suppress access checks permission. */
	private static final ReflectPermission SUPPRESS_ACCESS_CHECKS_PERMISSION = new ReflectPermission("suppressAccessChecks");

	/** The cached delegate. */
	private static final AtomicReference<RuntimeInstance> CACHED_DELEGATE=new AtomicReference<RuntimeInstance>();

	/**
	 * Allows custom implementations to extend the {@code RuntimeInstance} class.
	 */
	protected RuntimeInstance() {
	}

	/**
	 * Obtain a {@code RuntimeInstance} instance using the method described in.
	 *
	 * @return an instance of {@code RuntimeInstance}.
	 * {@link #getInstance}.
	 */
	private static RuntimeInstance findDelegate() {
		try {
			RuntimeInstance result = createRuntimeInstanceFromSPI();
			if (result == null) {
				File dir = new File(System.getProperty("java.home"));
				File properties = new File(dir,"lib"+File.separator+LDP4J_CLIENT_API_CFG);
				if (properties.canRead()) {
					InputStream is = null;
					try {
						is = new FileInputStream(properties);
						Properties jvmConfiguration = new Properties();
						jvmConfiguration.load(is);
						String delegateClassName = jvmConfiguration.getProperty(LDP4J_CLIENT_API_PROPERTY);
						if (delegateClassName != null) {
							result = createRuntimeInstanceForClassName(delegateClassName);
						}
					} finally {
						if (is != null) {
							try {
								is.close();
							} catch (IOException e) {
								if(LOGGER.isWarnEnabled()) {
									LOGGER.warn("Could not close configuration file stream", e);
								}
							}
						}
					}
				}
			}

			if (result == null) {
				String delegateClassName = System.getProperty(LDP4J_CLIENT_API_PROPERTY);
				if (delegateClassName != null) {
					result = createRuntimeInstanceForClassName(delegateClassName);
				}
			}

			if (result == null) {
				result = new DefaultRuntimeInstance();
			}
			
			return result;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Creates the runtime instance from using the Java Service Provider mechanism.
	 *
	 * @return the runtime instance
	 */
	private static RuntimeInstance createRuntimeInstanceFromSPI() {
		if(!"disable".equalsIgnoreCase(System.getProperty(LDP4J_CLIENT_API_SPI_RUNTIMEINSTANCE_FINDER))) {
			for (RuntimeInstance delegate : ServiceLoader.load(RuntimeInstance.class)) {
				return delegate;
			}
		}
		return null;
	}

	/**
	 * Creates a runtime instance a given class name.
	 *
	 * @param delegateClassName the delegate class name
	 * @return the runtime instance
	 */
	private static RuntimeInstance createRuntimeInstanceForClassName(String delegateClassName) {
		RuntimeInstance r = null;
		try {
			Class<?> delegateClass = Class.forName(delegateClassName);
			if(RuntimeInstance.class.isAssignableFrom(delegateClass)) {
				Object impl = delegateClass.newInstance();
				r = RuntimeInstance.class.cast(impl);
			}
		} catch (InstantiationException e) {
			logLoadFailure(delegateClassName, e);
		} catch (ClassNotFoundException e) {
			logLoadFailure(delegateClassName, e);
		} catch (IllegalAccessException e) {
			logLoadFailure(delegateClassName, e);
		}
		return r;
	}

	private static void logLoadFailure(String delegateClassName, Throwable e) {
		if(LOGGER.isErrorEnabled()) {
			LOGGER.error("Could load delegate class '"+delegateClassName+"'", e);
		}
	}

	/**
	 * Obtain a {@code RuntimeInstance} instance. If an instance had not already
	 * been created and set via {@link #setInstance(RuntimeInstance)}, the first
	 * invocation will create an instance which will then be cached for future
	 * use.
	 * 
	 * <p>
	 * The algorithm used to locate the RuntimeInstance subclass to use consists
	 * of the following steps:
	 * </p>
	 * <ul>
	 * <li>
	 * If a resource with the name of
	 *
	 * @return an instance of {@code RuntimeInstance}.
	 * {@code META-INF/services/{@value #LDP4J_CLIENT_API_PROPERTY}} exists, then
	 * its first line, if present, is used as the UTF-8 encoded name of the
	 * implementation class.</li>
	 * <li>
	 * If the $java.home/lib/{@value #LDP4J_CLIENT_API_CFG} file exists and it is readable by
	 * the {@code java.util.Properties.load(InputStream)} method and it contains
	 * an entry whose key is {@code {@value #LDP4J_CLIENT_API_PROPERTY}}, then the
	 * value of that entry is used as the name of the implementation class.</li>
	 * <li>
	 * If a system property with the name
	 * {@code {@value #LDP4J_CLIENT_API_PROPERTY}} is defined, then its value is
	 * used as the name of the implementation class.</li>
	 * <li>
	 * Finally, a default implementation class name is used.</li>
	 * </ul>
	 */
	public static RuntimeInstance getInstance() {
		RuntimeInstance result = RuntimeInstance.CACHED_DELEGATE.get();
		if (result != null) {
			return result;
		} 
		synchronized(RuntimeInstance.CACHED_DELEGATE) {
			result=RuntimeInstance.CACHED_DELEGATE.get();
			if(result==null) {
				RuntimeInstance.CACHED_DELEGATE.set(findDelegate());
				result=RuntimeInstance.CACHED_DELEGATE.get();
			}
			return result;
		}
	}

	/**
	 * Set the runtime delegate that will be used by Client Business Logic API
	 * classes. If this method is not called prior to {@link #getInstance} then
	 * an implementation will be sought as described in {@link #getInstance}.
	 *
	 * @param delegate the {@code RuntimeInstance} runtime delegate instance.
	 * @throws SecurityException if there is a security manager and the permission
	 * ReflectPermission("suppressAccessChecks") has not been
	 * granted.
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public static void setInstance(final RuntimeInstance delegate) throws SecurityException, IllegalArgumentException {
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			security.checkPermission(SUPPRESS_ACCESS_CHECKS_PERMISSION);
		}
		RuntimeInstance.CACHED_DELEGATE.set(delegate);
	}

	/**
	 * Create a <i>Linked Data Platform Container</i> client for the specified URL.
	 *
	 * @param target The identity of the container.
	 * @return A proxy client to the specified container.
	 * @see org.ldp4j.client.ILDPContainer
	 */
	public abstract ILDPContainer createContainer(URL target);
	
	/**
	 * Create a <i>Linked Data Platform Resource</i> client for the specified URL.
	 *
	 * @param target The identity of the resource.
	 * @return A proxy client to the specified resource.
	 * @see org.ldp4j.client.ILDPResource
	 */
	public abstract ILDPResource createResource(URL target);
	
	/**
	 * Creates a the {@link ITypeAdapter type adapter} for the specified source 
	 * and target classes.
	 *
	 * @param <S> the generic source class
	 * @param <T> the generic target class
	 * @param sourceClass the source class
	 * @param targetClass the target class
	 * @return an ITypeAdapter instance capable of transforming source class instances into target class instances.
	 * @throws UnsupportedSourceException If the source class is not supported.
	 * @throws UnsupportedTargetException If the target class is not supported.
	 * @see ITypeAdapter#transform(Object)
	 */
	public abstract <S,T> ITypeAdapter<S, T> createTypeAdapter(Class<? extends S> sourceClass, Class<? extends T> targetClass) throws UnsupportedSourceException, UnsupportedTargetException;
	
	/**
	 * The default fall-back {@code RuntimeInstance} implementation.
	 */
	static class DefaultRuntimeInstance extends RuntimeInstance {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ILDPContainer createContainer(URL target) {
			throw new IllegalStateException("No RuntimeInstance found");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ILDPResource createResource(URL target) {
			throw new IllegalStateException("No RuntimeInstance found");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <S, T> ITypeAdapter<S, T> createTypeAdapter(
				Class<? extends S> sourceClass, 
				Class<? extends T> targetClass) {
			throw new IllegalStateException("No RuntimeInstance found");
		}

	}

}