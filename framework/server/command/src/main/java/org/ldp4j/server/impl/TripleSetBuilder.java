/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.impl;

import static org.ldp4j.rdf.util.RDFModelDSL.blankNode;
import static org.ldp4j.rdf.util.RDFModelDSL.triple;
import static org.ldp4j.rdf.util.RDFModelDSL.uriRef;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.namespace.QName;

import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.IndividualVisitor;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NameVisitor;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.ValueVisitor;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.vocabulary.Term;
import org.ldp4j.rdf.BlankNode;
import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.util.TripleSet;
import org.ldp4j.server.api.ResourceIndex;

final class TripleSetBuilder {

	private interface NameMapper<T extends Resource<?>> {
		
		T getResource(Name<?> name);
		
	}

	private static final class LocalNameVisitor extends NameVisitor implements NameMapper<BlankNode> {

		private BlankNode resource=null;

		@Override
		public BlankNode getResource(Name<?> name) {
			name.accept(this);
			return resource;
		}

		@Override
		public void visitURI(URI id) {
			resource=blankNode(id.toString());
		}

		@Override
		public void visitQName(QName id) {
			resource=blankNode(id.toString());
		}

		@Override
		public void visitTerm(Term id) {
			resource=blankNode(id.qualifiedEntityName());
		}

		@Override
		public void visitString(String id) {
			resource=blankNode(id);
		}

		@Override
		public void visitNumber(Number id) {
			resource=blankNode(id.toString());
		}

		@Override
		public void visitObject(Object id) {
			resource=blankNode(id.toString());
		}
	}

//	private static final class GlobalNameVisitor extends NameVisitor implements NameMapper<URIRef> {
//
//		private final URI base;
//
//		private URIRef resource=null;
//
//		private GlobalNameVisitor(URI base) {
//			this.base = base;
//		}
//		
//		private URIRef encode(Object id) {
//			try {
//				return uriRef(base.resolve(URLEncoder.encode(id.toString(), "UTF-8")));
//			} catch (UnsupportedEncodingException e) {
//				throw new IllegalStateException("UTF-8 charset should be supported",e);
//			}
//		}
//
//		@Override
//		public URIRef getResource(Name<?> name) {
//			name.accept(this);
//			return resource;
//		}
//
//		@Override
//		public void visitURI(URI id) {
//			resource=uriRef(id);
//		}
//
//		@Override
//		public void visitQName(QName id) {
//			resource=uriRef(id);
//		}
//
//		@Override
//		public void visitTerm(Term id) {
//			resource=uriRef(id.as(URI.class));
//		}
//
//		@Override
//		public void visitString(String id) {
//			resource=encode(id);
//		}
//
//		@Override
//		public void visitNumber(Number id) {
//			resource=encode(id);
//		}
//
//		@Override
//		public void visitObject(Object id) {
//			resource=encode(id);
//		}
//
//	}


	private final class TripleGenerator implements ValueVisitor {
	
		private final Resource<?> subject;
		private final URI predicate;
	
		private TripleGenerator(Individual<?,?> individual, Property property) {
			this.subject = toResource(individual);
			this.predicate = property.predicate();
		}

		private void addTriple(Object object) {
			triples.add(
				triple(
					subject, 
					predicate, 
					object
				)
			);
		}

		@Override
		public void visitIndividual(Individual<?,?> individual) {
			addTriple(toResource(individual));
			TripleSetBuilder.this.generateTriples(individual);
		}
	
		@Override
		public void visitLiteral(Literal<?> literal) {
			addTriple(literal.get());
		}
	}

	private final ResourceIndex index;
	private final Map<Object,Resource<?>> localResources;
	private final List<Object> visitedIndividuals;

	private final URI base;
	private TripleSet triples;

	TripleSetBuilder(ResourceIndex index, URI base) {
		this.index=index;
		this.base = base;
		this.localResources=new HashMap<Object,Resource<?>>();
		this.visitedIndividuals=new ArrayList<Object>();
		this.triples=new TripleSet();
	}

	private Resource<?> toResource(Individual<?,?> individual) {
		final AtomicReference<Resource<?>> result=new AtomicReference<Resource<?>>();
		individual.accept(
			new IndividualVisitor() {
				@Override
				public void visitManagedIndividual(ManagedIndividual individual) {
					ManagedIndividualId id = individual.id();
					URI path = index.resolveResource(ResourceId.createId(id.name(),id.managerId()));
					if(path==null) {
						throw new IllegalStateException("Could not resolve individual '"+id+"'");
					}
					result.set(uriRef(base.resolve(path)));
				}
				@Override
				public void visitLocalIndividual(LocalIndividual individual) {
					result.set(toSessionResource(individual.name()));
				}
				@Override
				public void visitExternalIndividual(ExternalIndividual individual) {
					result.set(uriRef(individual.id()));
				}
			}
		);
		return result.get();
	}

	private Resource<?> toSessionResource(Name<?> id) {
		Resource<?> local=localResources.get(id);
		if(local==null) {
			NameMapper<?> mapper=new LocalNameVisitor();
			local=mapper.getResource(id);
			localResources.put(id, local);
		}
		return local;
	}

	TripleSet build() {
		TripleSet result = triples;
		clear();
		return result;
	}

	void clear() {
		triples=new TripleSet();
		localResources.clear();
		visitedIndividuals.clear();
	}

	void generateTriples(Individual<?,?> individual) {
		Object id = individual.id();
		if(!visitedIndividuals.contains(id)) {
			visitedIndividuals.add(id);
			for(Property property:individual) {
				property.accept(new TripleGenerator(individual,property));
			}
		}
	}

}