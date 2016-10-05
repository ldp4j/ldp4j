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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.ldp4j.rdf.query.FillableQueryTemplate;

public class FillableQueryTemplateTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testFillInQuery$() throws Exception {
		String rawTemplate = loadResource("query_template_ng.sparql");
		FillableQueryTemplate sut=new FillableQueryTemplate();
		sut.setTemplate(rawTemplate);
		sut.setNamedGraphs(new HashSet<String>(Arrays.asList("graph1","graph2")));
		Map<String,String> mappings=new HashMap<String,String>();
		mappings.put("graph1", "http://example.org/foaf/aliceFoaf");
		mappings.put("graph2", "http://example.org/foaf/bobFoaf");
		String result = sut.instantiate(mappings);
		System.out.println(String.format("Raw template:\n%s\nMappings: %s\nResult:\n%s",rawTemplate,mappings,result));
		assertThat(result,equalTo(loadResource("resolved_query_template_ng.sparql")));
	}

	private String loadResource(String resource) throws IOException {
		InputStream is = ClassLoader.getSystemResourceAsStream(resource);
		try {
			return IOUtils.toString(is);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

}
