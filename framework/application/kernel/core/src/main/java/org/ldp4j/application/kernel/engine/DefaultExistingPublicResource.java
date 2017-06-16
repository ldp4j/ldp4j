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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSets;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.data.validation.ValidationConstraintFactory;
import org.ldp4j.application.data.validation.ValidationReport;
import org.ldp4j.application.data.validation.Validator;
import org.ldp4j.application.data.validation.Validator.ValidatorBuilder;
import org.ldp4j.application.engine.context.ApplicationExecutionException;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.PublicBasicContainer;
import org.ldp4j.application.engine.context.PublicDirectContainer;
import org.ldp4j.application.engine.context.PublicIndirectContainer;
import org.ldp4j.application.engine.context.PublicRDFSource;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.application.engine.context.PublicResourceVisitor;
import org.ldp4j.application.ext.InconsistentContentException;
import org.ldp4j.application.ext.InvalidContentException;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.resource.Attachment;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.template.AttachedTemplate;
import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.application.vocabulary.RDF;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

abstract class DefaultExistingPublicResource extends DefaultPublicResource {

	private static final class AdditionalValidationConstraintConfigurator implements PublicResourceVisitor<Void> {

		private final DataSet metadata;
		private final Individual<?, ?> individual;
		private final ValidatorBuilder builder;

		private AdditionalValidationConstraintConfigurator(DataSet metadata,
				Individual<?, ?> individual, ValidatorBuilder builder) {
			this.metadata = metadata;
			this.individual = individual;
			this.builder = builder;
		}

		@Override
		public Void visitRDFSource(PublicRDFSource resource) {
			// Nothing to do
			return null;
		}

		@Override
		public Void visitBasicContainer(PublicBasicContainer resource) {
			// Nothing to do
			return null;
		}

		@Override
		public Void visitDirectContainer(PublicDirectContainer resource) {
			((DefaultPublicDirectContainer)resource).configureMemberValidationConstraints(builder,individual,metadata);
			return null;
		}

		@Override
		public Void visitIndirectContainer(PublicIndirectContainer resource) {
			((DefaultPublicIndirectContainer)resource).configureMemberValidationConstraints(builder,individual,metadata);
			return null;
		}
	}

	private static final class AdditionalMetadataPopulator implements PublicResourceVisitor<Void> {

		private final Context ctx;
		private final ContentPreferences contentPreferences;
		private final Individual<?, ?> individual;

		private AdditionalMetadataPopulator(Context ctx,
				ContentPreferences contentPreferences,
				Individual<?, ?> individual) {
			this.ctx = ctx;
			this.contentPreferences = contentPreferences;
			this.individual = individual;
		}

		@Override
		public Void visitRDFSource(PublicRDFSource resource) {
			// Nothing to do
			return null;
		}

		@Override
		public Void visitBasicContainer(PublicBasicContainer resource) {
			// Nothing to do
			return null;
		}

		@Override
		public Void visitDirectContainer(PublicDirectContainer resource) {
			((DefaultPublicDirectContainer)resource).fillInMemberMetadata(contentPreferences,individual,ctx);
			return null;
		}

		@Override
		public Void visitIndirectContainer(PublicIndirectContainer resource) {
			((DefaultPublicIndirectContainer)resource).fillInMemberMetadata(contentPreferences,individual,ctx);
			return null;
		}
	}

	private static final URI HAS_ATTACHMENT = URI.create("http://www.ldp4j.org/ns/application#hasAttachment");

	private final ManagedIndividualId individualId;

	protected DefaultExistingPublicResource(DefaultApplicationContext applicationContext, Endpoint endpoint) {
		super(applicationContext,endpoint);
		this.individualId=
			ManagedIndividualId.
				createId(
					id().name(),
					id().templateId());
	}

	@Override
	public final ManagedIndividualId individualId() {
		return this.individualId;
	}

	@Override
	public final Map<String,PublicResource> attachments() {
		Map<String,PublicResource> result=new HashMap<String, PublicResource>();
		for(Attachment attachment:resolveAs(Resource.class).attachments()) {
			result.put(attachment.id(), createResource(attachment.resourceId()));
		}
		return result;
	}

	@Override
	public final DataSet entity(ContentPreferences contentPreferences) throws ApplicationExecutionException {
		DataSet dataSet=resourceData(contentPreferences);
		DataSet representation = DataSets.createDataSet(id().name());
		DataSets.
			merge(
				dataSet,
				representation);
		Context ctx = new Context(representation);
		fillInMetadata(
			contentPreferences,
			ctx.newIndividual(individualId()),
			ctx);
		return representation;
	}

	@Override
	public final DataSet query(Query query, ContentPreferences contentPreferences) throws ApplicationExecutionException {
		return applicationContext().query(endpoint(),query);
	}

	@Override
	public final void delete() throws ApplicationExecutionException {
		applicationContext().deleteResource(endpoint());
	}

