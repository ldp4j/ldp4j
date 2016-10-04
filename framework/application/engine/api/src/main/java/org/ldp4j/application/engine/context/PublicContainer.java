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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-api:0.2.2
 *   Bundle      : ldp4j-application-engine-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.context;

import java.util.Collection;

import org.ldp4j.application.data.DataSet;

public interface PublicContainer extends PublicRDFSource {

	Collection<PublicResource> members();

	/**
	 * Create a resource using the specified data set and preferences.
	 *
	 * @param dataSet
	 *            the data for the new resource
	 * @param preferences
	 *            the preferences to use for creating the resource
	 * @return the create public resource
	 * @throws ApplicationExecutionException
	 *             if the application fails to create the resource
	 * @throws UnsupportedInteractionModelException
	 *             if the preferred interaction model is not supported
	 * @throws InvalidIndirectIdentifierException
	 *             if not valid indirect identifier is specified when trying to
	 *             create a member in an indirect container
	 */
	PublicResource createResource(DataSet dataSet, CreationPreferences preferences)
			throws 	ApplicationExecutionException;

}