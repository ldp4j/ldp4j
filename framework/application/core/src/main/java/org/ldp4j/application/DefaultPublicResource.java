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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetFactory;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.data.validation.ValidationConstraintFactory;
import org.ldp4j.application.data.validation.ValidationReport;
import org.ldp4j.application.data.validation.Validator;
import org.ldp4j.application.data.validation.Validator.ValidatorBuilder;
import org.ldp4j.application.domain.LDP;
import org.ldp4j.application.domain.RDF;
import org.ldp4j.application.endpoint.Endpoint;
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
import org.ldp4j.application.resource.Attachment;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.template.AttachedTemplate;
import org.ldp4j.application.vocabulary.Term;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

abstract class DefaultPublicResource extends DefaultPublicEndpoint implements PublicResource {

	protected static final class Context {

		private final DataSet dataSet;

		private Context(DataSet dataSet) {
			this.dataSet = dataSet;
		}

		public URI property(Term term) {
			return term.as(URI.class);
		}

		public Individual<?,?> reference(URI externalIndividual) {
			return dataSet.individual(externalIndividual, ExternalIndividual.class);
		}

		public Individual<?,?> reference(Term term) {
			return reference(term.as(URI.class));
		}

		public Individual<?,?> newIndividual(URI id) {
			return dataSet.individual(id, ExternalIndividual.class);
		}

		@SuppressWarnings("rawtypes")
		public Individual<?,?> newIndividual(Name<?> id) {
			return dataSet.individual((Name)id, LocalIndividual.class);
		}

		public Individual<?,?> newIndividual(ManagedIndividualId id) {
			return dataSet.individual(id, ManagedIndividual.class);
		}

		public Individual<?,?> newIndividual(PublicResource resource) {
			ResourceId resourceId = ((DefaultPublicResource)resource).id();
			ManagedIndividualId id = ManagedIndividualId.createId(resourceId.name(), resourceId.templateId());
			return newIndividual(id);
		}

		public Value resourceSurrogate(PublicResource member) {
			ResourceId resourceId = ((DefaultPublicResource)member).id();
			ManagedIndividualId surrogateId = ManagedIndividualId.createId(resourceId.name(), resourceId.templateId());
			return dataSet.individualOfId(surrogateId);
		}

		public Value value(Object value) {
			return DataSetUtils.newLiteral(value);
		}
	}

	private static final URI HAS_ATTACHMENT = URI.create("http://www.ldp4j.org/ns/application#hasAttachment");
	private final ManagedIndividualId individualId;

	protected DefaultPublicResource(DefaultApplicationContext applicationContext, Endpoint endpoint) {
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
		DataSet representation = DataSetFactory.createDataSet(id().name());
		DataSetUtils.
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
			DataSetUtils.remove(metadata, dataSet);

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

	protected DataSet metadata() {
		DataSet metadata =
			DataSetFactory.
				createDataSet(id().name());

		Context ctx = new Context(metadata);
		ManagedIndividualId id = individualId();

		fillInMetadata(
			ContentPreferences.defaultPreferences(),
			ctx.newIndividual(id),
			ctx);
		return metadata;
	}

	protected DataSet resourceData(ContentPreferences contentPreferences) throws ApplicationExecutionException {
		return applicationContext().getResource(endpoint());
	}

	protected final ResourceId id() {
		return endpoint().resourceId();
	}

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
		resource.accept(
			new PublicResourceVisitor<Void>() {
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
		);
	}

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
				builder.withPropertyConstraint(ValidationConstraintFactory.readOnlyProperty(property));
				for(AttachedTemplate attachedTemplate:entry.getValue()) {
					PublicResource resource = attachments().get(attachedTemplate.id());
					if(resource!=null) {
						configureAdditionalValidationConstraints(builder,individual,metadata,resource);
					}
				}
			} else {
				builder.withPropertyConstraint(ValidationConstraintFactory.readOnlyProperty(individual.id(),propertyId));
			}
		}
	}

	private void configureAdditionalValidationConstraints(final ValidatorBuilder builder, final Individual<?, ?> individual, final DataSet metadata, PublicResource resource) {
		resource.accept(
			new PublicResourceVisitor<Void>() {
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
		);
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
			InvalidContentException error = new InconsistentContentException("Protocol/framework managed metadata validation failed: "+report.validationFailures(),constraints);
			throw error;
		}
	}

	protected final ManagedIndividualId indirectIndividualId() {
		ManagedIndividualId result=this.individualId;
		URI indirectId = resolveAs(Resource.class).indirectId();
		if(indirectId!=null) {
			result=ManagedIndividualId.createId(indirectId, result);
		}
		return result;
	}



}
