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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-api:0.2.2
 *   Bundle      : ldp4j-application-engine-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public final class ListenerManager<T> {

	private static final Logger LOGGER=LoggerFactory.getLogger(ListenerManager.class);

	private final ReadWriteLock lock;
	private final List<T> listeners;

	private ListenerManager() {
		this.lock=new ReentrantReadWriteLock();
		this.listeners=Lists.newArrayList();
	}

	private void logListenerLifecycle(T listener, String action) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("%s %08X (%s)",action,listener.hashCode(),listener.getClass().getName()));
		}
	}

	public void registerListener(T listener) {
		this.lock.writeLock().lock();
		try {
			if(!this.listeners.contains(listener)) {
				this.listeners.add(listener);
				logListenerLifecycle(listener, "Registered");
			}
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	public void deregisterListener(T listener) {
		this.lock.writeLock().lock();
		try {
			if(this.listeners.remove(listener)) {
				logListenerLifecycle(listener,"Deregistered");
			}
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	public void notify(Notification<T> notification) {
		List<T> currentListeners=new ArrayList<T>();
		this.lock.readLock().lock();
		try {
			currentListeners.addAll(this.listeners);
		} finally {
			this.lock.readLock().unlock();
		}

		// TODO: What about using a thread pool to propagate the notifications?
		// Maybe a configurable option
		for(T listener:currentListeners) {
			try {
				notification.propagate(listener);
			} catch (Exception e) {
				LOGGER.warn(
					"Propagation failure {} ({})",
					Integer.toHexString(listener.hashCode()).toUpperCase(Locale.ENGLISH),
					listener.getClass().getName(),e);
			}
		}
	}

	public static <T> ListenerManager<T> newInstance() {
		return new ListenerManager<T>();
	}

}
