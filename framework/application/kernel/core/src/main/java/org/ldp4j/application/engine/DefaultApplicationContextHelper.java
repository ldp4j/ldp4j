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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-kernel-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine;

import java.net.URI;
import java.util.List;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.IndividualVisitor;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.NewIndividual;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.RelativeIndividual;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.ValueVisitor;
import org.ldp4j.application.engine.resource.Container;
import org.ldp4j.application.engine.resource.FeatureException;
import org.ldp4j.application.engine.resource.Resource;
import org.ldp4j.application.engine.session.WriteSessionConfiguration;
import org.ldp4j.application.engine.template.TemplateIntrospector;
import org.ldp4j.application.engine.template.TemplateManagementService;

import com.google.common.collect.Lists;

final class DefaultApplicationContextHelper {

	private static final class IndirectIdCollector implements ValueVisitor {

		private final class InnerVisitor implements IndividualVisitor {

			private final List<URI> ids;

			private InnerVisitor() {
				this.ids=Lists.newArrayList();
			}

			@Override
			public void visitManagedIndividual(ManagedIndividual individual) {
				// TODO: We should fail here
			}

			@Override
			public void visitLocalIndividual(LocalIndividual individual) {
				// TODO: We should fail here
			}

			@Override
			public void visitExternalIndividual(ExternalIndividual individual) {
				this.ids.add(individual.id());
			}

			@Override
			public void visitRelativeIndividual(RelativeIndividual individual) {
				// TODO: We should fail here
			}

			@Override
			public void visitNewIndividual( NewIndividual individual) {
				// TODO: We should fail here
			}
		}

		private final InnerVisitor innerVisitor;

		private IndirectIdCollector() {
			this.innerVisitor=new InnerVisitor();
		}

		@Override
		public void visitLiteral(Literal<?> value) {
			// TODO: We should fail here
		}

		@Override
		public void visitIndividual(Individual<?, ?> value) {
			value.accept(this.innerVisitor);
		}

		private List<URI> getCollectedIds() {
			return this.innerVisitor.ids;
		}

	}

	private static final URI NEW_RESOURCE_SURROGATE_ID = URI.create("");

	private final TemplateManagementService templateManagementService;

	private DefaultApplicationContextHelper(TemplateManagementService templateManagementService) {
		this.templateManagementService = templateManagementService;
	}

	WriteSessionConfiguration createConfiguration(Resource resource) {
		return
			WriteSessionConfiguration.
				builder().
					withTarget(resource).
					build();
	}

	WriteSessionConfiguration createConfiguration(Container container, DataSet dataSet, String desiredPath) throws FeatureException {
		return
			WriteSessionConfiguration.
				builder().
					withTarget(container).
					withPath(desiredPath).
					withIndirectId(getIndirectId(container, dataSet)).
					build();
	}

	private URI getIndirectId(Container container, DataSet dataSet) {
		TemplateIntrospector introspector=
			TemplateIntrospector.
				newInstance(
					this.templateManagementService.
						templateOfId(container.id().templateId()));
		if(!introspector.isIndirectContainer()) {
			return null;
		}
		Property property = getInsertedContentRelation(dataSet,introspector.getInsertedContentRelation());
		if(property==null) {
			// TODO: Check if this situation is a failure
			return null;
		}
		final List<URI> indirectIdentities= findIndirectIds(property);
		if(indirectIdentities.size()==1) {
			return indirectIdentities.get(0);
		}
		// TODO: We should fail here, either because no valid identifiers were
		// specified or because to many of them were specified
		return null;
	}

	private Property getInsertedContentRelation(DataSet dataSet, URI insertedContentRelation) {
		NewIndividual individual = dataSet.individual(NEW_RESOURCE_SURROGATE_ID, NewIndividual.class);
		return individual.property(insertedContentRelation);
	}

	private List<URI> findIndirectIds(Property property) {
		IndirectIdCollector collector=new IndirectIdCollector();
		for(Value v:property) {
			v.accept(collector);
		}
		return collector.getCollectedIds();
	}

	static DefaultApplicationContextHelper create(TemplateManagementService templateManagementService) {
		return new DefaultApplicationContextHelper(templateManagementService);
	}

}
