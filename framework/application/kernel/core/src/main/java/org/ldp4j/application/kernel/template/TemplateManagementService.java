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
package org.ldp4j.application.kernel.template;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.kernel.service.Service;
import org.ldp4j.application.kernel.service.ServiceBuilder;
import org.ldp4j.application.kernel.spi.ModelFactory;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.TemplateLibrary;

public final class TemplateManagementService implements Service {

	private static final class TemplateManagementServiceBuilder extends ServiceBuilder<TemplateManagementService> {

		private TemplateManagementServiceBuilder() {
			super(TemplateManagementService.class);
		}

		@Override
		public TemplateManagementService build() {
			return new TemplateManagementService(super.modelFactory());
		}

	}

	private interface ServiceState {

		void configure(List<Class<?>> handlerClasses, List<ResourceHandler> handlers) throws TemplateManagementServiceConfigurationException;

		TemplateLibrary templateLibrary();

		<T extends ResourceHandler> T getHandler(Class<? extends T> handlerClass);

	}

	private final class ConfiguredServiceState implements ServiceState {

		private final TemplateManager manager;

		private ConfiguredServiceState(TemplateManager manager) {
			this.manager = manager;
		}

		@Override
		public void configure(List<Class<?>> handlerClasses,List<ResourceHandler> handlers) throws TemplateManagementServiceConfigurationException {
			throw new IllegalStateException("Template management service is already configured");
		}

		@Override
		public TemplateLibrary templateLibrary() {
			return this.manager.templateLibrary();
		}

		@Override
		public <T extends ResourceHandler> T getHandler(Class<? extends T> handlerClass) {
			ResourceTemplate template=templateLibrary().findByHandler(handlerClass);
			return this.manager.getHandler(handlerClass, template);
		}

	}

	private final class NewServiceState implements ServiceState {

		private NewServiceState() {
		}

		@Override
		public void configure(List<Class<?>> handlerClasses,List<ResourceHandler> handlers) throws TemplateManagementServiceConfigurationException {
			TemplateManager manager=
				TemplateManager.
					builder().
						withHandlerClasses(handlerClasses).
						withHandlers(handlers).
						build();
			TemplateManagementService.this.updateState(manager);
		}

		@Override
		public TemplateLibrary templateLibrary() {
			throw new IllegalStateException("Template management service has not been configured yet");
		}

		@Override
		public <T extends ResourceHandler> T getHandler(Class<? extends T> handlerClass) {
			throw new IllegalStateException("Template management service has not been configured yet");
		}

	}

	private final ModelFactory resourceFactory;

	private final AtomicReference<ServiceState> state;

	private TemplateManagementService(ModelFactory resourceFactory) {
		this.resourceFactory=resourceFactory;
		this.state=new AtomicReference<ServiceState>(new NewServiceState());
	}

	private void updateState(TemplateManager manager) {
		if(!this.state.compareAndSet(this.state.get(),new ConfiguredServiceState(manager))) {
			throw new IllegalStateException("Template management service is already configured");
		}
		this.resourceFactory.useTemplates(manager.templateLibrary());
	}

	private ServiceState getState() {
		return this.state.get();
	}

	public void configure(List<Class<?>> handlerClasses, List<ResourceHandler> handlers) throws TemplateManagementServiceConfigurationException {
		getState().configure(handlerClasses, handlers);
	}

	public ResourceTemplate templateOfHandler(Class<? extends ResourceHandler> handlerClass) {
		checkNotNull(handlerClass,"Resource handler cannot be null");
		return ImmutableTemplateFactory.newImmutable(getState().templateLibrary().findByHandler(handlerClass));
	}

	public ResourceTemplate templateOfId(String templateId) {
		checkNotNull(templateId,"Template identifier cannot be null");
		return ImmutableTemplateFactory.newImmutable(getState().templateLibrary().findById(templateId));
	}

	public <T extends ResourceTemplate> T templateOfId(String templateId, Class<? extends T> templateClass) {
		checkNotNull(templateClass,"Template class cannot be null");
		ResourceTemplate found = templateOfId(templateId);
		if(found==null) {
			return null;
		} else if(!templateClass.isInstance(found)) {
			throw new IllegalArgumentException("Cannot cast template '"+templateId+"' to '"+templateClass.getCanonicalName()+"' ("+found.getClass().getCanonicalName()+")");
		}
		return templateClass.cast(found);
	}

	public <T extends ResourceHandler> T getHandler(Class<? extends T> handlerClass) {
		return getState().getHandler(handlerClass);
	}

	public static ServiceBuilder<TemplateManagementService> serviceBuilder() {
		return new TemplateManagementServiceBuilder();
	}

	public static TemplateManagementService defaultService() {
		return serviceBuilder().build();
	}

}