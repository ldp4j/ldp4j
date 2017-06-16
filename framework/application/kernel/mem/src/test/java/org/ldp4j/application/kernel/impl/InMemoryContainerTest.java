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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.kernel.impl.InMemoryContainer;
import org.ldp4j.application.kernel.resource.Container;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.resource.Slug;

public class InMemoryContainerTest {

	private static final Name<Integer> CONTAINER_NAME = NamingScheme.getDefault().name(1);

	private static final ResourceId CONTAINER_ID = ResourceId.createId(CONTAINER_NAME, "template");

	private Container container;

	@Before
	public void setUp() {
		this.container = new InMemoryContainer(CONTAINER_ID);
	}

	@Test
	public void testAddSlug$new() throws Exception {
		Slug slug = this.container.addSlug("slug");
		assertThat(slug,notNullValue());
		assertThat(this.container.findSlug("slug"),sameInstance(slug));
	}

	@Test
	public void testAddSlug$clash$matching_path() throws Exception {
		Slug original = this.container.addSlug("slug");
		Slug slug = this.container.addSlug("slug");
		assertThat(slug,sameInstance(original));
		assertThat(this.container.findSlug("slug"),sameInstance(original));
	}

	@Test
	public void testAddSlug$new$derived_path$mergeable() throws Exception {
		Slug original = this.container.addSlug("slug");
		Slug slug = this.container.addSlug("slug_23");
		assertThat(slug,sameInstance(original));
		assertThat(original.version(),equalTo(23L));
		assertThat(this.container.findSlug("slug"),sameInstance(original));
	}

	@Test
	public void testAddSlug$new$derived_path$not_mergeable$lower() throws Exception {
		Slug original = this.container.addSlug("slug");
		original.nextPath();
		assertThat(original.version(),equalTo(1L));
		original.nextPath();
		assertThat(original.version(),equalTo(2L));
		original.nextPath();
		assertThat(original.version(),equalTo(3L));
		Slug slug = this.container.addSlug("slug_2");
		assertThat(slug,notNullValue());
		assertThat(slug.preferredPath(),equalTo("slug_2"));
		assertThat(slug.version(),equalTo(1L));
		assertThat(original.version(),equalTo(3L));
	}

	@Test
	public void testAddSlug$new$derived_path$not_mergeable$equal() throws Exception {
		Slug original = this.container.addSlug("slug");
		original.nextPath();
		assertThat(original.version(),equalTo(1L));
		original.nextPath();
		assertThat(original.version(),equalTo(2L));
		original.nextPath();
		assertThat(original.version(),equalTo(3L));
		Slug slug = this.container.addSlug("slug_3");
		assertThat(slug,sameInstance(original));
		assertThat(original.version(),equalTo(3L));
	}

}
