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
package org.ldp4j.application.template;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.spi.PersistencyManager;
import org.ldp4j.application.spi.Service;
import org.ldp4j.application.spi.ServiceBuilder;

public final class TemplateManagementService implements Service {

	private static class TemplateManagementServiceBuilder extends ServiceBuilder<TemplateManagementService> {

		private TemplateManagementServiceBuilder() {
			super(TemplateManagementService.class);
		}

		public TemplateManagementService build() {
			return new TemplateManagementService(persistencyManager());
		}

	}

	private final PersistencyManager persistencyManager;

	private final Lock read;
	private final Lock write;

	private TemplateManager manager=null;

	private TemplateManagementService(PersistencyManager persistencyManager) {
		this.persistencyManager = persistencyManager;
		ReadWriteLock lock=new ReentrantReadWriteLock();
		this.read=lock.readLock();
		this.write=lock.writeLock();
	}

	public void configure(List<Class<?>> handlerClasses, List<ResourceHandler> handlers) throws TemplateManagementServiceConfigurationException {
		write.lock();
		try {
			this.manager=
				TemplateManager.
						builder().
							withPersistencyManager(this.persistencyManager).
							withHandlerClasses(handlerClasses).
							withHandlers(handlers).
							build();
		} finally {
			write.unlock();
		}
	}

	public <T extends ResourceHandler> T getHandler(Class<? extends T> handlerClass) {
		read.lock();
		try {
			ResourceTemplate template=this.persistencyManager.templateOfHandler(handlerClass);
			return this.manager.getHandler(handlerClass, template);
		} finally {
			read.unlock();
		}
	}

	public static ServiceBuilder<TemplateManagementService> serviceBuilder() {
		return new TemplateManagementServiceBuilder();
	}

	public static TemplateManagementService defaultService() {
		return serviceBuilder().build();
	}

}