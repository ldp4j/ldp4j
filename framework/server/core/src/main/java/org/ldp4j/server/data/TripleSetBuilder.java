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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.data;

import static org.ldp4j.rdf.util.RDFModelDSL.blankNode;
import static org.ldp4j.rdf.util.RDFModelDSL.literal;
import static org.ldp4j.rdf.util.RDFModelDSL.triple;
import static org.ldp4j.rdf.util.RDFModelDSL.typedLiteral;
import static org.ldp4j.rdf.util.RDFModelDSL.uriRef;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.IndividualVisitor;
import org.ldp4j.application.data.LanguageLiteral;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.LiteralVisitor;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NameVisitor;
import org.ldp4j.application.data.NewIndividual;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.RelativeIndividual;
import org.ldp4j.application.data.RelativeIndividualId;
import org.ldp4j.application.data.TimeUtils;
import org.ldp4j.application.data.TypedLiteral;
import org.ldp4j.application.data.ValueVisitor;
import org.ldp4j.application.vocabulary.Term;
import org.ldp4j.rdf.BlankNode;
import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.URIRef;
import org.ldp4j.rdf.util.TripleSet;

final class TripleSetBuilder {

	/**
	 * TODO: Verify that the translation of managed individuals with indirect id
	 * works as mandated by the specification
	 */
	private final class IndividualTranslator implements IndividualVisitor {

		private Resource<?> resource;

		Resource<?> getResource() {
			return this.resource;
		}

		private URIRef resolveManagedIndividualId(ManagedIndividualId target) {
			ManagedIndividualId id=target;
			URI indirectId=id.indirectId();
			if(indirectId!=null) {
				id=ManagedIndividualId.createId(id.name(),id.managerId());
			}
			URI path=TripleSetBuilder.this.resourceResolver.resolveResource(id);
			if(path==null) {
				return null;
			} else if(indirectId!=null) {
				path=path.resolve(indirectId);
			}
			return uriRef(TripleSetBuilder.this.base.resolve(path));
		}

		@Override
		public void visitManagedIndividual(ManagedIndividual individual) {
			ManagedIndividualId id = individual.id();
			this.resource=resolveManagedIndividualId(id);
		}

		@Override
		public void visitRelativeIndividual(RelativeIndividual individual) {
			RelativeIndividualId id = individual.id();
			ManagedIndividualId mid = ManagedIndividualId.createId(id.path(), id.parentId());
			this.resource=resolveManagedIndividualId(mid);
		}

		@Override
		public void visitLocalIndividual(LocalIndividual individual) {
			this.resource=toSessionResource(individual.name());
		}

		@Override
		public void visitExternalIndividual(ExternalIndividual individual) {
			this.resource=uriRef(individual.id());
		}

		@Override
		public void visitNewIndividual(NewIndividual individual) {
			// Nothing to do
		}
	}

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
			this.resource=blankNode(id.toString());
		}

		@Override
		public void visitQName(QName id) {
			this.resource=blankNode(id.toString());
		}

		@Override
		public void visitTerm(Term id) {
			this.resource=blankNode(id.qualifiedEntityName());
		}

		@Override
		public void visitString(String id) {
			this.resource=blankNode(id);
		}

		@Override
		public void visitNumber(Number id) {
			this.resource=blankNode(id.toString());
		}

		@Override
		public void visitObject(Object id) {
			this.resource=blankNode(id.toString());
		}

	}

	private final class TripleGenerator implements ValueVisitor {

		class LiteralTranslator implements LiteralVisitor {
			private void append(org.ldp4j.rdf.Literal<?> object) {
				TripleSetBuilder.this.triples.add(
					triple(
						TripleGenerator.this.subject,
						TripleGenerator.this.predicate,
						object
					)
				);
			}
			@Override
			public void visitLiteral(Literal<?> literal) {
				append(literal(literal.get()));
			}
			@Override
			public void visitTypedLiteral(TypedLiteral<?> literal) {
				Object value = literal.get();
				if(value instanceof DateTime) {
					value=TripleSetBuilder.this.timeUtils.from((DateTime)value).toXMLGregorianCalendar();
				} else if(value instanceof Duration) {
					value=TripleSetBuilder.this.timeUtils.from((Duration)value).toDuration();
				}
				append(typedLiteral(value,literal.type()));
			}
			@Override
			public void visitLanguageLiteral(LanguageLiteral literal) {
				append(literal(literal.get(),literal.language()));
			}
		}

		private final Resource<?> subject;
		private final URI predicate;

		private TripleGenerator(Resource<?> resource, Property property) {
			this.subject = resource;
			this.predicate = property.predicate();
		}

		@Override
		public void visitIndividual(Individual<?,?> individual) {
			// Individual is a NewIndividual, ignore
			if(this.subject==null) {
				return;
			}
			Resource<?> object = toResource(individual);
			if(object==null) {
				// Object individual is a NewIndividual or an unknown Managed Individual
				return;
			}
			TripleSetBuilder.this.triples.add(
				triple(
					this.subject,
					this.predicate,
					object
				)
			);
			TripleSetBuilder.this.generateTriples(individual);
		}

		@Override
		public void visitLiteral(Literal<?> literal) {
			// Individual is a NewIndividual, ignore
			if(this.subject==null) {
				return;
			}
			literal.accept(new LiteralTranslator());
		}
	}

	private final ResourceResolver resourceResolver;
	private final Map<Object,Resource<?>> localResources;
	private final List<Object> visitedIndividuals;

	private final URI base;
	private TripleSet triples;
	private final TimeUtils timeUtils;

	TripleSetBuilder(ResourceResolver resourceResolver, URI base) {
		this.resourceResolver=resourceResolver;
		this.base = base;
		this.localResources=new HashMap<Object,Resource<?>>();
		this.visitedIndividuals=new ArrayList<Object>();
		this.triples=new TripleSet();
		// TODO: Make this configurable --> See Literals class
		this.timeUtils=TimeUtils.newInstance();
	}

	private Resource<?> toResource(Individual<?,?> individual) {
		IndividualTranslator translator = new IndividualTranslator();
		individual.accept(translator);
		return translator.getResource();
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
			Resource<?> resource = toResource(individual);
			if(resource==null) {
				// Could not resolve resource, so skip processing
				return;
			}
			visitedIndividuals.add(id);
			for(Property property:individual) {
				property.accept(new TripleGenerator(resource,property));
			}
		}
	}

}