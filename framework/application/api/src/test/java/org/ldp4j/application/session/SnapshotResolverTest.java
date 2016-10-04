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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.session;

import java.net.URI;

import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.ApplicationContextException;
import org.ldp4j.application.spi.ResourceSnapshotResolver;
import org.ldp4j.application.spi.RuntimeDelegate;
import org.ldp4j.application.spi.ShutdownListener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(JMockit.class)
public class SnapshotResolverTest {

	private final class MockedRuntimeDelegate extends RuntimeDelegate {
		private final ResourceSnapshotResolver resolver;

		private MockedRuntimeDelegate(ResourceSnapshotResolver resolver) {
			this.resolver = resolver;
		}

		@Override
		public boolean isOffline() {
			return false;
		}

		@Override
		public WriteSession createSession() throws ApplicationContextException {
			return null;
		}

		@Override
		public ResourceSnapshotResolver createResourceResolver(URI canonicalBase,ReadSession session) {
			return this.resolver;
		}

		@Override
		public void registerShutdownListener(ShutdownListener listener) {
		}

	}

	private static final URI CANONICAL_BASE=URI.create("http://www.ldp4j.org/context/");
	private static final URI NON_HIERARCHICAL_CANONICAL_BASE=URI.create("urn:www.ldp4j.org/context/");
	private static final URI NON_ABSOLUTE_CANONICAL_BASE=URI.create("context/");

	@Test
	public void testToURI(@Mocked ReadSession session, final @Mocked ResourceSnapshotResolver resolver) throws Exception {
		SnapshotResolver sut = buildResolver(session, resolver);
		sut.fromURI(CANONICAL_BASE);
		new Verifications() {{
			resolver.resolve(CANONICAL_BASE);
		}};
	}

	@Test
	public void testFromURI(@Mocked ReadSession session, final @Mocked ResourceSnapshotResolver resolver, final @Mocked ResourceSnapshot snapshot) throws Exception {
		SnapshotResolver sut = buildResolver(session, resolver);
		sut.toURI(snapshot);
		new Verifications() {{
			resolver.resolve(snapshot);
		}};
	}

	@Test(expected=NullPointerException.class)
	public void testBuilder$nullSession() {
		SnapshotResolver.
			builder().
				withReadSession(null).
				withCanonicalBase(CANONICAL_BASE).
				build();
	}

	@Test(expected=NullPointerException.class)
	public void testBuilder$nullCanonicalBase(@Mocked ReadSession session) {
		SnapshotResolver.
			builder().
				withReadSession(session).
				withCanonicalBase(null).
				build();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testBuilder$nonHierarchicalCanonicalBase(@Mocked ReadSession session) {
		SnapshotResolver.
			builder().
				withReadSession(session).
				withCanonicalBase(NON_HIERARCHICAL_CANONICAL_BASE).
				build();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testBuilder$nonAbsoluteCanonicalBase(@Mocked ReadSession session) {
		SnapshotResolver.
			builder().
				withReadSession(session).
				withCanonicalBase(NON_ABSOLUTE_CANONICAL_BASE).
				build();
	}

	@Test
	public void testBuilder$validBase(@Mocked ReadSession session, final @Mocked ResourceSnapshotResolver resolver) {
		buildResolver(session, resolver);
	}

	private SnapshotResolver buildResolver(ReadSession session,
			final ResourceSnapshotResolver resolver) {
		RuntimeDelegate.setInstance(new MockedRuntimeDelegate(resolver));
		SnapshotResolver result=
			SnapshotResolver.
				builder().
					withReadSession(session).
					withCanonicalBase(CANONICAL_BASE).
					build();
		assertThat(result,notNullValue());
		assertThat(Deencapsulation.getField(result, "resolver"),sameInstance((Object)resolver));
		return result;
	}
}