	@Override
	public final void modify(DataSet dataSet) throws ApplicationExecutionException {
		DataSet metadata = metadata();
		try {
			// First check that the framework/protocol metadata has not been messed
			// around
			validate(dataSet, metadata);

			// Second, remove the framework/protocol metadata from the
			// representation that will be handed to the application
			DataSets.remove(metadata, dataSet);

			// Third, request the modification using the cleansed and validated data
			applicationContext().modifyResource(endpoint(),dataSet);
		} catch (InvalidContentException error) {
			applicationContext().registerContentFailure(endpoint(),error);
			throw new ApplicationExecutionException("Protocol/framework managed metadata validation failure",error);
		}
	}

	@Override
	public DataSet getConstraintReport(String constraintsId) throws ApplicationExecutionException {
		return applicationContext().getConstraintReport(endpoint(),constraintsId);
	}

	@Override
	protected DataSet metadata() {
		DataSet metadata =
			DataSets.
				createDataSet(id().name());

		Context ctx = new Context(metadata);
		ManagedIndividualId id = individualId();

		fillInMetadata(
			ContentPreferences.defaultPreferences(),
			ctx.newIndividual(id),
			ctx);
		return metadata;
	}

	@Override
	protected DataSet resourceData(ContentPreferences contentPreferences) throws ApplicationExecutionException {
		return applicationContext().getResource(endpoint());
	}

	@Override
	protected final ResourceId id() {
		return endpoint().resourceId();
	}

	@Override
	protected void fillInMetadata(ContentPreferences contentPreferences, final Individual<?, ?> individual, final Context ctx) {
		individual.
			addValue(
				ctx.property(RDF.TYPE),
				ctx.reference(LDP.RESOURCE));
		for(Entry<String, PublicResource> entry:attachments().entrySet()) {
			AttachedTemplate attachedTemplate = template().attachedTemplate(entry.getKey());
			individual.addValue(
				attachedTemplate.predicate().or(HAS_ATTACHMENT),
				ctx.newIndividual(entry.getValue()));
			populateAdditionalMetadata(contentPreferences,individual,ctx,entry.getValue());
		}
	}

	private void populateAdditionalMetadata(final ContentPreferences contentPreferences, final Individual<?, ?> individual, final Context ctx, PublicResource resource) {
		resource.accept(new AdditionalMetadataPopulator(ctx, contentPreferences, individual));
	}

	@Override
	protected void configureValidationConstraints(ValidatorBuilder builder, Individual<?,?> individual, DataSet metadata) {
		builder.withPropertyConstraint(ValidationConstraintFactory.mandatoryPropertyValues(individual.property(RDF.TYPE.as(URI.class))));
		Multimap<URI,AttachedTemplate> attachmentMap=LinkedHashMultimap.create();
		for(AttachedTemplate attachedTemplate:template().attachedTemplates()) {
			URI propertyId = attachedTemplate.predicate().or(HAS_ATTACHMENT);
			attachmentMap.put(propertyId, attachedTemplate);
		}
		for(Entry<URI, Collection<AttachedTemplate>> entry:attachmentMap.asMap().entrySet()) {
			URI propertyId=entry.getKey();
			Property property = individual.property(propertyId);
			if(property!=null) {
				configurePropertyValidationConstraints(builder,individual,metadata,property,entry.getValue());
			} else {
				builder.withPropertyConstraint(ValidationConstraintFactory.readOnlyProperty(individual.id(),propertyId));
			}
		}
	}

	private void configurePropertyValidationConstraints(
			ValidatorBuilder builder,
			Individual<?, ?> individual,
			DataSet metadata,
			Property property,
			Collection<AttachedTemplate> value) {
		builder.withPropertyConstraint(ValidationConstraintFactory.readOnlyProperty(property));
		for(AttachedTemplate attachedTemplate:value) {
			PublicResource resource = attachments().get(attachedTemplate.id());
			if(resource!=null) {
				configureAdditionalValidationConstraints(builder,individual,metadata,resource);
			}
		}
	}

	private void configureAdditionalValidationConstraints(final ValidatorBuilder builder, final Individual<?, ?> individual, final DataSet metadata, PublicResource resource) {
		resource.accept(new AdditionalValidationConstraintConfigurator(metadata,individual,builder));
	}

	private void validate(DataSet dataSet, DataSet metadata) throws InvalidContentException {
		ManagedIndividualId id = individualId();
		Individual<?,?> individual=metadata.individualOfId(id);

		ValidatorBuilder builder=Validator.builder();

		configureValidationConstraints(builder,individual,metadata);

		Validator validator=builder.build();

		ValidationReport report = validator.validate(dataSet);
		if(!report.isValid()) {
			// TODO: Add validation constraints
			Constraints constraints = Constraints.constraints();
			throw new InconsistentContentException("Protocol/framework managed metadata validation failed: "+report.validationFailures(),constraints);
		}
	}

	@Override
	protected final ManagedIndividualId indirectIndividualId() {
		ManagedIndividualId result=this.individualId;
		URI indirectId = resolveAs(Resource.class).indirectId();
		if(indirectId!=null) {
			result=ManagedIndividualId.createId(indirectId, result);
		}
		return result;
	}

}
