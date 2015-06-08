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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-data-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.net.URI;
import java.util.Set;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.vocabulary.RDF;
import org.ldp4j.application.vocabulary.Term;

import com.google.common.collect.Sets;

class IndividualHelperImpl implements IndividualHelper {

	private final Individual<?, ?> individual;

	IndividualHelperImpl(Individual<?,?> individual) {
		this.individual = individual;
	}

	@Override
	public Set<URI> types() {
		Property property = this.individual.property(RDF.TYPE.as(URI.class));
		final Set<URI> types=Sets.newLinkedHashSet();
		for(Value value:property) {
			value.accept(
				new ValueVisitor() {
					@Override
					public void visitLiteral(Literal<?> value) {
						// Discard invalid types
					}
					@Override
					public void visitIndividual(Individual<?, ?> value) {
						value.accept(
							new IndividualVisitor() {
								@Override
								public void visitManagedIndividual(ManagedIndividual individual) {
									// Discard invalid types
								}
								@Override
								public void visitRelativeIndividual(RelativeIndividual individual) {
									// Discard invalid types
								}
								@Override
								public void visitLocalIndividual(LocalIndividual individual) {
									// Discard invalid types
								}
								@Override
								public void visitExternalIndividual(ExternalIndividual individual) {
									types.add(individual.id());
								}
								@Override
								public void visitNewIndividual(NewIndividual individual) {
									// Discard invalid types
								}
							}
						);
					}
				}
			);
		}
		return types;
	}

	@Override
	public PropertyHelper property(URI propertyId) {
		return new PropertyHelperImpl(propertyId,this.individual);
	}

	@Override
	public PropertyHelper property(String propertyId) {
		return property(URI.create(propertyId));
	}

	@Override
	public PropertyHelper property(Term property) {
		return property(property.as(URI.class));
	}

}