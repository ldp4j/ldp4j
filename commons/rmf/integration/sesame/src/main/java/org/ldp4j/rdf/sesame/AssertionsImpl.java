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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

final class AssertionsImpl implements Assertions {

	private static final class LiteralComparator implements Comparator<Literal>, Serializable {

		private static final long serialVersionUID = -4543665504300717043L;

		@Override
		public int compare(Literal o1, Literal o2) {
			return o1.toString().compareTo(o2.toString());
		}

	}

	private static final class IndividualComparator implements Comparator<Individual>, Serializable {

		private static final long serialVersionUID = -1532046353145514095L;

		@Override
		public int compare(Individual i1, Individual i2) {
			int result=evalAnon(i1)-evalAnon(i2);
			if(result==0) {
				result=evalRef(i1)-evalRef(i2);
				if(result==0) {
					result=i1.getSubject().toString().compareTo(i2.getSubject().toString());
				}
			}
			return result;
		}

		int evalAnon(Individual i1) {
			return i1.isAnonymous()?1:0;
		}
		
		int evalRef(Individual i1) {
			return i1.isReference()?0:1;
		}

	}

	private final URI predicate;
	private final SortedSet<Literal> values;
	private final Map<Resource,Individual> links;

	private boolean hasBlankNodes=false;

	AssertionsImpl(URI predicate) {
		this.predicate = predicate;
		this.values=new TreeSet<Literal>(new LiteralComparator());
		this.links=new HashMap<Resource,Individual>();
	}

	void add(Literal value) {
		this.values.add(value);
	}
	
	void add(Individual individual) {
		this.links.put(individual.getSubject(),individual);
	}

	private List<Value> getValues() {
		List<Value> result=new ArrayList<Value>();
		result.addAll(this.values);
		SortedSet<Individual> sortedLinks=new TreeSet<Individual>(new IndividualComparator());
		sortedLinks.addAll(links.values());
		for(Individual link:sortedLinks) {
			result.add(link.getSubject());
		}
		return result;
	}

	@Override
	public String toString() {
		return getPredicate()+(hasBlankNodes?"* : ":" : ")+getValues();
	}

	@Override
	public Iterator<Value> iterator() {
		return getValues().iterator();
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public URI getPredicate() {
		return predicate;
	}
}