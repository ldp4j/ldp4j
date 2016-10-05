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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.ldp4j.commons.IndentUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * TODO: Check how to add support for collections (http://www.w3.org/TR/turtle/#collections) 
 * TODO: Double check blank-node handling options (http://www.w3.org/TR/turtle/#unlabeled-bnodes)
 * TODO: Double check quotation issues (http://www.w3.org/TR/turtle/#turtle-literals)
 */
final class GraphRenderer {
	
	private static final String NL = System.getProperty("line.separator");

	private final Resource base;
	private final Map<String, String> namespaces;
	private final Graph graph;
	private StringBuilder builder;
	private TurtleValueUtils utils;
	private IndentUtils indenter;

	private final boolean disableFolding;

	GraphRenderer(Graph graph, boolean enableFolding) {
		this.disableFolding = !enableFolding;
		this.graph = graph;
		this.base = graph.getBase();
		this.namespaces = graph.getNamespaces();
		this.builder = new StringBuilder();
		this.utils = new TurtleValueUtils((URI)base,namespaces);
		this.indenter = new IndentUtils(1);
	}
	
	String render() {
		renderPrefixes();
		renderBase();
		for(Individual individual:graph) {
			if(!individual.isReference() && (disableFolding || !individual.isFoldable())) {
				renderSubject(individual);
				renderPredicates(false,individual);
				renderNewLine();
			}
		}
		return builder.toString();
	}

	/**
	 * 
	 */
	private void renderNewLine() {
		builder.append(NL);
	}
	
	private void renderBase() {
		if(base!=null) {
			builder.append(NL).append("@base <"+base+"> .").append(NL);
		}
	}

	private void renderSubject(Individual individual) {
		builder.append(NL).append(utils.toString(individual.getSubject()));
		if(individual.isAnonymous()) {
			String tail=
				!individual.isReferred()?
					"\t# Not referenced":
					String.format(
						"\t# Referenced %d times by resources: %s",
						individual.getReferences(),
						renderReferrers(individual.getReferrers()));
			builder.append(tail);
		}
	}

	private String renderReferrers(Set<Resource> referrers) {
		StringBuilder buffer=new StringBuilder();
		Iterator<Resource> it=referrers.iterator();
		while(it.hasNext()) {
			buffer.append(utils.toString(it.next()));
			if(it.hasNext()) {
				buffer.append(", ");
			}
		}
		return buffer.toString();
	}

	private void renderPrefixes() {
		SortedSet<String> prefixesDirectives=new TreeSet<String>();
		for(Entry<String,String> entry:namespaces.entrySet()) {
			prefixesDirectives.add(String.format("@prefix %s: <%s> .",entry.getValue(),entry.getKey()));
		}
	
		for(String directive:prefixesDirectives) {
			builder.append(directive).append(NL);
		}
	}

	private void renderPredicates(boolean anonymous, Individual individual) {
		Iterator<URI> predicateIterator = individual.iterator();
		while(predicateIterator.hasNext()) {
			URI predicate=predicateIterator.next();
			renderPredicateValues(individual,predicate);
			String predicateSeparator=" .";
			if(predicateIterator.hasNext()) {
				predicateSeparator=" ;";
			} else if(anonymous) {
				predicateSeparator="";
			}
			
			builder.append(predicateSeparator);
		}
	}

	private void renderPredicateValues(Individual individual, URI predicate) {
		Assertions assertions=individual.getAssertions(predicate);		
		Resource subject=individual.getSubject();		
		builder.append(NL).append(indenter.indent()).append(utils.toString(predicate));
		String statementSeparator=" ";
		if(assertions.size()>3) {
			indenter.increase();
			statementSeparator=NL.concat(indenter.indent());
		}
		Iterator<Value> objectIterator = assertions.iterator();
		String valueSeparator;
		do {
			builder.append(statementSeparator);
			Value object = objectIterator.next();
			if(tryRenderingFolded(subject,object)) {
				valueSeparator=" ,";
			} else {
				builder.append(utils.toString(object));
				valueSeparator=",";
			}
			if(objectIterator.hasNext()) {
				builder.append(valueSeparator);
			}
		} while(objectIterator.hasNext());
		if(assertions.size()>3) {
			indenter.decrease();
		}
	}

	private void renderFoldedBlankNode(Individual individual) {
		builder.append("[").append(" # FOLDED: ").append(individual.getSubject().toString());
		indenter.increase();
		renderPredicates(true,individual);
		indenter.decrease();
		builder.append(NL).append(indenter.indent()).append("]");
	}

	private boolean tryRenderingFolded(Resource subject, Value object) {
		boolean result=false;
		if(!disableFolding && object instanceof Resource) {
			Individual individual=graph.findIndividual((Resource)object);
			if(individual!=null && individual.canBeFoldedBy(subject)) {
				renderFoldedBlankNode(individual);
				result=true;
			}
		}
		return result;
	}
	
}