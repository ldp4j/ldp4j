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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.ext;

/**
 * This exception may be thrown when a query is not understood by a resource
 * handler that supports querying.
 *
 * @see Queryable
 */
public class InvalidQueryException extends ApplicationUsageException {

	private static final long serialVersionUID = 954186954933177035L;

	private final Query query;

	/**
	 * Create a new instance with a message, a cause, and a query.
	 *
	 * @param message
	 *            the description of the failure
	 * @param cause
	 *            the underlying cause of the exception
	 * @param query
	 *            the query that caused the failure
	 */
	public InvalidQueryException(String message, Throwable cause, Query query) {
		super(message, cause);
		this.query = query;
	}

	/**
	 * Create a new instance with a message and a query.
	 *
	 * @param message
	 *            the description of the failure
	 * @param query
	 *            the query that caused the failure
	 */
	public InvalidQueryException(String message, Query query) {
		this(message,null,query);
	}

	/**
	 * Create a new instance with a cause and a query.
	 *
	 * @param cause
	 *            the underlying cause of the exception
	 * @param query
	 *            the query that caused the failure
	 */
	public InvalidQueryException(Throwable cause, Query query) {
		this("Invalid query",cause,query);
	}


	/**
	 * Return the {@code Query} instance that caused the exception.
	 *
	 * @return the {@code Query} instance that caused the exception
	 */
	public Query getQuery() {
		return query;
	}

}
