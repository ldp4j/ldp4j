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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-jpa:0.2.2
 *   Bundle      : ldp4j-application-kernel-jpa-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.persistence.jpa;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;
import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.kernel.constraints.ConstraintReport;
import org.ldp4j.application.kernel.constraints.ConstraintReportId;
import org.ldp4j.application.kernel.constraints.ConstraintReportRepository;
import org.ldp4j.application.kernel.persistence.encoding.SerializationUtils;
import org.ldp4j.application.kernel.persistence.jpa.JPAConstraintReport;
import org.ldp4j.application.kernel.persistence.jpa.JPAResource;
import org.ldp4j.application.kernel.persistence.jpa.JPARuntimeDelegate;
import org.ldp4j.application.kernel.resource.ResourceId;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class JPAConstraintReportRepositoryTest extends AbstractJPARepositoryTest<ConstraintReportRepository> {

	@Test
	public void testSerializability() throws IOException {
		Constraints constraints = constraints();
		byte[] serialize = SerializationUtils.serialize(constraints);
		Constraints deserialize = SerializationUtils.deserialize(serialize,Constraints.class);
		assertThat(constraints.toString(),equalTo(deserialize.toString()));
	}

	@Test
	public void testRepository() throws Exception {
		ResourceId resourceId = ResourceId.createId(name("resource"),"template");
		Date date = new Date();
		final ConstraintReport ep1 = new JPAConstraintReport(ConstraintReportId.create(resourceId, "failure1"), new Date(date.getTime()-3600000),httpRequest(),constraints());
		final ConstraintReport ep2 = new JPAConstraintReport(ConstraintReportId.create(resourceId, "failure2"), date,httpRequest(),null);
		withinTransaction(
			new Task<ConstraintReportRepository>("Creating reports") {
				@Override
				public void execute(ConstraintReportRepository sut) {
					sut.add(ep1);
					sut.add(ep2);
				}
			}
		);
		clear();
		withinTransaction(
			new Task<ConstraintReportRepository>("Retrieving reports by id") {
				@Override
				public void execute(ConstraintReportRepository sut) {
					ConstraintReport result1 = sut.constraintReportOfId(ep1.id());
					System.out.println(result1);
					ConstraintReport result2 = sut.constraintReportOfId(ep2.id());
					System.out.println(result2);
				}
			}
		);
		clear();
		withinTransaction(
			new Task<ConstraintReportRepository>("Removing reports by resource") {
				@Override
				public void execute(ConstraintReportRepository sut) {
					sut.removeByResource(new JPAResource(ep1.id().resourceId()));
					ConstraintReport result1 = sut.constraintReportOfId(ep1.id());
					System.out.println(result1);
					ConstraintReport result2 = sut.constraintReportOfId(ep2.id());
					System.out.println(result2);
				}
			}
		);
	}

	@Override
	protected ConstraintReportRepository getSubjectUnderTest(JPARuntimeDelegate delegate) {
		return delegate.getConstraintReportRepository();
	}

}
