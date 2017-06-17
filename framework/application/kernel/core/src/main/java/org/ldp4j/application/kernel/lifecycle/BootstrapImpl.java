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

import java.util.List;

import org.ldp4j.application.engine.ApplicationConfigurationException;
import org.ldp4j.application.ext.Configuration;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.kernel.template.TemplateManagementService;
import org.ldp4j.application.kernel.template.TemplateManagementServiceConfigurationException;
import org.ldp4j.application.setup.Bootstrap;

import com.google.common.collect.Lists;

final class BootstrapImpl<T extends Configuration> implements Bootstrap<T> {

	private final T configuration;
	private final List<ResourceHandler> handlers;
	private final List<Class<?>> handlerClasses;
	private final TemplateManagementService templateManagementService;

	BootstrapImpl(T configuration, TemplateManagementService templateManagementService) {
		this.configuration = configuration;
		this.templateManagementService = templateManagementService;
		this.handlers=Lists.newArrayList();
		this.handlerClasses=Lists.newArrayList();
	}

	@Override
	public T configuration() {
		return configuration;
	}

	@Override
	public void addHandler(ResourceHandler handler) {
		checkNotNull(handler,"Resource handler cannot be null");
		this.handlers.add(handler);
	}

	@Override
	public void addHandlerClass(Class<? extends ResourceHandler> handlerClass) {
		checkNotNull(handlerClass,"Resource handler class cannot be null");
		this.handlerClasses.add(handlerClass);
	}

	void configureTemplates() throws ApplicationConfigurationException {
		try {
			this.templateManagementService.configure(this.handlerClasses, this.handlers);
		} catch (TemplateManagementServiceConfigurationException e) {
			throw new ApplicationConfigurationException("Invalid application resource handler configuration",e);
		}
	}

}