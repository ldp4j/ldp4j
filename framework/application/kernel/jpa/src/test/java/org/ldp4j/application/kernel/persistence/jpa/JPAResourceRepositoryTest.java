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

import java.util.Date;

import org.junit.Test;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.kernel.constraints.ConstraintReport;
import org.ldp4j.application.kernel.persistence.jpa.JPARuntimeDelegate;
import org.ldp4j.application.kernel.resource.Attachment;
import org.ldp4j.application.kernel.resource.Container;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.resource.ResourceRepository;
import org.ldp4j.application.kernel.resource.Slug;
import org.ldp4j.example.AddressHandler;
import org.ldp4j.example.PersonContainerHandler;
import org.ldp4j.example.PersonHandler;

public class JPAResourceRepositoryTest extends AbstractJPARepositoryTest<ResourceRepository> {

	@Test
	public void testRepository() throws Exception {
		final Name<String> resourceName = NamingScheme.getDefault().name("resource");
		final ResourceId resourceId = ResourceId.createId(resourceName,PersonHandler.ID);
		final Name<String> containerName = NamingScheme.getDefault().name("container");
		final ResourceId containerId = ResourceId.createId(containerName,PersonContainerHandler.ID);
		final Resource resource = rootResource(resourceName, PersonHandler.ID);
		final Container container = rootContainer(containerName, PersonContainerHandler.ID);
		final ResourceId memberId = ResourceId.createId(NamingScheme.getDefault().name("member"), PersonHandler.ID);
		final ResourceId attachmentId = ResourceId.createId(NamingScheme.getDefault().name("attachment"),AddressHandler.ID);
		withinTransaction(
			new Task<ResourceRepository>("Creating root resources") {
				@Override
				public void execute(ResourceRepository sut) {
					sut.add(resource);
					sut.add(container);
				}
			}
		);
		clear();
		withinTransaction(
			new Task<ResourceRepository>("Adding attachments, slugs, and members") {
				@Override
				public void execute(ResourceRepository sut) {
					Resource result1 = sut.resourceOfId(resourceId);
					debug("Retrieving resource {%s}: %s",resourceId,result1);
					Resource attachment = result1.attach(PersonHandler.ADDRESS_ID,attachmentId);
					sut.add(attachment);
					debug("Created attachment: %s",attachment);
					Container result2 = sut.containerOfId(containerId);
					debug("Retrieving container {%s}: %s",containerId,result2);
					Slug slug1=result2.addSlug("test");
					debug("Created slug: %s",slug1);
					Slug slug2=result2.addSlug("anotherTest");
					debug("Created slug: %s",slug2);
					Resource member = result2.addMember(memberId);
					sut.add(member);
					debug("Created member: %s",member);
				}
			}
		);
		clear();
		withinTransaction(
			new Task<ResourceRepository>("Removing attachments and members") {
				@Override
				public void execute(ResourceRepository sut) {
					Resource result1 = sut.resourceOfId(resourceId);
					debug("Retrieving resource {%s}: %s",resourceId,result1);
					Container result2 = sut.containerOfId(containerId);
					debug("Retrieving container {%s}: %s",containerId,result2);
					Resource result3 = sut.resourceById(memberId,Resource.class);
					debug("Retrieving member resource {%s}: %s",memberId,result3);
					sut.remove(result2);
					debug("Deleted resource {%s}",result2.id());
					Resource result4 = sut.resourceById(attachmentId,Resource.class);
					debug("Retrieving attached resource {%s}: %s",attachmentId,result4);
					Attachment attachment = result1.findAttachment(result4.id());
					debug("Retrieving attachment {%s}: %s",result4.id(),attachment);
					result1.detach(attachment);
					debug("Detached resource {%s}",attachment.id());
					sut.remove(result4);
					debug("Deleted resource {%s}",result4.id());
				}
			}
		);
		clear();
		withinTransaction(
			new Task<ResourceRepository>("Adding constraint report") {
				@Override
				public void execute(ResourceRepository sut) {
					Resource result1 = sut.resourceOfId(resourceId);
					debug("Retrieving resource {%s}: %s",resourceId,result1);
					Container result2 = sut.containerOfId(containerId);
					debug("Retrieving container {%s}: %s",containerId,result2);
					Resource result3 = sut.resourceById(memberId,Resource.class);
					debug("Retrieving member resource {%s}: %s",memberId,result3);
					Resource result4 = sut.resourceById(attachmentId,Resource.class);
					debug("Retrieving attached resource {%s}: %s",attachmentId,result4);
					ConstraintReport report = result1.addConstraintReport(null,new Date(),httpRequest());
					debug("Created report {%s}",report.id());
				}
			}
		);
		clear();
		withinTransaction(
			new Task<ResourceRepository>("Checking final state") {
				@Override
				public void execute(ResourceRepository sut) {
					Resource result1 = sut.resourceOfId(resourceId);
					debug("Retrieving resource {%s}: %s",resourceId,result1);
					Container result2 = sut.containerOfId(containerId);
					debug("Retrieving container {%s}: %s",containerId,result2);
					Resource result3 = sut.resourceById(memberId,Resource.class);
					debug("Retrieving member resource {%s}: %s",memberId,result3);
				}
			}
		);
	}

	@Override
	protected ResourceRepository getSubjectUnderTest(JPARuntimeDelegate delegate) {
		return delegate.getResourceRepository();
	}

}
