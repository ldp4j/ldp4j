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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.impl;

import static org.reflections.ReflectionUtils.getAllMethods;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withModifier;
import static org.reflections.ReflectionUtils.withParameters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javassist.Modifier;

import org.ldp4j.server.Format;
import org.ldp4j.server.IContent;
import org.ldp4j.server.LinkedDataPlatformException;
import org.ldp4j.server.core.ILinkedDataPlatformContainer;
import org.ldp4j.server.core.ILinkedDataPlatformResourceHandler;
import org.ldp4j.server.core.Init;
import org.ldp4j.server.core.Initializable;
import org.ldp4j.server.core.InitializationException;
import org.ldp4j.server.spi.ILinkedDataPlatformRegistry;
import org.ldp4j.server.spi.IResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.util.concurrent.Monitor;

/**
 * Default registry implementation of the <b>ALM iStack Linked Data Platform
 * Server</b>.<br />
 * Methods throw IllegalArgumentException if null arguments are passed. <br />
 * Current implementation is NOT thread-safe.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 * @see org.ldp4j.server.spi.ILinkedDataPlatformRegistry
 */
public class LinkedDataPlatformRegistry implements ILinkedDataPlatformRegistry {

	public interface IControlledContainer extends ILinkedDataPlatformContainer {

		boolean isInitializable();
		
		boolean initialize(IResourceManager manager) throws InitializationException;

		ILinkedDataPlatformContainer getDelegate();
		
	}
	
	public static class ControlledContainer implements IControlledContainer {

		private final ILinkedDataPlatformContainer delegate;
		private final Method initializer;
		private final Monitor monitor = new Monitor();
		private final Monitor.Guard notInitializing = new Monitor.Guard(monitor) {
			public boolean isSatisfied() {
				return initializing == false;
			}
		};

		private boolean initialized=false;
		private boolean initializing=false;

		public ControlledContainer(ILinkedDataPlatformContainer delegate, Method initializer) {
			this.delegate = delegate;
			this.initializer = initializer;
		}
		
		@Override
		public String getContainerId() {
			return delegate.getContainerId();
		}

		@Override
		public String createResource(IContent body, Format format) throws LinkedDataPlatformException {
			return delegate.createResource(body, format);
		}

		@Override
		public IContent getSummary(Collection<String> resources, Format format) throws LinkedDataPlatformException {
			return delegate.getSummary(resources,format);
		}

		@Override
		public boolean isInitializable() {
			return initializer!=null;
		}

		@Override
		public boolean initialize(IResourceManager manager) throws InitializationException {
			if(!isInitializable()) {
				return false;
			}
			try {
				monitor.enterWhen(notInitializing);
				try {
					boolean isInitializedOnEntry=initialized;
					if(!initialized) {
						initializing=true;
						initializer.invoke(delegate, manager);
						initialized=true;
						initializing=false;
					}
					return isInitializedOnEntry;
				} catch (Exception e) {
					throw new InitializationException("Container initialization failed",e);
				} finally {
					monitor.leave();
				}
			} catch (InterruptedException e) {
				throw new InitializationException("Interrupted while trying to initialize the container",e);
			}
		}

		@Override
		public ILinkedDataPlatformContainer getDelegate() {
			return delegate;
		}
		
	}

	/**
	 * Internal logger.
	 */
	private static final Logger LOGGER=LoggerFactory.getLogger(LinkedDataPlatformRegistry.class);
	
	private Method getInitMethod(Class<?> containerclass) {
		Method result = null;
		@SuppressWarnings("unchecked")
		Set<Method> initializers = 
			getAllMethods(
				containerclass,
				Predicates.<Method>and(
					withModifier(Modifier.PUBLIC),
					withParameters(IResourceManager.class),
					withAnnotation(Init.class)));
		if(initializers.isEmpty()) {
			String error = String.format("No initializer found in container class '%s'",containerclass.getCanonicalName());
			LOGGER.warn(error);
		} else if (initializers.size()>1) {
			String error = String.format("More than one initializer defined in container class '%s'",containerclass.getCanonicalName());
			LOGGER.warn(error);
		} else {
			result=initializers.iterator().next();
		}
		return result;
	}

	private boolean validateContainer(ILinkedDataPlatformContainer container) {
		boolean isValid=false;
		Class<? extends ILinkedDataPlatformContainer> containerClass = container.getClass();
		if(containerClass.getAnnotation(Initializable.class)==null) {
			controlledContainers.put(container.getContainerId(), new ControlledContainer(container,null));
			isValid=true;
		} else {
			Method method = getInitMethod(containerClass);
			if(method!=null) {
				controlledContainers.put(container.getContainerId(), new ControlledContainer(container,method));
				isValid=true;
			}
		}
		return isValid;
	}

