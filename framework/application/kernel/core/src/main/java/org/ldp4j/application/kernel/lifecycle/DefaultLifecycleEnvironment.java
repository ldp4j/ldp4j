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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.2
 *   Bundle      : ldp4j-application-kernel-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.lifecycle;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.LinkedList;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.engine.ApplicationContextBootstrapException;
import org.ldp4j.application.lifecycle.ApplicationLifecycleListener;
import org.ldp4j.application.lifecycle.LifecycleEnvironment;
import org.ldp4j.application.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

final class DefaultLifecycleEnvironment implements LifecycleEnvironment {

	private static final Logger LOGGER=LoggerFactory.getLogger(DefaultLifecycleEnvironment.class);

	private final LinkedList<Managed> managedObjects; // NOSONAR
	private final LinkedList<ApplicationLifecycleListener> listeners; // NOSONAR

	DefaultLifecycleEnvironment() {
		this.managedObjects=Lists.newLinkedList();
		this.listeners=Lists.newLinkedList();
	}

	@Override
	public void register(Managed managed) {
		checkNotNull(managed,"Managed objects cannot be null");
		this.managedObjects.add(managed);
		LOGGER.debug("Registered managed object {}",managed);
	}

	@Override
	public void addApplicationLifecycleListener(ApplicationLifecycleListener listener) {
		checkNotNull(listener,"Application lifecycle listeners cannot be null");
		this.listeners.add(listener);
		LOGGER.debug("Registered application lifecycle listener {}",listener);
	}

	void start(ApplicationContext context) throws ApplicationContextBootstrapException {
		LOGGER.info("Starting application components...");
		LOGGER.debug("Starting managed objects...");
		for(Managed managed:this.managedObjects) {
			try {
				managed.start();
				LOGGER.trace("Started managed object {}.",managed);
			} catch(Exception e) {
				LOGGER.warn("Could not start managed object {}",managed,e);
				throw new ApplicationContextBootstrapException("Could not start managed object "+managed, e);
			}
		}
		LOGGER.debug("Notifying start-up event to application lifecycle listeners...");
		for(ApplicationLifecycleListener listener:this.listeners) {
			try {
				listener.applicationStarted(context);
				LOGGER.trace("Notified start-up to {}.",listener);
			} catch(Exception e) {
				LOGGER.warn("Listener {} failed when notifying startup",listener,e);
				throw new ApplicationContextBootstrapException("Listeners "+listener+" failed when notifying startup", e);
			}
		}
		LOGGER.info("Application components started.");
	}

	void stop() {
		LOGGER.info("Stopping application components...");
		LOGGER.debug("Stopping managed objects...");
		Iterator<Managed> iterator = this.managedObjects.descendingIterator();
		while(iterator.hasNext()) {
			Managed managed=iterator.next();
			try {
				managed.stop();
				LOGGER.trace("Stopped managed object {}.",managed);
			} catch(Exception e) {
				LOGGER.warn("Could not stop managed object {}",managed,e);
			}
		}
		LOGGER.debug("Notifying stop event to application lifecycle listeners...");
		for(ApplicationLifecycleListener listener:this.listeners) {
			try {
				listener.applicationStopped();
				LOGGER.trace("Notified stop to {}.",listener);
			} catch(Exception e) {
				LOGGER.warn("Listener {} failed when notifying stop",listener,e);
			}
		}
		LOGGER.info("Application components stopped.");
	}

}