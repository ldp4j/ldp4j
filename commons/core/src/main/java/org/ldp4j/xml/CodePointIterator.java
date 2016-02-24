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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.xml;

import java.util.Iterator;

import org.ldp4j.util.CharSequences;

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

	@Override
	public boolean hasNext() {
		return this.next < this.length;
	}

	@Override
	public Integer next() {
		this.index=this.next;
		final Integer codePoint = Character.codePointAt(this.s, this.next);
		this.next+=Character.charCount(codePoint);
		return codePoint;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public int index() {
		return this.index;
	}

}