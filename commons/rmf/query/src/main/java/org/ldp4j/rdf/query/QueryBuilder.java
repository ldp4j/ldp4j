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
package org.ldp4j.rdf.query;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.ldp4j.commons.Collector;
import org.ldp4j.rdf.Node;
import org.ldp4j.rdf.Triple;

public final class QueryBuilder {

	public static final class SelectQueryBuilder extends FillableQueryTemplateBuilder<SelectQueryBuilder> {
		
		private static final class SelectQueryImpl<R> implements SelectQuery<R> {

			private final FillableQueryTemplate template;
			private final QueryResultBuilderFactory<R, Map<String, Node>> factory;
			private final ImmutableBindingMap bindingMap;

			private SelectQueryImpl(FillableQueryTemplate template, QueryResultBuilderFactory<R, Map<String, Node>> factory, ImmutableBindingMap bindings) {
				this.template = template;
				this.factory = factory;
				this.bindingMap = bindings;
			}

			@Override
			public QueryTemplate getTemplate() {
				return template;
			}

			@Override
			public QueryResultBuilder<R,Map<String,Node>> newResultBuilder() {
				return factory.create();
			}

			@Override
			public Set<String> getVariables() {
				return bindingMap.getBindingNames();
			}

			@Override
			public <T extends Node> boolean isCompatibleValue(String variable, T value) {
				return bindingMap.isCompatibleValue(variable, value);
			}
		}

		private ImmutableBindingMap bindingMap;

		private SelectQueryBuilder() {
			this.bindingMap=new ImmutableBindingMap();
		}
		
		public SelectQueryBuilder withBinding(String variable, Class<? extends Node> clazz) {
			this.bindingMap=bindingMap.addBinding(variable, clazz);
			return this;
		}
		
		public <R> SelectQuery<R> build(final QueryResultBuilderFactory<R,Map<String,Node>> factory) {
			final FillableQueryTemplate template=new FillableQueryTemplate();
			populateFillable(template);
			return new SelectQueryImpl<R>(template, factory,bindingMap);
		}

		@Override
		protected SelectQueryBuilder getBuilder() {
			return this;
		}

	}

	public static final class DescribeQueryBuilder extends FillableQueryTemplateBuilder<DescribeQueryBuilder> {
		
		private static final class DescribeQueryImpl<R> implements DescribeQuery<R> {

			private final QueryResultBuilderFactory<R, Triple> factory;
			private final FillableQueryTemplate template;

			private DescribeQueryImpl(
					QueryResultBuilderFactory<R, Triple> factory,
					FillableQueryTemplate template) {
				this.factory = factory;
				this.template = template;
			}

			@Override
			public QueryTemplate getTemplate() {
				return template;
			}

			@Override
			public QueryResultBuilder<R, Triple> newResultBuilder() {
				return factory.create();
			}
		}

		public <R> DescribeQuery<R> build(final QueryResultBuilderFactory<R,Triple> factory) {
			final FillableQueryTemplate template=new FillableQueryTemplate();
			populateFillable(template);
			return new DescribeQueryImpl<R>(factory, template);
		}

		@Override
		protected DescribeQueryBuilder getBuilder() {
			return this;
		}

	}

	public static final class AskQueryBuilder extends FillableQueryTemplateBuilder<AskQueryBuilder> {
		
		private static final class AskQueryImpl<R> implements AskQuery<R> {

			private final FillableQueryTemplate template;
			private final QueryResultBuilderFactory<R, Boolean> factory;

			private AskQueryImpl(FillableQueryTemplate template,
					QueryResultBuilderFactory<R, Boolean> factory) {
				this.template = template;
				this.factory = factory;
			}

			@Override
			public QueryTemplate getTemplate() {
				return template;
			}

			@Override
			public QueryResultBuilder<R,Boolean> newResultBuilder() {
				return factory.create();
			}
		}

		public <R> AskQuery<R> build(final QueryResultBuilderFactory<R,Boolean> factory) {
			final FillableQueryTemplate template=new FillableQueryTemplate();
			populateFillable(template);
			return new AskQueryImpl<R>(template, factory);
		}

		@Override
		protected AskQueryBuilder getBuilder() {
			return this;
		}

	}

	public abstract static class FillableQueryTemplateBuilder<T extends FillableQueryTemplateBuilder<T>> extends QueryTemplateBuilder<T> {
		
		private String rawQuery;

		private FillableQueryTemplateBuilder() {
		}

		public T withTemplate(String template) {
			this.rawQuery = template;
			return getBuilder();
		}

		protected void populateFillable(FillableQueryTemplate template) {
			if(this.rawQuery==null) {
				throw new IllegalStateException("No raw query template has been provided");
			}
			populateTemplate(template);
			template.setTemplate(rawQuery);
		}

	}
	
	public abstract static class QueryTemplateBuilder<T extends QueryTemplateBuilder<T>> {

		private Collector<String> namedGraphs;
		private String defaultGraphName;
		
		private QueryTemplateBuilder() {
			this.namedGraphs=Collector.newCollector();
		}

		protected abstract T getBuilder();

		public T usesDefaultGraph(String defaultGraphName) {
			this.defaultGraphName=defaultGraphName;
			return getBuilder();
		}
		
		public T withRequiredNamedGraphs(String... namedGraphs) {
			this.namedGraphs.collect(namedGraphs);
			return getBuilder();
		}
		
		public T withRequiredNamedGraphs(Collection<String> namedGraphs) {
			this.namedGraphs.collect(namedGraphs);
			return getBuilder();
		}
		
		public T withRequiredNamedGraphs(Iterable<String> namedGraphs) {
			this.namedGraphs.collect(namedGraphs);
			return getBuilder();
		}
		
		protected void populateTemplate(AbstractQueryTemplate template) {
			template.setDefaultGraph(defaultGraphName);
			template.setNamedGraphs(namedGraphs.asSet());
		}
		
	}
	
	private QueryBuilder() {
	}

	public static SelectQueryBuilder newSelectQuery() {
		return new SelectQueryBuilder();
	}
	
	public static AskQueryBuilder newAskQuery() {
		return new AskQueryBuilder();
	}

	public static DescribeQueryBuilder newDescribeQuery() {
		return new DescribeQueryBuilder();
	}

}
