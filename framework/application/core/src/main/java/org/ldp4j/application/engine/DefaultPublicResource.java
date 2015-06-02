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
package org.ldp4j.application.engine;

import java.net.URI;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.validation.Validator.ValidatorBuilder;
import org.ldp4j.application.engine.context.ApplicationExecutionException;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.application.engine.endpoint.Endpoint;
import org.ldp4j.application.engine.resource.ResourceId;
import org.ldp4j.application.vocabulary.Term;

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

	protected DefaultPublicResource(DefaultApplicationContext applicationContext, Endpoint endpoint) {
		super(applicationContext,endpoint);
	}

	protected abstract DataSet metadata();

	protected abstract DataSet resourceData(ContentPreferences contentPreferences) throws ApplicationExecutionException;

	protected abstract ResourceId id();

	protected abstract void fillInMetadata(ContentPreferences contentPreferences, final Individual<?, ?> individual, final Context ctx);

	protected abstract void configureValidationConstraints(ValidatorBuilder builder, Individual<?,?> individual, DataSet metadata);

	protected abstract ManagedIndividualId indirectIndividualId();

}
