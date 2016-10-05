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

import java.util.Iterator;
import java.util.Objects;

import org.ldp4j.rdf.Node;
import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.URIRef;


public final class InmutableTripleSet implements ITripleSet {

	private static final String TRIPLES_PARAM = "Triples cannot be null";

	private final TripleSet triples;

	public InmutableTripleSet(Triple...triples) {
		this.triples = RDFOperations.toTripleSet(triples);
	}

	public <T extends Iterable<Triple>> InmutableTripleSet(T triples) {
		this.triples = RDFOperations.toTripleSet(triples);
	}

	@Override
	public InmutableTripleSet get(Resource<?> subject, URIRef predicate, Node object) {
		return new InmutableTripleSet(triples.get(subject, predicate, object));
	}

	@Override
	public InmutableTripleSet get(ITripleMatcher tripleMatcher) {
		return new InmutableTripleSet(triples.get(tripleMatcher));
	}

	@Override
	public boolean contains(Triple triple) {
		return triples.contains(triple);
	}

	@Override
	public <T extends Iterable<Triple>> boolean containsAll(T triples) {
		return this.triples.containsAll(triples);
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
		return triples.toArray();
	}

	@Override
	public Iterator<Triple> iterator() {
		return triples.iterator();
	}

	@Override
	public String toString() {
		return triples.toString();
	}

	public InmutableTripleSet add(Triple... triples) {
		if(triples==null) {
			return this;
		}
		return new InmutableTripleSet(RDFOperations.union(this.triples,triples));
	}

	public <T extends ITripleSet> InmutableTripleSet add(T triples) {
		if(triples==null) {
			return this;
		}
		return new InmutableTripleSet(RDFOperations.union(this.triples,triples));
	}

	public InmutableTripleSet add(Resource<?> subject, URIRef predicate, Node object) {
		Objects.requireNonNull(subject,"Subject cannot be null");
		Objects.requireNonNull(predicate, "Predicate cannot be null");
		Objects.requireNonNull(object, "Object cannot be null");
		return add(RDFModelDSL.triple(subject,predicate,object));
	}

	public InmutableTripleSet remove(Triple... triples) {
		Objects.requireNonNull(triples, TRIPLES_PARAM);
		TripleSet result = RDFOperations.toTripleSet(this.triples);
		result.remove(triples);
		return new InmutableTripleSet(result);
	}

	public <T extends Iterable<Triple>> InmutableTripleSet remove(T triples) {
		Objects.requireNonNull(triples, TRIPLES_PARAM);
		TripleSet result = RDFOperations.toTripleSet(this.triples);
		result.remove(triples);
		return new InmutableTripleSet(result);
	}

	public InmutableTripleSet remove(final Resource<?> subject, final URIRef predicate, final Node object) {
		return remove(new TriplePatternMatcher(subject, predicate, object));
	}

	public InmutableTripleSet remove(ITripleMatcher tripleMatcher) {
		Objects.requireNonNull(tripleMatcher, "Triple matcher cannot be null");
		TripleSet result=new TripleSet();
		for(Triple t:triples) {
			if(!tripleMatcher.accept(t)) {
				result.add(t);
			}
		}
		return new InmutableTripleSet(result);
	}

}