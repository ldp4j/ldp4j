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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-api:0.2.2
 *   Bundle      : ldp4j-application-kernel-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.resource;

import java.net.URI;
import java.util.Date;
import java.util.Set;

import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.kernel.constraints.ConstraintReport;
import org.ldp4j.application.kernel.constraints.ConstraintReportId;

/**
 * TODO: Update API to enable using Individual identifiers instead of plain
 * URIs when dealing with indirect identifiers.
 */
public interface Resource {

	ResourceId id();

	void setIndirectId(URI indirectId);

	URI indirectId();

	boolean isRoot();

	ResourceId parentId();

	Attachment findAttachment(ResourceId resourceId);

	Resource attach(String attachmentId, ResourceId resourceId);

	<T extends Resource> T attach(String attachmentId, ResourceId resourceId, Class<? extends T> clazz);

	boolean detach(Attachment attachment);

	Set<? extends Attachment> attachments();

	void accept(ResourceVisitor visitor);

	ConstraintReport addConstraintReport(Constraints constraints, Date date, HttpRequest request);

	Set<ConstraintReportId> constraintReports();

	void removeFailure(ConstraintReport report);

}