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
package org.ldp4j.server.utils;

import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.ldp4j.server.utils.URIHelper;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class URIHelperTest {

	private static URI uri(String uri) {
		return URI.create(uri);
	}

	private URI p0=uri("http://www.example.org/l1/l2/l3/");
	private URI p1=uri("http://www.example.org/l1/l2/");
	private URI p2=uri("http://www.example.org/l1/");
	private URI p3=uri("http://www.example.org/");

	@Test
	public void testGetParents$hashURI() throws Exception {
		URI base=uri("http://www.example.org/l1/l2/l3#fragment");
		List<URI> parents = URIHelper.getParents(base);
		assertThat(parents,notNullValue());
		assertThat(parents,contains(base,this.p1,this.p2,this.p3));
	}

	@Test
	public void testGetParents$slashURI_open() throws Exception {
		URI base=uri("http://www.example.org/l1/l2/l3/fragment");
		List<URI> parents = URIHelper.getParents(base);
		assertThat(parents,notNullValue());
		assertThat(parents,contains(base,this.p0,this.p1,this.p2,this.p3));
	}

	@Test
	public void testGetParents$slashURI_close() throws Exception {
		URI base=uri("http://www.example.org/l1/l2/l3/fragment/");
		List<URI> parents = URIHelper.getParents(base);
		assertThat(parents,notNullValue());
		assertThat(parents,contains(base,this.p0,this.p1,this.p2,this.p3));
	}

	@Test
	public void testGetParents$mixedURI() throws Exception {
		URI base=uri("http://www.example.org/l1/l2/l3/#fragment");
		List<URI> parents = URIHelper.getParents(base);
		assertThat(parents,notNullValue());
		assertThat(parents,contains(base,this.p0,this.p1,this.p2,this.p3));
	}

}