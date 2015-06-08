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

/**
 * ApplicationEngine is an abstract facade class for the <b>LDP4j Application
 * Engine API</b>. The class defines the default behavior of the <b>LDP4j Application
 * Engine</b> and provides an extension point for pluging-in alternative
 * implementations of the <i>LDP4j Application Engine API</i>. These extension points
 * are designed for use by other <i>LDP4j Application Engine</i> classes and are not
 * intended to be called directly by applications using the <i>LDP4j Application
 * Engine API</i>.<br />
 * <br/>
 *
 * An implementation of LPD4j Application Engine API MUST provide a concrete
 * subclass of {@code ApplicationEngine}. Using the supplied
 * {@code ApplicationEngine} this can be achieved in one of two ways:<br />
 * <ol>
 * <li>An instance of {@code ApplicationEngine} can be instantiated and injected
 * using its static method {@link ApplicationEngine#setEngine(ApplicationEngine) setEngine}. In this case the
 * implementation is responsible for creating the instance; this option is
 * intended for use with implementations based on IoC frameworks.</li>
 * <li>The class to be used can be configured, (see {@link ApplicationEngine#engine() below}). In this case the
 * <i>LDP4j Application Engine</i> is responsible for instantiating an instance of the
 * class. The configured class MUST have a public constructor which takes no
 * arguments.</li>
 * </ol>
 *
 * A <i>LDP4j Application Engine API</i> implementation may rely on a particular
 * implementation of the {@code ApplicationEngine} being used, however applications
 * SHOULD NOT depend on this nor override the supplied {@code ApplicationEngine}
 * instance with an application-supplied alternative as doing so may cause
 * unexpected problems. <br/>
 *
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public abstract class ApplicationEngine {

	private static final String DISABLE = "disable";

	/**
	 * Name of the system property for controlling the resolution of the
	 * {@code ApplicationEngine} runtime delegate instance leveraging the
	 * {@link java.util.ServiceLoader} mechanism.
	 *
	 * The absence of this property or any value but {@value #DISABLE}, despite
	 * the case, will enable the enable the usage of this mechanism.
	 */
	public static final String LDP4J_APPLICATION_ENGINE_FINDER = "org.ldp4j.application.engine.finder";

	/**
	 * Name of the configuration file where the
	 * {@value #LDP4J_APPLICATION_ENGINE_PROPERTY} property that
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
	private final Map<String,ApplicationContext> contexts;
	private final Stack<String> loadedContexts;
	private final AtomicReference<ApplicationContext> currentContext;

	private ApplicationEngineState state;

	protected static abstract class ApplicationContextManager<T extends ApplicationContext> {

		private final Class<? extends T> managedClass;

		protected ApplicationContextManager(Class<? extends T> managedClazz) {
			checkNotNull(managedClazz);
			this.managedClass=managedClazz;
		}

		protected final boolean isManaged(ApplicationContext instance) {
			return this.managedClass.isInstance(instance);
		}

		protected abstract T createContext(String applicationClassName) throws ApplicationContextCreationException;

		protected final boolean disposeContext(ApplicationContext applicationContext) throws ApplicationContextTerminationException {
			return doDisposeContext(this.managedClass.cast(applicationContext));
		}

		protected abstract boolean doDisposeContext(T applicationContext) throws ApplicationContextTerminationException ;

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
		this.state=ApplicationEngineState.UNAVAILABLE;
		this.loadedContexts=new Stack<String>();
	}

	private void refreshCurrentContext() {
		String current=this.loadedContexts.isEmpty()?null:this.loadedContexts.peek();
		this.currentContext.set(this.contexts.get(current));
	}

	private boolean isApplicationContextLoaded(ApplicationContext context) {
		return this.contexts.containsKey(context.applicationClassName()) && this.contexts.containsValue(context);
	}

	private void unsafeDisposeContext(ApplicationContext applicationContext) throws ApplicationContextTerminationException {
		applicationContextManager().disposeContext(applicationContext);
		this.loadedContexts.remove(applicationContext.applicationClassName());
		this.contexts.remove(applicationContext.applicationClassName());
	}

	private void setState(ApplicationEngineState newState) {
		this.state=newState;
		ApplicationEngine.notifyStateChange(newState);
	}

	private synchronized void checkApplicationEngineActive() {
		if(!ApplicationEngineState.STARTED.equals(this.state)) {
			throw new ApplicationEngineRuntimeException("Application engine is not available ("+this.state+")");
		}
	}

	public synchronized final void start() throws ApplicationEngineLifecycleException {
		if(ApplicationEngineState.STARTED.equals(this.state)) {
			return;
		}
		ApplicationEngineState newState=ApplicationEngineState.UNDEFINED;
		try {
			setUp();
			newState=ApplicationEngineState.STARTED;
		} finally {
			setState(newState);
		}
	}

	public synchronized final void shutdown() throws ApplicationEngineLifecycleException {
		if(!ApplicationEngineState.STARTED.equals(this.state)) {
			return;
		}
		this.currentContext.set(null);
		for(ApplicationContext ctx:this.contexts.values()) {
			try {
				unsafeDisposeContext(ctx);
			} catch (ApplicationContextTerminationException e) {
				if(LOGGER.isErrorEnabled()) {
					LOGGER.error("Could not terminate context", e);
				}
			}
		}
		try {
			cleanUp();
		} finally {
			setState(ApplicationEngineState.SHUTDOWN);
		}
	}

	public final ApplicationContext load(String applicationClassName) throws ApplicationContextCreationException {
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

	public final boolean dispose(ApplicationContext applicationContext) throws ApplicationContextTerminationException {
		checkNotNull(applicationContext,"Application context cannot be null");
		checkApplicationEngineActive();
		write.lock();
		try {
			if(!applicationContextManager().isManaged(applicationContext)) {
				throw new ApplicationContextTerminationException("Invalid application context class "+applicationContext.getClass().getName());
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

	@Deprecated
	public final ApplicationContext currentContext() {
		checkApplicationEngineActive();
		return currentContext.get();
	}

	protected abstract ApplicationContextManager<? extends ApplicationContext> applicationContextManager();

	protected void setUp() throws ApplicationEngineInitializationException {
		// To be overriden by implementations
	}

	protected void cleanUp() throws ApplicationEngineTerminationException {
		// To be overriden by implementations
	}

	/**
	 * Obtain an {@code ApplicationEngine} instance using the method described
	 * in {@link ApplicationEngine#engine()}.
	 *
	 * @return an instance of {@code ApplicationEngine} if available, or null if
	 *         a default implementation class is to be selected.
	 */
	private static ApplicationEngine findDelegate() {
		try {
			ApplicationEngine result=createApplicationEngineFromSPI();
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
	 * Get the configuration file for the Application Engine, that is, the a
	 * file named {@value #LDP4J_APPLICATION_ENGINE_CFG} in the <code>lib</code>
	 * directory of current JAVA_HOME.
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

	private static ApplicationEngine createApplicationEngineFromSPI() {
		if(!DISABLE.equalsIgnoreCase(System.getProperty(LDP4J_APPLICATION_ENGINE_FINDER))) {
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

	private static void handleFailure(String delegateClassName, String action, Exception failure) {
		if(LOGGER.isWarnEnabled()) {
			LOGGER.warn("Could not "+action+" delegate class "+delegateClassName,failure);
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

	/**
	 * Obtain a {@code ApplicationEngine} instance. If an instance had not
	 * already been created and set via {@link #setEngine(ApplicationEngine)},
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
	 * If a file named {@value #LDP4J_APPLICATION_ENGINE_CFG} in the
	 * <code>lib</code> directory of current JAVA_HOME exists and it is readable
	 * by the {@code java.util.Properties.load(InputStream)} method and it
	 * contains an entry whose key is
	 * {@value #LDP4J_APPLICATION_ENGINE_PROPERTY}, then the value of that entry
	 * is used as the name of the implementation class.</li>
	 * <li>
	 * If a system property named {@value #LDP4J_APPLICATION_ENGINE_PROPERTY} is
	 * defined, then its value is used as the name of the implementation class.</li>
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
				result.setState(state);
			}
			return result;
		}
	}

	/**
	 * Set the application engine that will be used by clients. If this method
	 * is not called prior to {@link ApplicationEngine#engine()} then an
	 * implementation will be sought as described in
	 * {@link ApplicationEngine#engine()}.
	 *
	 * @param delegate
	 *            the {@code ApplicationEngine} runtime delegate instance.
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
				try {
					current.shutdown();
				} catch (ApplicationEngineLifecycleException e) {
					LOGGER.error("Shutdown of previous engine failed. Full stacktrace follows:",e);
				}
			}
			ApplicationEngine.CACHED_DELEGATE.set(delegate);
			if(delegate!=null) {
				delegate.setState(ApplicationEngineState.AVAILABLE);
			}
		}
	}

	public static void registerLifecycleListener(ApplicationEngineLifecycleListener listener) {
		LISTENERS.registerListener(listener);
	}

	public static void deregisterLifecycleListener(ApplicationEngineLifecycleListener listener) {
		LISTENERS.deregisterListener(listener);
	}

	/**
	 * Return an object of the specified type to allow access to the
	 * provider-specific API. If the provider's ApplicationEngine implementation
	 * does not support the specified class, the ApplicationEngineException is
	 * thrown.
	 *
	 * @param clazz
	 *            the class of the object to be returned. This is normally
	 *            either the underlying {@code ApplicationEngine} implementation
	 *            class or an interface that it implements.
	 * @return an instance of the specified class
	 * @throws ApplicationEngineException
	 *             if the provider does not support the call
	 */
	public <T> T unwrap(Class<? extends T> clazz) throws ApplicationEngineException {
		checkNotNull(clazz,"Target class cannot be null");
		if(!clazz.isInstance(this)) {
			throw new ApplicationEngineException("Application Engine implementation is not compatible with "+clazz.getCanonicalName());
		}
		return clazz.cast(this);
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
