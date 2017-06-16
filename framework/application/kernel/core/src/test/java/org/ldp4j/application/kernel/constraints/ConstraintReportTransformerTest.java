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
package org.ldp4j.application.kernel.constraints;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Date;

import org.junit.Test;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.kernel.constraints.ConstraintReport;
import org.ldp4j.application.kernel.constraints.ConstraintReportTransformer;
import org.ldp4j.application.kernel.resource.Resource;

public class ConstraintReportTransformerTest {

	@Test
	public void test() {
		Resource resource =
			TestHelper.
				createResource(
					TestHelper.name("name"),
					"template");

		ConstraintReport report=
			resource.
				addConstraintReport(
					TestHelper.constraints(),
					new Date(),
					TestHelper.httpRequest());

		ConstraintReportTransformer sut=
			ConstraintReportTransformer.create(resource,report);

		DataSet result =
			sut.transform(
				TestHelper.
					createEndpoint(
						"path",
						resource,
						new Date(),
						EntityTag.createStrong("mytag")));

		assertThat(result,notNullValue());
		assertThat(result.individualOfId(TestHelper.managedIndividualId(resource.id())),notNullValue());
		assertThat(result.individualOfId(TestHelper.name("s0")),notNullValue());
		assertThat(result.individualOfId(TestHelper.name("s1")),notNullValue());
	}

}
