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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.2
 *   Bundle      : ldp4j-application-kernel-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.template;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import mockit.Deencapsulation;

import org.junit.Test;
import org.ldp4j.example.PersonContainerHandler;
import org.ldp4j.example.PersonHandler;

public class HandlerIdTest {

	private HandlerId resourceHandlerId() {
		return HandlerId.createId(PersonHandler.class);
	}

	private HandlerId containerHandlerId() {
		return HandlerId.createId(PersonContainerHandler.class);
	}

	@Test
	public void testEqualsForEqualResourceHandlerId() throws Exception {
		HandlerId id1 = resourceHandlerId();
		HandlerId id2 = resourceHandlerId();
		assertThat(id1,equalTo(id2));
		assertThat(id2,equalTo(id1));
	}

	@Test
	public void testEqualsForEqualContainerHandlerId() throws Exception {
		HandlerId id1 = containerHandlerId();
		HandlerId id2 = containerHandlerId();
		assertThat(id1,equalTo(id2));
		assertThat(id2,equalTo(id1));
	}

	@Test
	public void testEqualsForDifferentHandlerIds() throws Exception {
		HandlerId id1 = resourceHandlerId();
		HandlerId id2 = containerHandlerId();
		assertThat(id1,not(equalTo(id2)));
		assertThat(id2,not(equalTo(id1)));
	}

	@Test
	public void testEqualsForNonHandlerIds() throws Exception {
		Object id1 = resourceHandlerId();
		Object id2 = new Object();
		assertThat(id1,not(equalTo(id2)));
		assertThat(id2,not(equalTo(id1)));
	}

	@Test
	public void testEqualsForNullObjects() throws Exception {
		HandlerId id1 = resourceHandlerId();
		assertThat(id1.equals(null),not(equalTo(true)));
	}

	@Test
	public void testEqualsOnClassLoaders$sameClassNameAndSystemHashCode() throws Exception {
		HandlerId id1 = resourceHandlerId();
		HandlerId id2 = containerHandlerId();
		Deencapsulation.setField(id2, "className",Deencapsulation.getField(id1, "className"));
		Deencapsulation.setField(id2, "systemHashCode",Deencapsulation.getField(id1, "systemHashCode"));
		assertThat(id1,not(equalTo(id2)));
		assertThat(id2,not(equalTo(id1)));
	}

	@Test
	public void testEqualsOnClassLoaders$sameClassName() throws Exception {
		HandlerId id1 = resourceHandlerId();
		HandlerId id2 = containerHandlerId();
		Deencapsulation.setField(id2, "className",Deencapsulation.getField(id1, "className"));
		assertThat(id1,not(equalTo(id2)));
		assertThat(id2,not(equalTo(id1)));
	}

	@Test
	public void testHashCodeForEqualResourceHandlerId() throws Exception {
		HandlerId id1 = resourceHandlerId();
		HandlerId id2 = resourceHandlerId();
		assertThat(id1.hashCode(),equalTo(id2.hashCode()));
		assertThat(id2.hashCode(),equalTo(id1.hashCode()));
	}

	@Test
	public void testHashCodeForEqualContainerHandlerId() throws Exception {
		HandlerId id1 = containerHandlerId();
		HandlerId id2 = containerHandlerId();
		assertThat(id1.hashCode(),equalTo(id2.hashCode()));
		assertThat(id2.hashCode(),equalTo(id1.hashCode()));
	}

	@Test
	public void testHashCodeForDifferentHandlerIds() throws Exception {
		HandlerId id1 = resourceHandlerId();
		HandlerId id2 = containerHandlerId();
		assertThat(id1.hashCode(),not(equalTo(id2.hashCode())));
		assertThat(id2.hashCode(),not(equalTo(id1.hashCode())));
	}

	@Test
	public void testHasCustomString() throws Exception {
		HandlerId id = resourceHandlerId();
		assertThat(id.toString(),not(equalTo(HandlerId.class.getName()+"@"+System.identityHashCode(id))));
	}

}
