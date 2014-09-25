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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-application:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-application-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.spi;

import org.ldp4j.application.endpoint.EndpointFactoryService;
import org.ldp4j.application.endpoint.EndpointManagementService;
import org.ldp4j.application.resource.ResourceControllerService;
import org.ldp4j.application.resource.ResourceFactoryService;
import org.ldp4j.application.session.WriteSessionService;
import org.ldp4j.application.template.TemplateManagementService;

public abstract class RuntimeInstance {

	private static RuntimeInstance INSTANCE=null;

	public static synchronized void setInstance(RuntimeInstance instance) {
		if(RuntimeInstance.INSTANCE!=null && instance!=null) {
			throw new IllegalStateException("RuntimeInstance has already been initialized");
		}
		RuntimeInstance.INSTANCE=instance;
		if(RuntimeInstance.INSTANCE!=null) {
			RuntimeInstance.INSTANCE.initialize();
		}
	}
	
	public static synchronized RuntimeInstance getInstance() {
		if(RuntimeInstance.INSTANCE==null) {
			throw new IllegalStateException("RuntimeInstance not initialized yet");
		}
		return RuntimeInstance.INSTANCE;
	}
	
	private void initialize() {
		getServiceRegistry().
			registerServiceBuilder(
				EndpointFactoryService.
					serviceBuilder().
						setRuntimeInstance(this)).
			registerServiceBuilder(
				ResourceFactoryService.
					serviceBuilder().
						setRuntimeInstance(this)).
			registerServiceBuilder(
				EndpointManagementService.
					serviceBuilder().
						setRuntimeInstance(this)).
			registerServiceBuilder(
				WriteSessionService.
					serviceBuilder().
						setRuntimeInstance(this)).
			registerServiceBuilder(
				TemplateManagementService.
					serviceBuilder().
						setRuntimeInstance(this)).
			registerServiceBuilder(
				ResourceControllerService.
					serviceBuilder().
						setRuntimeInstance(this));
	}
	
	public abstract RepositoryRegistry getRepositoryRegistry();

	public abstract ServiceRegistry getServiceRegistry();

	public abstract ResourceHandlerRegistry getResourceHandlerRegistry();
	
}
