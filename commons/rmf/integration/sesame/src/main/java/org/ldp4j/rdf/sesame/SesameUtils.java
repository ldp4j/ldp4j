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
 *   Artifact    : org.ldp4j.commons.rmf:integration-sesame:1.0.0-SNAPSHOT
 *   Bundle      : integration-sesame-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.sesame;

import java.io.StringWriter;
import java.util.Collection;

import org.ldp4j.commons.Assertions;
import org.ldp4j.rdf.io.Metadata;
import org.ldp4j.rdf.io.Module;
import org.ldp4j.rdf.io.Source;
import org.ldp4j.rdf.io.SourceFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;

public final class SesameUtils {

	private static final String COULD_NOT_REMOVE_CONTEXTS = "Could not remove contexts";
	private static final String TURTLE_FORMAT_SHOULD_BE_SUPPORTED = "Turtle format should be supported";
	private static final String COULD_NOT_SERIALIZE_CONTENTS = "Could not serialize contents";
	private static final String COULD_NOT_CONNECT_TO_THE_REPOSITORY = "Could not connect to the repository";

	private SesameUtils() {
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
		Assertions.notNull(repository, "repository");
		if(!repository.isInitialized()) {
			throw new IllegalStateException("Template support has been disposed");
		}
		try {
			return repository.getConnection();
		} catch(RepositoryException e) {
			throw new SesameRepositoryFailure(COULD_NOT_CONNECT_TO_THE_REPOSITORY,e);
		}
	}

	private static Resource[] toArray(Collection<? extends Resource> ctxs) {
		Assertions.notNull(ctxs, "ctxs");
		return ctxs.toArray(new Resource[ctxs.size()]);
	}

	public static String serialize(Repository repository, Resource target, Resource... contexts) throws SesameUtilsException {
		RepositoryConnection connection=getConnection(repository);
		try {
			return serialize(connection,contexts);
		} finally {
			close(connection);
		}
	}

	public static String serialize(Repository repository, Resource... contexts) throws SesameUtilsException {
		RepositoryConnection connection=getConnection(repository);
		try {
			return serialize(connection,contexts);
		} finally {
			close(connection);
		}
	}

	public static String serialize(Repository repository, Collection<? extends Resource> ctxs) throws SesameUtilsException {
		return serialize(repository, toArray(ctxs));
	}

	public static <T> void load(Repository repository, T content, URI ctx) throws SesameUtilsException {
		RepositoryConnection connection=getConnection(repository);
		try {
			load(connection, content, ctx);
		} finally {
			close(connection);
		}
	}

	public static void remove(Repository repository, Resource... contexts) throws SesameRepositoryFailure {
		RepositoryConnection connection=getConnection(repository);
		try {
			remove(connection, contexts);
		} finally {
			close(connection);
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

	public static String serialize(RepositoryConnection connection, Collection<? extends Resource> ctxs) throws SesameUtilsException {
		return serialize(connection,toArray(ctxs));
	}

	public static String serialize(RepositoryConnection connection, Resource... contexts) throws SesameUtilsException {
		try {
			StringWriter writer = new StringWriter();
			connection.export(Rio.createWriter(RDFFormat.TURTLE, writer), contexts);
			return writer.toString();
		} catch (RepositoryException e) {
			throw new SesameRepositoryFailure(COULD_NOT_SERIALIZE_CONTENTS, e);
		} catch (RDFHandlerException e) {
			throw new SesameUtilsAssertionError(COULD_NOT_SERIALIZE_CONTENTS,e);
		} catch (UnsupportedRDFormatException e) {
			throw new SesameUtilsAssertionError(TURTLE_FORMAT_SHOULD_BE_SUPPORTED,e);
		}
	}

	public static <T> void loadSource(RepositoryConnection connection, String base, Source<T> content, URI ctx) throws SesameUtilsException {
		new ContentLoader(connection, base, ctx).process(content);
	}

	public static <T> void loadModule(RepositoryConnection connection, Module<T> content, URI ctx) throws SesameUtilsException {
		loadSource(connection,content.getBase(),content.getSource(),ctx);
	}

	public static <T> void load(RepositoryConnection connection, T content, URI ctx) throws SesameUtilsException {
		Assertions.notNull(ctx, "ctx");
		loadSource(connection,ctx.toString(),SourceFactory.create(content,Metadata.DEFAULT),ctx);
	}

	public static void remove(RepositoryConnection connection, Resource... contexts) throws SesameRepositoryFailure {
		try {
			connection.clear(contexts);
		} catch (RepositoryException e) {
			throw new SesameRepositoryFailure(COULD_NOT_REMOVE_CONTEXTS,e);
		}
	}

}