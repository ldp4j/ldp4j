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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-sdk:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-engine-sdk-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.sdk;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

import java.lang.reflect.Array;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class IterableArrayIteratorTest {

	@SuppressWarnings("unchecked")
	static <T> List<T>[] createArray(Class<? extends T> clazz, int length) {
		return (List<T>[])Array.newInstance(List.class,length);
	}

	@Test
	public void testIterableArrayIterator() throws Exception {
		List<String>[] strings = createInput();
		populateInput(strings);
		List<String> all = calculateExpectations(strings);
		IterableArrayIterator<String> sut = new IterableArrayIterator<String>(strings);
		List<String> iterated = exerciseSut(sut);
		assertThat(iterated,hasItems(all.toArray(new String[all.size()])));
	}

	private List<String> exerciseSut(IterableArrayIterator<String> sut) {
		List<String> iterated=Lists.newArrayList();
		Iterators.addAll(iterated, sut);
		return iterated;
	}

	private List<String> calculateExpectations(List<String>[] strings) {
		List<String> all=Lists.newArrayList();
		for(List<String> str:strings) {
			all.addAll(str);
		}
		return all;
	}

	private void populateInput(List<String>[] strings) {
		strings[0].add("a1");
		strings[0].add("a2");
		strings[1].add("m1");
		strings[1].add("m2");
		strings[1].add("m3");
		strings[2].add("d1");
		strings[2].add("d2");
		strings[2].add("d3");
		strings[2].add("d4");
	}

	private List<String>[] createInput() {
		List<String>[] strings=createArray(String.class,3);
		for(int i=0;i<strings.length;i++) {
			strings[i]=Lists.newArrayList();
		}
		return strings;
	}

}
