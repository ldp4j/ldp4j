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
package org.ldp4j.application.impl;

import java.util.Date;

import org.ldp4j.application.ConstraintReport;
import org.ldp4j.application.ConstraintReportId;
import org.ldp4j.application.HttpRequest;
import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.resource.ResourceId;

import static com.google.common.base.Preconditions.*;

public class InMemoryConstraintReport implements ConstraintReport {

	private final ResourceId resourceId;
	private final Date date;
	private final HttpRequest request;
	private final Constraints constraints;

	private ConstraintReportId id;

	InMemoryConstraintReport(ResourceId resourceId, Date date, HttpRequest request, Constraints constraints) {
		this.resourceId = resourceId;
		this.date = date;
		this.request = request;
		this.constraints = constraints;

	}

	ResourceId resourceId() {
		// TODO Auto-generated method stub
		return null;
	}

	void setConstraintsId(String constraintsId) {
		checkNotNull(constraintsId,"Constraints identifier cannot be null");
		checkState(this.id==null,"Constraint report identifier already set");
		this.id=ConstraintReportId.create(this.resourceId, constraintsId);
	}

	@Override
	public ConstraintReportId id() {
		return this.id;
	}

	@Override
	public Date getDate() {
		return this.date;
	}

	@Override
	public HttpRequest getRequest() {
		return this.request;
	}

	@Override
	public Constraints getConstraints() {
		return this.constraints;
	}

}