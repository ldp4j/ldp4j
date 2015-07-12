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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-persistency:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-persistency-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.persistence.jpa;

import java.util.Date;

import org.junit.Test;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.engine.constraints.ConstraintReport;
import org.ldp4j.application.engine.constraints.ConstraintReportId;
import org.ldp4j.application.engine.constraints.ConstraintReportRepository;
import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.engine.resource.ResourceId;

public class JPAConstraintReportRepositoryTest extends AbstractJPARepositoryTest<ConstraintReportRepository> {

	@Test
	public void testRepository() throws Exception {
		Name<String> name = NamingScheme.getDefault().name("resource");
		ResourceId resourceId = ResourceId.createId(name,"template");
		Date date = new Date();
		final HttpRequest req1 = httpRequest();
		final ConstraintReport ep1 = new JPAConstraintReport(ConstraintReportId.create(resourceId, "failure1"), new Date(date.getTime()-3600000),req1,null);
		final ConstraintReport ep2 = new JPAConstraintReport(ConstraintReportId.create(resourceId, "failure2"), date,null,null);
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