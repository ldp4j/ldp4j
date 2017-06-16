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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Test;
import org.ldp4j.application.ext.Query;
import org.ldp4j.server.testing.TestingUtil;

public class QueryResponseHelperTest {

	@Test
	public void validateQueryExtraction() throws MalformedURLException, IOException {
		Query query =
			QueryResponseHelper.
				getQuery(
					new URL("http://www.example.org/"),
					"resource/",
					TestingUtil.loadResource("queries/query_dataset.ttl"));
		assertThat(query.size(),equalTo(2));
		assertThat(query.getParameter("param1").rawValues(),contains("value1"));
		assertThat(query.getParameter("param2").rawValues(),contains("value2"));
	}

	@Test
	public void validateResourceExtraction() throws MalformedURLException, IOException {
		List<String> resources =
			QueryResponseHelper.
				getResources(
					new URL("http://www.example.org/"),
					"resource/",
					TestingUtil.loadResource("queries/query_dataset.ttl"));
		assertThat(resources,contains("http://localhost:62631/ldp4j-server-core/ldp4j/api/rootQueryableResource/"));
	}

}
