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

import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.endpoint.EndpointRepository;
import org.ldp4j.application.kernel.persistence.jpa.JPARuntimeDelegate;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.example.PersonHandler;

public class JPAEndpointRepositoryTest extends AbstractJPARepositoryTest<EndpointRepository> {

	@Rule
	public TestName name=new TestName();

	private Name<String> resourceName() {
		return NamingScheme.getDefault().name(this.name.getMethodName());
	}

	@Test
	public void testRepository() throws Exception {
		Resource resource = rootResource(resourceName(),PersonHandler.ID);
		final Endpoint ep1 = super.endpoint("path",resource);
		withinTransaction(
			new Task<EndpointRepository>("Creating endpoint") {
				@Override
				public void execute(EndpointRepository sut) {
					sut.add(ep1);
				}
			}
		);
		clear();
		withinTransaction(
			new Task<EndpointRepository>("Finding endpoint by path") {
				@Override
				public void execute(EndpointRepository sut) {
					Endpoint result = sut.endpointOfPath(ep1.path());
					System.out.println(result);
				}
			}
		);
		clear();
		withinTransaction(
			new Task<EndpointRepository>("Finding endpoint by resource id") {
				@Override
				public void execute(EndpointRepository sut) {
					Endpoint result = sut.endpointOfResource(ep1.resourceId());
					System.out.println(result);
				}
			}
		);
		clear();
	}

	@Test
	public void testUniqueResourceId() throws Exception {
		Resource resource = rootResource(resourceName(),PersonHandler.ID);
		final Endpoint ep1 = super.endpoint("path1",resource);
		final Endpoint ep2 = super.endpoint("path2",resource);
		withinTransaction(
			new Task<EndpointRepository>("Creating first endpoint") {
				@Override
				public void execute(EndpointRepository sut) {
					sut.add(ep1);
				}
			}
		);
		clear();
		try {
			withinTransaction(
				new Task<EndpointRepository>("Creating endpoint with for the same resource") {
					@Override
					public void execute(EndpointRepository sut) {
						sut.add(ep2);
					}
				}
			);
			fail("Should not allowing storing multiple endpoints for the same resource");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResourceDeletion() throws Exception {
		final Resource resource = rootResource(resourceName(),PersonHandler.ID);
		final Endpoint ep1 = super.endpoint("path3",resource);
		final Date deleted = new Date(ep1.created().getTime()+1000);
		withinTransaction(
			new Task<EndpointRepository>("Creating first endpoint") {
				@Override
				public void execute(EndpointRepository sut) {
					sut.add(ep1);
				}
			}
		);
		clear();
		withinTransaction(
			new Task<EndpointRepository>("Delete endpoint") {
				@Override
				public void execute(EndpointRepository sut) {
					Endpoint endpoint = sut.endpointOfPath(ep1.path());
					endpoint.delete(deleted);
				}
			}
		);
		withinTransaction(
			new Task<EndpointRepository>("Check deletion") {
				@Override
				public void execute(EndpointRepository sut) {
					Endpoint endpointByPath = sut.endpointOfPath(ep1.path());
					debug("Retrieving endpoint by path {%s} : %s",ep1.path(),endpointByPath);
					Endpoint endpointByResource = sut.endpointOfResource(resource.id());
					debug("Retrieving endpoint by resource {%s} : %s",resource.id(),endpointByResource);
				}
			}
		);
		final String path = "path4";
		withinTransaction(
			new Task<EndpointRepository>("Create new endoint for 'same' resource") {
				@Override
				public void execute(EndpointRepository sut) {
					Endpoint newEndpoint = endpoint(path,resource);
					sut.add(newEndpoint);
					debug("Adding endpoint: %s",newEndpoint);
				}
			}
		);
		withinTransaction(
			new Task<EndpointRepository>("Check new resource") {
				@Override
				public void execute(EndpointRepository sut) {
					Endpoint legacyEndpointByPath = sut.endpointOfPath(ep1.path());
					debug("Retrieving endpoint by path {%s} : %s",ep1.path(),legacyEndpointByPath);
					Endpoint endpointByPath = sut.endpointOfPath(path);
					debug("Retrieving endpoint by path {%s} : %s",path,endpointByPath);
					Endpoint endpointByResource = sut.endpointOfResource(resource.id());
					debug("Retrieving endpoint by resource {%s} : %s",resource.id(),endpointByResource);
				}
			}
		);
	}

	@Override
	protected EndpointRepository getSubjectUnderTest(JPARuntimeDelegate delegate) {
		return delegate.getEndpointRepository();
	}

}
