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

import org.ldp4j.application.ext.Parameter;
import org.ldp4j.application.ext.Query;

enum RetrievalScenario {
	RESOURCE_RETRIEVAL,
	CONSTRAINT_REPORT_RETRIEVAL,
	MIXED_QUERY,
	QUERY_NOT_SUPPORTED,
	;

	static final String CONSTRAINT_QUERY_PARAMETER = "ldp:constrainedBy";

	static RetrievalScenario forContext(OperationContext context) {
		Query query=context.getQuery();
		RetrievalScenario result=RESOURCE_RETRIEVAL;
		if(!query.isEmpty()) {
			if(query.hasParameter(CONSTRAINT_QUERY_PARAMETER)) {
				if(query.size()==1) {
					result=CONSTRAINT_REPORT_RETRIEVAL;
				} else {
					result=MIXED_QUERY;
				}
			} else if(!context.isResourceQueryable()) {
				result=QUERY_NOT_SUPPORTED;
			}
		}
		return result;
	}

	static Parameter constraintReportId(OperationContext context) {
		return context.getQuery().getParameter(CONSTRAINT_QUERY_PARAMETER);
	}

	static String constraintReportLink(OperationContext context, String constraintReportId) {
		return context.base()+context.path()+"?"+CONSTRAINT_QUERY_PARAMETER+"="+constraintReportId;
	}

}