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

import org.ldp4j.application.kernel.spi.ModelFactory;
import org.ldp4j.application.kernel.spi.RuntimeDelegate;

import static com.google.common.base.Preconditions.*;

public abstract class Builder<T,B extends Builder<T,B>> {

	private ModelFactory modelFactory;
	private RuntimeDelegate runtimeDelegate;
	private ServiceRegistry serviceRegistry;

	final B setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry=checkNotNull(serviceRegistry,"Service registry cannot be null");
		return builder();
	}

	final B setRuntimeInstance(RuntimeDelegate runtimeDelegate) {
		this.runtimeDelegate = checkNotNull(runtimeDelegate,"Runtime delegate cannot be null");
		return builder();
	}

	public final B withResourceFactory(ModelFactory resourceFactory) {
		this.modelFactory=resourceFactory;
		return builder();
	}

	private final RuntimeDelegate runtimeDelegate() {
		if(this.runtimeDelegate!=null) {
			return this.runtimeDelegate;
		} else {
			return RuntimeDelegate.getInstance();
		}
	}

	private final ServiceRegistry serviceRegistry() {
		if(this.serviceRegistry!=null) {
			return this.serviceRegistry;
		} else {
			return ServiceRegistry.getInstance();
		}
	}

	protected final <S extends Service> S service(Class<? extends S> serviceClass) {
		return serviceRegistry().getService(serviceClass);
	}

	protected final ModelFactory modelFactory() {
		if(this.modelFactory==null) {
			this.modelFactory=runtimeDelegate().getModelFactory();
		}
		return this.modelFactory;
	}

	protected abstract B builder();

	public abstract T build();

}