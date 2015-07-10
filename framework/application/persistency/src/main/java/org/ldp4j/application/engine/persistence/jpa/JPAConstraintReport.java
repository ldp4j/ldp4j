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
package org.ldp4j.application.engine.persistence.jpa;

import java.util.Date;

import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.engine.constraints.ConstraintReport;
import org.ldp4j.application.engine.constraints.ConstraintReportId;
import org.ldp4j.application.engine.context.HttpRequest;

import com.google.common.base.MoreObjects;

final class JPAConstraintReport implements ConstraintReport {

	private long primaryKey;

	private ConstraintReportId id;
	private Date date;
	private HttpRequest request;
	private Constraints constraints;

	private JPAConstraintReport() {
	}

	JPAConstraintReport(ConstraintReportId id, Date date, HttpRequest request, Constraints constraints) {
		this();
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

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("primaryKey",this.primaryKey).
					add("id",this.id).
					add("date",this.date.getTime()).
					toString();
	}

}