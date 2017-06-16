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
package org.ldp4j.application.kernel.resource;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.kernel.resource.Container;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.service.Service;
import org.ldp4j.application.kernel.service.ServiceBuilder;
import org.ldp4j.application.kernel.session.WriteSessionConfiguration;
import org.ldp4j.application.kernel.session.WriteSessionService;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.TemplateManagementService;

public class ResourceControllerService implements Service {

	private static final class ResourceControllerServiceBuilder extends ServiceBuilder<ResourceControllerService> {

		private ResourceControllerServiceBuilder() {
			super(ResourceControllerService.class);
		}

		@Override
		public ResourceControllerService build() {
			return
				new ResourceControllerService(
					service(WriteSessionService.class),
					service(TemplateManagementService.class));
		}

	}

	private final WriteSessionService writeSessionService;
	private final TemplateManagementService templateManagementService;

	private ResourceControllerService(WriteSessionService writeSessionService, TemplateManagementService templateManagementService) {
		this.writeSessionService=writeSessionService;
		this.templateManagementService = templateManagementService;
	}

	private <T extends Resource> Adapter adapter(T resource, WriteSessionConfiguration configuration) {
		ResourceTemplate template=this.templateManagementService.templateOfId(resource.id().templateId());
		Class<? extends ResourceHandler> handlerClass = template.handlerClass();
		ResourceHandler delegate=this.templateManagementService.getHandler(handlerClass);
		return AdapterFactory.newAdapter(resource,delegate,this.writeSessionService,configuration);
	}

	public DataSet getResource(Resource resource, WriteSessionConfiguration configuration) throws FeatureException {
		return adapter(resource, configuration).get();
	}

	public DataSet queryResource(Resource resource, Query query, WriteSessionConfiguration configuration) throws FeatureException {
		return adapter(resource, configuration).query(query);
	}

	public void updateResource(Resource resource, DataSet dataSet, WriteSessionConfiguration configuration) throws FeatureException {
		adapter(resource, configuration).update(dataSet);
	}

	public void deleteResource(Resource resource, WriteSessionConfiguration configuration) throws FeatureException {
		adapter(resource, configuration).delete();
	}

	public Resource createResource(Container container, DataSet dataSet, WriteSessionConfiguration configuration) throws FeatureException {
		return adapter(container,configuration).create(dataSet);
	}

	public static ServiceBuilder<ResourceControllerService> serviceBuilder() {
		return new ResourceControllerServiceBuilder();
	}

	public static ResourceControllerService defaultService() {
		return serviceBuilder().build();
	}

}