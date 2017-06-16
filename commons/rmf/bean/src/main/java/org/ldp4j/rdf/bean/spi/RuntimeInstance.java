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
package org.ldp4j.rdf.bean.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ReflectPermission;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.rdf.bean.JARBContext;
import org.ldp4j.rdf.bean.NamingPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * RuntimeInstance is an abstract factory class that provides various methods for
 * the creation of objects that implement the ALM iStack LDP Kernel SPI (LKS for short).
 * These methods are designed for use by other LKA classes and are not intended
 * to be called directly by applications.
 * RuntimeInstance allows the standard LKA classes to use different LKS
 * implementations without any code changes. <br>
 * <br>
 *
 * An implementation of LKS MUST provide a concrete subclass of RuntimeInstance.
 * Using the supplied RuntimeInstance this can be provided to LKS in one of two
 * ways:<br>
 *	<ol>
 * 		<li>An instance of RuntimeInstance can be instantiated and injected using its
 * static method <code>setInstance</code>. In this case the implementation is responsible
 * for creating the instance; this option is intended for use with
 * implementations based on IoC frameworks.</li>
 * 		<li>The class to be used can be configured, (see below). In this case CBLS is
 * responsible for instantiating an instance of the class and the configured
 * class MUST have a public constructor which takes no arguments.</li>
 * 	</ol>
 *
 * Note that an implementation MAY supply an alternate implementation of the
 * RuntimeInstance API class (provided it passes the TCK signature test and
 * behaves according to the specification) that supports alternate means of
 * locating a concrete subclass. <br>
 * <br>
 *
 * A CBLS implementation may rely on a particular implementation of
 * RuntimeInstance being used – applications SHOULD NOT override the supplied
 * RuntimeInstance instance with an application-supplied alternative and doing so
 * may cause unexpected problems.
 * <br>
 *
 */
public abstract class RuntimeInstance {

	private static final String INSTANTIATE_ACTION = "instantiate";

	private static final Logger LOGGER=LoggerFactory.getLogger(RuntimeInstance.class);

	public static final String RDF_BEAN_SPI_RUNTIMEINSTANCE_FINDER = "org.ldp4j.rdf.bean.spi.runtimeinstance.finder";

	/**
	 * Name of the configuration file where the
	 * {@link RuntimeInstance#RDF_BEAN_SPI_PROPERTY} property that
	 * identifies the {@link RuntimeInstance} implementation to be returned from
	 * {@link RuntimeInstance#getInstance()} can be defined.
	 */
	public static final String RDF_BEAN_CFG = "rdf-bean.properties";

	/**
	 * Name of the property identifying the {@link RuntimeInstance} implementation
	 * to be returned from {@link RuntimeInstance#getInstance()}.
	 */
	public static final String RDF_BEAN_SPI_PROPERTY = "org.ldp4j.rdf.bean.spi.RuntimeInstance";

	private static final AtomicReference<RuntimeInstance> CACHED_DELEGATE=new AtomicReference<RuntimeInstance>();

	private static ReflectPermission suppressAccessChecksPermission = new ReflectPermission("suppressAccessChecks");


	/**
	 * Allows custom implementations to extend the {@code RuntimeInstance} class.
	 */
	protected RuntimeInstance() {
	}

	/**
	 * Obtain a {@code RuntimeInstance} instance using the method described in
	 * {@link #getInstance}.
	 *
	 * @return an instance of {@code RuntimeInstance}.
	 */
	private static RuntimeInstance findDelegate() {
		try {
			RuntimeInstance result=createRuntimeInstanceFromSPI();
			if(result==null) {
				result=createRuntimeInstanceFromConfigurationFile();
			}

			if(result==null) {
				final String delegateClassName = System.getProperty(RDF_BEAN_SPI_PROPERTY);
				if(delegateClassName!=null) {
					result=createRuntimeInstanceForClassName(delegateClassName);
				}
			}

			if(result==null) {
				result=new DefaultRuntimeInstance();
			}

			return result;
		} catch (final Exception ex) {
			throw new IllegalStateException("Could not find runtime delegate",ex);
		}
	}

