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
package org.ldp4j.application.ext;

import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.setup.Bootstrap;
import org.ldp4j.application.setup.Environment;

/**
 * The entry point for an LDP4j Application. This class provides the means for
 * controlling the life-cycle of the application: the bootstrapping of the
 * application and the application termination.
 */
public abstract class Application<T extends Configuration> {

	/**
	 * Returns the {@link Class} of the configuration class type parameter.
	 *
	 * @return the configuration class
	 * @see Generics#getTypeParameter(Class, Class)
	 */
	public final Class<T> getConfigurationClass() {
		return Generics.getTypeParameter(getClass(), Configuration.class);
	}

	/**
	 * Return the name of the application.
	 *
	 * @return the application's name
	 */
	public String getName() {
		return getClass().getSimpleName();
	}

	/**
	 * Setup the application. <br>
	 *
	 * The implementation class will have to register the resource handler
	 * instances and classes in the {@code Bootstrap} instance, and publish
	 * the root resources for the application in the {@code Environment} instance.
	 *
	 * @param environment
	 *            the environment for publishing root resources.
	 * @param bootstrap
	 *            the bootstrap for registering resource handler instances and
	 *            classes.
	 * @throws ApplicationSetupException
	 *             if any failure prevented setting up the application.
	 */
	public abstract void setup(Environment environment, Bootstrap<T> bootstrap) throws ApplicationSetupException;

	/**
	 * Initialize the application. <br>
	 *
	 * The implementation class may synchronize its internal state with the
	 * state of the resources already managed by the LDP4j framework.
	 *
	 * @param session
	 *            the session to use for controlling the lifecycle of the
	 *            resources managed by the framework.
	 * @throws ApplicationInitializationException
	 *             if any failure prevented the initialization of the
	 *             application.
	 */
	public abstract void initialize(WriteSession session) throws ApplicationInitializationException;

	/**
	 * Shutdown the application. <br>
	 *
	 * The implementation class should carry out any termination activity
	 * required before shutting down the application.
	 *
	 * @throws ApplicationShutdownException
	 *             if any failure prevented the graceful termination of the
	 *             application.
	 */
	public abstract void shutdown() throws ApplicationShutdownException;

}