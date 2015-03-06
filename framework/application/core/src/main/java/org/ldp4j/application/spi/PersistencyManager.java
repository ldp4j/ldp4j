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
package org.ldp4j.application.spi;

import java.util.Date;

import org.ldp4j.application.ConstraintReport;
import org.ldp4j.application.ConstraintReportId;
import org.ldp4j.application.HttpRequest;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.data.validation.ValidationReport;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.resource.Container;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.template.ResourceTemplate;
import org.ldp4j.application.template.TemplateLibrary;

public interface PersistencyManager {

	Transaction currentTransaction();

	ResourceTemplate registerHandler(Class<?> clazz) throws TemplateCreationException;

	boolean isHandlerRegistered(Class<?> handlerClass);

	TemplateLibrary exportTemplates();

	Endpoint createEndpoint(Resource resource, String path, EntityTag entityTag, Date lastModified);

	Resource createResource(String templateId, Name<?> resourceId, Resource parent);

	<T extends Resource> T createResource(String templateId, Name<?> resourceId, Resource parent, Class<? extends T> expectedResourceClass);

	void add(Endpoint endpoint);

	void add(Resource resource);

	void remove(Endpoint endpoint);

	void remove(Resource resource);

	Endpoint endpointOfPath(String path);

	Endpoint endpointOfResource(ResourceId id);

	<T extends Resource> T resourceOfId(ResourceId id, Class<? extends T> expectedResourceClass);

	Resource resourceOfId(ResourceId id);

	Container containerOfId(ResourceId id);

	ResourceTemplate templateOfHandler(Class<? extends ResourceHandler> handlerClass);

	ResourceTemplate templateOfId(String templateId);

	<T extends ResourceTemplate> T templateOfId(String templateId, Class<? extends T> templateClass);

	@Deprecated
	String add(Resource resource, ValidationReport report);

	@Deprecated
	ValidationReport failureOfResource(Resource resource, String failureId);

	ConstraintReport createConstraintReport(Resource resource, Constraints constraints, Date date, HttpRequest request);

	void add(ConstraintReport report);

	ConstraintReport constraintReportOfId(ConstraintReportId id);
}
