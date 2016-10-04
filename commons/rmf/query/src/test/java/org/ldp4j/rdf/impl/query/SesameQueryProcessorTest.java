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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.ldp4j.rdf.BlankNode;
import org.ldp4j.rdf.Node;
import org.ldp4j.rdf.NodeVisitor;
import org.ldp4j.rdf.URIRef;
import org.ldp4j.rdf.impl.query.SesameQueryProcessor;
import org.ldp4j.rdf.io.ModuleFactory;
import org.ldp4j.rdf.query.ImmutableDataSet;
import org.ldp4j.rdf.query.QueryBuilder;
import org.ldp4j.rdf.query.QueryProcessingException;
import org.ldp4j.rdf.query.QueryResultBuilder;
import org.ldp4j.rdf.query.QueryResultBuilderFactory;
import org.ldp4j.rdf.query.QueryResultHandler;
import org.ldp4j.rdf.query.SelectQuery;

public class SesameQueryProcessorTest {

	private CustomProcessor sut;

	private static class CustomProcessor {

		private static final class CustomQueryResultBuilderFactory implements QueryResultBuilderFactory<Map<String, List<String>>, Map<String, Node>> {
			private static final NodeVisitor<String> INPUT_VISITOR = new NodeVisitor<String>() {
				@Override
				public String visitURIRef(URIRef node, String defaultResult) {
					return node.getIdentity().toString();
				}
			};
			private static final NodeVisitor<String> INFERENCE_VISITOR = new NodeVisitor<String>() {
				@Override
				public String visitBlankNode(BlankNode node, String defaultResult) {
					return node.getIdentity().toString();
				}
			};
			@Override
			public QueryResultBuilder<Map<String,List<String>>, Map<String, Node>> create() {
				return new QueryResultBuilder<Map<String,List<String>>, Map<String,Node>>() {

					private final Map<String,List<String>> usedResources=new HashMap<String,List<String>>();

					@Override
					public QueryResultHandler<Map<String, Node>> getHandler() {

						return new QueryResultHandler<Map<String,Node>>() {
							
							@Override
							public void startResult() {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void handleSolution(Map<String, Node> result) {
								List<String> inputs = getInferenceInputs(result.get(INFERENCE));
								inputs.add(getNewInput(result.get(INPUT)));
							}

							private String getNewInput(Node input) {
								return input.accept(INPUT_VISITOR);
							}

							private List<String> getInferenceInputs(Node inference) {
								String parsedInference = inference.accept(INFERENCE_VISITOR);
								List<String> inputs=usedResources.get(parsedInference);
								if(inputs==null) {
									inputs=new ArrayList<String>();
									usedResources.put(parsedInference, inputs);
								}
								return inputs;
							}

							@Override
							public void endResult() {
								// TODO Auto-generated method stub
								
							}
						};
					}

					@Override
					public Map<String, List<String>> getResult() {
						return Collections.unmodifiableMap(usedResources);
					}
				};
			}
		}

		private SesameQueryProcessor<Map<String,List<String>>,Map<String,Node>> queryProcessor;

		private static final String INFERENCE = "inference";
		private static final String INPUT = "input";

		public CustomProcessor(String rawQuery) {
			SelectQuery<Map<String,List<String>>> query=
				QueryBuilder.
					newSelectQuery().
						withTemplate(rawQuery).
						withBinding(INPUT, URIRef.class).
						withBinding(INFERENCE, BlankNode.class).
						build(new CustomQueryResultBuilderFactory());
			this.queryProcessor = new SesameQueryProcessor<Map<String,List<String>>,Map<String,Node>>(query);
		}

		public Map<String,List<String>> getUsedResources(String base, String source) throws QueryProcessingException {
			return queryProcessor.process(ImmutableDataSet.newInstance().withDefaultGraph(ModuleFactory.create(base, source)));
		}
		
	}
	
	@Before
	public void setUp() throws Exception {
		sut = new CustomProcessor(loadResource("relaxed_valid_query_template.sparql"));
	}
	
	@Test
	public void testParse$validInference() throws Exception {
		Map<String, List<String>> result = sut.getUsedResources("http://www.example.org/entities/",loadResource("valid_query_data.ttl"));
		assertThat(result,notNullValue());
		assertThat(result.size(),equalTo(1));
		Collection<List<String>> values = result.values();
		assertThat(values.iterator().next(),hasItems("http://www.example.org/entities/dataSet1","http://www.example.org/entities/vocabulary1"));
	}

	@Test(expected=QueryProcessingException.class)
	public void testParse$invalidInference() throws Exception {
		sut.getUsedResources("http://www.example.org/entities/",loadResource("invalid_query_data.ttl"));
	}

	private String loadResource(String resource) throws IOException {
		return IOUtils.toString(ClassLoader.getSystemResource("org/ldp4j/rdf/query/"+resource));
	}

}
