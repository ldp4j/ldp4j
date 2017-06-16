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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:0.2.2
 *   Bundle      : ldp4j-application-data-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.vocabulary.RDF;
import org.ldp4j.application.vocabulary.Term;

import com.google.common.collect.Sets;

class IndividualHelperImpl implements IndividualHelper {

	private static final class TypeCollector implements ValueVisitor {

		private final class CollectorWorker implements IndividualVisitor {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void visitManagedIndividual(ManagedIndividual individual) {
				// Discard invalid types
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void visitRelativeIndividual(RelativeIndividual individual) {
				// Discard invalid types
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void visitLocalIndividual(LocalIndividual individual) {
				// Discard invalid types
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void visitExternalIndividual(ExternalIndividual individual) {
				types.add(individual.id());
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void visitNewIndividual(NewIndividual individual) {
				// Discard invalid types
			}

		}

		private final Set<URI> types;
		private final CollectorWorker worker;

		private TypeCollector() {
			this.types=Sets.newLinkedHashSet();
			this.worker = new CollectorWorker();
		}

		private Set<URI> collectedTypes() {
			return this.types;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visitLiteral(Literal<?> value) {
			// Discard invalid types
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visitIndividual(Individual<?, ?> value) {
			value.accept(this.worker);
		}

	}

	private final Individual<?, ?> individual;

	IndividualHelperImpl(Individual<?,?> individual) {
		this.individual = individual;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<URI> types() {
		Property property = this.individual.property(RDF.TYPE.as(URI.class));
		if(property==null) {
			return Collections.emptySet();
		}
		TypeCollector collector = new TypeCollector();
		for(Value value:property) {
			value.accept(collector);
		}
		return collector.collectedTypes();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyHelper property(URI propertyId) {
		return new PropertyHelperImpl(propertyId,this.individual);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyHelper property(String propertyId) {
		return property(URI.create(propertyId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyHelper property(Term property) {
		return property(property.as(URI.class));
	}

}