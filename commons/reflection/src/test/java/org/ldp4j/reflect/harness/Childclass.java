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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-ext:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-ext-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.reflect.harness;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Childclass extends Superclass<String> implements Parentinterface {

	private Set<String> known;

	public Childclass(String... known) {
		this.known = Sets.newLinkedHashSet(Arrays.asList(known));
	}

	@Override
	public int getInt() {
		return super.getInt();
	}

	public long getLong() {
		return 0;
	}

	@Override
	public String get() {
		return null;
	}

	@Override
	public Set<String> knownElements(String... ids) {
		return Sets.filter(super.knownElements(ids),new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				return known.contains(input);
			}
		});
	}

	public List<String> factoryGeneric(String first, String... others) {
		return ImmutableList.<String>builder().add(first).add(others).build();
	}

	@Override
	public <T> List<? extends T> factoryGeneric(T first, T... others) {
		List<? extends T> factoryGeneric = super.factoryGeneric(first, others);
		List<T> result=Lists.newArrayList();
		for(int i=0;i<factoryGeneric.size();i++) {
			if(i%2==0) {
				result.add(factoryGeneric.get(i));
			}
		}
		return result;
	}

}