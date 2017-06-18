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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-server-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.frontend;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.ldp4j.server.testing.QueryHelper;
import org.ldp4j.server.testing.QueryHelper.ResultProcessor;
import org.ldp4j.server.testing.TestingUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

final class DynamicResponseHelper {

	private URL url;
	private String path;
	private String data;

	DynamicResponseHelper(URL url, String path, String data) {
		this.url = url;
		this.path = path;
		this.data = data;
	}

	private QueryHelper queryHelper() {
		return
			QueryHelper.
				newInstance().
					withModel(
						TestingUtil.
							asModel(this.data,this.url,relativePath())).
					withURIRefParam(
						"resource", absolutePath());
	}

	private String absolutePath() {
		return this.url+relativePath();
	}

	private String relativePath() {
		return "ldp4j/api/"+this.path;
	}

	List<String> getUpdates() throws IOException {
		return
			queryHelper().
				withQuery().
					fromResource("queries/updates.sparql").
				select(
					new ResultProcessor<List<String>>() {
						private Builder<String>  builder=ImmutableList.<String>builder();
						@Override
						protected void processSolution() {
							this.builder.add(literal("update").getLexicalForm().toString());
						}
						@Override
						public List<String> getResult() {
							return this.builder.build();
						}
					}
				);
	}

	String getResolution() throws IOException {
		return
			queryHelper().
				withQuery().
					fromResource("queries/resolution.sparql").
				select(
					new ResultProcessor<String>() {
						private String resolution;
						@Override
						protected void processSolution() {
							this.resolution=literal("resolution").getLexicalForm().toString();
						}
						@Override
						public String getResult() {
							return this.resolution;
						}
					}
				);
	}

	String getRoundtrip() throws IOException {
		return
			queryHelper().
				withQuery().
					fromResource("queries/roundtrip.sparql").
				select(
					new ResultProcessor<String>() {
						private String resolution;
						@Override
						protected void processSolution() {
							this.resolution=literal("roundtrip").getLexicalForm().toString();
						}
						@Override
						public String getResult() {
							return this.resolution;
						}
					}
				);
	}
}
