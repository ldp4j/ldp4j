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

import org.ldp4j.rdf.io.Module;
import org.ldp4j.rdf.query.DataSet;
import org.ldp4j.rdf.query.Query;
import org.ldp4j.rdf.query.QueryProcessingException;
import org.ldp4j.rdf.query.QueryProcessor;
import org.ldp4j.rdf.query.QueryTemplate;

public final class SesameQueryProcessor<R,S> implements QueryProcessor<R,S> {

	private final Query<R,S> query;

	public SesameQueryProcessor(Query<R,S> query) {
		this.query = query;
	}
	
	private QuerySupport getSupport(QueryTemplate template,DataSet dataSets) throws QueryProcessingException {
		try {
			QuerySupport support=QuerySupport.newInstance(template);
			support.addDefaultGraph(dataSets.getDefaultGraph());
			for(String graphName:template.getRequiredNamedGraphs()) {
				Module<?> namedGraph = dataSets.getNamedGraph(graphName);
				if(namedGraph==null) {
					throw new QueryProcessingException(String.format("Required named graph '%s' is not available",graphName));
				}
				support.addNamedGraph(graphName, namedGraph);
			}
			return support;
		} catch (InvalidContentsException e) {
			throw new QueryProcessingException(e);
		}
	}

	@Override
	public R process(DataSet dataSets) throws QueryProcessingException {
		QuerySupport support = getSupport(query.getTemplate(),dataSets);
		try {
			return QueryExecutionStrategy.createStrategy(query).execute(support);
		} catch (QueryExecutionException e) {
			throw new QueryProcessingException(e);
		} finally {
			support.dispose();
		}
	}

	@Override
	public Query<R,S> getQuery() {
		return query;
	}
	
}