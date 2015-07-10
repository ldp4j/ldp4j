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
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.engine.endpoint.Endpoint;
import org.ldp4j.application.engine.endpoint.EndpointRepository;
import org.ldp4j.application.engine.resource.ResourceId;

public class JPAEndpointRepositoryTest extends AbstractJPARepositoryTest<EndpointRepository> {

	@Test
	public void testAdd() throws Exception {
		Name<String> name = NamingScheme.getDefault().name("resource");
		ResourceId resourceId = ResourceId.createId(name,"template");
		EntityTag entityTag = new EntityTag("Entity tag");
		final Endpoint ep1 = Endpoint.create("path",resourceId,new Date(), entityTag);
		withinTransaction(
			new Task<EndpointRepository>() {
				@Override
				public void execute(EndpointRepository sut) {
					sut.add(ep1);
				}
			}
		);
		clear();
		withinTransaction(
			new Task<EndpointRepository>() {
				@Override
				public void execute(EndpointRepository sut) {
					Endpoint result = sut.endpointOfPath(ep1.path());
					System.out.println(result);
				}
			}
		);
		clear();
		withinTransaction(
			new Task<EndpointRepository>() {
				@Override
				public void execute(EndpointRepository sut) {
					Endpoint result = sut.endpointOfResource(ep1.resourceId());
					System.out.println(result);
				}
			}
		);
		clear();
	}

	@Override
	protected EndpointRepository getSubjectUnderTest(JPARuntimeDelegate delegate) {
		return delegate.getEndpointRepository();
	}

}
