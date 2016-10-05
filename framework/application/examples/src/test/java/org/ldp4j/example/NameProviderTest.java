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

import org.junit.Test;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.ApplicationRuntimeException;

public class NameProviderTest {

	private Name<String> name(String id) {
		return NamingScheme.getDefault().name(id);
	}

	@Test
	public void testCreate$nonNull() throws Exception {
		Name<String> owner = name("test");
		NameProvider sut = NameProvider.create(owner);
		assertThat(sut,notNullValue());
		assertThat(sut.owner(),equalTo(owner));
		assertThat(sut.pendingMemberNames(),hasSize(0));
		assertThat(sut.pendingResourceNames(),hasSize(0));
	}

	@Test(expected=NullPointerException.class)
	public void testCreate$null() throws Exception {
		NameProvider.create(null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMemberNameLifecycle() {
		Name<String> owner = name("test");
		NameProvider sut = NameProvider.create(owner);
		sut.addMemberName(name("m1"));
		sut.addMemberName(name("m2"));
		assertThat(sut.pendingMemberNames(),contains(name("m1"),name("m2")));
		assertThat(sut.nextMemberName(),equalTo(name("m1")));
		assertThat(sut.pendingMemberNames(),contains(name("m2")));
		assertThat(sut.nextMemberName(),equalTo(name("m2")));
		assertThat(sut.pendingMemberNames(),hasSize(0));
		try {
			sut.nextMemberName();
			fail("Should not provide member names if not available");
		} catch (ApplicationRuntimeException e) {
			assertThat(e.getMessage(),equalTo("No more member names available for resource 'test'"));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testResourceNameLifecycle() {
		Name<String> owner = name("test");
		NameProvider sut = NameProvider.create(owner);
		sut.addResourceName(name("m1"));
		sut.addResourceName(name("m2"));
		assertThat(sut.pendingResourceNames(),contains(name("m1"),name("m2")));
		assertThat(sut.nextResourceName(),equalTo(name("m1")));
		assertThat(sut.pendingResourceNames(),contains(name("m2")));
		assertThat(sut.nextResourceName(),equalTo(name("m2")));
		assertThat(sut.pendingResourceNames(),hasSize(0));
		try {
			sut.nextResourceName();
			fail("Should not provide resource names if not available");
		} catch (ApplicationRuntimeException e) {
			assertThat(e.getMessage(),equalTo("No more resource names available for resource 'test'"));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAttachmentNameLifecycle() {
		Name<String> owner = name("test");
		NameProvider sut = NameProvider.create(owner);
		sut.addAttachmentName("attachment",name("m1"));
		sut.addAttachmentName("attachment",name("m2"));
		assertThat(sut.pendingAttachmentNames("attachment"),contains(name("m1"),name("m2")));
		assertThat(sut.nextAttachmentName("attachment"),equalTo(name("m1")));
		assertThat(sut.pendingAttachmentNames("attachment"),contains(name("m2")));
		assertThat(sut.nextAttachmentName("attachment"),equalTo(name("m2")));
		assertThat(sut.pendingAttachmentNames("attachment"),hasSize(0));
		try {
			sut.nextAttachmentName("attachment");
			fail("Should not provide attachment names if not available");
		} catch (ApplicationRuntimeException e) {
			assertThat(e.getMessage(),equalTo("No more attachment <<attachment>> names available for resource 'test'"));
		}
	}

	@Test
	public void testAttachmentNameLifecycle$unknownAttachment() {
		Name<String> owner = name("test");
		NameProvider sut = NameProvider.create(owner);
		assertThat(sut.pendingAttachmentNames("attachment"),hasSize(0));
		try {
			sut.nextAttachmentName("attachment");
			fail("Should not provide attachment names if not available");
		} catch (ApplicationRuntimeException e) {
			assertThat(e.getMessage(),equalTo("No more attachment <<attachment>> names available for resource 'test'"));
		}
	}

}
