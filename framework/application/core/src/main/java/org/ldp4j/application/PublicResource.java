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
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.validation.ValidationConstraintFactory;
import org.ldp4j.application.data.validation.ValidationReport;
import org.ldp4j.application.data.validation.Validator;
import org.ldp4j.application.data.validation.Validator.ValidatorBuilder;
import org.ldp4j.application.domain.LDP;
import org.ldp4j.application.domain.RDF;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.ext.InvalidContentException;
import org.ldp4j.application.resource.Attachment;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.template.AttachedTemplate;
import org.ldp4j.application.vocabulary.Term;

public abstract class PublicResource extends Public {
	
	private static final URI HAS_ATTACHMENT = URI.create("http://www.ldp4j.org/ns/application#hasAttachment");

	protected static final class Context {
		
		private final DataSet dataSet;
	
		private Context(DataSet dataSet) {
			this.dataSet = dataSet;
		}
		
		public URI property(Term term) {
			return term.as(URI.class);
		}
		
		public Value reference(URI externalIndividual) {
			return dataSet.individual(externalIndividual, ExternalIndividual.class);
		}

		public Value reference(Term term) {
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
			ResourceId resourceId = resource.id();
			ManagedIndividualId id = ManagedIndividualId.createId(resourceId.name(), resourceId.templateId());
			return newIndividual(id);
		}

		public Value resourceSurrogate(PublicResource member) {
			ResourceId resourceId = member.id();
			ManagedIndividualId surrogateId = ManagedIndividualId.createId(resourceId.name(), resourceId.templateId());
			return dataSet.individualOfId(surrogateId);
		}

		public Value value(Object value) {
			return DataSetUtils.newLiteral(value);
		}
	}

	protected PublicResource(ApplicationContext applicationContext, Endpoint endpoint) {
		super(applicationContext, endpoint);
	}

	public final Map<String,PublicResource> attachments() {
		Map<String,PublicResource> result=new HashMap<String, PublicResource>();
		for(Attachment attachment:resolveAs(Resource.class).attachments()) {
			result.put(attachment.id(), createResource(attachment.resourceId()));
		}
		return result;
	}
	
	public final DataSet entity() throws ApplicationExecutionException {
		return entity(ContentPreferences.defaultPreferences());
	}
	
	/**
	 * @return
	 */
	protected DataSet metadata() {
		DataSet metadata = 
			DataSetFactory.
				createDataSet(id().name()); 
	
		Context ctx = new Context(metadata);
		ManagedIndividualId id=
			ManagedIndividualId.
				createId(
					id().name(), 
					id().templateId());
	
		fillInMetadata(
			ContentPreferences.defaultPreferences(),
			ctx.newIndividual(id),
			ctx);
		return metadata;
	}

	protected DataSet resourceData(ContentPreferences contentPreferences) throws ApplicationExecutionException {
		return applicationContext().getResource(endpoint());
	}
	
	public final DataSet entity(ContentPreferences contentPreferences) throws ApplicationExecutionException {
		DataSet dataSet=resourceData(contentPreferences);
		DataSet representation = DataSetFactory.createDataSet(id().name()); 
		DataSetUtils.
			merge(
				dataSet, 
				representation);
		Context ctx = new Context(representation);
		ManagedIndividualId id=
			ManagedIndividualId.
				createId(
					id().name(), 
					id().templateId());
		fillInMetadata(
			contentPreferences,
			ctx.newIndividual(id),
			ctx);
		return representation;
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
			populateAdditionalMetadata(contentPreferences,individual, ctx, entry.getValue());
		}
	}

	private void populateAdditionalMetadata(final ContentPreferences contentPreferences, final Individual<?, ?> individual, final Context ctx, PublicResource resource) {
		resource.accept(
			new PublicVisitor<Void>() {
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
					resource.fillInMemberMetadata(contentPreferences,individual,ctx);
					return null;
				}
				@Override
				public Void visitIndirectContainer(PublicIndirectContainer resource) {
					resource.fillInMemberMetadata(contentPreferences,individual,ctx);
					return null;
				}
			}
		);
	}

	protected void configureValidationConstraints(ValidatorBuilder builder, Individual<?,?> individual, DataSet metadata) {
		builder.withPropertyConstraint(ValidationConstraintFactory.mandatoryPropertyValues(individual.property(RDF.TYPE.as(URI.class))));
		for(Entry<String, PublicResource> entry:attachments().entrySet()) {
			AttachedTemplate attachedTemplate = template().attachedTemplate(entry.getKey());
			URI propertyId = attachedTemplate.predicate().or(HAS_ATTACHMENT);
			builder.withPropertyConstraint(ValidationConstraintFactory.readOnlyProperty(individual.property(propertyId)));
			configureAdditionalValidationConstraints(builder,individual,metadata,entry.getValue());
		}
	}

	private void configureAdditionalValidationConstraints(final ValidatorBuilder builder, final Individual<?, ?> individual, final DataSet metadata, PublicResource resource) {
		resource.accept(
			new PublicVisitor<Void>() {
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
					resource.configureMemberValidationConstraints(builder,individual,metadata);
					return null;
				}
				@Override
				public Void visitIndirectContainer(PublicIndirectContainer resource) {
					resource.configureMemberValidationConstraints(builder,individual,metadata);
					return null;
				}
			}
		);
	}

	public void delete() throws ApplicationExecutionException {
		applicationContext().deleteResource(endpoint());
	}

	public void modify(DataSet dataSet) throws ApplicationExecutionException {
		DataSet metadata = metadata();

		// First check that the framework/protocol metadata has not been messed
		// around
		validate(dataSet, metadata);

		// Second, remove the framework/protocol metadata from the
		// representation that will be handed to the application
		DataSetUtils.remove(metadata, dataSet);

		// Third, request the modification using the cleansed and validated data
		applicationContext().modifyResource(endpoint(),dataSet);
	}

	private void validate(DataSet dataSet, DataSet metadata) throws ApplicationExecutionException {
		ManagedIndividualId id = ManagedIndividualId.createId(id().name(),id().templateId());
		Individual<?,?> individual=metadata.individualOfId(id);

		ValidatorBuilder builder=Validator.builder();

		configureValidationConstraints(builder,individual,metadata);
		
		Validator validator=builder.build();

		ValidationReport report = validator.validate(dataSet);
		if(!report.isValid()) {
			InvalidContentException error = new InvalidContentException("Protocol/framework managed metadata validation failed: "+report.validationFailures());
			throw new ApplicationExecutionException("Protocol/framework managed metadata validation failure",error);
		}
	}

}
