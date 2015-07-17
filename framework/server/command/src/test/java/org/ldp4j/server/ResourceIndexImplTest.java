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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.net.URI;

import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.server.ResourceIndexImpl;
import org.ldp4j.server.blueprint.ComponentRegistry;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@RunWith(JMockit.class)
public class ResourceIndexImplTest {

	private final class FailingAnswer implements Answer<Object> {
		@Override
		public Object answer(InvocationOnMock invocation) throws Throwable {
			throw failure;
		}
	}

	private ResourceIndexImpl sut;

	private String templateId="templateId";

	private final ResourceId id1 = ResourceId.createId(NamingScheme.getDefault().name(1),templateId);
	private final URI path1 = URI.create("urn:1");

	private final ResourceId id2 = ResourceId.createId(NamingScheme.getDefault().name(2),templateId);
	private final URI path2 = URI.create("urn:2");

	private final ResourceId id3 = ResourceId.createId(NamingScheme.getDefault().name(3),templateId);
	private final URI path3 = URI.create("urn:3");

	private final String message = "Just testing...";

	private final Exception failure=new RuntimeException(message);

	@Before
	public void setUp() throws Exception {
		sut=new ResourceIndexImpl(null);
	}

	@Test
	public void testGetRegistry() {
		assertThat(sut.getRegistry(),is(nullValue()));
		ComponentRegistry mock = Mockito.mock(ComponentRegistry.class);
		ResourceIndexImpl impl = new ResourceIndexImpl(mock);
		assertThat(impl.getRegistry(),is(sameInstance(mock)));
	}

	@Test
	public void testPublish$valid() {
		publishValid(id1,path1);
		publishValid(id2,path2);
	}

	@Test
	public void testPublish$repeated() {
		publishValid(id1,path1);
		sut.publish(id1,path1);
		assertPublication(id1, path1);
	}

	@Test
	public void testPublish$resourceAlreadyPublished() {
		testPublish$valid();
		try {
			sut.publish(id1, path3);
		} catch(IllegalStateException e) {
			assertThat(sut.isActive(path3),is(equalTo(false)));
			assertThat(sut.resolveLocation(path3),nullValue());
			assertPublication(id1,path1);
		}
	}

	@Test
	public void testPublish$pathAlreadyInUse() {
		testPublish$valid();
		try {
			sut.publish(id3, path1);
		} catch(IllegalStateException e) {
			assertThat(sut.isPublished(id3),equalTo(false));
			assertThat(sut.resolveResource(managedIndividualId(id3)),nullValue());
			assertPublication(id1,path1);
		}
	}

	private ManagedIndividualId managedIndividualId(ResourceId id) {
		return ManagedIndividualId.createId(id.name(),id.templateId());
	}

	@Test

	public void testPublish$resourceAndPathAlreadyUsed() {
		testPublish$valid();
		try {
			sut.publish(id1, path2);
		} catch(IllegalStateException e) {
			assertPublication(id1,path1);
			assertPublication(id2,path2);
		}
	}

	@Test
	public void testUnpublish$existing() {
		publishValid(id1,path1);
		assertThat(sut.unpublish(id1),is(equalTo(true)));
		assertUnpublish(id1, path1);
	}

	@Test
	public void testUnpublish$unknown() {
		assertThat(sut.unpublish(id1),is(equalTo(false)));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testUnlocksMockito() {
		BiMap<ResourceId, URI> mock =
			(BiMap<ResourceId, URI>)Mockito.mock(BiMap.class, new FailingAnswer());
		ResourceIndexImpl impl=
			Deencapsulation.
				newInstance(
					ResourceIndexImpl.class,
					new Class<?>[]{ComponentRegistry.class,BiMap.class},
					null,mock);
		doTestUnlock(impl);
	}

	@Test
	public void testUnlocksJMockit(@Mocked final HashBiMap<ResourceId,URI> mock) {
		new NonStrictExpectations() {{
			HashBiMap.<ResourceId,URI>create(); result=mock;
			mock.get(withAny(id1)); result=failure; minTimes=1;
			mock.containsKey(withAny(id1)); result=failure; minTimes=1;
			mock.containsValue(withAny(path1)); result=failure; minTimes=1;
			mock.inverse(); result=failure; minTimes=1;
			mock.remove(withAny(id1));result=failure;
		}};
		doTestUnlock(new ResourceIndexImpl(null));
	}

	private void publishValid(ResourceId id, URI path) {
		assertUnpublish(id, path);
		sut.publish(id, path);
		assertPublication(id, path);
	}

	private void doTestUnlock(ResourceIndexImpl impl) {
		try {
			impl.isActive(path1);
		} catch (RuntimeException e) {
			assertThat(e.getMessage(),is(equalTo(message)));
		}
		try {
			impl.resolveLocation(path1);
		} catch (RuntimeException e) {
			assertThat(e.getMessage(),is(equalTo(message)));
		}
		try {
			impl.isPublished(id1);
		} catch (RuntimeException e) {
			assertThat(e.getMessage(),is(equalTo(message)));
		}
		try {
			impl.resolveResource(managedIndividualId(id1));
		} catch (RuntimeException e) {
			assertThat(e.getMessage(),is(equalTo(message)));
		}
		try {
			impl.publish(id1, path1);
		} catch (RuntimeException e) {
			assertThat(e.getMessage(),is(equalTo(message)));
		}
		try {
			impl.unpublish(id1);
		} catch (RuntimeException e) {
			assertThat(e.getMessage(),is(equalTo(message)));
		}
	}

	private void assertUnpublish(ResourceId id, URI path) {
		assertThat(sut.isPublished(id),equalTo(false));
		assertThat(sut.isActive(path),equalTo(false));
		assertThat(sut.resolveResource(managedIndividualId(id)),is(nullValue()));
		assertThat(sut.resolveLocation(path),is(nullValue()));
	}

	private void assertPublication(ResourceId id, URI path) {
		assertThat(sut.isPublished(id),equalTo(true));
		assertThat(sut.isActive(path),equalTo(true));
		assertThat(sut.resolveResource(managedIndividualId(id)),is(sameInstance(path)));
		assertThat((Object)sut.resolveLocation(path),is(sameInstance((Object)id)));
	}

}
