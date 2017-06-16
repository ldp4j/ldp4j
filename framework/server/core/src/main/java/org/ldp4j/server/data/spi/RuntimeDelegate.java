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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.data.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ReflectPermission;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RuntimeDelegate {

	private static final String INSTANTIATE_ACTION = "instantiate";

	private static final Logger LOGGER=LoggerFactory.getLogger(RuntimeDelegate.class);

	public static final String LDP4J_SERVER_DATA_SPI_RUNTIMEDELEGATE_FINDER = "org.ldp4j.server.data.spi.RuntimeDelegate.finder";

	/**
	 * Name of the configuration file where the
	 * {@link RuntimeDelegate#LDP4J_SERVER_DATA_SPI_PROPERTY} property that
	 * identifies the {@link RuntimeDelegate} implementation to be returned from
	 * {@link RuntimeDelegate#getInstance()} can be defined.
	 */
	public static final String LDP4J_SERVER_DATA_SPI_CFG = "ldp4j-server-data.properties";

	/**
	 * Name of the property identifying the {@link RuntimeDelegate} implementation
	 * to be returned from {@link RuntimeDelegate#getInstance()}.
	 */
	public static final String LDP4J_SERVER_DATA_SPI_PROPERTY = "org.ldp4j.server.data.spi.RuntimeDelegate";

	private static final AtomicReference<RuntimeDelegate> CACHED_DELEGATE=new AtomicReference<RuntimeDelegate>();

	private static ReflectPermission suppressAccessChecksPermission = new ReflectPermission("suppressAccessChecks");

	/**
	 * Allows custom implementations to extend the {@code RuntimeInstance} class.
	 */
	protected RuntimeDelegate() {
	}

	/**
	 * Obtain a {@code RuntimeInstance} instance using the method described in
	 * {@link #getInstance}.
	 *
	 * @return an instance of {@code RuntimeInstance}.
	 */
	private static RuntimeDelegate findDelegate() {
		try {
			RuntimeDelegate result=createRuntimeDelegateFromSPI();
			if(result==null) {
				result=createRuntimeDelegateFromConfigurationFile();
			}

			if(result==null) {
				String delegateClassName = System.getProperty(LDP4J_SERVER_DATA_SPI_PROPERTY);
				if(delegateClassName!=null) {
					result=createRuntimeDelegateForClassName(delegateClassName);
				}
			}

			if(result==null) {
				result=new DefaultRuntimeInstance();
			}

			return result;
		} catch (Exception ex) {
			throw new IllegalStateException("Could not find runtime delegate",ex);
		}
	}

	private static RuntimeDelegate createRuntimeDelegateFromConfigurationFile() {
		RuntimeDelegate result=null;
		File configFile = getConfigurationFile();
		if(configFile.canRead()) {
			InputStream is=null;
			try {
				is=new FileInputStream(configFile);
				Properties configProperties=new Properties();
				configProperties.load(is);
				String delegateClassName=configProperties.getProperty(LDP4J_SERVER_DATA_SPI_PROPERTY);
				if(delegateClassName!=null) {
					result=createRuntimeDelegateForClassName(delegateClassName);
				}
				if(delegateClassName==null && LOGGER.isWarnEnabled()) {
					LOGGER.warn("MutableConfiguration file '"+configFile.getAbsolutePath()+"' does not define a delegate class name");
				}
			} catch(FileNotFoundException e) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("Could not find runtime instance configuration file '"+configFile.getAbsolutePath()+"'",e);
				}
			} catch(IOException e) {
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
	 * {@value #LDP4J_SERVER_DATA_SPI_CFG} in the <code>lib</code> directory of
	 * current JAVA_HOME.
	 *
	 * @return The configuration file for the runtime instance.
	 */
	private static File getConfigurationFile() {
		return new File(new File(System.getProperty("java.home")),"lib"+File.separator+LDP4J_SERVER_DATA_SPI_CFG);
	}

	/**
	 * Close an input stream logging possible failures.
	 * @param is The input stream that is to be closed.
	 * @param message The message to log in case of failure.
	 */
	private static void closeQuietly(InputStream is, String message) {
		if(is!=null) {
		try {
			is.close();
		} catch (Exception e) {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(message,e);
			}
		}
		}
	}

	private static RuntimeDelegate createRuntimeDelegateFromSPI() {
		if(!"disable".equalsIgnoreCase(System.getProperty(LDP4J_SERVER_DATA_SPI_RUNTIMEDELEGATE_FINDER))) {
			for (RuntimeDelegate delegate : ServiceLoader.load(RuntimeDelegate.class)) {
				return delegate;
			}
		}
		return null;
	}

	private static RuntimeDelegate createRuntimeDelegateForClassName(String delegateClassName) {
		RuntimeDelegate result = null;
		try {
			Class<?> delegateClass = Class.forName(delegateClassName);
			if(RuntimeDelegate.class.isAssignableFrom(delegateClass)) {
				Object impl = delegateClass.newInstance();
				result = RuntimeDelegate.class.cast(impl);
			}
		} catch (ClassNotFoundException e) {
			handleFailure(delegateClassName, "find", e);
		} catch (InstantiationException e) {
			handleFailure(delegateClassName, INSTANTIATE_ACTION, e);
		} catch (IllegalAccessException e) {
			handleFailure(delegateClassName, INSTANTIATE_ACTION, e);
		}
		return result;
	}

	private static void handleFailure(String delegateClassName, String action,
			Exception failure) {
		if(LOGGER.isWarnEnabled()) {
			LOGGER.warn("Could not "+action+" delegate class "+delegateClassName,failure);
		}
	}

	/**
	 * Obtain a {@code RuntimeDelegate} instance. If an instance had not already
	 * been created and set via {@link #setInstance(RuntimeDelegate)}, the first
	 * invocation will create an instance which will then be cached for future
	 * use.
	 *
	 * <p>
	 * The algorithm used to locate the RuntimeDelegate subclass to use consists
	 * of the following steps:
	 * </p>
	 * <ul>
	 * <li>
	 * If a resource with the name of
	 * {@code META-INF/services/org.ldp4j.server.data.spi.RuntimeDelegate} exists, then
	 * its first line, if present, is used as the UTF-8 encoded name of the
	 * implementation class.</li>
	 * <li>
	 * If the $java.home/lib/ldp4j-server-data.properties file exists and it is readable by
	 * the {@code java.util.Properties.load(InputStream)} method and it contains
	 * an entry whose key is {@code org.ldp4j.server.data.spi.RuntimeDelegate}, then the
	 * value of that entry is used as the name of the implementation class.</li>
	 * <li>
	 * If a system property with the name
	 * {@code org.ldp4j.server.data.spi.RuntimeDelegate} is defined, then its value is
	 * used as the name of the implementation class.</li>
	 * <li>
	 * Finally, a default implementation class name is used.</li>
	 * </ul>
	 *
	 * @return an instance of {@code RuntimeDelegate}.
	 */
	public static RuntimeDelegate getInstance() {
		RuntimeDelegate result = RuntimeDelegate.CACHED_DELEGATE.get();
		if (result != null) {
			return result;
		}
		synchronized(RuntimeDelegate.CACHED_DELEGATE) {
			result=RuntimeDelegate.CACHED_DELEGATE.get();
			if(result==null) {
				RuntimeDelegate.CACHED_DELEGATE.set(findDelegate());
				result=RuntimeDelegate.CACHED_DELEGATE.get();
			}
			return result;
		}
	}

	/**
	 * Set the runtime delegate that will be used by Data API classe.
	 * If this method is not called prior to {@link #getInstance} then
	 * an implementation will be sought as described in {@link #getInstance}.
	 *
	 * @param delegate
	 *            the {@code RuntimeInstance} runtime delegate instance.
	 * @throws SecurityException
	 *             if there is a security manager and the permission
	 *             ReflectPermission("suppressAccessChecks") has not been
	 *             granted.
	 */
	public static void setInstance(final RuntimeDelegate delegate) {
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			security.checkPermission(suppressAccessChecksPermission);
		}
		RuntimeDelegate.CACHED_DELEGATE.set(delegate);
	}

	public abstract Set<MediaType> getSupportedMediaTypes();

	public abstract MediaTypeProvider getMediaTypeProvider(MediaType mediaType);

	public abstract void registerMediaTypeProvider(MediaTypeProvider provider);

	private static class DefaultRuntimeInstance extends RuntimeDelegate {

		private static final String ERROR_MESSAGE = String.format("No implementation for '%s' found",RuntimeDelegate.class);

		@Override
		public Set<MediaType> getSupportedMediaTypes() {
			throw new AssertionError(ERROR_MESSAGE);
		}

		@Override
		public MediaTypeProvider getMediaTypeProvider(MediaType mediaType) {
			throw new AssertionError(ERROR_MESSAGE);
		}

		@Override
		public void registerMediaTypeProvider(MediaTypeProvider provider) {
			throw new AssertionError(ERROR_MESSAGE);
		}

	}

}