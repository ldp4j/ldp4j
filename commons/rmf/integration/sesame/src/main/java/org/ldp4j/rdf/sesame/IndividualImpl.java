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
 *   Artifact    : org.ldp4j.commons.rmf:integration-sesame:0.2.2
 *   Bundle      : integration-sesame-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.sesame;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class IndividualImpl implements Individual {

	private static final String INDIVIDUAL_PARAM = "Individual cannot be null";

	private static final String OBJECT_PARAM = "Object cannot be null";

	private static final String PREDICATE_PARAM = "Predicate cannot be null";

	private static final class PredicateComparator implements Comparator<URI>, Serializable {

		private static final long serialVersionUID = 2092671073721681488L;

		private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

		@Override
		public int compare(URI o1, URI o2) {
			if(o1.toString().equals(RDF_TYPE)) {
				return -1;
			} else if(o2.toString().equals(RDF_TYPE)) {
				return 1;
			} else {
				return TurtleValueUtils.compare(o1,o2);
			}
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(IndividualImpl.class);

	private final Resource subject;
	private final boolean anonymous;
	private final SortedSet<URI> predicates;
	private final Set<URI> protectedPredicates;
	private final Map<URI, AssertionsImpl> assertions;

	private final SortedSet<Resource> referrers;

	private long references;

	IndividualImpl(Resource subject) {
		this.subject = subject;
		this.anonymous=subject instanceof BNode;
		if(anonymous && LOGGER.isTraceEnabled()) {
			LOGGER.trace(String.format("Individual '%s' is anonymous",subject));
		}
		this.predicates=new TreeSet<URI>(new PredicateComparator());
		this.protectedPredicates=Collections.unmodifiableSet(this.predicates);
		this.assertions=new HashMap<URI,AssertionsImpl>();
		this.referrers=new TreeSet<Resource>(new ResourceComparator());
		this.references=0;
	}

	@Override
	public Iterator<URI> iterator() {
		return protectedPredicates.iterator();
	}

	private AssertionsImpl findAssertions(URI predicate) {
		AssertionsImpl asserts = assertions.get(predicate);
		if(asserts==null) {
			asserts=new AssertionsImpl(predicate);
			predicates.add(predicate);
			assertions.put(predicate, asserts);
		}
		return asserts;
	}

	private void addReferrer(Resource referrer) {
		if(LOGGER.isTraceEnabled() && !referrers.contains(referrer)) {
			LOGGER.trace(String.format("Resource '%s' is referenced by '%s'",subject,referrer));
		}
		referrers.add(referrer);
		references++;
		if(LOGGER.isTraceEnabled() && references>1) {
			LOGGER.trace(String.format("Resource '%s' is referenced %d times",subject,references));
		}
	}

	void addAssertion(URI predicate, Literal object) {
		Objects.requireNonNull(predicate,PREDICATE_PARAM);
		Objects.requireNonNull(object,OBJECT_PARAM);
		findAssertions(predicate).add(object);
	}

	void addLink(URI predicate, IndividualImpl individual) {
		Objects.requireNonNull(predicate,PREDICATE_PARAM);
		Objects.requireNonNull(individual,INDIVIDUAL_PARAM);
		findAssertions(predicate).add(individual);
		individual.addReferrer(subject);
	}

	@Override
	public boolean isAnonymous() {
		return anonymous;
	}

	@Override
	public boolean isReference() {
		return assertions.isEmpty();
	}

	@Override
	public boolean isReferred() {
		return references>0;
	}

	@Override
	public boolean isFoldable() {
		return
			!isReference() &&
			anonymous &&
			referrers.size()==1 &&
			references==1;
	}

	@Override
	public Resource getSubject() {
		return getSubject(Resource.class);
	}

	@Override
	public <T extends Resource> T getSubject(Class<T> clazz) {
		Objects.requireNonNull(clazz,"Class cannot be null");
		if(!clazz.isInstance(subject)) {
			throw new IllegalStateException("Subject cannot be casted to '"+clazz.getCanonicalName());
		}
		return clazz.cast(subject);
	}

	@Override
	public boolean canBeFoldedBy(Resource subject) {
		return
			isFoldable() &&
			referrers.contains(subject);
	}

	@Override
	public Assertions getAssertions(URI predicate) {
		AssertionsImpl asserts = assertions.get(predicate);
		if(asserts==null) {
			asserts=new AssertionsImpl(predicate);
		}
		return asserts;
	}

	@Override
	public long getReferences() {
		return references;
	}

	@Override
	public Set<Resource> getReferrers() {
		return Collections.unmodifiableSet(referrers);
	}

	@Override
	public String toString(){
		String nl = System.getProperty("line.separator");
		StringBuilder out=new StringBuilder();
		out.append("Individual(").append(subject).append(") {").append(nl);
		for(URI predicate:this) {
			out.append("\t").append(getAssertions(predicate)).append(nl);
		}
		out.append("}");
		return out.toString();
	}

}