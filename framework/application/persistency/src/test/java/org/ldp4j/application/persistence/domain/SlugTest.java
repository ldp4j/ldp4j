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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-persistency:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-persistency-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.persistence.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

public class SlugTest {

	private Container container;

	@Before
	public void setUp() {
		this.container = new Container();
	}

	@Test
	public void testNextSlugPath$default() throws Exception {
		Slug slug=new Slug();
		slug.setPath("slug");
		slug.setContainer(container);
		assertThat(slug.nextSlugPath(),equalTo("slug"));
	}

	@Test
	public void testNextSlugPath$regular() throws Exception {
		Slug slug=new Slug();
		slug.setPath("slug");
		slug.setVersion(23);
		slug.setContainer(container);
		assertThat(slug.nextSlugPath(),equalTo("slug_23"));
		assertThat(slug.nextSlugPath(),equalTo("slug_24"));
	}

	@Test
	public void testNextSlugPath$reset() throws Exception {
		Slug slug=new Slug();
		slug.setPath("slug");
		slug.setVersion(23);
		slug.setContainer(container);
		assertThat(slug.nextSlugPath(),equalTo("slug_23"));
		slug.setVersion(23);
		assertThat(slug.nextSlugPath(),equalTo("slug_23"));
	}

	@Test
	public void testCreate$regular() throws Exception {
		Slug slug=Slug.create("slug", container);
		System.out.println(slug);
		assertThat(slug.getPath(),equalTo("slug"));
		assertThat(slug.getVersion(),equalTo(0L));
		assertThat(slug.getContainer(),equalTo(this.container));
	}

	@Test
	public void testCreate$simpleCornerCase() throws Exception {
		Slug slug=Slug.create("_", container);
		System.out.println(slug);
		assertThat(slug.getPath(),equalTo("_"));
		assertThat(slug.getVersion(),equalTo(0L));
		assertThat(slug.getContainer(),equalTo(this.container));
	}

	@Test
	public void testCreate$simpleCornerCaseSimpleClash() throws Exception {
		Slug slug=Slug.create("_23", container);
		System.out.println(slug);
		assertThat(slug.getPath(),equalTo("_23"));
		assertThat(slug.getVersion(),equalTo(0L));
		assertThat(slug.getContainer(),equalTo(this.container));
	}

	@Test
	public void testCreate$simpleCornerCaseComplexClash() throws Exception {
		Slug slug=Slug.create("_23_24", container);
		System.out.println(slug);
		assertThat(slug.getPath(),equalTo("_23"));
		assertThat(slug.getVersion(),equalTo(24L));
		assertThat(slug.getContainer(),equalTo(this.container));
	}

	@Test
	public void testCreate$compositeCornerCase() throws Exception {
		Slug slug=Slug.create("__", container);
		System.out.println(slug);
		assertThat(slug.getPath(),equalTo("__"));
		assertThat(slug.getVersion(),equalTo(0L));
		assertThat(slug.getContainer(),equalTo(this.container));
	}

	@Test
	public void testCreate$compositeCornerCaseSimpleClash() throws Exception {
		Slug slug=Slug.create("__23", container);
		System.out.println(slug);
		assertThat(slug.getPath(),equalTo("_"));
		assertThat(slug.getVersion(),equalTo(23L));
		assertThat(slug.getContainer(),equalTo(this.container));
	}

	@Test
	public void testCreate$compositeCornerCaseComplexClash() throws Exception {
		Slug slug=Slug.create("__23_24", container);
		System.out.println(slug);
		assertThat(slug.getPath(),equalTo("__23"));
		assertThat(slug.getVersion(),equalTo(24L));
		assertThat(slug.getContainer(),equalTo(this.container));
	}

	@Test
	public void testCreate$simpleClash() throws Exception {
		Slug slug=Slug.create("slug_23", container);
		System.out.println(slug);
		assertThat(slug.getPath(),equalTo("slug"));
		assertThat(slug.getVersion(),equalTo(23L));
		assertThat(slug.getContainer(),equalTo(this.container));
	}

	@Test
	public void testCreate$compositeClash() throws Exception {
		Slug slug=Slug.create("slug_23_24", container);
		System.out.println(slug);
		assertThat(slug.getPath(),equalTo("slug_23"));
		assertThat(slug.getVersion(),equalTo(24L));
		assertThat(slug.getContainer(),equalTo(this.container));
	}

}
