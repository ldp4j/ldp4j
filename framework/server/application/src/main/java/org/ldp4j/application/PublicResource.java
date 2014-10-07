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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-application:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-application-1.0.0-SNAPSHOT.jar
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
import org.ldp4j.application.domain.LDP;
import org.ldp4j.application.domain.RDF;
import org.ldp4j.application.endpoint.Endpoint;
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
		DataSet dataSet=applicationContext().getResource(endpoint());
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
			ctx.newIndividual(id),
			ctx);
		return representation;
	}

	protected final ResourceId id() {
		return endpoint().resourceId();
	}

	protected void fillInMetadata(final Individual<?, ?> individual, final Context ctx) {
		individual.
			addValue(
				ctx.property(RDF.TYPE), 
				ctx.reference(LDP.RESOURCE));
		for(Entry<String, PublicResource> entry:attachments().entrySet()) {
			AttachedTemplate attachedTemplate = template().attachedTemplate(entry.getKey());
			individual.addValue(
				attachedTemplate.predicate().or(HAS_ATTACHMENT), 
				ctx.newIndividual(entry.getValue()));
			populateAdditionalMetadata(individual, ctx, entry.getValue());
		}
	}

	private void populateAdditionalMetadata(final Individual<?, ?> individual, final Context ctx, PublicResource resource) {
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
					resource.fillInMemberMetadata(individual, ctx);
					return null;
				}
				@Override
				public Void visitIndirectContainer(PublicIndirectContainer resource) {
					resource.fillInMemberMetadata(individual, ctx);
					return null;
				}
			}
		);
	}

	public void delete() throws ApplicationExecutionException {
		applicationContext().deleteResource(endpoint());
	}

	public void modify(DataSet dataSet) throws ApplicationExecutionException {
		applicationContext().modifyResource(endpoint(),dataSet);
	}

}
