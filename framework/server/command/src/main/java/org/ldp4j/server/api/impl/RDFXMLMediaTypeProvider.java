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
package org.ldp4j.server.api.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.ldp4j.rdf.Format;
import org.ldp4j.rdf.RDFContext;
import org.ldp4j.rdf.Triple;
import org.ldp4j.server.api.spi.ContentTransformationException;

import com.google.common.collect.ImmutableSet;

public class RDFXMLMediaTypeProvider extends AbstractMediaTypeProvider {

	// TODO: Find out a way for determining the base to be used in the serialization/deserialization...
	private static final String DEFAULT_BASE = "http://www.ldp4j.org";

	private static final MediaType MEDIA_TYPE = new MediaType("application","rdf+xml");
	private static final Set<MediaType> SUPPORTED_MEDIA_TYPES = ImmutableSet.<MediaType>builder().add(MEDIA_TYPE).build();

	@Override
	public Set<MediaType> getSupportedMediaTypes() {
		return SUPPORTED_MEDIA_TYPES;
	}

	@Override
	protected Iterable<Triple> doUnmarshallContent(String content, MediaType type) throws ContentTransformationException {
		try {
			RDFContext context = RDFContext.createContext(URI.create(DEFAULT_BASE));
			return context.deserialize(content, Format.RDF_XML);
		} catch (IOException e) {
			throw new ContentTransformationException("Could not unmarshall contents",e);
		}
	}

	@Override
	protected String doMarshalContent(Iterable<Triple> content, MediaType type) throws ContentTransformationException {
		try {
			RDFContext context = RDFContext.createContext(URI.create(DEFAULT_BASE));
			StringWriter writer = new StringWriter();
			context.serialize(content, Format.RDF_XML, writer);
			return writer.toString(); 
		} catch (IOException e) {
			throw new ContentTransformationException("Could not marshall contents",e);
		}
	}

}