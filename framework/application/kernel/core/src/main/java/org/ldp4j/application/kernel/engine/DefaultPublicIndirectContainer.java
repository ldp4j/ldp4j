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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.2
 *   Bundle      : ldp4j-application-kernel-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.engine;

import java.net.URI;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.validation.ValidationConstraintFactory;
import org.ldp4j.application.data.validation.Validator.ValidatorBuilder;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.PublicIndirectContainer;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.application.engine.context.PublicResourceVisitor;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.template.IndirectContainerTemplate;
import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.application.vocabulary.Term;

final class DefaultPublicIndirectContainer extends DefaultPublicMembershipAwareContainer<IndirectContainerTemplate> implements PublicIndirectContainer {

	protected DefaultPublicIndirectContainer(DefaultApplicationContext applicationContext, Endpoint endpoint) {
		super(applicationContext, endpoint, IndirectContainerTemplate.class);
	}

	@Override
	public <T> T accept(PublicResourceVisitor<T> visitor) {
		return visitor.visitIndirectContainer(this);
	}

	@Override
	protected Term containerType() {
		return LDP.INDIRECT_CONTAINER;
	}

	@Override
	protected void fillInMetadata(ContentPreferences contentPreferences, Individual<?, ?> individual, Context ctx) {
		super.fillInMetadata(contentPreferences,individual,ctx);
		individual.
			addValue(
				ctx.property(LDP.INSERTED_CONTENT_RELATION),
				ctx.reference(containerTemplate().insertedContentRelation()));
	}

	@Override
	protected void configureValidationConstraints(ValidatorBuilder builder, Individual<?, ?> individual, DataSet metadata) {
		super.configureValidationConstraints(builder, individual, metadata);
		builder.withPropertyConstraint(ValidationConstraintFactory.readOnlyProperty(individual.property(LDP.INSERTED_CONTENT_RELATION.as(URI.class))));
	}

	@Override
	protected ManagedIndividual createMemberIndividual(Context ctx, PublicResource member) {
		return ctx.newIndividual(((DefaultPublicResource)member).indirectIndividualId());
	}

}
