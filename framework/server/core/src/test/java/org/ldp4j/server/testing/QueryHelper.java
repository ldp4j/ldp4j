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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.testing;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Maps;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public final class QueryHelper {

	public final class QueryBuilder {

		private QueryBuilder() {
		}

		public QueryHelper fromResource(String resource) throws IOException {
			return fromString(TestingUtil.loadResource(resource));
		}

		public QueryHelper fromResource(URI resource) throws IOException {
			try(InputStream is=resource.toURL().openStream()) {
				return fromString(IOUtils.toString(is));
			}
		}

		public QueryHelper fromString(String query) {
			QueryHelper.this.rawQuery=query;
			return QueryHelper.this;
		}
	}

	public static abstract class ResultProcessor<T> {

		private QuerySolution solution;

		public void handle(QuerySolution solution) {
			this.solution=solution;
			this.processSolution();
		}

		public final Literal literal(String binding) {
			if(!this.solution.contains(binding)) {
				return null;
			}
			RDFNode node = this.solution.get(binding);
			if(!node.canAs(Literal.class)) {
				throw new IllegalStateException("Binding '"+binding+"' is not a literal");
			}
			return node.asLiteral();
		}

		public Resource resource(String binding) {
			if(!this.solution.contains(binding)) {
				return null;
			}
			RDFNode node = this.solution.get(binding);
			if(!node.canAs(Resource.class)) {
				throw new IllegalStateException("Binding '"+binding+"' is not a resource");
			}
			return node.asResource();
		}

		protected abstract void processSolution();

		public abstract T getResult();

	}

	private final Map<String,String> params;

	private String rawQuery;

	private Model model;

	private QueryHelper() {
		this.params=Maps.newLinkedHashMap();
	}

	private Query build() {
		String resolvedQuery=this.rawQuery;
		for(Entry<String,String> entry:this.params.entrySet()) {
			resolvedQuery=
				TestingUtil.
					interpolate(
						resolvedQuery,
						entry.getKey(),
						entry.getValue());
		}
		return QueryFactory.create(resolvedQuery);
	}

	private QueryHelper withParam(String param, String value) {
		this.params.put(param, value);
		return this;
	}

	public QueryBuilder withQuery() {
		return new QueryBuilder();
	}

	public QueryHelper withModel(Model model) {
		this.model=model;
		return this;
	}

	public QueryHelper withBlankNodeParam(String param, String bNodeId) {
		return withParam(param,"_:"+bNodeId);
	}

	public QueryHelper withURIRefParam(String param, String uri) {
		return withParam(param,"<"+uri+">");
	}

	public <T> T select(ResultProcessor<T> collector) {
		try(QueryExecution execution = QueryExecutionFactory.create(build(),this.model)) {
			ResultSet resultSet = execution.execSelect();
			while(resultSet.hasNext()) {
				collector.handle(resultSet.next());
			}
			return collector.getResult();
		}
	}

	public static QueryHelper newInstance() {
		return new QueryHelper();
	}

}
