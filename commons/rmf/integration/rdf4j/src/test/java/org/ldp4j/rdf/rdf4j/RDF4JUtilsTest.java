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
 *   Artifact    : org.ldp4j.commons.rmf:integration-rdf4j:0.2.1
 *   Bundle      : integration-rdf4j-0.2.1.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.rdf4j;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Test;


public class RDF4JUtilsTest extends AbstractRDFTestCase {

	protected URL getExamplesResource() {
		return ClassLoader.getSystemResource("turtles.ttl");
	}

	@Test
	public void shouldPrettyPrint() throws Exception {
		RepositoryConnection connection=getConnection();
		URI ctx=connection.getValueFactory().createURI("http://example.org/one");
		URI ctx_res=connection.getValueFactory().createURI("http://example.org/two");
		RDF4JUtils.load(connection, getExamplesResource(), ctx);
		List<URI> ctxs=new ArrayList<URI>();
		ctxs.add(ctx);
		String result = RDF4JUtils.prettyPrint(connection, ctxs);
		dumpTurtle(result);
		RDF4JUtils.load(connection, result, ctx_res);
	}

}