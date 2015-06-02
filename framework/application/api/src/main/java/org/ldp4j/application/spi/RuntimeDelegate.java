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
package org.ldp4j.application.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ReflectPermission;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.application.session.WriteSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RuntimeDelegate {

	private static final String DISABLE = "disable";

	/**
	 * Name of the system property for controlling the resolution of the
	 * {@code RuntimeDelegate} instance leveraging the
	 * {@link java.util.ServiceLoader} mechanism.
	 *
	 * The absence of this property or any value but {@value #DISABLE}, despite
	 * the case, will enable the enable the usage of this mechanism.
	 */
	public static final String APPLICATION_ENGINE_SPI_FINDER = "org.ldp4j.application.spi.finder";

	/**
	 * Name of the configuration file where the
	 * {@value #APPLICATION_ENGINE_SPI_PROPERTY} property that
	 * identifies the {@link RuntimeDelegate} implementation to be returned from
	 * {@link RuntimeDelegate#engine()} can be defined.
	 */
	public static final String APPLICATION_ENGINE_SPI_CFG = "application-engine.properties";

	/**
	 * Name of the property identifying the {@link RuntimeDelegate} implementation
	 * to be returned from {@link RuntimeDelegate#engine()}.
	 */
	public static final String APPLICATION_ENGINE_SPI_PROPERTY = "org.ldp4j.application.spi.RuntimeDelegate";

	private static final String INSTANTIATE_ACTION = "instantiate";

	private static final Logger LOGGER=LoggerFactory.getLogger(RuntimeDelegate.class);

	private static final AtomicReference<RuntimeDelegate> CACHED_DELEGATE=new AtomicReference<RuntimeDelegate>();

	private static final ReflectPermission SUPPRESS_ACCESS_CHECKS_PERMISSION = new ReflectPermission("suppressAccessChecks");

	/**
	 * Allows custom implementations to extend the {@code RuntimeDelegate} class.
	 */
	protected RuntimeDelegate() {
	}


	/**
	 * Obtain an {@code RuntimeDelegate} instance using the method described
	 * in {@link RuntimeDelegate#engine()}.
	 *
	 * @return an instance of {@code ApplicationEngine} if available, or null if
	 *         a default implementation class is to be selected.
	 */
	private static RuntimeDelegate findDelegate() {
		try {
			RuntimeDelegate result=createRuntimeDelegateFromSPI();
			if(result==null) {
				result=createRuntimeDelegateFromConfigurationFile();
			}

			if(result==null) {
				String delegateClassName = System.getProperty(APPLICATION_ENGINE_SPI_PROPERTY);
				if(delegateClassName!=null) {
					result=createRuntimeDelegateForClassName(delegateClassName);
				}
			}

			return result;
		} catch (Exception ex) {
			throw new IllegalStateException("Could not find application engine",ex);
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
				String delegateClassName=configProperties.getProperty(APPLICATION_ENGINE_SPI_PROPERTY);
				if(delegateClassName!=null) {
					result=createRuntimeDelegateForClassName(delegateClassName);
				}
				if(delegateClassName==null && LOGGER.isWarnEnabled()) {
					LOGGER.warn("Configuration file '"+configFile.getAbsolutePath()+"' does not define a delegate class name");
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
				closeQuietly(is, "Could not close configuration properties");
			}
		}
		return result;
	}

	/**
	 * Get the configuration file for the Application Engine, that is, the a
	 * file named {@value #APPLICATION_ENGINE_SPI_CFG} in the <code>lib</code>
	 * directory of current JAVA_HOME.
	 *
	 * @return The configuration file for the runtime instance.
	 */
	private static File getConfigurationFile() {
		return new File(new File(System.getProperty("java.home")),"lib"+File.separator+APPLICATION_ENGINE_SPI_CFG);
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
		if(!DISABLE.equalsIgnoreCase(System.getProperty(APPLICATION_ENGINE_SPI_FINDER))) {
			try {
				for (RuntimeDelegate delegate : ServiceLoader.load(RuntimeDelegate.class)) {
					return delegate;
				}
			} catch (ServiceConfigurationError ex) {
				LOGGER.error("Could not load "+RuntimeDelegate.class.getCanonicalName()+" service. Full stacktrace follows:",ex);
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

	private static void handleFailure(String delegateClassName, String action, Exception failure) {
		if(LOGGER.isWarnEnabled()) {
			LOGGER.warn("Could not "+action+" delegate class "+delegateClassName,failure);
		}
	}

	/**
	 * Obtain a {@code ApplicationEngine} instance. If an instance had not
	 * already been created and set via {@link #setEngine(RuntimeDelegate)},
	 * the first invocation will create an instance which will then be cached
	 * for future use.
	 *
	 * <p>
	 * The algorithm used to locate the {@code ApplicationEngine} subclass to
	 * use consists of the following steps:
	 * </p>
	 * <ul>
	 * <li>
	 * If a resource with the name of
	 * {@code META-INF/services/org.ldp4j.application.engine.ApplicationEngine}
	 * exists, then its first line, if present, is used as the UTF-8 encoded
	 * name of the implementation class.</li>
	 * <li>
	 * If a file named {@value #APPLICATION_ENGINE_SPI_CFG} in the
	 * <code>lib</code> directory of current JAVA_HOME exists and it is readable
	 * by the {@code java.util.Properties.load(InputStream)} method and it
	 * contains an entry whose key is
	 * {@value #APPLICATION_ENGINE_SPI_PROPERTY}, then the value of that entry
	 * is used as the name of the implementation class.</li>
	 * <li>
	 * If a system property named {@value #APPLICATION_ENGINE_SPI_PROPERTY} is
	 * defined, then its value is used as the name of the implementation class.</li>
	 * <li>
	 * Finally, a default implementation class name is used.</li>
	 * </ul>
	 *
	 * @return an instance of {@code ApplicationEngine}.
	 */
	public static RuntimeDelegate getInstance() {
		RuntimeDelegate result = RuntimeDelegate.CACHED_DELEGATE.get();
		if (result != null) {
			return result;
		}
		synchronized(RuntimeDelegate.CACHED_DELEGATE) {
			result=RuntimeDelegate.CACHED_DELEGATE.get();
			if(result==null) {
				result=findDelegate();
				if(result==null) {
					result=new DefaultRuntimeDelegate();
				}
				RuntimeDelegate.CACHED_DELEGATE.set(result);
			}
			return result;
		}
	}

	/**
	 * Set the {@code RuntimeDelegate} that will be used by clients. If this method
	 * is not called prior to {@link RuntimeDelegate#engine()} then an
	 * implementation will be sought as described in
	 * {@link RuntimeDelegate#engine()}.
	 *
	 * @param delegate
	 *            the {@code ApplicationEngine} runtime delegate instance.
	 * @throws SecurityException
	 *             if there is a security manager and the permission
	 *             ReflectPermission("suppressAccessChecks") has not been
	 *             granted.
	 */
	public static void setInstance(final RuntimeDelegate delegate) {
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			security.checkPermission(SUPPRESS_ACCESS_CHECKS_PERMISSION);
		}
		synchronized(RuntimeDelegate.CACHED_DELEGATE) {
			RuntimeDelegate.CACHED_DELEGATE.set(delegate);
		}
	}

	private static class DefaultRuntimeDelegate extends RuntimeDelegate {

		private static final String ERROR_MESSAGE=
				String.format(
					"No implementation for class '%s' could be found",
					RuntimeDelegate.class.getName());

		@Override
		public WriteSession createSession() {
			throw new UnsupportedOperationException(ERROR_MESSAGE);
		}

		@Override
		public boolean isOffline() {
			throw new UnsupportedOperationException(ERROR_MESSAGE);
		}

	}

	public abstract boolean isOffline();

	public abstract WriteSession createSession();


}