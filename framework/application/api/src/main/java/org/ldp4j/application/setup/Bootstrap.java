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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.setup;

import org.ldp4j.application.ext.Configuration;
import org.ldp4j.application.ext.ResourceHandler;

/**
 * Utility interface for bootstrapping an LDP4j Application. Using this class
 * the application will be able to register the {@code ResourceHandler}
 * instances and classes that make up the application. <br> <br>
 *
 * Upon registration, the <b>LDP4j Application Engine</b> will analyze the
 * templates defined by the resource handlers, discovering any additional
 * resource handler not explicitly registered.
 *
 * @param <T>
 *            The configuration class required by the application that is to be
 *            bootstrapped.
 */
public interface Bootstrap<T extends Configuration> {

	/**
	 * Retrieve the configuration of the application. The returned value will
	 * never be null.
	 *
	 * @return an instance of the configuration.
	 */
	T configuration();

	/**
	 * Register an initialized {@code ResourceHandler} instance of the
	 * application. <br>
	 * <br>
	 *
	 * This method is to be used if and only if the resource handler requires a
	 * to be initialized in a controller manner. Otherwise, the resource handler
	 * class can be registered using the {@link #addHandlerClass(Class)} method.
	 *
	 * @param handler
	 *            the handler instance.
	 * @throws NullPointerException
	 *             if the handler instance is {@code null}.
	 */
	void addHandler(ResourceHandler handler);

	/**
	 * Register a {@code ResourceHandler} class that is used by the application. <br>
	 * <br>
	 *
	 * This method is to be used if and only if resource handler instances do
	 * not require any special initialization. Otherwise, the an initialized
	 * resource handler instance can be registered using the
	 * {@link #addHandler(ResourceHandler)} method.
	 *
	 * @param handlerClass
	 *            the handler class.
	 * @throws NullPointerException
	 *             if the handler class is {@code null}.
	 */
	void addHandlerClass(Class<? extends ResourceHandler> handlerClass);

}