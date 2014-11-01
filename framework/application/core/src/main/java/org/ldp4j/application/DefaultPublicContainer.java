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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.ldp4j.application.ContentPreferences.Preference;
import org.ldp4j.application.CreationPreferences.InteractionModel;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.IndividualVisitor;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.validation.ValidationConstraint;
import org.ldp4j.application.data.validation.ValidationConstraintFactory;
import org.ldp4j.application.data.validation.Validator.ValidatorBuilder;
import org.ldp4j.application.domain.LDP;
import org.ldp4j.application.domain.RDF;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.resource.Container;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.template.BasicContainerTemplate;
import org.ldp4j.application.template.ContainerTemplate;
import org.ldp4j.application.template.DirectContainerTemplate;
import org.ldp4j.application.template.IndirectContainerTemplate;
import org.ldp4j.application.template.MembershipAwareContainerTemplate;
import org.ldp4j.application.template.ResourceTemplate;
import org.ldp4j.application.template.TemplateVisitor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

abstract class DefaultPublicContainer<T extends ContainerTemplate> extends DefaultPublicRDFSource implements PublicContainer {

	private final class IndividualFilter implements IndividualVisitor {
		private final List<Individual<?, ?>> toRemove;

		private IndividualFilter(List<Individual<?, ?>> toRemove) {
			this.toRemove = toRemove;
		}

		@Override
		public void visitManagedIndividual(ManagedIndividual individual) {
			ManagedIndividualId id = individual.id();
			if(!isSelf(id)) {
				toRemove.add(individual);
			}
		}

		@Override
		public void visitLocalIndividual(LocalIndividual individual) {
			toRemove.add(individual);
		}

		@Override
		public void visitExternalIndividual(ExternalIndividual individual) {
			toRemove.add(individual);
		}
	}

	private final Class<? extends T> templateClass;

	protected DefaultPublicContainer(ApplicationContext applicationContext, Endpoint endpoint, Class<? extends T> templateClass) {
		super(applicationContext, endpoint);
		this.templateClass = templateClass;
	}

	protected final T containerTemplate() {
		ResourceTemplate template = template();
		if(!this.templateClass.isInstance(template)) {
			throw new IllegalStateException("Invalid container template exception: expected an instance of class "+this.templateClass.getCanonicalName()+" but got an instance of "+template.getClass().getCanonicalName());
		}
		return this.templateClass.cast(template);
	}

	@Override
	protected final DataSet resourceData(ContentPreferences contentPreferences) throws ApplicationExecutionException {
		DataSet dataSet = super.resourceData(contentPreferences);
		if(contentPreferences.isMinimalInclusionRequired()) {
			List<Individual<?,?>> toClean=new ArrayList<Individual<?,?>>();
			for(Individual<?,?> individual:dataSet) {
				individual.accept(new IndividualFilter(toClean));
			}
			for(Individual<?,?> individual:toClean) {
				cleanIndividual(individual);
			}
		}
		return dataSet;
	}

	@Override
	protected void fillInMetadata(ContentPreferences contentPreferences,Individual<?, ?> individual, Context ctx) {
		super.fillInMetadata(contentPreferences,individual,ctx);
		individual.
			addValue(
				ctx.property(RDF.TYPE),
				ctx.reference(LDP.CONTAINER));
		if(contentPreferences.mayInclude(Preference.CONTAINMENT_TRIPLES)) {
			for(PublicResource member:members()) {
				individual.addValue(
					ctx.property(LDP.CONTAINS),
					ctx.newIndividual(member));
			}
		}
	}

	@Override
	protected void configureValidationConstraints(ValidatorBuilder builder, Individual<?, ?> individual, DataSet metadata) {
		super.configureValidationConstraints(builder, individual, metadata);
		URI propertyId = LDP.CONTAINS.as(URI.class);
		Property property = individual.property(propertyId);
		ValidationConstraint<Property> constraint=null;
		if(property!=null) {
			constraint=ValidationConstraintFactory.readOnlyProperty(property);
		} else {
			constraint=ValidationConstraintFactory.readOnlyProperty(propertyId);
		}
		builder.withPropertyConstraint(constraint);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Collection<PublicResource> members() {
		List<PublicResource> members=Lists.newArrayList();
		for(ResourceId memberId:resolveAs(Container.class).memberIds()) {
			members.add(createResource(memberId));
		}
		return Collections.unmodifiableList(members);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DefaultPublicResource createResource(DataSet dataSet, CreationPreferences preferences) throws ApplicationExecutionException, UnsupportedInteractionModelException {
		verifyInteractionModel(preferences.getInteractionModel());
		Resource resource=applicationContext().createResource(endpoint(),dataSet,preferences.getPath());
		return createResource(resource.id());
	}

	private void cleanIndividual(Individual<?,?> individual) {
		for(Property property:individual) {
			URI propertyId = property.predicate();
			for(Value value:property) {
				individual.removeValue(propertyId,value);
			}
		}
	}

	private boolean isSelf(ManagedIndividualId id) {
		ResourceId resourceId = super.endpoint().resourceId();
		return id.managerId().equals(resourceId.templateId()) && id.name().equals(resourceId.name());
	}

	private void verifyInteractionModel(InteractionModel interactionModel) throws UnsupportedInteractionModelException {
		if(interactionModel==null) {
			return;
		}
		final Set<InteractionModel> acceptable=Sets.newHashSet();
		containerTemplate().memberTemplate().accept(
			new TemplateVisitor() {
				@Override
				public void visitResourceTemplate(ResourceTemplate template) {
				}
				@Override
				public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
				}
				@Override
				public void visitDirectContainerTemplate(DirectContainerTemplate template) {
				}
				@Override
				public void visitBasicContainerTemplate(BasicContainerTemplate template) {
				}
				@Override
				public void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
					throw new AssertionError("We should not arrive here");
				}
				@Override
				public void visitContainerTemplate(ContainerTemplate template) {
					throw new AssertionError("We should not arrive here");
				}
			}
		);
		if(!acceptable.contains(interactionModel)) {
			throw new UnsupportedInteractionModelException(interactionModel,acceptable);
		}

	}

}
