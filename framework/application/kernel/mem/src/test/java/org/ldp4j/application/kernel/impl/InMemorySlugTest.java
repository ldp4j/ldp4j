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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-mem:0.2.2
 *   Bundle      : ldp4j-application-kernel-mem-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.kernel.impl.InMemoryContainer;
import org.ldp4j.application.kernel.impl.InMemorySlug;
import org.ldp4j.application.kernel.resource.Container;
import org.ldp4j.application.kernel.resource.ResourceId;

public class InMemorySlugTest {

	private static final Name<Integer> CONTAINER_NAME = NamingScheme.getDefault().name(1);

	private static final ResourceId CONTAINER_ID = ResourceId.createId(CONTAINER_NAME, "template");

	private Container container;

	@Before
	public void setUp() {
		this.container = new InMemoryContainer(CONTAINER_ID);
	}

	@Test
	public void testNextPath$default() throws Exception {
		InMemorySlug slug=InMemorySlug.create("slug", container);
		assertThat(slug.nextPath(),equalTo("slug"));
	}

	@Test
	public void testNextSlugPath$regular() throws Exception {
		InMemorySlug slug=InMemorySlug.create("slug", container);
		slug.setVersion(23);
		assertThat(slug.nextPath(),equalTo("slug_23"));
		assertThat(slug.nextPath(),equalTo("slug_24"));
	}

	@Test
	public void testNextSlugPath$reset() throws Exception {
		InMemorySlug slug=InMemorySlug.create("slug", container);
		slug.setVersion(23);
		assertThat(slug.nextPath(),equalTo("slug_23"));
		slug.setVersion(23);
		assertThat(slug.nextPath(),equalTo("slug_23"));
	}

	@Test
	public void testCreate$regular() throws Exception {
		InMemorySlug slug=InMemorySlug.create("slug", container);
		System.out.println(slug);
		assertThat(slug.preferredPath(),equalTo("slug"));
		assertThat(slug.version(),equalTo(0L));
		assertThat(slug.containerId(),equalTo(this.container.id()));
	}

	@Test
	public void testCreate$simpleCornerCase() throws Exception {
		InMemorySlug slug=InMemorySlug.create("_", container);
		System.out.println(slug);
		assertThat(slug.preferredPath(),equalTo("_"));
		assertThat(slug.version(),equalTo(0L));
		assertThat(slug.containerId(),equalTo(this.container.id()));
	}

	@Test
	public void testCreate$simpleCornerCaseSimpleClash() throws Exception {
		InMemorySlug slug=InMemorySlug.create("_23", container);
		System.out.println(slug);
		assertThat(slug.preferredPath(),equalTo("_23"));
		assertThat(slug.version(),equalTo(0L));
		assertThat(slug.containerId(),equalTo(this.container.id()));
	}

	@Test
	public void testCreate$simpleCornerCaseComplexClash() throws Exception {
		InMemorySlug slug=InMemorySlug.create("_23_24", container);
		System.out.println(slug);
		assertThat(slug.preferredPath(),equalTo("_23"));
		assertThat(slug.version(),equalTo(24L));
		assertThat(slug.containerId(),equalTo(this.container.id()));
	}

	@Test
	public void testCreate$compositeCornerCase() throws Exception {
		InMemorySlug slug=InMemorySlug.create("__", container);
		System.out.println(slug);
		assertThat(slug.preferredPath(),equalTo("__"));
		assertThat(slug.version(),equalTo(0L));
		assertThat(slug.containerId(),equalTo(this.container.id()));
	}

	@Test
	public void testCreate$compositeCornerCaseSimpleClash() throws Exception {
		InMemorySlug slug=InMemorySlug.create("__23", container);
		System.out.println(slug);
		assertThat(slug.preferredPath(),equalTo("_"));
		assertThat(slug.version(),equalTo(23L));
		assertThat(slug.containerId(),equalTo(this.container.id()));
	}

	@Test
	public void testCreate$compositeCornerCaseComplexClash() throws Exception {
		InMemorySlug slug=InMemorySlug.create("__23_24", container);
		System.out.println(slug);
		assertThat(slug.preferredPath(),equalTo("__23"));
		assertThat(slug.version(),equalTo(24L));
		assertThat(slug.containerId(),equalTo(this.container.id()));
	}

	@Test
	public void testCreate$simpleClash() throws Exception {
		InMemorySlug slug=InMemorySlug.create("slug_23", container);
		System.out.println(slug);
		assertThat(slug.preferredPath(),equalTo("slug"));
		assertThat(slug.version(),equalTo(23L));
		assertThat(slug.containerId(),equalTo(this.container.id()));
	}

	@Test
	public void testCreate$compositeClash() throws Exception {
		InMemorySlug slug=InMemorySlug.create("slug_23_24", container);
		System.out.println(slug);
		assertThat(slug.preferredPath(),equalTo("slug_23"));
		assertThat(slug.version(),equalTo(24L));
		assertThat(slug.containerId(),equalTo(this.container.id()));
	}

}
