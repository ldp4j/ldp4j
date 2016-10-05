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
package org.ldp4j.xml;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.ldp4j.util.CharSequences;

/**
 * An iterator over a {@code CharSequence} that allows receiving the sequence of
 * Unicode code points that make up the {@code CharSequence}.
 */
final class CodePointIterator implements Iterator<Integer> {

	private final CharSequence s;
	private final int length;
	private int next;
	private int index;

	CodePointIterator(final CharSequence s) {
		this.s=s;
		this.index=-1;
		this.next=0;
		if(!CharSequences.isNullOrEmpty(s)) {
			this.length=s.length();
		} else {
			this.length=0;
		}
	}

	/**
	 * Returns {@code true} if the iteration has more code points. (In other
	 * words, returns {@code true} if {@link #next} would return a code point
	 * rather than throwing an exception.)
	 *
	 * @return {@code true} if the iteration has more code points
	 */
	@Override
	public boolean hasNext() {
		return this.next < this.length;
	}

	/**
	 * Returns the next code point in the iteration.
	 *
	 * @return the next code point in the iteration
	 * @throws NoSuchElementException
	 *             if the iteration has no more code points
	 */
	@Override
	public Integer next() {
		if(!hasNext()) {
			throw new NoSuchElementException("No more codepoints available in the CharSequence");
		}
		this.index=this.next;
		final Integer codePoint = Character.codePointAt(this.s, this.next);
		this.next+=Character.charCount(codePoint);
		return codePoint;
	}

	/**
	 * The operation is not supported by the iterator.
	 *
	 * @throws UnsupportedOperationException
	 *             always, as the {@code remove} operation is not supported by
	 *             this iterator
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	int index() {
		return this.index;
	}

}