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
package org.ldp4j.application.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ldp4j.application.spi.Service;
import org.ldp4j.application.spi.ServiceBuilder;
import org.ldp4j.application.spi.ServiceRegistry;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

final class InMemoryServiceRegistry implements ServiceRegistry {

	private ClassToInstanceMap<Service> services=MutableClassToInstanceMap.<Service>create();
	private Map<Class<?>,ServiceBuilder<?>> builders=new LinkedHashMap<Class<?>, ServiceBuilder<?>>();

	InMemoryServiceRegistry() {
		super();
	}

	@Override
	public <T extends Service> ServiceRegistry registerService(Class<? extends T> serviceClass, T serviceInstance) {
		checkNotNull(serviceClass,"Service class cannot be null");
		checkNotNull(serviceInstance,"Service instance cannot be null");
		services.put(serviceClass, serviceInstance);
		return this;
	}

	@Override
	public <T extends Service> ServiceRegistry registerServiceBuilder(ServiceBuilder<T> serviceBuilder) {
		checkNotNull(serviceBuilder,"Service builder cannot be null");
		builders.put(serviceBuilder.serviceClass(), serviceBuilder);
		return this;
	}

	@Override
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

	@Override
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

}