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
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.UnknownResourceException;
import org.ldp4j.application.session.ResourceSnapshot;

@RunWith(JMockit.class)
public class InMemoryResourceHandlerTest {

	private static class Resource extends InMemoryResourceHandler {

		protected Resource() {
			super("resource");
		}

	}

	private Name<String> name(String id) {
		return NamingScheme.getDefault().name(id);
	}


	@Test
	public void testCreation() throws Exception {
		Resource sut=new Resource();
		assertThat(sut.size(),equalTo(0));
		assertThat(sut.toString(),equalTo("resource"));
	}

	@Test
	public void testResourceLifecycle(@Mocked final ResourceSnapshot s1, @Mocked final ResourceSnapshot s2, @Mocked final DataSet r1, @Mocked final DataSet r2) throws Exception {
		final Name<String> n1 = name("1");
		final Name<String> n2 = name("2");

		new Expectations() {{
			s1.name();result=n1;
			s2.name();result=n2;
		}};

		Resource sut=new Resource();

		sut.add(n1,r1);
		sut.add(n2,r2);

		assertThat(sut.size(),equalTo(2));
		assertThat(sut.get(s1),sameInstance(r1));
		assertThat(sut.get(s2),sameInstance(r2));

		sut.remove(n2);
		assertThat(sut.size(),equalTo(1));
		try {
			sut.get(s2);
			fail("Resource 2 should not be available");
		} catch (UnknownResourceException e) {
		}

		sut.update(n1, r2);
		assertThat(sut.size(),equalTo(1));
		assertThat(sut.get(s1),sameInstance(r2));

	}

	@Test
	public void testUtilityMethods(@Mocked final DataSet r1, @Mocked final DataSet r2) throws Exception {
		final Name<String> n1 = name("1");
		final Name<String> n2 = name("2");

		Resource sut=new Resource();

		sut.add(n1,r1);
		sut.add(n2,r2);

		assertThat(sut.hasResource(n1),equalTo(true));
		assertThat(sut.hasResource(n2),equalTo(true));

		sut.remove(n2);
		assertThat(sut.size(),equalTo(1));
		assertThat(sut.hasResource(n2),equalTo(false));

		sut.clear();
		assertThat(sut.size(),equalTo(0));
		assertThat(sut.hasResource(n1),equalTo(false));

	}

}
