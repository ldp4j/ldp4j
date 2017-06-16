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
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.validation.ValidationConstraint;
import org.ldp4j.application.data.validation.ValidationConstraintFactory;
import org.ldp4j.application.data.validation.Validator.ValidatorBuilder;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.PublicMembershipAwareContainer;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.application.engine.context.ContentPreferences.Preference;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.template.MembershipAwareContainerTemplate;
import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.application.vocabulary.RDF;
import org.ldp4j.application.vocabulary.Term;

abstract class DefaultPublicMembershipAwareContainer<T extends MembershipAwareContainerTemplate> extends DefaultPublicContainer<T> implements PublicMembershipAwareContainer {

	protected DefaultPublicMembershipAwareContainer(DefaultApplicationContext applicationContext, Endpoint endpoint, Class<? extends T> templateClass) {
		super(applicationContext, endpoint,templateClass);
	}

	protected abstract Term containerType();

	final void fillInMemberMetadata(ContentPreferences contentPreferences, Individual<?,?> individual, Context ctx) {
		if(!contentPreferences.mayInclude(Preference.MEMBERSHIP_TRIPLES)) {
			return;
		}

		URI predicate = containerTemplate().membershipPredicate();
		switch(containerTemplate().membershipRelation()) {
			case HAS_MEMBER:
				populateHasMember(individual, ctx, predicate);
				break;
			case IS_MEMBER_OF:
				populateIsMemberOf(individual, ctx, predicate);
				break;
			default:
				throw new IllegalStateException("Unsupported membership relation "+containerTemplate().membershipRelation());
		}
	}

	private void populateIsMemberOf(Individual<?, ?> individual, Context ctx, URI predicate) {
		for(PublicResource member:members()) {
			ManagedIndividual tmp=createMemberIndividual(ctx, member);
			tmp.addValue(
				predicate,
				individual);
		}
	}

	protected abstract ManagedIndividual createMemberIndividual(Context ctx, PublicResource member);

	private void populateHasMember(Individual<?, ?> individual, Context ctx, URI predicate) {
		for(PublicResource member:members()) {
			individual.addValue(
				predicate,
				createMemberIndividual(ctx, member));
		}
	}

	@Override
	protected void fillInMetadata(ContentPreferences contentPreferences, Individual<?, ?> individual, Context ctx) {
		super.fillInMetadata(contentPreferences,individual,ctx);
		T template = containerTemplate();
		individual.
			addValue(
				ctx.property(RDF.TYPE),
				ctx.reference(containerType())).
			addValue(
				ctx.property(template.membershipRelation().term()),
				ctx.reference(template.membershipPredicate()));
		Individual<?, ?> membershipResource=null;
		if(!isRoot()) {
			membershipResource = ctx.newIndividual(parent());
		} else {
			membershipResource = ctx.reference(LDP.MEMBER_SUBJECT);
		}
		individual.
			addValue(
				ctx.property(LDP.MEMBERSHIP_RESOURCE),
				membershipResource);
		if(contentPreferences.mayInclude(Preference.MEMBERSHIP_TRIPLES)) {
			fillInMemberMetadata(contentPreferences,membershipResource,ctx);
		}
	}

	final void configureMemberValidationConstraints(ValidatorBuilder builder, Individual<?, ?> individual, DataSet metadata) {
		URI predicate = containerTemplate().membershipPredicate();
		switch(containerTemplate().membershipRelation()) {
			case HAS_MEMBER:
				configureHasMemberValidationConstraints(builder,individual,metadata,predicate);
				break;
			case IS_MEMBER_OF:
				configureIsMemberOfValidationConstraints(builder,individual,metadata,predicate);
				break;
			default:
				throw new AssertionError("Unsupported membership relation "+containerTemplate().membershipRelation());
		}
	}

	private void configureIsMemberOfValidationConstraints(ValidatorBuilder builder, Individual<?, ?> individual, DataSet metadata, URI predicate) { // NOSONAR
		if(members().isEmpty()) {
			builder.withPropertyConstraint(ValidationConstraintFactory.readOnlyProperty(predicate));
		} else {
			for(PublicResource member:members()) {
				Individual<?,?> tmp=metadata.individualOfId(member.individualId());
				builder.withPropertyConstraint(ValidationConstraintFactory.readOnlyProperty(tmp.property(predicate)));
			}
		}
	}

	private void configureHasMemberValidationConstraints(ValidatorBuilder builder, Individual<?, ?> individual, DataSet metadata, URI predicate) { // NOSONAR
		ValidationConstraint<Property> constraint=null;
		Property property = individual.property(predicate);
		if(property!=null) {
			constraint=ValidationConstraintFactory.readOnlyProperty(property);
		} else {
			constraint=ValidationConstraintFactory.readOnlyProperty(individual.id(),predicate);
		}
		builder.withPropertyConstraint(constraint);
	}

	@Override
	protected void configureValidationConstraints(ValidatorBuilder builder, Individual<?, ?> individual, DataSet metadata) {
		super.configureValidationConstraints(builder, individual, metadata);
		builder.withPropertyConstraint(ValidationConstraintFactory.mandatoryPropertyValues(individual.property(containerTemplate().membershipRelation().term().as(URI.class))));
		Property rootProperty = individual.property(LDP.MEMBERSHIP_RESOURCE.as(URI.class));
		if(rootProperty!=null) {
			builder.withPropertyConstraint(ValidationConstraintFactory.readOnlyProperty(rootProperty));
		} else {
			builder.withPropertyConstraint(ValidationConstraintFactory.readOnlyProperty(LDP.MEMBERSHIP_RESOURCE.as(URI.class)));
		}
	}

}
