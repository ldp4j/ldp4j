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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.commons;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.commons.ObjectUtils;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ObjectUtilsTest {

	private List<String> list;
	private Set<String> set;

	@Before
	public void setUp() {
		list = new ArrayList<String>();
		set = new HashSet<String>();
	}

	@Test
	public void testAreEqualClasses$null$null() {
		assertThat(ObjectUtils.areEqualClasses(null, null),equalTo(true));
	}

	@Test
	public void testAreEqualClasses$null$notNull() {
		assertThat(ObjectUtils.areEqualClasses(null, String.class),equalTo(false));
	}

	@Test
	public void testAreEqualClasses$notNull$null() {
		assertThat(ObjectUtils.areEqualClasses(String.class,null),equalTo(false));
	}

	@Test
	public void testAreEqualClasses$different() {
		assertThat(ObjectUtils.areEqualClasses(Integer.class, String.class),equalTo(false));
	}

	@Test
	public void testAreEqualClasses$same() {
		assertThat(ObjectUtils.areEqualClasses(String.class, String.class),equalTo(true));
	}

	@Test
	public void testAreEqualObjects$null$null() {
		assertThat(ObjectUtils.areEqualObjects(null, null),equalTo(true));
	}

	@Test
	public void testAreEqualObjects$null$notNull() {
		assertThat(ObjectUtils.areEqualObjects(null, list),equalTo(false));
	}

	@Test
	public void testAreEqualObjects$notNull$null() {
		assertThat(ObjectUtils.areEqualObjects(list, null),equalTo(false));
	}

	@Test
	public void testAreEqualObjects$different() {
		assertThat(ObjectUtils.areEqualObjects(list, set),equalTo(false));
	}

	@Test
	public void testAreEqualObjects$same() {
		assertThat(ObjectUtils.areEqualObjects(list, list),equalTo(true));
	}

}
