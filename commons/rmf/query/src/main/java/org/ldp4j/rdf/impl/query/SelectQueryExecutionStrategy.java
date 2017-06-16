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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ldp4j.rdf.Node;
import org.ldp4j.rdf.query.Query;
import org.ldp4j.rdf.query.QueryResultHandler;
import org.ldp4j.rdf.query.SelectQuery;
import org.ldp4j.rdf.sesame.SesameModelParser;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

final class SelectQueryExecutionStrategy<R> extends QueryExecutionStrategy<R,Map<String,Node>>{

	private final class TupleQueryResultHandlerAdapter implements TupleQueryResultHandler {

		private static final String EXPECTED_BINDINGS_MISSING = "Expected bindings missing";

		private final QueryResultHandler<Map<String,Node>> handler;
		private final SesameModelParser parser;

		public TupleQueryResultHandlerAdapter(QueryResultHandler<Map<String, Node>> handler) {
			this.parser=new SesameModelParser();
			this.handler = handler;
		}

		@Override
		public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
			if(!bindingNames.containsAll(query.getVariables())) {
				throw new TupleQueryResultHandlerException(EXPECTED_BINDINGS_MISSING);
			}
			handler.startResult();
		}

		@Override
		public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
			Map<String,Node> result=new HashMap<String,Node>();
			for(String varName:query.getVariables()) {
				Node node = parser.parseValue(bindingSet.getValue(varName));
				if(!query.isCompatibleValue(varName, node)) {
					throw new TupleQueryResultHandlerException("Incompatible value for binding '"+varName+"'");
				}
				result.put(varName, node);
			}
			if(!query.getVariables().equals(result.keySet())) {
				throw new TupleQueryResultHandlerException(EXPECTED_BINDINGS_MISSING);
			}
			handler.handleSolution(Collections.unmodifiableMap(result));
		}

		@Override
		public void handleLinks(List<String> linkUrls) {
			/**
			 * This method is not yet needed
			 */
		}

		@Override
		public void handleBoolean(boolean value) {
			/**
			 * This method is not yet needed
			 */
		}

		@Override
		public void endQueryResult() {
			handler.endResult();
		}
	}

	private final SelectQuery<R> query;

	SelectQueryExecutionStrategy(SelectQuery<R> query) {
		this.query = query;
	}

	@Override
	protected Query<R, Map<String, Node>> getQuery() {
		return query;
	}

	@Override
	protected void doExecute(
			RepositoryConnection connection,
			String instantiatedQuery,
			QueryResultHandler<Map<String, Node>> handler)
			throws QueryExecutionException {
		try {
			TupleQuery preparedQuery=
			connection.
				prepareTupleQuery(
					QueryLanguage.SPARQL,
					instantiatedQuery);
			TupleQueryResultHandler adapter=new TupleQueryResultHandlerAdapter(handler);
			preparedQuery.evaluate(adapter);
		} catch (MalformedQueryException e) {
			throw new QueryProcessingFailure(e);
		} catch (QueryEvaluationException e) {
			throw new QueryProcessingFailure(e);
		} catch (TupleQueryResultHandlerException e) {
			throw new QueryExecutionException(e);
		} catch (RepositoryException e) {
			throw new QueryTemplateSupportFailure("Unexpected Sesame failure",e);
		}
	}

}
