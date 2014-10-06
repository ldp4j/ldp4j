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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.ldp4j.rdf.Format;
import org.ldp4j.rdf.RDFContext;
import org.ldp4j.rdf.Triple;
import org.ldp4j.server.Context;
import org.ldp4j.server.spi.ContentTransformationException;

import com.google.common.collect.ImmutableSet;

public class TurtleMediaTypeProvider extends AbstractMediaTypeProvider {

	private static final MediaType MEDIA_TYPE = new MediaType("text","turtle");

	private static final Set<MediaType> SUPPORTED_MEDIA_TYPES = ImmutableSet.<MediaType>builder().add(MEDIA_TYPE).build();

	@Override
	public Set<MediaType> getSupportedMediaTypes() {
		return SUPPORTED_MEDIA_TYPES;
	}

	@Override
	protected Iterable<Triple> doUnmarshallContent(Context context, String content, MediaType type) throws ContentTransformationException {
		try {
			RDFContext rdfContext = RDFContext.createContext(context.getBase());
			return rdfContext.deserialize(content, Format.TURTLE);
		} catch (IOException e) {
			throw new ContentTransformationException("Could not unmarshall contents",e);
		}
	}

	@Override
	protected String doMarshallContent(Context context, Iterable<Triple> content, MediaType type) throws ContentTransformationException {
		try {
			RDFContext rdfContext = RDFContext.createContext(context.getBase());
			StringWriter writer = new StringWriter();
			rdfContext.serialize(content, Format.TURTLE, writer);
			return writer.toString(); 
		} catch (IOException e) {
			throw new ContentTransformationException("Could not marshall contents",e);
		}
	}

}
