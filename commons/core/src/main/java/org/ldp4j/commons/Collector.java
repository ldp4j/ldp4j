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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class Collector<S> {

	private final List<S> base;

	private Collector() {
		this.base = new ArrayList<S>();
	}

	@SafeVarargs
	public final Collector<S> collect(S... elements) {
		this.base.addAll(Arrays.asList(elements));
		return this;
	}

	public Collector<S> collect(Collection<? extends S> elements) {
		this.base.addAll(elements);
		return this;
	}

	public Collector<S> collect(Iterator<? extends S> elements) {
		while(elements.hasNext()) {
			this.base.add(elements.next());
		}
		return this;
	}

	public Collector<S> collect(Iterable<? extends S> elements) {
		for(Iterator<? extends S> it=elements.iterator();it.hasNext();) {
			this.base.add(it.next());
		}
		return this;
	}

	public List<S> asList() {
		return new ArrayList<S>(base);
	}

	public Set<S> asSet() {
		return new HashSet<S>(base);
	}

	public static <T> Collector<T> newCollector() {
		return new Collector<T>();
	}

}