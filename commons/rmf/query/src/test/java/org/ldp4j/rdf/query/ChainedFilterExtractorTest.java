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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class ChainedFilterExtractorTest {

	private ChainedFilterExtractor sut;

	protected String loadResource(String resource) throws IOException {
		return IOUtils.toString(ClassLoader.getSystemResource(resource));
	}

	@Before
	public void setUp() throws Exception {
		String varNamePattern = "(\\w+)";
		String varPattern = "(((\\?)|(\\$))(\\w+))";
		String selectPattern = "SELECT(?:(\\s+DISTINCT)?)((\\s+(((\\?)|(\\$))(\\w+)))+)";
		sut = new ChainedFilterExtractor(selectPattern,varPattern,varNamePattern);
	}
	

	@Test
	public void testExtract$found() throws Exception {
		assertThat(sut.extract("RANDOM CONTENT\t\nSELECT\n\t\t?who\n\t\t$g\n\t\t?mbox\nMORE RANDOM TEXT"),contains("who","g","mbox"));
		assertThat(sut.extract("RANDOM CONTENT\t\nSELECT\tDISTINCT\t\t?who\n\t\t?g\n\t\t?mbox\nMORE RANDOM TEXT"),contains("who","g","mbox"));
		assertThat(sut.extract(loadResource("unrestricted_graph_names.sparql")),contains("who","g","mbox"));
//		assertThat(sut.extract(loadResource("restricted_graph_names.sparql")),contains("nick"));
	}

	@Test
	public void testExtract$notFound() throws Exception {
		assertThat(sut.extract("RANDOM CONTENT\t\nselect\n\t\t?who\n\t\t?g\n\t\t?mbox\nMORE RANDOM TEXT"),empty());
	}

	
	@Test
	public void testExtractPrefix() throws Exception {
		System.out.println(extractUris("   <my%20uri>  <{not a uri}> <[\"sda\bsd\"]> <[\"sda\bsd\"]> <\\> <http://www.other.uri> ."));
		System.out.println(extractPrefixes("PREFIX uri1:<http://www.other.uri/1>\n\tPREFIX uri2:<http://www.other.uri/2>\tRANDOM"));
	}
	
	/**
	 * @param harness
	 * @return
	 */
	private Map<String, URI> extractPrefixes(String harness) {
		String prefixPattern = "PREFIX\\s+(\\w+):\\s*(<([^>]*)>)\\s+";
		Pattern p = Pattern.compile(prefixPattern);
		Matcher m = p.matcher(harness);
		Map<String,URI> table=new HashMap<String, URI>();
		while(m.find()) {
			String cURI = m.group(2);
			System.err.println("Trying URI '"+cURI+"'...");
			List<URI> uris=extractUris(cURI);
			if(uris.size()==1) {
				table.put(m.group(1),uris.get(0));
			}
		}
		return table;
	}

	/**
	 * @param text
	 * @return 
	 */
	private List<URI> extractUris(String text) {
		String iriPattern="<(([^<>\\\"{}\\|\\^`\\\\]|[\\x00-\\x20])*?)>";
		Pattern iriP=Pattern.compile(iriPattern);
		List<URI> uris=new ArrayList<URI>();
		Matcher matcher=iriP.matcher(text);
		while(matcher.find()) {
			try {
				uris.add(new URI(matcher.group(1)));
			} catch (URISyntaxException e) {
				System.err.println(String.format("Discarded '%s': %s",matcher.group(1),e.getMessage()));
			}
		}
		return uris;
	}
}
