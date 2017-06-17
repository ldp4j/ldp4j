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
package org.ldp4j.application.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ReflectPermission;
import java.net.URI;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.application.ApplicationApiRuntimeException;
import org.ldp4j.application.ApplicationContextException;
import org.ldp4j.application.session.ReadSession;
import org.ldp4j.application.session.ResourceSnapshot;
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
	 * in {@link RuntimeDelegate#getInstance()}.
	 *
	 * @return an instance of {@code RuntimeDelegate} if available, or null if
	 *         a default implementation class is to be selected.
	 */
	private static RuntimeDelegate findDelegate() {
		RuntimeDelegate result=null;
		try {
			result=createRuntimeDelegateFromSPI();
			if(result==null) {
				result=createRuntimeDelegateFromConfigurationFile();
			}
			if(result==null) {
				String delegateClassName = System.getProperty(APPLICATION_ENGINE_SPI_PROPERTY);
				if(delegateClassName!=null) {
					result=createRuntimeDelegateForClassName(delegateClassName);
				}
			}
		} catch (Exception ex) {
			LOGGER.warn("Could not find application engine",ex);
		}
		return result;
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
				if(delegateClassName==null) {
					LOGGER.warn("Configuration file '{}' does not define a delegate class name",configFile.getAbsolutePath());
				}
			} catch(FileNotFoundException e) {
				LOGGER.debug("Could not find runtime instance configuration file '{}'",configFile.getAbsolutePath(),e);
			} catch(IOException e) {
				LOGGER.warn("Could not load runtime instance configuration file '{}'",configFile.getAbsolutePath(),e);
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
	 *
	 * @param is
	 *            The input stream that is to be closed.
	 * @param message
	 *            The message to log in case of failure.
	 */
	private static void closeQuietly(InputStream is, String message) {
		if(is!=null) {
			try {
				is.close();
			} catch (Exception e) {
				LOGGER.warn(message,e);
			}
		}
	}

	private static RuntimeDelegate createRuntimeDelegateFromSPI() {
		if(!DISABLE.equalsIgnoreCase(System.getProperty(APPLICATION_ENGINE_SPI_FINDER))) {
			Iterator<RuntimeDelegate> iterator = ServiceLoader.load(RuntimeDelegate.class).iterator();
			while(iterator.hasNext()) {
				try {
					return iterator.next();
				} catch (ServiceConfigurationError ex) {
					LOGGER.error("Could not load {} service. Full stacktrace follows.",RuntimeDelegate.class.getCanonicalName(),ex);
				}
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
		LOGGER.warn("Could not {} delegate class {}",action,delegateClassName,failure);
	}

	/**
	 * Obtain a {@code RuntimeDelegate} instance. If an instance had not
	 * already been created and set via {@link RuntimeDelegate#setDelegate(RuntimeDelegate)},
	 * the first invocation will create an instance which will then be cached
	 * for future use.
	 *
	 * <p>
	 * The algorithm used to locate the {@code RuntimeDelegate} subclass to
	 * use consists of the following steps:
	 * </p>
	 * <ul>
	 * <li>
	 * If a resource with the name of
	 * {@code META-INF/services/}<tt>{@value #APPLICATION_ENGINE_SPI_PROPERTY}</tt>
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
	 * is not called prior to {@link RuntimeDelegate#getInstance()} then an
	 * implementation will be sought as described in
	 * {@link RuntimeDelegate#getInstance()}.
	 *
	 * @param delegate
	 *            the {@code RuntimeDelegate} runtime delegate instance.
	 * @throws SecurityException
	 *             if there is a security manager and the permission
	 *             ReflectPermission("suppressAccessChecks") has not been
	 *             granted.
	 */
	public static void setInstance(final RuntimeDelegate delegate) {
		SecurityManager security = getSecurityManager();
		if (security != null) {
			security.checkPermission(SUPPRESS_ACCESS_CHECKS_PERMISSION);
		}
		synchronized(RuntimeDelegate.CACHED_DELEGATE) {
			RuntimeDelegate.CACHED_DELEGATE.set(delegate);
		}
	}


	private static SecurityManager getSecurityManager() {
		return System.getSecurityManager();
	}

	private static class DefaultRuntimeDelegate extends RuntimeDelegate {

		private static final class NullResourceSnapshotResolver implements ResourceSnapshotResolver {
			@Override
			public URI resolve(ResourceSnapshot resource) {
				return null;
			}
			@Override
			public ResourceSnapshot resolve(URI endpoint) {
				return null;
			}
		}

		@Override
		public WriteSession createSession() throws ApplicationContextException {
			throw new ApplicationContextException("No runtime delegate found");
		}

		@Override
		public boolean isOffline() {
			return true;
		}

		@Override
		public ResourceSnapshotResolver createResourceResolver(URI canonicalBase, ReadSession session) {
			return new NullResourceSnapshotResolver();
		}

		@Override
		public void registerShutdownListener(ShutdownListener listener) {
			throw new ApplicationApiRuntimeException("No runtime delegate found");
		}

	}

	/**
	 * Return if the application is offline.
	 *
	 * @return if the application is offline.
	 */
	public abstract boolean isOffline();

	/**
	 * Create a {@code WriteSession}.
	 *
	 * @return a write session.
	 * @throws ApplicationContextException
	 *             if the session could not be created.
	 */
	public abstract WriteSession createSession() throws ApplicationContextException;

	/**
	 * Create a {@code ResourceSnapshotResolver}
	 *
	 * @param canonicalBase
	 *            the canonical base URI of the application.
	 * @param session
	 *            the {@code ReadSession} that the resolver may use for finding
	 *            out the snapshots.
	 * @return a resolver.
	 */
	public abstract ResourceSnapshotResolver createResourceResolver(URI canonicalBase, ReadSession session);

	/**
	 * Register a {@code ShutdownListener}
	 *
	 * @param listener
	 *            the listener to register
	 */
	public abstract void registerShutdownListener(ShutdownListener listener);

}