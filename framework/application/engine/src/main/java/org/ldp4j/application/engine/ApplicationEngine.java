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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-engine-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ReflectPermission;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;










import org.ldp4j.application.engine.context.ApplicationContext;
import org.ldp4j.application.engine.lifecycle.ApplicationEngineLifecycleListener;
import org.ldp4j.application.engine.lifecycle.ApplicationEngineState;
import org.ldp4j.application.engine.util.ListenerManager;
import org.ldp4j.application.engine.util.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public abstract class ApplicationEngine {

	public static final String LDP4J_APPLICATION_ENGINE_FINDER = "org.ldp4j.application.application.engine.finder";

	/**
	 * Name of the configuration file where the
	 * {@link ApplicationEngine#LDP4J_APPLICATION_SPI_PROPERTY} property that
	 * identifies the {@link ApplicationEngine} implementation to be returned from
	 * {@link ApplicationEngine#engine()} can be defined.
	 */
	public static final String LDP4J_APPLICATION_ENGINE_CFG = "ldp4j-application-engine.properties";

	/**
	 * Name of the property identifying the {@link ApplicationEngine} implementation
	 * to be returned from {@link ApplicationEngine#engine()}.
	 */
	public static final String LDP4J_APPLICATION_ENGINE_PROPERTY = "org.ldp4j.application.ApplicationEngine";

	private static final String INSTANTIATE_ACTION = "instantiate";

	private static final Logger LOGGER=LoggerFactory.getLogger(ApplicationEngine.class);

	private static final AtomicReference<ApplicationEngine> CACHED_DELEGATE=new AtomicReference<ApplicationEngine>();

	private static final ReflectPermission SUPPRESS_ACCESS_CHECKS_PERMISSION = new ReflectPermission("suppressAccessChecks");

	private static final ListenerManager<ApplicationEngineLifecycleListener> LISTENERS=ListenerManager.newInstance();

	private final Lock read;
	private final Lock write;
	private final AtomicBoolean shutdown;
	private final Map<String,ApplicationContext> contexts;
	private final Stack<String> loadedContexts;
	private final AtomicReference<ApplicationContext> currentContext;

	protected static abstract class ApplicationContextManager<T extends ApplicationContext> {

		private final Class<? extends T> managedClass;

		protected ApplicationContextManager(Class<? extends T> managedClazz) {
			checkNotNull(managedClazz);
			this.managedClass=managedClazz;
		}

		protected final boolean isManaged(ApplicationContext instance) {
			return this.managedClass.isInstance(instance);
		}

		protected abstract T createContext(String applicationClassName) throws ApplicationInitializationException;

		protected final void disposeContext(ApplicationContext applicationContext) {
			doDisposeContext(this.managedClass.cast(applicationContext));
		}

		protected abstract void doDisposeContext(T applicationContext);

	}

	/**
	 * Allows custom implementations to extend the {@code ApplicationEngine} class.
	 */
	protected ApplicationEngine() {
		this.contexts=Maps.newLinkedHashMap();
		ReadWriteLock lock = new ReentrantReadWriteLock();
		this.read=lock.readLock();
		this.write=lock.writeLock();
		this.currentContext=new AtomicReference<ApplicationContext>();
		this.shutdown=new AtomicBoolean(false);
		this.loadedContexts=new Stack<String>();
	}

	private void refreshCurrentContext() {
		this.currentContext.set(this.contexts.get(this.loadedContexts.peek()));
	}

	private boolean isApplicationContextLoaded(ApplicationContext context) {
		return this.contexts.containsKey(context.applicationClassName()) && this.contexts.containsValue(context);
	}

	private void checkApplicationEngineActive() {
		if(!shutdown.compareAndSet(false,false)) {
			throw new IllegalStateException("Application engine is shutdown");
		}
	}

	private void unsafeDisposeContext(ApplicationContext applicationContext) {
		applicationContextManager().disposeContext(applicationContext);
		this.loadedContexts.remove(applicationContext.applicationClassName());
		this.contexts.remove(applicationContext.applicationClassName());
	}

	public final ApplicationContext load(String applicationClassName) throws ApplicationInitializationException {
		checkNotNull(applicationClassName,"Application class name cannot be null");
		checkApplicationEngineActive();
		write.lock();
		try {
			if(this.loadedContexts.contains(applicationClassName)) {
				throw new IllegalStateException("Application class '"+applicationClassName+"' is already been loaded");
			}
			ApplicationContext context=applicationContextManager().createContext(applicationClassName);
			this.contexts.put(applicationClassName, context);
			this.loadedContexts.push(applicationClassName);
			refreshCurrentContext();
			return context;
		} finally {
			write.unlock();
		}
	}

	public final ApplicationContext findByName(String applicationName) {
		checkNotNull(applicationName,"Application name cannot be null");
		checkApplicationEngineActive();
		read.lock();
		try {
			for(ApplicationContext ctx:this.contexts.values()) {
				if(ctx.applicationName().equals(applicationName)) {
					return ctx;
				}
			}
			return null;
		} finally {
			read.unlock();
		}
	}

	public final ApplicationContext findByClassName(String applicationClassName) {
		checkNotNull(applicationClassName,"Application class name cannot be null");
		checkApplicationEngineActive();
		read.lock();
		try {
			return this.contexts.get(applicationClassName);
		} finally {
			read.unlock();
		}
	}

	public final boolean dispose(ApplicationContext applicationContext) {
		checkNotNull(applicationContext,"Application context cannot be null");
		checkApplicationEngineActive();
		write.lock();
		try {
			if(!applicationContextManager().isManaged(applicationContext)) {
				throw new IllegalArgumentException("Invalid application context class "+applicationContext.getClass().getName());
			}
			if(!isApplicationContextLoaded(applicationContext)) {
				return false;
			}
			unsafeDisposeContext(applicationContext);
			refreshCurrentContext();
			return true;
		} finally {
			write.unlock();
		}
	}

	public final ApplicationContext currentContext() {
		checkApplicationEngineActive();
		return currentContext.get();
	}

	public synchronized final void shutdown() {
		if(!shutdown.compareAndSet(false,true)) {
			return;
		}
		this.currentContext.set(null);
		for(ApplicationContext ctx:this.contexts.values()) {
			unsafeDisposeContext(ctx);
		}
		cleanUp();
		ApplicationEngine.notifyStateChange(ApplicationEngineState.SHUTDOWN);
	}

	protected abstract ApplicationContextManager<? extends ApplicationContext> applicationContextManager();

	protected void cleanUp() {
		// To be overriden by implementations
	}

	/**
	 * Obtain a {@code RuntimeInstance} instance using the method described in
	 * {@link #getInstance}.
	 *
	 * @return an instance of {@code ApplicationEngine}.
	 */
	private static ApplicationEngine findDelegate() {
		try {
			ApplicationEngine result=createRuntimeInstanceFromSPI();
			if(result==null) {
				result=createApplicationEngineFromConfigurationFile();
			}

			if(result==null) {
				String delegateClassName = System.getProperty(LDP4J_APPLICATION_ENGINE_PROPERTY);
				if(delegateClassName!=null) {
					result=createApplicationEngineForClassName(delegateClassName);
				}
			}

			return result;
		} catch (Exception ex) {
			throw new IllegalStateException("Could not find application engine",ex);
		}
	}

	private static ApplicationEngine createApplicationEngineFromConfigurationFile() {
		ApplicationEngine result=null;
		File configFile = getConfigurationFile();
		if(configFile.canRead()) {
			InputStream is=null;
			try {
				is=new FileInputStream(configFile);
				Properties configProperties=new Properties();
				configProperties.load(is);
				String delegateClassName=configProperties.getProperty(LDP4J_APPLICATION_ENGINE_PROPERTY);
				if(delegateClassName!=null) {
					result=createApplicationEngineForClassName(delegateClassName);
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
		return new File(new File(System.getProperty("java.home")),"lib"+File.separator+LDP4J_APPLICATION_ENGINE_CFG);
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

	private static ApplicationEngine createRuntimeInstanceFromSPI() {
		if(!"disable".equalsIgnoreCase(System.getProperty(LDP4J_APPLICATION_ENGINE_FINDER))) {
			try {
				for (ApplicationEngine delegate : ServiceLoader.load(ApplicationEngine.class)) {
					return delegate;
				}
			} catch (ServiceConfigurationError ex) {
				LOGGER.error("Could not load LDP4j Application Engine service. Full stacktrace follows:",ex);
			}
		}
		return null;
	}

	private static ApplicationEngine createApplicationEngineForClassName(String delegateClassName) {
		ApplicationEngine result = null;
		try {
			Class<?> delegateClass = Class.forName(delegateClassName);
			if(ApplicationEngine.class.isAssignableFrom(delegateClass)) {
				Object impl = delegateClass.newInstance();
				result = ApplicationEngine.class.cast(impl);
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
	 * been created and set via {@link #setEngine(RuntimeInstance)}, the first
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
	 * @return an instance of {@code ApplicationEngine}.
	 */
	public static ApplicationEngine engine() {
		ApplicationEngine result = ApplicationEngine.CACHED_DELEGATE.get();
		if (result != null) {
			return result;
		}
		synchronized(ApplicationEngine.CACHED_DELEGATE) {
			result=ApplicationEngine.CACHED_DELEGATE.get();
			if(result==null) {
				result=findDelegate();
				ApplicationEngineState state=ApplicationEngineState.AVAILABLE;
				if(result==null) {
					result=new DefaultApplicationEngine();
					state=ApplicationEngineState.UNDEFINED;
				}
				ApplicationEngine.CACHED_DELEGATE.set(result);
				ApplicationEngine.notifyStateChange(state);
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
	public static void setEngine(final ApplicationEngine delegate) {
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			security.checkPermission(SUPPRESS_ACCESS_CHECKS_PERMISSION);
		}
		synchronized(ApplicationEngine.CACHED_DELEGATE) {
			ApplicationEngine current=ApplicationEngine.CACHED_DELEGATE.get();
			if(current!=null) {
				current.shutdown();
				ApplicationEngine.notifyStateChange(ApplicationEngineState.UNAVAILABLE);
			}
			ApplicationEngine.CACHED_DELEGATE.set(delegate);
			if(delegate!=null) {
				ApplicationEngine.notifyStateChange(ApplicationEngineState.AVAILABLE);
			}
		}
	}

	private static final void notifyStateChange(final ApplicationEngineState newState) {
		LISTENERS.notify(
			new Notification<ApplicationEngineLifecycleListener>() {
				@Override
				public void propagate(ApplicationEngineLifecycleListener listener) {
					listener.stateChanged(newState);
				}
			}
		);
	}

	public static void registerLifecycleListener(ApplicationEngineLifecycleListener listener) {
		LISTENERS.registerListener(listener);
	}

	public static void deregisterLifecycleListener(ApplicationEngineLifecycleListener listener) {
		LISTENERS.deregisterListener(listener);
	}

	private static class DefaultApplicationEngine extends ApplicationEngine {

		private static final String ERROR_MESSAGE=
				String.format(
					"No implementation for class '%s' could be found",
					ApplicationEngine.class.getName());

		@Override
		protected ApplicationContextManager<ApplicationContext> applicationContextManager() {
			throw new ApplicationEngineRuntimeException(ERROR_MESSAGE);
		}

	}

}