	private static RuntimeInstance createRuntimeInstanceFromConfigurationFile() {
		RuntimeInstance result=null;
		final File configFile = getConfigurationFile();
		if(configFile.canRead()) {
			InputStream is=null;
			try {
				is=new FileInputStream(configFile);
				final Properties configProperties=new Properties();
				configProperties.load(is);
				final String delegateClassName=configProperties.getProperty(RDF_BEAN_SPI_PROPERTY);
				if(delegateClassName!=null) {
					result=createRuntimeInstanceForClassName(delegateClassName);
				}
				if(delegateClassName==null && LOGGER.isWarnEnabled()) {
					LOGGER.warn("Configuration file '"+configFile.getAbsolutePath()+"' does not define a delegate class name");
				}
			} catch(final FileNotFoundException e) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("Could not find runtime instance configuration file '"+configFile.getAbsolutePath()+"'",e);
				}
			} catch(final IOException e) {
				if(LOGGER.isWarnEnabled()) {
					LOGGER.warn("Could not load runtime instance configuration file '"+configFile.getAbsolutePath()+"'",e);
				}
			} finally {
				closeQuietly(is, "Could find close properties");
			}
		}
		return result;
	}

	/**
	 * Get the configuration file for the Runtime Instance: a file named
	 * {@link RuntimeInstance#RDF_BEAN_CFG} in the <code>lib</code> directory of
	 * current JAVA_HOME.
	 *
	 * @return The configuration file for the runtime instance.
	 */
	private static File getConfigurationFile() {
		return new File(new File(System.getProperty("java.home")),"lib"+File.separator+RDF_BEAN_CFG);
	}

	/**
	 * Close an input stream logging possible failures.
	 * @param is The input stream that is to be closed.
	 * @param message The message to log in case of failure.
	 */
	private static void closeQuietly(final InputStream is, final String message) {
		if(is!=null) {
		try {
			is.close();
		} catch (final Exception e) {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(message,e);
			}
		}
		}
	}

	private static RuntimeInstance createRuntimeInstanceFromSPI() {
		if(!"disable".equalsIgnoreCase(System.getProperty(RDF_BEAN_SPI_RUNTIMEINSTANCE_FINDER))) {
			for (final RuntimeInstance delegate : ServiceLoader.load(RuntimeInstance.class)) {
				return delegate;
			}
		}
		return null;
	}

	private static RuntimeInstance createRuntimeInstanceForClassName(final String delegateClassName) {
		RuntimeInstance result = null;
		try {
			final Class<?> delegateClass = Class.forName(delegateClassName);
			if(RuntimeInstance.class.isAssignableFrom(delegateClass)) {
				final Object impl = delegateClass.newInstance();
				result = RuntimeInstance.class.cast(impl);
			}
		} catch (final ClassNotFoundException e) {
			handleFailure(delegateClassName, "find", e);
		} catch (final InstantiationException e) {
			handleFailure(delegateClassName, INSTANTIATE_ACTION, e);
		} catch (final IllegalAccessException e) {
			handleFailure(delegateClassName, INSTANTIATE_ACTION, e);
		}
		return result;
	}

	/**
	 * @param delegateClassName
	 * @param action
	 * @param failure
	 */
	private static void handleFailure(final String delegateClassName, final String action,
			final Exception failure) {
		if(LOGGER.isWarnEnabled()) {
			LOGGER.warn("Could not "+action+" delegate class "+delegateClassName,failure);
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
	 * {@code META-INF/services/org.centeropenmiddleware.almistack.poc.clients.spi.RuntimeInstance} exists, then
	 * its first line, if present, is used as the UTF-8 encoded name of the
	 * implementation class.</li>
	 * <li>
	 * If the $java.home/lib/poc-business-logic.properties file exists and it is readable by
	 * the {@code java.util.Properties.load(InputStream)} method and it contains
	 * an entry whose key is {@code org.centeropenmiddleware.almistack.poc.clients.spi.RuntimeInstance}, then the
	 * value of that entry is used as the name of the implementation class.</li>
	 * <li>
	 * If a system property with the name
	 * {@code org.centeropenmiddleware.almistack.poc.clients.spi.RuntimeInstance} is defined, then its value is
	 * used as the name of the implementation class.</li>
	 * <li>
	 * Finally, a default implementation class name is used.</li>
	 * </ul>
	 *
	 * @return an instance of {@code RuntimeInstance}.
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
	 * @param delegate
	 *            the {@code RuntimeInstance} runtime delegate instance.
	 * @throws SecurityException
	 *             if there is a security manager and the permission
	 *             ReflectPermission("suppressAccessChecks") has not been
	 *             granted.
	 */
	public static void setInstance(final RuntimeInstance delegate) {
		final SecurityManager security = System.getSecurityManager();
		if (security != null) {
			security.checkPermission(suppressAccessChecksPermission);
		}
		RuntimeInstance.CACHED_DELEGATE.set(delegate);
	}

	public abstract JARBContext newContext(NamingPolicy policy);

	static class DefaultRuntimeInstance extends RuntimeInstance {

		private static final String ERROR_MESSAGE = String.format("No %s runtime instance found",RuntimeInstance.class);

		@Override
		public JARBContext newContext(final NamingPolicy policy) {
			throw new IllegalStateException(ERROR_MESSAGE);
		}

	}

}