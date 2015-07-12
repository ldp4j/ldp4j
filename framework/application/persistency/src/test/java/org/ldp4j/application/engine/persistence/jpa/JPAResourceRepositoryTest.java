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

import org.junit.Test;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.engine.resource.Attachment;
import org.ldp4j.application.engine.resource.Container;
import org.ldp4j.application.engine.resource.Resource;
import org.ldp4j.application.engine.resource.ResourceId;
import org.ldp4j.application.engine.resource.ResourceRepository;
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
			new Task<ResourceRepository>() {
				@Override
				public void execute(ResourceRepository sut) {
					sut.add(resource);
					sut.add(container);
				}
			}
		);
		clear();
		withinTransaction(
			new Task<ResourceRepository>() {
				@Override
				public void execute(ResourceRepository sut) {
					Resource result1 = sut.resourceOfId(resourceId);
					System.out.println(result1);
					Resource attachment = result1.attach(PersonHandler.ADDRESS_ID,attachmentId);
					sut.add(attachment);
					Container result2 = sut.containerOfId(containerId);
					System.out.println(result2);
					result2.addSlug("test");
					result2.addSlug("anotherTest");
					Resource member = result2.addMember(memberId);
					sut.add(member);
				}
			}
		);
		clear();
		withinTransaction(
			new Task<ResourceRepository>() {
				@Override
				public void execute(ResourceRepository sut) {
					Resource result1 = sut.resourceOfId(resourceId);
					System.out.println(result1);
					Container result2 = sut.containerOfId(containerId);
					System.out.println(result2);
					Resource result3 = sut.resourceById(memberId,Resource.class);
					System.out.println(result3);
					sut.remove(result2);
					Resource result4 = sut.resourceById(attachmentId,Resource.class);
					System.out.println(result4);
					Attachment attachment = result1.findAttachment(result4.id());
					result1.detach(attachment);
					sut.remove(result4);
				}
			}
		);
		clear();
		withinTransaction(
			new Task<ResourceRepository>() {
				@Override
				public void execute(ResourceRepository sut) {
					Resource result1 = sut.resourceOfId(resourceId);
					System.out.println(result1);
					Container result2 = sut.containerOfId(containerId);
					System.out.println(result2);
					Resource result3 = sut.resourceById(memberId,Resource.class);
					System.out.println(result3);
					Resource result4 = sut.resourceById(attachmentId,Resource.class);
					System.out.println(result4);
				}
			}
		);
	}

	@Override
	protected ResourceRepository getSubjectUnderTest(JPARuntimeDelegate delegate) {
		return delegate.getResourceRepository();
	}

}
