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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.2.2
 *   Bundle      : ldp4j-commons-http-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import static com.google.common.base.Preconditions.checkPositionIndex;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

final class ImmutableAlternatives implements Alternatives {

	static final class Builder {

		private ImmutableList.Builder<Entry> entries;

		private Builder() {
			this.entries=ImmutableList.builder();
		}

		Builder add(ImmutableQuality quality, ImmutableAlternative alternative) {
			Objects.requireNonNull(alternative,"Alternative cannot be null");
			this.entries.add(new Entry(quality,alternative));
			return this;
		}

		ImmutableAlternatives build() {
			return new ImmutableAlternatives(this.entries.build());
		}
	}

	private final class AlternativeIterator implements Iterator<Alternative> {
		private final Iterator<Entry> it;

		private AlternativeIterator(Iterator<Entry> it) {
			this.it = it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public Alternative next() {
			if(!hasNext()) {
				throw new NoSuchElementException("No more alternatives are available");
			}
			return it.next().alternative;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove alternatives");
		}
	}

	private static final class Entry {

		private final ImmutableQuality quality;
		private final ImmutableAlternative alternative;

		private Entry(ImmutableQuality quality, ImmutableAlternative alternative) {
			this.quality = quality;
			this.alternative = alternative;
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						add("quality",this.quality).
						add("alternative",this.alternative).
						toString();
		}

	}

	private final ImmutableList<Entry> entries;

	private ImmutableAlternatives(final ImmutableList<Entry> entries) {
		this.entries = entries;
	}

	@Override
	public Iterator<Alternative> iterator() {
		return new AlternativeIterator(this.entries.iterator());
	}

	@Override
	public int size() {
		return this.entries.size();
	}

	@Override
	public boolean isEmpty() {
		return this.entries.isEmpty();
	}

	@Override
	public ImmutableAlternative alternative(int i) {
		return entry(i).alternative;
	}

	@Override
	public ImmutableQuality quality(int i) {
		return entry(i).quality;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("entries",this.entries).
					toString();
	}

	private Entry entry(int i) {
		checkPositionIndex(i,this.entries.size(),"No such alternative");
		return this.entries.get(i);
	}

	static Builder builder() {
		return new Builder();
	}

}
