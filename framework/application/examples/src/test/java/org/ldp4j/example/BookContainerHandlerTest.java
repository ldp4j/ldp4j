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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSets;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.ApplicationRuntimeException;
import org.ldp4j.application.ext.UnknownResourceException;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;

@RunWith(JMockit.class)
public class BookContainerHandlerTest {

	@Mocked WriteSession session;
	@Mocked ContainerSnapshot snapshot;
	@Mocked ResourceSnapshot memberSnapshot;

	private Name<String> name(String string) {
		return NamingScheme.getDefault().name(string);
	}

	@Test
	public void testGetHandler$failure() {
		BookContainerHandler containerHandler = new BookContainerHandler();
		try {
			containerHandler.bookHandler();
			fail("Should fail if no book handler is available");
		} catch(IllegalStateException e) {
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreate$happyPath() throws Exception {
		BookContainerHandler containerHandler = new BookContainerHandler();
		BookHandler resourceHandler=new BookHandler();
		final Name<String> containerName = name("container");
		final NameProvider provider = NameProvider.create(containerName);
		final Name<String> memberName = name("resource");
		provider.addMemberName(memberName);
		containerHandler.addNameProvider(provider.owner(),provider);
		containerHandler.setBookHandler(resourceHandler);

		final DataSet data = DataSets.createDataSet(memberName);

		new Expectations() {{
			snapshot.name();result=containerName;
			memberSnapshot.name();result=memberName;
		}};
		containerHandler.create(snapshot, data, session);
		assertThat(provider.pendingMemberNames(),not((contains(memberName))));
		assertThat(resourceHandler.get(memberSnapshot),sameInstance(data));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreate$exceptionPath() throws Exception {
		BookContainerHandler containerHandler = new BookContainerHandler();
		BookHandler resourceHandler=new BookHandler();
		final Name<String> containerName = name("container");
		final NameProvider provider = NameProvider.create(containerName);
		final Name<String> memberName = name("resource");
		provider.addMemberName(memberName);
		containerHandler.addNameProvider(provider.owner(),provider);
		containerHandler.setBookHandler(resourceHandler);

		final DataSet data = DataSets.createDataSet(memberName);

		new Expectations() {{
			snapshot.name();result=containerName;
			memberSnapshot.name();result=memberName;
			session.saveChanges();result=new WriteSessionException("FAILURE");
		}};
		try {
			containerHandler.create(snapshot, data, session);
			fail("Should not create if the session fails");
		} catch (ApplicationRuntimeException e) {
			assertThat(e.getCause(),instanceOf(WriteSessionException.class));
			assertThat(e.getCause().getMessage(),equalTo("FAILURE"));
			assertThat(provider.pendingMemberNames(),not((contains(memberName))));
			try {
				resourceHandler.get(memberSnapshot);
				fail("Resource should not be available in the resource handler");
			} catch (UnknownResourceException e1) {
			}
		}
	}

}
