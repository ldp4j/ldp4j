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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-bean:0.2.2
 *   Bundle      : rmf-bean-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.bean.impl.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.ldp4j.rdf.BlankNode;
import org.ldp4j.rdf.Literal;
import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.URIRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class IndividualImpl implements Individual, Iterable<URIRef> {

	private static final String INDIVIDUAL_PARAM = "Individual cannot be null";
	private static final String PREDICATE_PARAM  = "Predicate cannot be null";
	private static final String OBJECT_PARAM     = "Object cannot be null";

	private static final class PredicateComparator implements Comparator<URIRef>, Serializable {

		private static final long serialVersionUID = 2092671073721681488L;

		private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

		@Override
		public int compare(URIRef o1, URIRef o2) {
			if(o1.toString().equals(RDF_TYPE)) {
				return -1;
			} else if(o2.toString().equals(RDF_TYPE)) {
				return 1;
			} else {
				return ModelUtils.compare(o1,o2);
			}
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(IndividualImpl.class);

	private final Resource<?> subject;
	private final boolean anonymous;
	private final SortedSet<URIRef> predicates;
	private final Set<URIRef> protectedPredicates;
	private final Map<URIRef, AssertionsImpl> assertions;

	private final SortedSet<Resource<?>> referrers;

	private long references;

	IndividualImpl(Resource<?> subject) {
		this.subject = subject;
		this.anonymous=subject instanceof BlankNode;
		if(anonymous && LOGGER.isTraceEnabled()) {
			LOGGER.trace(String.format("Individual '%s' is anonymous",subject));
		}
		this.predicates=new TreeSet<URIRef>(new PredicateComparator());
		this.protectedPredicates=Collections.unmodifiableSet(this.predicates);
		this.assertions=new HashMap<URIRef,AssertionsImpl>();
		this.referrers=new TreeSet<Resource<?>>(new ResourceComparator());
		this.references=0;
	}

	@Override
	public Iterator<URIRef> iterator() {
		return protectedPredicates.iterator();
	}

	private AssertionsImpl findAssertions(URIRef predicate) {
		AssertionsImpl asserts = assertions.get(predicate);
		if(asserts==null) {
			asserts=new AssertionsImpl(predicate);
			predicates.add(predicate);
			assertions.put(predicate, asserts);
		}
		return asserts;
	}

	private void addReferrer(Resource<?> referrer) {
		if(LOGGER.isTraceEnabled() && !referrers.contains(referrer)) {
			LOGGER.trace(String.format("Resource '%s' is referenced by '%s'",subject,referrer));
		}
		referrers.add(referrer);
		references++;
		if(LOGGER.isTraceEnabled() && references>1) {
			LOGGER.trace(String.format("Resource '%s' is referenced %d times",subject,references));
		}
	}

	void addAssertion(URIRef predicate, Literal<?> object) {
		Objects.requireNonNull(predicate,PREDICATE_PARAM);
		Objects.requireNonNull(object,OBJECT_PARAM);
		findAssertions(predicate).add(object);
	}

	void addLink(URIRef predicate, IndividualImpl individual) {
		Objects.requireNonNull(predicate,PREDICATE_PARAM);
		Objects.requireNonNull(individual,INDIVIDUAL_PARAM);
		findAssertions(predicate).add(individual);
		individual.addReferrer(subject);
	}

	@Override
	public Resource<?> getIdentity() {
		return subject;
	}

	@Override
	public Set<Literal<?>> getPropertyValues(URIRef predicate) {
		Set<Literal<?>> result=new HashSet<Literal<?>>();
		result.addAll(getAssertions(predicate).getLiterals());
		return result;
	}

	@Override
	public Set<Individual> getPropertyObjects(URIRef predicate) {
		Set<Individual> result=new HashSet<Individual>();
		result.addAll(getAssertions(predicate).getIndividuals());
		return result;
	}

	@Override
	public boolean isAnonymous() {
		return anonymous;
	}

	@Override
	public boolean isReference() {
		return assertions.isEmpty();
	}

	boolean isReferred() {
		return references>0;
	}

	boolean isFoldable() {
		return
			!isReference() &&
			anonymous &&
			referrers.size()==1 &&
			references==1;
	}

	boolean canBeFoldedBy(Resource<?> subject) {
		return
			isFoldable() &&
			referrers.contains(subject);
	}

	Assertions getAssertions(URIRef predicate) {
		AssertionsImpl asserts = assertions.get(predicate);
		if(asserts==null) {
			asserts=new AssertionsImpl(predicate);
		}
		return asserts;
	}

	long getReferences() {
		return references;
	}

	Set<Resource<?>> getReferrers() {
		return Collections.unmodifiableSet(referrers);
	}

	@Override
	public String toString(){
		String nl = System.getProperty("line.separator");
		StringBuilder out=new StringBuilder();
		out.append("Individual(").append(subject).append(") {").append(nl);
		for(URIRef predicate:this) {
			out.append("\t").append(getAssertions(predicate)).append(nl);
		}
		out.append("}");
		return out.toString();
	}

	@Override
	public Set<URIRef> getProperties() {
		return protectedPredicates;
	}

}