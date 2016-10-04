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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.2.2
 *   Bundle      : ldp4j-commons-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.commons;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.commons.Collector;

public class CollectorTest {

	private Collector<String> sut;
	private List<String> valueListDifferent;
	private String[] valueArrayDifferent;
	private String[] valueArrayRepeated;
	private List<String> valueListRepeated;

	@Before
	public void setUp() throws Exception {
		sut=Collector.<String>newCollector();
		valueArrayDifferent = new String[]{"String1","String2","String3"};
		valueListDifferent = Arrays.asList(valueArrayDifferent);
		valueArrayRepeated = new String[]{"String1","String2","String3","String1"};
		valueListRepeated = Arrays.asList(valueArrayRepeated);
	}

	private void verifyDifferent(Set<String> set, List<String> list) {
		assertThat(set.size(),equalTo(list.size()));
		assertThat(set.size(),equalTo(3));
		assertThat(set,hasItems(valueArrayDifferent));
		assertThat(list,equalTo(valueListDifferent));
	}

	private void verifyRepeated(Set<String> set, List<String> list) {
		assertThat(set.size(),equalTo(valueListDifferent.size()));
		assertThat(set,hasItems(valueArrayDifferent));
		assertThat(list,equalTo(valueListRepeated));
	}

	@Test
	public void testCollect$enum$different() throws Exception {
		sut.collect(valueArrayDifferent);
		verifyDifferent(sut.asSet(), sut.asList());
	}

	@Test
	public void testCollect$list$different() throws Exception {
		sut.collect(valueListDifferent);
		verifyDifferent(sut.asSet(), sut.asList());
	}

	@Test
	public void testCollect$iterable$different() throws Exception {
		sut.collect(
			new Iterable<String>(){
				@Override
				public Iterator<String> iterator() {
					return valueListDifferent.iterator();
				}
			}
		);
		verifyDifferent(sut.asSet(), sut.asList());
	}

	@Test
	public void testCollect$enum$repeated() throws Exception {
		sut.collect(valueArrayRepeated);
		verifyRepeated(sut.asSet(), sut.asList());
	}

	@Test
	public void testCollect$list$repeated() throws Exception {
		sut.collect(valueListRepeated);
		verifyRepeated(sut.asSet(), sut.asList());
	}

	@Test
	public void testCollect$iterable$repeated() throws Exception {
		sut.collect(
			new Iterable<String>(){
				@Override
				public Iterator<String> iterator() {
					return valueListRepeated.iterator();
				}
			}
		);
		verifyRepeated(sut.asSet(), sut.asList());
	}

}
