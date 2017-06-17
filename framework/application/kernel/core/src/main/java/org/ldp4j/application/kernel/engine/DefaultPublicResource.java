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
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literals;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.validation.Validator.ValidatorBuilder;
import org.ldp4j.application.engine.context.ApplicationExecutionException;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.vocabulary.Term;

abstract class DefaultPublicResource extends DefaultPublicEndpoint implements PublicResource {

	protected static final class Context {

		private final DataSet dataSet;

		protected Context(DataSet dataSet) {
			this.dataSet = dataSet;
		}

		public URI property(Term term) {
			return term.as(URI.class);
		}

		public ExternalIndividual reference(URI externalIndividual) {
			return this.dataSet.individual(externalIndividual, ExternalIndividual.class);
		}

		public ExternalIndividual reference(Term term) {
			return reference(term.as(URI.class));
		}

		public ExternalIndividual newIndividual(URI id) {
			return this.dataSet.individual(id, ExternalIndividual.class);
		}

		@SuppressWarnings("rawtypes")
		public LocalIndividual newIndividual(Name<?> id) {
			return this.dataSet.individual((Name)id, LocalIndividual.class);
		}

		public ManagedIndividual newIndividual(ManagedIndividualId id) {
			return this.dataSet.individual(id, ManagedIndividual.class);
		}

		public ManagedIndividual newIndividual(PublicResource resource) {
			ResourceId resourceId = ((DefaultPublicResource)resource).id();
			ManagedIndividualId id = ManagedIndividualId.createId(resourceId.name(), resourceId.templateId());
			return newIndividual(id);
		}

		public Value resourceSurrogate(PublicResource member) {
			ResourceId resourceId = ((DefaultPublicResource)member).id();
			ManagedIndividualId surrogateId = ManagedIndividualId.createId(resourceId.name(), resourceId.templateId());
			return this.dataSet.individualOfId(surrogateId);
		}

		public Value value(Object value) {
			return Literals.newLiteral(value);
		}
	}

	protected DefaultPublicResource(DefaultApplicationContext applicationContext, Endpoint endpoint) {
		super(applicationContext,endpoint);
	}

	protected abstract DataSet metadata();

	protected abstract DataSet resourceData(ContentPreferences contentPreferences) throws ApplicationExecutionException;

	protected abstract ResourceId id();

	protected abstract void fillInMetadata(ContentPreferences contentPreferences, Individual<?, ?> individual, Context ctx);

	protected abstract void configureValidationConstraints(ValidatorBuilder builder, Individual<?,?> individual, DataSet metadata);

	protected abstract ManagedIndividualId indirectIndividualId();

}
