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

import java.net.URI;
import java.util.Date;
import java.util.Set;

import org.junit.Test;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.engine.constraints.ConstraintReport;
import org.ldp4j.application.engine.constraints.ConstraintReportId;
import org.ldp4j.application.engine.constraints.ConstraintReportRepository;
import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.engine.context.HttpRequest.HttpMethod;
import org.ldp4j.application.engine.resource.Attachment;
import org.ldp4j.application.engine.resource.Resource;
import org.ldp4j.application.engine.resource.ResourceId;
import org.ldp4j.application.engine.resource.ResourceVisitor;
import org.ldp4j.application.sdk.HttpRequestBuilder;

public class JPAConstraintReportRepositoryTest extends AbstractJPARepositoryTest<ConstraintReportRepository> {

	@Test
	public void testRepository() throws Exception {
		Name<String> name = NamingScheme.getDefault().name("resource");
		ResourceId resourceId = ResourceId.createId(name,"template");
		Date date = new Date();
		final HttpRequest req1=
			HttpRequestBuilder.
				newInstance().
					withMethod(HttpMethod.POST).
					withHost("www.example.org").
					withAbsolutePath("service/resource/").
					withEntity("body").
					build();
		final ConstraintReport ep1 = new JPAConstraintReport(ConstraintReportId.create(resourceId, "failure1"), new Date(date.getTime()-3600000),req1,null);
		final ConstraintReport ep2 = new JPAConstraintReport(ConstraintReportId.create(resourceId, "failure2"), date,null,null);
		withinTransaction(
			new Task<ConstraintReportRepository>() {
				@Override
				public void execute(ConstraintReportRepository sut) {
					sut.add(ep1);
					sut.add(ep2);
				}
			}
		);
		clear();
		withinTransaction(
			new Task<ConstraintReportRepository>() {
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
			new Task<ConstraintReportRepository>() {
				@Override
				public void execute(ConstraintReportRepository sut) {
					sut.removeByResource(new Resource() {

						@Override
						public ResourceId id() {
							return ep1.id().resourceId();
						}

						@Override
						public void setIndirectId(URI indirectId) {
							// TODO Auto-generated method stub

						}

						@Override
						public URI indirectId() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public boolean isRoot() {
							// TODO Auto-generated method stub
							return false;
						}

						@Override
						public ResourceId parentId() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Attachment findAttachment(ResourceId resourceId) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Resource attach(String attachmentId,
								ResourceId resourceId) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public <T extends Resource> T attach(
								String attachmentId, ResourceId resourceId,
								Class<? extends T> clazz) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public boolean detach(Attachment attachment) {
							// TODO Auto-generated method stub
							return false;
						}

						@Override
						public Set<? extends Attachment> attachments() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public void accept(ResourceVisitor visitor) {
							// TODO Auto-generated method stub

						}

						@Override
						public ConstraintReport addConstraintReport(
								Constraints constraints, Date date,
								HttpRequest request) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Set<ConstraintReportId> constraintReports() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public void removeFailure(ConstraintReport report) {
							// TODO Auto-generated method stub

						}

					});
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
