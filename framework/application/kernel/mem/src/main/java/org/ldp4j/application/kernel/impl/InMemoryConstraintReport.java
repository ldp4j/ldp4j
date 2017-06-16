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

import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.kernel.constraints.ConstraintReport;
import org.ldp4j.application.kernel.constraints.ConstraintReportId;

final class InMemoryConstraintReport implements ConstraintReport {

	private final ConstraintReportId id;
	private final Date date;
	private final HttpRequest request;
	private final Constraints constraints;

	InMemoryConstraintReport(ConstraintReportId id, Date date, HttpRequest request, Constraints constraints) {
		this.id=id;
		this.date = date;
		this.request = request;
		this.constraints = constraints;
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