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
 *   Artifact    : org.ldp4j.framework:ldp4j-client-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-client-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.client.impl;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.client.ILDPContainer;
import org.ldp4j.client.ILDPResource;
import org.ldp4j.client.impl.cxf.CXFRemoteLDPProvider;
import org.ldp4j.client.impl.spi.IRemoteLDPProvider;
import org.ldp4j.client.impl.spi.ISourceTypeAdapter;
import org.ldp4j.client.spi.ITypeAdapter;
import org.ldp4j.client.spi.RuntimeInstance;
import org.ldp4j.client.spi.UnsupportedSourceException;
import org.ldp4j.client.spi.UnsupportedTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CoreRuntimeInstance extends RuntimeInstance {

	private static final Logger LOGGER=LoggerFactory.getLogger(CoreRuntimeInstance.class);

	// TODO: Make discoverable & configurable
	private static final IRemoteLDPProvider PROVIDER=new CXFRemoteLDPProvider();
	
	private final Map<Class<?>, Collection<ISourceTypeAdapter<?>>> registry=new HashMap<Class<?>, Collection<ISourceTypeAdapter<?>>>();

	private final ReadWriteLock registryLock=new ReentrantReadWriteLock();

	public CoreRuntimeInstance() {
		LOGGER.info("Starting Core LDP Client API Implementation");
		registerSourceTypeAdapters(CharSequence.class, new CharSequenceSourceTypeAdapter());
		registerSourceTypeAdapters(String.class, new StringSourceTypeAdapter());
		registerSourceTypeAdapters(InputStream.class, new InputStreamSourceTypeAdapter());
	}
	
	@Override
	public ILDPContainer createContainer(URL target) {
		assertTargetNotNull(target);
		return new CoreLDPContainer(PROVIDER.createContainerProxy(target));
	}

	@Override
	public ILDPResource createResource(URL target) {
		assertTargetNotNull(target);
		return new CoreLDPResource(PROVIDER.createResourceProxy(target));
	}

	@Override
	public <S, T> ITypeAdapter<S, T> createTypeAdapter(Class<? extends S> sourceClass, Class<? extends T> targetClass) throws UnsupportedSourceException, UnsupportedTargetException {
		Collection<ISourceTypeAdapter<?>> sourceTypeAdapters = getSourceTypeAdapters(sourceClass);
		for(ISourceTypeAdapter<?> sta:sourceTypeAdapters) {
			ITypeAdapter<S, T> result=tryCreateTypeAdapter(targetClass, sta);
			if(result!=null) {
				return result;
			}
		}
		throw new UnsupportedTargetException("No source type adapter for source class '"+sourceClass.getCanonicalName()+"' supports target class '"+targetClass.getCanonicalName()+"'");
	}

	private void assertTargetNotNull(URL target) {
		if(target==null) {
			throw new IllegalArgumentException("Object 'target' cannot be null");
		}
	}

	private <S> void registerSourceTypeAdapters(Class<? extends S> sourceClass, ISourceTypeAdapter<S> sta) {
		registryLock.writeLock().lock();
		try {
			boolean isSourceClassRegistered=false;
			for(Entry<Class<?>, Collection<ISourceTypeAdapter<?>>> entry:registry.entrySet()) {
				Class<?> key = entry.getKey();
				if(key.equals(sourceClass)) {
					entry.getValue().add(sta);
					isSourceClassRegistered=true;
					LOGGER.debug("Registered additional source type adapter '{}' for source class '{}'",sta.getClass().getCanonicalName(),key.getCanonicalName());
				} else if(entry.getKey().isAssignableFrom(sourceClass)) {
					entry.getValue().add(sta);
					LOGGER.debug("Registered additional source type adapter '{}' for compatible source class '{}'",sta.getClass().getCanonicalName(),key.getCanonicalName());
				}
			}
			if(!isSourceClassRegistered) {
				Collection<ISourceTypeAdapter<?>> collection=new ArrayList<ISourceTypeAdapter<?>>();
				collection.add(sta);
				LOGGER.debug("Registered source type adapter '{}' for source class '{}'",sta.getClass().getCanonicalName(),sourceClass.getCanonicalName());
				registry.put(sourceClass, collection);
			}
		} finally {
			registryLock.writeLock().unlock();
		}
	}

	@SuppressWarnings("unchecked")
	// TODO This has to be fixed, maybe using a third party utility like Reflections
	private <S, T> ITypeAdapter<S, T> tryCreateTypeAdapter(Class<? extends T> targetClass, ISourceTypeAdapter<?> sta) {
		if(!sta.supportsTarget(targetClass)) {
			return null;
		}
		try {
			return (ITypeAdapter<S, T>) sta.createTypeAdapter(targetClass);
		} catch (UnsupportedTargetException e) {
			String errorMessage = "We've already checked that the source type adapter supports the target class";
			if(LOGGER.isErrorEnabled()) {
				LOGGER.warn(errorMessage.concat(". Full stacktrace follows"),e);
			}
			throw new AssertionError(errorMessage);
		}
	}

	private Collection<ISourceTypeAdapter<?>> getSourceTypeAdapters(Class<?> sourceClass) throws UnsupportedSourceException {
		registryLock.readLock().lock();
		try {
			LOGGER.debug("Retrieving  source type adapter for source class '"+sourceClass.getCanonicalName()+"'...");
			Collection<ISourceTypeAdapter<?>> result = registry.get(sourceClass);
			if(result==null) {
				// No straight forward support, maybe indirect support
				LOGGER.debug("No straight forwad support for source class '"+sourceClass.getCanonicalName()+"' is available. Checking for support by compatible classes...");
				result=findCompatibleSourceTypeAdapters(sourceClass);
			}
			if(result.isEmpty()) {
				throw new UnsupportedSourceException("Source type '"+sourceClass.getCanonicalName()+"' is not supported yet");
			}
			LOGGER.debug("Following source type adapters are available for source class '"+sourceClass.getCanonicalName()+"': "+result);
			return Collections.unmodifiableCollection(result);
		} finally {
			registryLock.readLock().unlock();
		}
	}

	private Collection<ISourceTypeAdapter<?>> findCompatibleSourceTypeAdapters(Class<?> sourceClass) {
		Collection<ISourceTypeAdapter<?>> result=new ArrayList<ISourceTypeAdapter<?>>();
		for(Entry<Class<?>, Collection<ISourceTypeAdapter<?>>> entry:registry.entrySet()) {
			LOGGER.trace("Checking supported source class '"+entry.getKey().getCanonicalName()+"'...");
			if(entry.getKey().isAssignableFrom(sourceClass)) {
				LOGGER.debug("Found compatible supported source class '"+entry.getKey().getCanonicalName()+"'...");
				result.addAll(entry.getValue());
			}
		}
		return result;
	}
	
}