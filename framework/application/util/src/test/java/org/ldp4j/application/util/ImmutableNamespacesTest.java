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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-util:0.2.2
 *   Bundle      : ldp4j-application-util-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.commons.testing.Utils;

public class ImmutableNamespacesTest {

	private static final String DEFAULT_PR2 = "pr2";
	private static final String DEFAULT_PR1 = "pr1";
	private static final String DEFAULT_NAMESPACE = "http://www.example.org/vocabulary1#";
	private static final String NS1 = "ns1";
	private static final String VALID_NAMESPACE = "http://www.example.org/vocabulary#";
	private ImmutableNamespaces sut;

	@Before
	public void setUp() {
		this.sut=
			new ImmutableNamespaces().
				withPrefix(DEFAULT_PR1, DEFAULT_NAMESPACE).
				withPrefix(DEFAULT_PR2, DEFAULT_NAMESPACE);
	}

	@Test
	public void testWithPrefix$valid() throws Exception {
		final ImmutableNamespaces ns = this.sut.withPrefix(NS1, VALID_NAMESPACE);
		assertThat(ns,not(sameInstance(this.sut)));
		assertThat(ns.getDeclaredPrefixes(),containsInAnyOrder(NS1,DEFAULT_PR1,DEFAULT_PR2));
		assertThat(ns.getNamespaceURI(NS1),equalTo(VALID_NAMESPACE));
		assertThat(ns.getPrefix(VALID_NAMESPACE),equalTo(NS1));
		assertThat(ns.getPrefixes(VALID_NAMESPACE),contains(NS1));
	}

	@Test(expected=NullPointerException.class)
	public void testWithPrefix$nullPrefix() throws Exception {
		this.sut.withPrefix(null, VALID_NAMESPACE);
	}

	@Test(expected=NullPointerException.class)
	public void testWithPrefix$nullNamespace() throws Exception {
		this.sut.withPrefix(NS1, null);
	}

	@Test
	public void testWithoutPrefix() throws Exception {
		final ImmutableNamespaces ns = this.sut.withoutPrefix(DEFAULT_PR1,DEFAULT_PR2);
		assertThat(ns,not(sameInstance(this.sut)));
		assertThat(ns.getDeclaredPrefixes(),hasSize(0));
	}

	@Test
	public void testGetDeclaredPrefixes() throws Exception {
		assertThat(this.sut.getDeclaredPrefixes(),containsInAnyOrder(DEFAULT_PR1,DEFAULT_PR2));
	}

	@Test
	public void testGetNamespaceURI() throws Exception {
		assertThat(this.sut.getNamespaceURI(DEFAULT_PR1),equalTo(DEFAULT_NAMESPACE));
		assertThat(this.sut.getNamespaceURI(DEFAULT_PR2),equalTo(DEFAULT_NAMESPACE));
	}

	@Test
	public void testGetPrefix$found() throws Exception {
		assertThat(this.sut.getPrefix(DEFAULT_NAMESPACE),anyOf(equalTo(DEFAULT_PR1),equalTo(DEFAULT_PR2)));
	}

	@Test
	public void testGetPrefix$notFound() throws Exception {
		assertThat(this.sut.getPrefix("not found"),nullValue());
	}

	@Test
	public void testGetPrefixes$found() throws Exception {
		assertThat(this.sut.getPrefixes(DEFAULT_NAMESPACE),containsInAnyOrder(DEFAULT_PR1,DEFAULT_PR2));
	}

	@Test
	public void testGetPrefixes$notFound() throws Exception {
		assertThat(this.sut.getPrefixes("not found"),hasSize(0));
	}

	@Test
	public void testToString$empty() throws Exception {
		final ImmutableNamespaces ns = new ImmutableNamespaces();
		assertThat(ns.toString(),not(equalTo(Utils.defaultToString(ns))));
	}

	@Test
	public void testToString$nonEmpty() throws Exception {
		assertThat(this.sut.toString(),not(equalTo(Utils.defaultToString(this.sut))));
	}

}
