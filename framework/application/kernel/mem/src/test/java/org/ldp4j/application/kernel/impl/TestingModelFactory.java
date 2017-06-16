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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-mem:0.2.2
 *   Bundle      : ldp4j-application-kernel-mem-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.impl;

import java.util.Date;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.impl.InMemoryContainer;
import org.ldp4j.application.kernel.impl.InMemoryEndpoint;
import org.ldp4j.application.kernel.impl.InMemoryResource;
import org.ldp4j.application.kernel.resource.Container;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;

public final class TestingModelFactory {

	private TestingModelFactory() {
	}

	public static Endpoint createEndpoint(String path, Resource resource, Date created, EntityTag entityTag) {
		return InMemoryEndpoint.create(path, resource.id(), created, entityTag);
	}

	public static Resource createResource(Name<?> name, String templateId) {
		return new InMemoryResource(ResourceId.createId(name, templateId));
	}

	public static Container createContainer(Name<?> name, String templateId) {
		return new InMemoryContainer(ResourceId.createId(name, templateId));
	}

}
