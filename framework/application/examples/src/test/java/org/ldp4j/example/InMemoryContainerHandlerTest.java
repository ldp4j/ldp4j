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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-examples:0.2.2
 *   Bundle      : ldp4j-application-examples-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.example;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.ApplicationRuntimeException;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;

@RunWith(JMockit.class)
public class InMemoryContainerHandlerTest {

	private static class Container extends InMemoryContainerHandler {

		protected Container() {
			super("container");
		}

		@Override
		public ResourceSnapshot create(ContainerSnapshot container,
				DataSet representation, WriteSession session) {
			return null;
		}

	}

	private Name<String> name(String id) {
		return NamingScheme.getDefault().name(id);
	}

	@Test
	public void tesNameProviderRoundtrip$happyPath(@Mocked final NameProvider provider) throws Exception {
		Container container = new Container();
		Name<String> name = name("test");
		container.addNameProvider(name, provider);
		assertThat(container.nameProvider(name),sameInstance(provider));
	}

	@Test
	public void tesNameProviderRoundtrip$exceptionPath(@Mocked final NameProvider provider) throws Exception {
		Container container = new Container();
		Name<String> name = name("test");
		container.addNameProvider(name, provider);
		Name<String> unknown = name("unknown");
		try {
			container.nameProvider(unknown);
			fail("Should not return provider for unknown resource");
		} catch(ApplicationRuntimeException e) {
			assertThat(e.getMessage(),equalTo("Unknown container 'unknown'"));
		}
	}

}
