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
package org.ldp4j.application.kernel.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ldp4j.application.kernel.endpoint.EndpointManagementService;
import org.ldp4j.application.kernel.lifecycle.ApplicationLifecycleService;
import org.ldp4j.application.kernel.resource.ResourceControllerService;
import org.ldp4j.application.kernel.session.WriteSessionService;
import org.ldp4j.application.kernel.spi.RuntimeDelegate;
import org.ldp4j.application.kernel.template.TemplateManagementService;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;


public final class ServiceRegistry {

	private static ServiceRegistry singleton;

	private final RuntimeDelegate delegate;
	private final ClassToInstanceMap<Service> services;
	private final Map<Class<?>,ServiceBuilder<?>> builders;

	public ServiceRegistry() {
		this.services=MutableClassToInstanceMap.<Service>create();
		this.builders=new LinkedHashMap<Class<?>, ServiceBuilder<?>>();
		this.delegate=RuntimeDelegate.getInstance();
	}

	public <T extends Service> ServiceRegistry registerService(Class<? extends T> serviceClass, T serviceInstance) {
		checkNotNull(serviceClass,"Service class cannot be null");
		checkNotNull(serviceInstance,"Service instance cannot be null");
		services.put(serviceClass, serviceInstance);
		return this;
	}

	public <T extends Service> ServiceRegistry registerServiceBuilder(ServiceBuilder<T> serviceBuilder) {
		checkNotNull(serviceBuilder,"Service builder cannot be null");
		serviceBuilder.setServiceRegistry(this);
		serviceBuilder.setRuntimeInstance(this.delegate);
		builders.put(serviceBuilder.serviceClass(), serviceBuilder);
		return this;
	}

	public <T extends Service> T getService(Class<? extends T> serviceClass) {
		checkNotNull(serviceClass,"Service class cannot be null");
		T instance = services.getInstance(serviceClass);
		if(instance==null) {
			ServiceBuilder<T> builder = serviceBuilder(serviceClass);
			if(builder!=null) {
				instance=builder.build();
				registerService(serviceClass,instance);
			}
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	public <T extends Service> ServiceBuilder<T> serviceBuilder(Class<? extends T> tartgetServiceClass) {
		checkNotNull(tartgetServiceClass,"Target service class cannot be null");
		for(Entry<Class<?>, ServiceBuilder<?>> entry:builders.entrySet()) {
			if(tartgetServiceClass==entry.getKey()) {
				return (ServiceBuilder<T>)entry.getValue();
			}
		}
		return null;
	}

	public static synchronized ServiceRegistry getInstance() {
		if(ServiceRegistry.singleton==null) {
			ServiceRegistry.singleton = new ServiceRegistry();
			initialize(ServiceRegistry.singleton);
		}
		return ServiceRegistry.singleton;
	}

	public static synchronized void setInstance(ServiceRegistry registry) {
		ServiceRegistry.singleton=registry;
	}

	private static void initialize(ServiceRegistry registry) {
		registry.
			registerServiceBuilder(
				TemplateManagementService.serviceBuilder()).
			registerServiceBuilder(
				EndpointManagementService.serviceBuilder()).
			registerServiceBuilder(
				WriteSessionService.serviceBuilder()).
			registerServiceBuilder(
				ResourceControllerService.serviceBuilder()).
			registerServiceBuilder(
				ApplicationLifecycleService.serviceBuilder());
	}

}