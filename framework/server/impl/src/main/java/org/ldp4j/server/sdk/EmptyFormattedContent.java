/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.sdk;

import java.io.IOException;

import org.ldp4j.server.Format;
import org.ldp4j.server.IContent;

public final class EmptyFormattedContent implements IContent {
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static final String EMPTY_TURTLE = 
		"@prefix dc: <http://purl.org/dc/elements/1.1/>."+LINE_SEPARATOR+
		"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.";
	private static final String EMPTY_RDF_XML = 
	"<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""+LINE_SEPARATOR+
	"\txmlns:dc=\"http://purl.org/dc/elements/1.1/\">"+LINE_SEPARATOR+
	"</rdf:RDF>";

	private final Format format;

	public EmptyFormattedContent(Format format) {
		this.format = format;
	}

	@Override
	public <S> S serialize(Class<S> clazz) throws IOException {
		String resource=null;
		if(Format.Turtle.equals(format)) {
			resource=EMPTY_TURTLE;
		} else {
			resource=EMPTY_RDF_XML;
		}
		return new StringContent(resource).serialize(clazz);
	}
}