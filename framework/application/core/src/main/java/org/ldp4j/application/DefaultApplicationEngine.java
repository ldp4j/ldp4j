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
package org.ldp4j.application;

import org.ldp4j.application.engine.ApplicationEngine;
import org.ldp4j.application.engine.ApplicationInitializationException;

public class DefaultApplicationEngine extends ApplicationEngine {

	private static final class DefaultApplicationContextManager extends ApplicationContextManager<DefaultApplicationContext> {
	
		private DefaultApplicationContextManager() {
			super(DefaultApplicationContext.class);
		}
	
		@Override
		protected DefaultApplicationContext createContext(String applicationClassName) throws ApplicationInitializationException {
			DefaultApplicationContext currentContext = DefaultApplicationContext.currentContext();
			currentContext.initialize(applicationClassName);
			return currentContext;
		}
	
		@Override
		protected void doDisposeContext(DefaultApplicationContext applicationContext) {
			applicationContext.shutdown();
		}
	}

	private static final DefaultApplicationContextManager APPLICATION_CONTEXT_MANAGER = new DefaultApplicationContextManager();

	@Override
	protected ApplicationContextManager<DefaultApplicationContext> applicationContextManager() {
		return APPLICATION_CONTEXT_MANAGER;
	}

}
