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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-query:0.2.2
 *   Bundle      : rmf-query-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.impl.query;

import java.util.Objects;

import org.ldp4j.rdf.query.Query;
import org.ldp4j.rdf.query.QueryResultBuilder;
import org.ldp4j.rdf.query.QueryResultHandler;
import org.ldp4j.rdf.query.SelectQuery;
import org.openrdf.repository.RepositoryConnection;

abstract class QueryExecutionStrategy<R,S> {

	R execute(QuerySupport support) throws QueryExecutionException {
		Objects.requireNonNull(support,"Query support cannot be null");
		RepositoryConnection connection=support.getConnection();
		Query<R, S> query = getQuery();
		try {
			QueryResultBuilder<R,S> builder = query.newResultBuilder();
			doExecute(
				connection,
				getInstantiatedQuery(query,support),
				builder.getHandler());
			return builder.getResult();
		} finally {
			support.close(connection);
		}
	}

	private String getInstantiatedQuery(Query<R, S> query, QuerySupport support) {
		return query.getTemplate().instantiate(support.getUsedNamedGraphs());
	}

	protected abstract Query<R,S> getQuery();

	protected abstract void doExecute(
			RepositoryConnection connection,
			String instantiatedQuery,
			QueryResultHandler<S> handler)
			throws QueryExecutionException;

	@SuppressWarnings("unchecked")
	public static <R,S> QueryExecutionStrategy<R,S> createStrategy(final Query<R,S> query) {
		QueryExecutionStrategy<R,S> strategy;
		if(query instanceof SelectQuery) {
			strategy=(QueryExecutionStrategy<R,S>)new SelectQueryExecutionStrategy<R>((SelectQuery<R>)query);
		} else {
			strategy=new QueryExecutionStrategy<R,S>() {
				@Override
				protected void doExecute(RepositoryConnection connection,
						String instantiatedQuery, QueryResultHandler<S> handler)
						throws QueryExecutionException {
					throw new QueryExecutionException("Query type '"+query.getClass().getCanonicalName()+"' is not supported");
				}

				@Override
				protected Query<R, S> getQuery() {
					return query;
				}
			};
		}
		return strategy;
	}

}