	/**
	 * Refresh the cache of registered containers. If a clash is detected the
	 * newly loaded container will not be registered.
	 * @see org.ldp4j.server.core.ILinkedDataPlatformContainer
	 */
	private void refreshContainers() {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Starting container refresh...");
		}
		ServiceLoader<ILinkedDataPlatformContainer> loader=ServiceLoader.load(ILinkedDataPlatformContainer.class);
		for(ILinkedDataPlatformContainer container:loader) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.trace(String.format("Found container '%s' (%s)",container.getContainerId(),container.getClass().getCanonicalName()));
			}
			ILinkedDataPlatformContainer registeredContainer=containers.get(container.getContainerId());
			if(registeredContainer==null) {
				boolean isValid = validateContainer(container);
				if(isValid) {
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug(String.format("Registering new container '%s' (%x)",container.getContainerId(),container.hashCode()));
					}
					containers.put(container.getContainerId(),container);
				} else {
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug(String.format("Discarding container '%s' (%x)",container.getContainerId(),container.hashCode()));
					}
				}
			} else {
				if(LOGGER.isWarnEnabled()) {
					if(!registeredContainer.equals(container)) {
						LOGGER.warn(String.format("Found duplicated container '%s': container %x clashes with container %x...",container.getContainerId(),container.hashCode(),registeredContainer.hashCode()));
					}
				}
			}
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Container refresh completed.");
		}
	}

	/**
	 * Refresh the cache of registered resource handlers. If a clash is detected the
	 * newly loaded resource handler will not be registered.
	 * @see org.ldp4j.server.core.ILinkedDataPlatformResourceHandler
	 */
	private void refreshResourceHandlers() {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Starting resource handler refresh...");
		}
		ServiceLoader<ILinkedDataPlatformResourceHandler> loader=ServiceLoader.load(ILinkedDataPlatformResourceHandler.class);
		for(ILinkedDataPlatformResourceHandler handler:loader) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Found resource handler for container '%s' (%s)",handler.getContainerId(),handler.getClass().getCanonicalName()));
			}
			ILinkedDataPlatformResourceHandler registeredContainer=resourceHandlers.get(handler.getContainerId());
			if(registeredContainer==null) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug(String.format("Registering new resource handler for container '%s' (%x)",handler.getContainerId(),handler.hashCode()));
				}
				resourceHandlers.put(handler.getContainerId(),handler);
			} else {
				if(LOGGER.isWarnEnabled()) {
					if(!registeredContainer.equals(handler)) {
						LOGGER.warn(String.format("Found duplicated resource handler for container '%s': resource handler %x clashes with resource handler %x...",handler.getContainerId(),handler.hashCode(),registeredContainer.hashCode()));
					}
				}
			}
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Resource handler refresh completed.");
		}
	}

	/**
	 * Cached containers, indexed by identifier.
	 */
	private final Map<String,ILinkedDataPlatformContainer> containers=new HashMap<String,ILinkedDataPlatformContainer>();

	private final ConcurrentMap<String, IControlledContainer> controlledContainers=new ConcurrentHashMap<String, LinkedDataPlatformRegistry.IControlledContainer>();

	/**
	 * Cached resource handlers, indexed by container identifier.
	 */
	private final Map<String,ILinkedDataPlatformResourceHandler> resourceHandlers=new HashMap<String,ILinkedDataPlatformResourceHandler>();

	private boolean loaded;

	public LinkedDataPlatformRegistry() {
		loaded=false;
	}

	// TODO: Fix the loading mechanism. Everybody should wait until the registry is properly initialized. Right now, only the first thread to invoke the load would have a loaded registry when returning.
	private void load() {
		synchronized(resourceHandlers) {
			if(loaded) {
				return;
			}
			loaded=true;
		}
		refreshContainers();
		refreshResourceHandlers();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getContainerIds() {
		load();
		return new ArrayList<String>(containers.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IControlledContainer getContainer(String containerId) {
		if(containerId==null) {
			throw new IllegalArgumentException("Object 'containerId' cannot be null");
		}
		if(containerId.isEmpty()) {
			throw new IllegalArgumentException("String 'containerId' cannot be empty");
		}
		load();
		return controlledContainers.get(containerId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ILinkedDataPlatformResourceHandler getResourceHandler(String containerId) {
		if(containerId==null) {
			throw new IllegalArgumentException("Object 'containerId' cannot be null");
		}
		if(containerId.isEmpty()) {
			throw new IllegalArgumentException("String 'containerId' cannot be empty");
		}
		load();
		return resourceHandlers.get(containerId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <C extends ILinkedDataPlatformContainer> C getContainer(String containerId, Class<C> containerClass) {
		 IControlledContainer container = getContainer(containerId);
		C result=null;
		if(container!=null) {
			if(containerClass.isInstance(container.getDelegate())) {
				result=containerClass.cast(container.getDelegate());
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <C extends ILinkedDataPlatformResourceHandler> C getResourceHandler(String containerId, Class<C> resourceHandlerClass) {
		ILinkedDataPlatformResourceHandler resourceHandler = getResourceHandler(containerId);
		C result=null;
		if(resourceHandler!=null) {
			if(resourceHandlerClass.isInstance(resourceHandler)) {
				result=resourceHandlerClass.cast(resourceHandler);
			}
		}
		return result;
	}

}