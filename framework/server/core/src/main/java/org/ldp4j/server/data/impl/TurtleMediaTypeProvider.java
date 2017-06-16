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
package org.ldp4j.server.data.impl;

import java.io.IOException;
import java.io.StringWriter;

import javax.ws.rs.core.MediaType;

import org.ldp4j.rdf.Format;
import org.ldp4j.rdf.RDFContext;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.impl.UnmarshallOptions;
import org.ldp4j.server.data.spi.ContentTransformationException;
import org.ldp4j.server.data.spi.Context;

public class TurtleMediaTypeProvider extends AbstractMediaTypeProvider {

	private static final MediaType MEDIA_TYPE = new MediaType("text","turtle");

	public TurtleMediaTypeProvider() {
		super(MEDIA_TYPE);
	}
	
	@Override
	public Iterable<Triple> unmarshallContent(Context context, String content, MediaType type) throws ContentTransformationException {
		try {
			RDFContext rdfContext = RDFContext.createContext(context.getBase());
			rdfContext.setOption(UnmarshallOptions.TRIPLE_ORDERING, UnmarshallOptions.Ordering.KEEP_TRIPLE_ORDER);
			return rdfContext.deserialize(content, Format.TURTLE);
		} catch (IOException e) {
			throw new ContentTransformationException("Could not unmarshall contents",e);
		}
	}

	@Override
	public String marshallContent(Context context, Iterable<Triple> content, MediaType type) throws ContentTransformationException {
		try {
			RDFContext rdfContext = RDFContext.createContext(context.getBase());
			rdfContext.setNamespaces(context.getNamespaces());
			StringWriter writer = new StringWriter();
			rdfContext.serialize(content, Format.TURTLE, writer);
			return writer.toString();
		} catch (IOException e) {
			throw new ContentTransformationException("Could not marshall contents",e);
		}
	}

}