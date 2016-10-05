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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class GraphImpl implements Graph {

	private final class IndividualIterator implements Iterator<Individual> {

		private final Iterator<Resource> subjects;

		private IndividualIterator(Iterator<Resource> subjects) {
			this.subjects = subjects;
		}

		@Override
		public boolean hasNext() {
			return subjects.hasNext();
		}

		@Override
		public Individual next() {
			return individuals.get(subjects.next());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove individuals");
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(TurtlePrettyPrinter.class);
	private final Resource base;

	private final SortedSet<Resource> subjects;
	private final Map<Resource, IndividualImpl> individuals;
	private final Map<String,String> definedNamespaces;
	private final SortedSet<String> usedNamespaces;

	public GraphImpl(Resource base) {
		this.base = base;
		subjects=new TreeSet<Resource>(new ResourceComparator());
		individuals=new HashMap<Resource,IndividualImpl>();
		definedNamespaces=new HashMap<String,String>();
		usedNamespaces=new TreeSet<String>();
	}

	private void collectNamespace(Value element) {
		URI uri=null;
		boolean datatype=false;
		if(element instanceof URI) {
			uri=(URI)element;
		} else if(element instanceof Literal) {
			uri=((Literal)element).getDatatype();
			datatype=true;
		}
		if(uri!=null && !(datatype && TurtleValueUtils.canOmmitDatatype(uri))) {
			String namespace=uri.getNamespace();
			if(LOGGER.isTraceEnabled() && !usedNamespaces.contains(namespace)) {
				LOGGER.trace(String.format("Collected namespace: %s",namespace));
			}
			usedNamespaces.add(namespace);
		}
	}

	private IndividualImpl getOrCreateIndividual(Resource subject) {
		IndividualImpl asserts = individuals.get(subject);
		if(asserts==null) {
			asserts=new IndividualImpl(subject);
			subjects.add(subject);
			individuals.put(subject, asserts);
		}
		return asserts;
	}

	void addNamespace(String prefix, String namespace) {
		this.definedNamespaces.put(namespace, prefix);
	}

	void add(Resource subject, URI predicate, Value object) {
		if(object instanceof Literal) {
			getOrCreateIndividual(subject).addAssertion(predicate, (Literal)object);
		} else {
			IndividualImpl target = getOrCreateIndividual(subject);
			IndividualImpl linked = getOrCreateIndividual((Resource)object);
			target.addLink(predicate, linked);
		}
		collectNamespace(subject);
		collectNamespace(predicate);
		collectNamespace(object);
	}

	@Override
	public Resource getBase() {
		return base;
	}

	@Override
	public Map<String,String> getNamespaces() {
		Map<String, String> mappings=new HashMap<String,String>();
		for(Entry<String,String> entry:definedNamespaces.entrySet()) {
			if(usedNamespaces.contains(entry.getKey())) {
				mappings.put(entry.getKey(),entry.getValue());
			}
		}
		return mappings;
	}

	@Override
	public IndividualImpl findIndividual(Resource object) {
		return individuals.get(object);
	}

	@Override
	public Iterator<Individual> iterator() {
		return new IndividualIterator(subjects.iterator());
	}

	@Override
	public String toString() {
		String nl = System.getProperty("line.separator");
		StringBuilder out=new StringBuilder();
		out.append("Graph(").append(base).append(") {").append(nl);
		for(Individual i:this) {
			String string = i.toString();
			string=string.replace(nl, nl.concat("\t"));
			out.append("\t").append(string).append(nl);
		}
		out.append("}");
		return out.toString();
	}

}