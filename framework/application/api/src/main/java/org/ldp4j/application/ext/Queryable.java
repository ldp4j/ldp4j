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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.ext;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;

/**
 * Interface to be implemented by {@link ResourceHandler} implementations that
 * also support resource deletion.
 */
public interface Queryable {

	/**
	 * Query the contents of a resource managed by a resource handler.
	 *
	 * @param resource
	 *            the resource that is to be queried.
	 * @param query
	 *            the query to be solved.
	 * @param session
	 *            the session to use for registering the side effects of the
	 *            operation.
	 * @return the data set that represents the specified resource query.
	 * @throws UnknownResourceException
	 *             if the specified resource is not managed by the resource
	 *             handler.
	 * @throws ApplicationRuntimeException
	 *             if an internal exception prevents the deletion of the
	 *             resource.
	 */
	DataSet query(ResourceSnapshot resource, Query query, WriteSession session)
				throws UnknownResourceException, ApplicationRuntimeException;

}