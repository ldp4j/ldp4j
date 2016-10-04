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
 *   Artifact    : org.ldp4j.commons.rmf:integration-rdf4j:0.2.2
 *   Bundle      : integration-rdf4j-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.rdf4j;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Objects;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.ldp4j.rdf.io.Metadata;
import org.ldp4j.rdf.io.Module;
import org.ldp4j.rdf.io.Source;
import org.ldp4j.rdf.io.SourceFactory;

public final class RDF4JUtils {

	private static final String TURTLE_FORMAT_SHOULD_BE_SUPPORTED = "Turtle format should be supported";
	private static final String COULD_NOT_SERIALIZE_CONTENTS = "Could not serialize contents";
	private static final String COULD_NOT_CONNECT_TO_THE_REPOSITORY = "Could not connect to the repository";

	private RDF4JUtils() {
	}

	private static Resource[] toArray(Collection<? extends Resource> ctxs) {
		Objects.requireNonNull(ctxs, "Resources cannot be null");
		return ctxs.toArray(new Resource[ctxs.size()]);
	}

	public static void close(RepositoryConnection connection) throws RDF4JRepositoryFailure {
		if(connection==null) {
			return;
		}
		try {
			connection.close();
		} catch (RepositoryException e) {
			throw new RDF4JRepositoryFailure("Could not close connection",e);
		}
	}

	public static RepositoryConnection getConnection(Repository repository) throws RDF4JRepositoryFailure {
		Objects.requireNonNull(repository, "Repository cannot be null");
		if(!repository.isInitialized()) {
			throw new IllegalStateException("Template support has been disposed");
		}
		try {
			return repository.getConnection();
		} catch(RepositoryException e) {
			throw new RDF4JRepositoryFailure(COULD_NOT_CONNECT_TO_THE_REPOSITORY,e);
		}
	}

	public static String prettyPrint(RepositoryConnection connection, Collection<? extends Resource> ctxs) throws RDF4JUtilsException {
		return prettyPrint(connection,toArray(ctxs));
	}

	public static String prettyPrint(RepositoryConnection connection, Resource... contexts) throws RDF4JUtilsException {
		try {
			StringWriter writer = new StringWriter();
			connection.export(new TurtlePrettyPrinter(writer), contexts);
			return writer.toString();
		} catch(RepositoryException e) {
			throw new RDF4JRepositoryFailure(COULD_NOT_SERIALIZE_CONTENTS,e);
		} catch (RDFHandlerException e) {
			throw new RDF4JUtilsAssertionError("Unexpected pretty printer failure",e);
		} catch(UnsupportedRDFormatException e) {
			throw new RDF4JUtilsAssertionError(TURTLE_FORMAT_SHOULD_BE_SUPPORTED, e);
		}
	}

	public static <T> void loadSource(RepositoryConnection connection, String base, Source<T> content, URI ctx) throws RDF4JUtilsException {
		new ContentLoader(connection, base, ctx).process(content);
	}

	public static <T> void loadModule(RepositoryConnection connection, Module<T> content, URI ctx) throws RDF4JUtilsException {
		loadSource(connection,content.getBase(),content.getSource(),ctx);
	}

	public static <T> void load(RepositoryConnection connection, T content, URI ctx) throws RDF4JUtilsException {
		Objects.requireNonNull(ctx, "Context URI cannot be null");
		loadSource(connection,ctx.toString(),SourceFactory.create(content,Metadata.DEFAULT),ctx);
	}

}