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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-examples:0.2.2
 *   Bundle      : ldp4j-application-examples-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.example;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.ext.InvalidQueryException;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.ext.Queryable;
import org.ldp4j.application.ext.annotations.Resource;
import org.ldp4j.application.session.ReadSession;
import org.ldp4j.application.session.ResourceSnapshot;

/**
 * An example resource handler that can be queried.
 */
@Resource(
	id=QueryableResourceHandler.ID
)
public class QueryableResourceHandler extends InMemoryResourceHandler implements Queryable {

	/**
	 * The identifier of the template defined by the handler.
	 */
	public static final String ID="queryableResourceTemplate";

	/**
	 * The query parameter name to provide to raise a failure in the processing
	 * of the query.
	 */
	public static final String FAILURE = "failure";

	/**
	 * Create a new instance.
	 */
	public QueryableResourceHandler() {
		super("QueryableResource");
	}

	/**
	 * Instead of retrieving the contents of the resource, return a description
	 * of the query received.
	 */
	@Override
	public DataSet query(ResourceSnapshot resource, Query query, ReadSession session) throws InvalidQueryException {
		return QuerySupport.getDescription(resource.name(), query);
	}

}