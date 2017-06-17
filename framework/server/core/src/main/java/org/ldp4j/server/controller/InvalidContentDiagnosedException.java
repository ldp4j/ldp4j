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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import javax.ws.rs.core.Response.Status;

import org.ldp4j.application.ext.InconsistentContentException;
import org.ldp4j.application.ext.InvalidContentException;
import org.ldp4j.application.ext.UnsupportedContentException;

public class InvalidContentDiagnosedException extends DiagnosedException {

	private static final long serialVersionUID = -3891626555696814109L;

	private final String constraintReportLink;

	public InvalidContentDiagnosedException(OperationContext context, InvalidContentException cause) {
		super(context,cause,diagnose(cause));
		this.constraintReportLink =
			RetrievalScenario.
				constraintReportLink(
					context,
					cause.getConstraintsId());
	}

	public String getConstraintReportLink() {
		return this.constraintReportLink;
	}

	private static Diagnosis diagnose(InvalidContentException rootCause) {
		Diagnosis diagnosis = Diagnosis.create(rootCause);
		if(rootCause instanceof InconsistentContentException) {
			diagnosis=
				diagnosis.
					statusCode(Status.CONFLICT).
					diagnostic("Specified values for application-managed properties are not consistent with the actual resource state: %s",rootCause.getMessage());
		} else if(rootCause instanceof UnsupportedContentException) {
			diagnosis=
				diagnosis.
					statusCode(MoreHttp.UNPROCESSABLE_ENTITY_STATUS_CODE).
					diagnostic("Could not understand content: %s",rootCause.getMessage());
		} else {
			diagnosis=diagnosis.statusCode(Status.BAD_REQUEST);
		}
		return diagnosis;
	}

}