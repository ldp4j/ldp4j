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

import org.ldp4j.rdf.Node;
import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.URIRef;

public final class TriplePatternMatcher implements ITripleMatcher {

	private static final class ObjectMatcher implements ITripleMatcher {
		private final Node object;
	
		public ObjectMatcher(Node object) {
			this.object = object;
		}
	
		@Override
		public boolean accept(Triple t) {
			return object==null || t.getObject().equals(object);
		}
	}

	private static final class PredicateMatcher implements ITripleMatcher {
		private final URIRef predicate;
	
		public PredicateMatcher(URIRef predicate) {
			this.predicate = predicate;
		}
	
		@Override
		public boolean accept(Triple t) {
			return predicate==null || t.getPredicate().equals(predicate);
		}
	}

	private static final class SubjectMatcher implements ITripleMatcher {
		private final Resource<?> subject;
	
		public SubjectMatcher(Resource<?> subject) {
			this.subject = subject;
		}
	
		@Override
		public boolean accept(Triple t) {
			return subject==null || t.getSubject().equals(subject);
		}
	}

	private final ITripleMatcher subjectFilter;
	private final ITripleMatcher predicateFilter;
	private final ITripleMatcher objectFilter;

	public TriplePatternMatcher(Resource<?> subject, URIRef predicate, Node object) {
		subjectFilter = new SubjectMatcher(subject);
		predicateFilter = new PredicateMatcher(predicate);
		objectFilter = new ObjectMatcher(object);
	}

	@Override
	public boolean accept(Triple t) {
		return 
			subjectFilter.accept(t) && 
			predicateFilter.accept(t) && 
			objectFilter.accept(t);
	}
}