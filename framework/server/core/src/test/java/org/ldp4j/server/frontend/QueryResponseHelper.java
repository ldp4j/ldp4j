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
package org.ldp4j.server.frontend;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.ldp4j.application.ext.Query;
import org.ldp4j.application.sdk.QueryBuilder;
import org.ldp4j.server.testing.QueryHelper;
import org.ldp4j.server.testing.QueryHelper.ResultProcessor;
import org.ldp4j.server.testing.TestingUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

final class QueryResponseHelper {

	private QueryResponseHelper() {
	}

	static Query getQuery(URL contextURL, String path, String data) throws IOException {
		return
			QueryHelper.
				newInstance().
					withModel(
						TestingUtil.
							asModel(data,contextURL,path)).
					withQuery().
						fromResource("queries/query_parameters.sparql").
					select(
						new ResultProcessor<Query>() {
							private QueryBuilder builder=QueryBuilder.newInstance();
							@Override
							protected void processSolution() {
								this.builder.
									withParameter(
										literal("parameterName").getString(),
										literal("rawValue").getString());
							}
							@Override
							public Query getResult() {
								return this.builder.build();
							}
						}
					);
	}

	static List<String> getResources(URL contextURL, String path, String data) throws IOException {
		return
			QueryHelper.
				newInstance().
					withModel(
						TestingUtil.
							asModel(data,contextURL,path)).
					withQuery().
						fromResource("queries/resources.sparql").
					select(
						new ResultProcessor<List<String>>() {
							private Builder<String>  builder=ImmutableList.<String>builder();
							@Override
							protected void processSolution() {
								this.builder.add(resource("resource").toString());
							}
							@Override
							public List<String> getResult() {
								return this.builder.build();
							}
						}
					);
	}

}
