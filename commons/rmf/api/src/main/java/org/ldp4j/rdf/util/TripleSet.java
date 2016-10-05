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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-api:0.2.2
 *   Bundle      : rmf-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.ldp4j.rdf.Node;
import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.URIRef;

public final class TripleSet implements ITripleSet {

	private static final String TRIPLE_MATCHER_PARAM = "Triple matcher cannot be null";

	private static final class ProtectedIterator implements Iterator<Triple> {

		private final Iterator<Triple> delegate;

		private ProtectedIterator(Iterator<Triple> delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public Triple next() {
			return delegate.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Removal not supported");
		}

	}

	private static final Triple[] EMPTY_TRIPLE_ARRAY = new Triple[0];

	private final SortedSet<Triple> triples=new TreeSet<Triple>();

	@Override
	public TripleSet get(Resource<?> subject, URIRef predicate, Node object) {
		return get(new TriplePatternMatcher(subject, predicate, object));
	}

	@Override
	public TripleSet get(ITripleMatcher tripleMatcher) {
		Objects.requireNonNull(tripleMatcher, TRIPLE_MATCHER_PARAM);
		TripleSet result=new TripleSet();
		for(Triple t:triples) {
			if(tripleMatcher.accept(t)) {
				result.add(t);
			}
		}
		return result;
	}

	@Override
	public boolean contains(Triple triple) {
		return triples.contains(triple);
	}

	@Override
	public <T extends Iterable<Triple>> boolean containsAll(T triples) {
		boolean contains=true;
		for(Iterator<Triple> i=triples.iterator();i.hasNext() && contains;) {
			contains=this.triples.contains(i.next());
		}
		return contains;
	}

	@Override
	public Iterator<Triple> iterator() {
		final Iterator<Triple> delegate=triples.iterator();
		return new ProtectedIterator(delegate);
	}

	@Override
	public int size() {
		return triples.size();
	}

	@Override
	public boolean isEmpty() {
		return triples.isEmpty();
	}

	@Override
	public Triple[] toArray() {
		return triples.toArray(EMPTY_TRIPLE_ARRAY);
	}

	public void add(Triple... triples) {
		if(triples!=null && triples.length>0) {
			this.triples.addAll(Arrays.asList(triples));
		}
	}

	public <T extends Iterable<Triple>> void add(T triples) {
		if(triples!=null) {
			if(triples instanceof TripleSet) {
				this.triples.addAll(((TripleSet)triples).triples);
			} else {
				for(Triple t:triples) {
					this.triples.add(t);
				}
			}
		}
	}

	public void add(Resource<?> subject, URIRef predicate, Node object) {
		Objects.requireNonNull(subject, "Subject cannot be null");
		Objects.requireNonNull(predicate, "Predicate cannot be null");
		Objects.requireNonNull(object, "Object cannot be null");
		add(RDFModelDSL.triple(subject,predicate,object));
	}

	public void remove(Triple... triples) {
		if(triples!=null && triples.length>0) {
			this.triples.removeAll(Arrays.asList(triples));
		}
	}

	public <T extends Iterable<Triple>> void remove(T triples) {
		if(triples!=null) {
			if(triples instanceof TripleSet) {
				this.triples.removeAll(((TripleSet)triples).triples);
			} else {
				for(Triple t:triples) {
					this.triples.remove(t);
				}
			}
		}
	}

	public void remove(Resource<?> subject, final URIRef predicate, final Node object) {
		remove(new TriplePatternMatcher(subject, predicate, object));
	}

	public void remove(ITripleMatcher tripleMatcher) {
		Objects.requireNonNull(tripleMatcher, TRIPLE_MATCHER_PARAM);
		Set<Triple> result=new HashSet<Triple>();
		for(Triple t:triples) {
			if(!tripleMatcher.accept(t)) {
				result.add(t);
			}
		}
		triples.removeAll(result);
	}

	public void clear() {
		this.triples.clear();
	}

	@Override
	public String toString() {
		return RDFOperations.toString(Collections.unmodifiableSortedSet(triples));
	}

}