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

import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.lifecycle.LifecycleEnvironment;

/**
 * Utility interface for initializing the root resources of an LDP4j
 * Application. Using this class the application will be able to define the
 * resources that will be published by default using a predefined path, and
 * which {@code ResourceHandler} will handle the requests sent to the specified
 * endpoint.
 */
public interface Environment {

	/**
	 * Publish a resource managed by a particular handler class at a given path.
	 *
	 * @param resourceName
	 *            The name of the resource that is to be published.
	 * @param handlerClass
	 *            The class of the handler that will manage the resource
	 *            behind the endpoint.
	 * @param path
	 *            The path used to publish the resource. The path must end with
	 *            a single '/'.
	 */
	void publishResource(Name<?> resourceName, Class<? extends ResourceHandler> handlerClass, String path);

	/**
	 * Retrieve the Application's {@link LifecycleEnvironment}.
	 *
	 * @return the Application's {@link LifecycleEnvironment}.
	 */
	LifecycleEnvironment lifecycle();

}
