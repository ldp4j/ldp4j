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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E> {

	private final Map<E, Boolean> valueMap = new ConcurrentHashMap<E, Boolean>();
	private transient Set<E> values = valueMap.keySet();

	public ConcurrentHashSet() {
	}

	@Override
	public boolean add(E e) {
		return valueMap.put(e, Boolean.TRUE) == null;
	}

	@Override
	public boolean remove(Object o) {
		return valueMap.remove(o) != null;
	}

	@Override
	public void clear() {
		valueMap.clear();
	}

	@Override
	public boolean contains(Object o) {
		return valueMap.containsKey(o);
	}

	@Override
	public boolean isEmpty() {
		return valueMap.isEmpty();
	}

	@Override
	public int size() {
		return valueMap.size();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return values.containsAll(c);
	}

	@Override
	public boolean equals(Object o) {
		return o == this || values.equals(o);
	}

	@Override
	public int hashCode() {
		return values.hashCode();
	}

	@Override
	public Iterator<E> iterator() {
		return values.iterator();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return values.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return values.retainAll(c);
	}

	@Override
	public Object[] toArray() {
		return values.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return values.toArray(a);
	}

	@Override
	public String toString() {
		return values.toString();
	}
}
