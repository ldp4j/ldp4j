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
 *   Artifact    : org.ldp4j.commons.rmf:integration-sesame:0.2.2
 *   Bundle      : integration-sesame-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.sesame;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Objects;

import org.ldp4j.rdf.io.Metadata;
import org.ldp4j.rdf.io.Module;
import org.ldp4j.rdf.io.Source;
import org.ldp4j.rdf.io.SourceFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.UnsupportedRDFormatException;

public final class SesameUtils {

	private static final String TURTLE_FORMAT_SHOULD_BE_SUPPORTED = "Turtle format should be supported";
	private static final String COULD_NOT_SERIALIZE_CONTENTS = "Could not serialize contents";
	private static final String COULD_NOT_CONNECT_TO_THE_REPOSITORY = "Could not connect to the repository";

	private SesameUtils() {
	}

	private static Resource[] toArray(Collection<? extends Resource> ctxs) {
		Objects.requireNonNull(ctxs, "Resources cannot be null");
		return ctxs.toArray(new Resource[ctxs.size()]);
	}

	public static void close(RepositoryConnection connection) throws SesameRepositoryFailure {
		if(connection==null) {
			return;
		}
		try {
			connection.close();
		} catch (RepositoryException e) {
			throw new SesameRepositoryFailure("Could not close connection",e);
		}
	}

	public static RepositoryConnection getConnection(Repository repository) throws SesameRepositoryFailure {
		Objects.requireNonNull(repository, "Repository cannot be null");
		if(!repository.isInitialized()) {
			throw new IllegalStateException("Template support has been disposed");
		}
		try {
			return repository.getConnection();
		} catch(RepositoryException e) {
			throw new SesameRepositoryFailure(COULD_NOT_CONNECT_TO_THE_REPOSITORY,e);
		}
	}

	public static String prettyPrint(RepositoryConnection connection, Collection<? extends Resource> ctxs) throws SesameUtilsException {
		return prettyPrint(connection,toArray(ctxs));
	}

	public static String prettyPrint(RepositoryConnection connection, Resource... contexts) throws SesameUtilsException {
		try {
			StringWriter writer = new StringWriter();
			connection.export(new TurtlePrettyPrinter(writer), contexts);
			return writer.toString();
		} catch(RepositoryException e) {
			throw new SesameRepositoryFailure(COULD_NOT_SERIALIZE_CONTENTS,e);
		} catch (RDFHandlerException e) {
			throw new SesameUtilsAssertionError("Unexpected pretty printer failure",e);
		} catch(UnsupportedRDFormatException e) {
			throw new SesameUtilsAssertionError(TURTLE_FORMAT_SHOULD_BE_SUPPORTED, e);
		}
	}

	public static <T> void loadSource(RepositoryConnection connection, String base, Source<T> content, URI ctx) throws SesameUtilsException {
		new ContentLoader(connection, base, ctx).process(content);
	}

	public static <T> void loadModule(RepositoryConnection connection, Module<T> content, URI ctx) throws SesameUtilsException {
		loadSource(connection,content.getBase(),content.getSource(),ctx);
	}

	public static <T> void load(RepositoryConnection connection, T content, URI ctx) throws SesameUtilsException {
		Objects.requireNonNull(ctx, "Context URI cannot be null");
		loadSource(connection,ctx.toString(),SourceFactory.create(content,Metadata.DEFAULT),ctx);
	}

}