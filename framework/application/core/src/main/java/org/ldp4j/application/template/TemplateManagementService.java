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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.spi.Service;
import org.ldp4j.application.spi.ServiceBuilder;

public final class TemplateManagementService implements Service {

	private static class TemplateManagementServiceBuilder extends ServiceBuilder<TemplateManagementService> {
		
		private TemplateManagementServiceBuilder() {
			super(TemplateManagementService.class);
		}

		public TemplateManagementService build() {
			return new TemplateManagementService();
		}
		
	}

	private final Lock lock=new ReentrantLock();

	private volatile TemplateManager manager=null;
	
	private TemplateManagementService() {
	}
	
	public void setTemplateManager(TemplateManager manager) {
		if(manager==null) {
			return;
		}
		lock.lock();
		try {
			this.manager=manager;
		} finally {
			lock.unlock();
		}
	}

	public ResourceTemplate findTemplateById(String templateId) {
		checkNotNull(templateId,"Template identifier cannot be null");
		checkState(manager!=null,"Template Management Service has not been initialized yet");
		return this.manager.getTemplate(templateId);
	}

	public <T extends ResourceTemplate> T findTemplateById(String templateId, Class<? extends T> templateClass) {
		ResourceTemplate found = findTemplateById(templateId);
		if(found==null) {
			return null;
		} else if(!templateClass.isInstance(found)) {
			// TODO: Define a specialized runtime exception
			throw new IllegalArgumentException("Cannot cast template '"+templateId+"' to '"+templateClass.getCanonicalName()+"' ("+found.getClass().getCanonicalName()+")");
		}
		return templateClass.cast(found);
	}

	public ResourceTemplate findTemplateByHandler(Class<? extends ResourceHandler> handlerClass) {
		checkNotNull(handlerClass,"Resource handler cannot be null");
		checkState(manager!=null,"Template Management Service has not been initialized yet");
		return this.manager.getTemplate(handlerClass);
	}
	
	public <T extends ResourceHandler> T getHandler(Class<? extends T> handlerClass) {
		ResourceTemplate template=findTemplateByHandler(handlerClass);
		return this.manager.getHandler(handlerClass, template);
	}

	public static ServiceBuilder<TemplateManagementService> serviceBuilder() {
		return new TemplateManagementServiceBuilder();
	}
	
	public static TemplateManagementService defaultService() {
		return serviceBuilder().build();
	}

}
