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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-core:0.2.2
 *   Bundle      : rmf-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf;

import java.io.StringWriter;
import java.net.URI;

import org.junit.Test;
import static org.ldp4j.rdf.util.RDFModelDSL.*;

public class FormatSupportTest {

	@Test
	public void testJSON() throws Exception {
		Iterable<Triple> content=
				tripleSet(
					triple(
						uriRef("example"),
						uriRef("http://www.example.org/vocab#size"),
						literal(3)),
					triple(
						uriRef("example"),
						uriRef("http://www.example.org/vocab#property"),
						uriRef("example/test")),
					triple(
						uriRef("example/test"),
						uriRef("http://www.example.org/vocab#propertyInverse"),
						uriRef("example")),
					triple(
						uriRef("example"),
						uriRef("http://www.example.org/vocab#otherProperty"),
						literal("27"))
				);
		RDFContext rdfContext = RDFContext.createContext(URI.create("http://www.example.org/"));
		StringWriter writer = new StringWriter();
		rdfContext.serialize(content, Format.JSON_LD, writer);
		System.out.println(writer.toString()); 
	}
	
}
