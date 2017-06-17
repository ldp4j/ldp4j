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

import java.util.Collections;
import java.util.Map;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.validation.Validator.ValidatorBuilder;
import org.ldp4j.application.engine.context.ApplicationExecutionException;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.application.engine.context.PublicResourceVisitor;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.resource.ResourceId;

final class DefaultGonePublicResource extends DefaultPublicResource {

	private static final String THE_ENDPOINT_IS_GONE = "The endpoint is gone";

	DefaultGonePublicResource(DefaultApplicationContext applicationContext, Endpoint endpoint) {
		super(applicationContext,endpoint);
	}

	@Override
	public Map<String, PublicResource> attachments() {
		return Collections.emptyMap();
	}

	@Override
	public ManagedIndividualId individualId() {
		throw new UnsupportedOperationException(THE_ENDPOINT_IS_GONE);
	}

	@Override
	public <T> T accept(PublicResourceVisitor<T> visitor) {
		throw new UnsupportedOperationException(THE_ENDPOINT_IS_GONE);
	}

	@Override
	public DataSet entity(ContentPreferences contentPreferences) throws ApplicationExecutionException {
		throw new UnsupportedOperationException(THE_ENDPOINT_IS_GONE);
	}

	@Override
	public DataSet query(Query query, ContentPreferences contentPreferences) throws ApplicationExecutionException {
		throw new UnsupportedOperationException(THE_ENDPOINT_IS_GONE);
	}

	@Override
	public void delete() throws ApplicationExecutionException {
		throw new UnsupportedOperationException(THE_ENDPOINT_IS_GONE);
	}

	@Override
	public void modify(DataSet dataSet) throws ApplicationExecutionException {
		throw new UnsupportedOperationException(THE_ENDPOINT_IS_GONE);
	}

	@Override
	public DataSet getConstraintReport(String failureId) {
		throw new UnsupportedOperationException(THE_ENDPOINT_IS_GONE);
	}

	@Override
	protected DataSet metadata() {
		throw new UnsupportedOperationException(THE_ENDPOINT_IS_GONE);
	}

	@Override
	protected DataSet resourceData(ContentPreferences contentPreferences) throws ApplicationExecutionException {
		throw new UnsupportedOperationException(THE_ENDPOINT_IS_GONE);
	}

	@Override
	protected ResourceId id() {
		throw new UnsupportedOperationException(THE_ENDPOINT_IS_GONE);
	}

	@Override
	protected void fillInMetadata(ContentPreferences contentPreferences, Individual<?, ?> individual, Context ctx) {
		throw new UnsupportedOperationException(THE_ENDPOINT_IS_GONE);
	}

	@Override
	protected void configureValidationConstraints(ValidatorBuilder builder, Individual<?, ?> individual, DataSet metadata) {
		throw new UnsupportedOperationException(THE_ENDPOINT_IS_GONE);
	}

	@Override
	protected ManagedIndividualId indirectIndividualId() {
		throw new UnsupportedOperationException(THE_ENDPOINT_IS_GONE);
	}

}