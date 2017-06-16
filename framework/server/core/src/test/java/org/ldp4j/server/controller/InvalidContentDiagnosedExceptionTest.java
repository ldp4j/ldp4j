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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

import javax.ws.rs.core.Response.Status;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.ext.InconsistentContentException;
import org.ldp4j.application.ext.InvalidContentException;

import com.google.common.base.Throwables;

@RunWith(JMockit.class)
public class InvalidContentDiagnosedExceptionTest {

	@Mocked private Constraints constraints;
	@Mocked	private OperationContext context;

	private InvalidContentException cause;

	private InvalidContentDiagnosedException sut;

	@Test
	public void testDiagnoseInconsistentContentException(@Mocked final Constraints constraints) throws Exception {
		prepareTest(new InconsistentContentException("Failure",constraints));
		assertThat(sut.getDiagnosis().statusCode(),equalTo(Status.CONFLICT.getStatusCode()));
		assertThat(sut.getDiagnosis().diagnostic(),equalTo("Specified values for application-managed properties are not consistent with the actual resource state: Failure"));
	}

	private void prepareTest(InvalidContentException failure) {
		cause = failure;
		cause.setConstraintsId("constraintId");
		new MockUp<RetrievalScenario>() {
			@Mock
			String constraintReportLink(OperationContext aContext, String constraintReportId) {
				assertThat(aContext,sameInstance(context));
				assertThat(constraintReportId,equalTo("constraintId"));
				return "link";
			}
		};
		sut=new InvalidContentDiagnosedException(context, cause);
		assertThat(sut.getConstraintReportLink(),equalTo("link"));
	}

	@Test
	public void testDiagnoseUnsupportedContentException(@Mocked final Constraints constraints) throws Exception {
		prepareTest(new org.ldp4j.application.ext.UnsupportedContentException("Failure",constraints));
		assertThat(sut.getDiagnosis().statusCode(),equalTo(MoreHttp.UNPROCESSABLE_ENTITY_STATUS_CODE));
		assertThat(sut.getDiagnosis().diagnostic(),equalTo("Could not understand content: Failure"));
	}

	@Test
	public void testDiagnoseOtherInvalidContentException(@Mocked final Constraints constraints) throws Exception {
		prepareTest(new org.ldp4j.application.ext.InvalidContentException("Failure",constraints));
		assertThat(sut.getDiagnosis().statusCode(),equalTo(Status.BAD_REQUEST.getStatusCode()));
		assertThat(sut.getDiagnosis().diagnostic(),equalTo(Throwables.getStackTraceAsString(cause)));
	}

}
