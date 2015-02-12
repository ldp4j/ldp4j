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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
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
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.application.endpoint.EndpointManagementService;
import org.ldp4j.application.lifecycle.ApplicationLifecycleService;
import org.ldp4j.application.resource.ResourceControllerService;
import org.ldp4j.application.session.WriteSessionService;
import org.ldp4j.application.template.TemplateManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RuntimeInstance {

	private static final String INSTANTIATE_ACTION = "instantiate";

	private static final Logger LOGGER=LoggerFactory.getLogger(RuntimeInstance.class);

	public static final String LDP4J_APPLICATION_SPI_RUNTIMEINSTANCE_FINDER = "org.ldp4j.application.spi.runtimeinstance.finder";

	/**
	 * Name of the configuration file where the
	 * {@link RuntimeInstance#LDP4J_APPLICATION_SPI_PROPERTY} property that
	 * identifies the {@link RuntimeInstance} implementation to be returned from
	 * {@link RuntimeInstance#getInstance()} can be defined.
	 */
	public static final String LDP4J_APPLICATION_SPI_CFG = "ldp4j-application.properties";

	/**
	 * Name of the property identifying the {@link RuntimeInstance} implementation
	 * to be returned from {@link RuntimeInstance#getInstance()}.
	 */
	public static final String LDP4J_APPLICATION_SPI_PROPERTY = "org.ldp4j.application.spi.RuntimeInstance";

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
				String delegateClassName = System.getProperty(LDP4J_APPLICATION_SPI_PROPERTY);
				if(delegateClassName!=null) {
					result=createRuntimeInstanceForClassName(delegateClassName);
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

	private static RuntimeInstance createRuntimeInstanceFromConfigurationFile() {
		RuntimeInstance result=null;
		File configFile = getConfigurationFile();
		if(configFile.canRead()) {
			InputStream is=null;
			try {
				is=new FileInputStream(configFile);
				Properties configProperties=new Properties();
				configProperties.load(is);
				String delegateClassName=configProperties.getProperty(LDP4J_APPLICATION_SPI_PROPERTY);
				if(delegateClassName!=null) {
					result=createRuntimeInstanceForClassName(delegateClassName);
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
	 * Get the configuration file for the Runtime Instance: a file named
	 * {@link RuntimeInstance#LDP4J_APPLICATION_SPI_CFG} in the <code>lib</code> directory of
	 * current JAVA_HOME.
	 *
	 * @return The configuration file for the runtime instance.
	 */
	private static File getConfigurationFile() {
		return new File(new File(System.getProperty("java.home")),"lib"+File.separator+LDP4J_APPLICATION_SPI_CFG);
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

	private static RuntimeInstance createRuntimeInstanceFromSPI() {
		if(!"disable".equalsIgnoreCase(System.getProperty(LDP4J_APPLICATION_SPI_RUNTIMEINSTANCE_FINDER))) {
			for (RuntimeInstance delegate : ServiceLoader.load(RuntimeInstance.class)) {
				return delegate;
			}
		}
		return null;
	}

	private static RuntimeInstance createRuntimeInstanceForClassName(String delegateClassName) {
		RuntimeInstance result = null;
		try {
			Class<?> delegateClass = Class.forName(delegateClassName);
			if(RuntimeInstance.class.isAssignableFrom(delegateClass)) {
				Object impl = delegateClass.newInstance();
				result = RuntimeInstance.class.cast(impl);
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

	/**
	 * @param delegateClassName
	 * @param action
	 * @param failure
	 */
	private static void handleFailure(String delegateClassName, String action, Exception failure) {
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
				RuntimeInstance delegate = findDelegate();
				delegate.initialize();
				RuntimeInstance.CACHED_DELEGATE.set(delegate);
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
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			security.checkPermission(suppressAccessChecksPermission);
		}
		if(delegate!=null) {
			delegate.initialize();
		}
		RuntimeInstance.CACHED_DELEGATE.set(delegate);
	}

	private static class DefaultRuntimeInstance extends RuntimeInstance {

		private static final String ERROR_MESSAGE = String.format("No implementation for class '%s' could be found",RuntimeInstance.class);

		@Override
		public ServiceRegistry getServiceRegistry() {
			throw new AssertionError(ERROR_MESSAGE);
		}

		@Override
		public PersistencyManager getPersistencyManager() {
			throw new AssertionError(ERROR_MESSAGE);
		}

	}

	private void initialize() {
		TemplateManagementService tms =
			TemplateManagementService.
				serviceBuilder().
					setRuntimeInstance(this).
						build();
		getPersistencyManager().
			setTemplateManagementService(tms);
		getServiceRegistry().
			registerService(
				TemplateManagementService.class,
				tms).
			registerServiceBuilder(
				EndpointManagementService.
					serviceBuilder().
						setRuntimeInstance(this)).
			registerServiceBuilder(
				WriteSessionService.
					serviceBuilder().
						setRuntimeInstance(this)).
			registerServiceBuilder(
				ResourceControllerService.
					serviceBuilder().
						setRuntimeInstance(this)).
			registerServiceBuilder(
				ApplicationLifecycleService.
					serviceBuilder().
						setRuntimeInstance(this));
	}

	public abstract PersistencyManager getPersistencyManager();

	public abstract ServiceRegistry getServiceRegistry();

}
