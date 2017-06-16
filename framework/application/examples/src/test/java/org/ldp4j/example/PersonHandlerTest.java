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

import java.net.URI;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSets;
import org.ldp4j.application.data.Literals;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.data.NewIndividual;
import org.ldp4j.application.ext.ApplicationRuntimeException;
import org.ldp4j.application.ext.InconsistentContentException;
import org.ldp4j.application.ext.UnknownResourceException;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;

@RunWith(JMockit.class)
public class PersonHandlerTest {

	@Mocked WriteSession session;
	@Mocked ResourceSnapshot snapshot;

	private DataSet withReadOnlyProperty(final Name<String> resourceName) {
		final DataSet data=empty(resourceName);
		ManagedIndividual individual = data.individual(ManagedIndividualId.createId(resourceName, PersonHandler.ID),ManagedIndividual.class);
		individual.addValue(PersonHandler.READ_ONLY_PROPERTY,Literals.of("value"));
		return data;
	}

	private DataSet empty(final Name<String> memberName) {
		final DataSet data = DataSets.createDataSet(memberName);
		data.individual(URI.create(""), NewIndividual.class);
		return data;
	}

	private Name<String> name(String string) {
		return NamingScheme.getDefault().name(string);
	}


	@Test
	public void testDeletion$happyPath() throws Exception {
		PersonHandler sut = new PersonHandler();
		final Name<String> resourceName = name("resource");
		final DataSet data=empty(resourceName);

		sut.add(resourceName, data);

		new Expectations() {{
			snapshot.name();result=resourceName;
			session.delete(snapshot);
			session.saveChanges();
		}};

		sut.delete(snapshot, session);
		try {
			assertThat(sut.get(snapshot),sameInstance(data));
			fail("Should be deleted if the operation succeeds");
		} catch (UnknownResourceException e) {
		}
	}

	@Test
	public void testDeletion$fail() throws Exception {
		PersonHandler sut = new PersonHandler();
		final Name<String> resourceName = name("resource");
		final DataSet data=empty(resourceName);

		sut.add(resourceName, data);

		new Expectations() {{
			snapshot.name();result=resourceName;
			session.delete(snapshot);
			session.saveChanges();result=new WriteSessionException("FAILURE");
		}};

		try {
			sut.delete(snapshot, session);
			fail("Should not delete if the session fails");
		} catch (ApplicationRuntimeException e) {
			assertThat(e.getCause(),instanceOf(WriteSessionException.class));
			assertThat(e.getCause().getMessage(),equalTo("FAILURE"));
			assertThat(sut.get(snapshot),sameInstance(data));
		}
	}

	@Test
	public void testUpdate$happyPath() throws Exception {
		PersonHandler sut = new PersonHandler();
		final Name<String> resourceName = name("resource");
		final DataSet data = withReadOnlyProperty(resourceName);
	
		sut.add(resourceName, data);
	
		new Expectations() {{
			snapshot.name();result=resourceName;
			session.modify(snapshot);
		}};
	
		DataSet modified = withReadOnlyProperty(resourceName);
	
		sut.update(snapshot,modified,session);
		assertThat(sut.get(snapshot),sameInstance(modified));
	}

	@Test
	public void testUpdate$sessionFailure() throws Exception {
		PersonHandler sut = new PersonHandler();
		final Name<String> resourceName = name("resource");
		final DataSet data=empty(resourceName);

		sut.add(resourceName, data);

		new Expectations() {{
			snapshot.name();result=resourceName;
			session.modify(snapshot);
			session.saveChanges();result=new WriteSessionException("FAILURE");
		}};

		try {
			sut.update(snapshot,empty(resourceName),session);
			fail("Should not modify if the session fails");
		} catch (ApplicationRuntimeException e) {
			assertThat(e.getCause(),instanceOf(WriteSessionException.class));
			assertThat(e.getCause().getMessage(),equalTo("FAILURE"));
			assertThat(sut.get(snapshot),sameInstance(data));
		}
	}

	@Test
	public void testUpdate$validationFailure() throws Exception {
		PersonHandler sut = new PersonHandler();
		final Name<String> resourceName = name("resource");
		final DataSet data = withReadOnlyProperty(resourceName);

		sut.add(resourceName, data);

		new Expectations() {{
			snapshot.name();result=resourceName;
		}};

		try {
			sut.update(snapshot,empty(resourceName),session);
			fail("Should not modify if the session fails");
		} catch (InconsistentContentException e) {
			assertThat(sut.get(snapshot),sameInstance(data));
		}
	}
}
