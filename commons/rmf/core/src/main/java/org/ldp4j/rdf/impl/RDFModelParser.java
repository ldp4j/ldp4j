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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-core:1.0.0-SNAPSHOT
 *   Bundle      : rmf-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.impl;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

import org.ldp4j.rdf.Format;
import org.ldp4j.rdf.Namespaces;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.util.TripleSet;
import org.megatwork.rdf.sesame.SesameModelParser;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RDFModelParser {

	private static final Logger LOGGER=LoggerFactory.getLogger(RDFModelParser.class);
	

	private final URI baseURI;
	private final Format format;

	RDFModelParser(URI baseURI, Format format) {
		this.baseURI=baseURI;
		this.format =format;
	}

	/**
	 * @param results
	 * @param message
	 */
	private void closeQuietly(RepositoryResult<?> results, String message) {
		if(results!=null) {
			try {
				results.close();
			} catch (OpenRDFException e) {
				if(LOGGER.isWarnEnabled()) {
					LOGGER.warn(message,e);
				}
			}
		}
	}

	/**
	 * @param repository
	 */
	private void shutDownQuietly(Repository repository) {
		if(repository!=null) {
			try {
				repository.shutDown();
			} catch (OpenRDFException e) {
				if(LOGGER.isWarnEnabled()) {
					LOGGER.warn("Could not shutdown internal repository",e);
				}
			}
		}
	}

	/**
	 * @param connection
	 */
	private void closeQuietly(RepositoryConnection connection) {
		if(connection!=null) {
			try {
				connection.close();
			} catch (OpenRDFException e) {
				if(LOGGER.isWarnEnabled()) {
					LOGGER.warn("Could not close connection",e);
				}
			}
		}
	}

	private RDFFormat getFormat() {
		return RDFFormat.forMIMEType(format.getMime(), RDFFormat.TURTLE);
	}

	private TripleSet importRepository(RepositoryConnection connection) throws RepositoryException {
		RepositoryResult<Statement> statements = null;
		try {
			statements=connection.getStatements(null, null, null, false);
			SesameModelParser tripleParser=new SesameModelParser(getNamespaces(connection));
			TripleSet result = new TripleSet();
			while(statements.hasNext()) {
				result.add(tripleParser.parseStatement(statements.next()));
			}
			return result;
		} finally {
			closeQuietly(statements, "Could not close results after parsing statements");
		}
	}

	private void populateRepository(String content, RepositoryConnection connection) throws IOException, RDFParseException, RepositoryException {
		connection.add(new StringReader(content), baseURI.toString(), getFormat());
	}

	private Namespaces getNamespaces(RepositoryConnection connection) throws RepositoryException {
		Namespaces ns = new Namespaces();
		RepositoryResult<Namespace> rr = null;
		try {
			rr=connection.getNamespaces();
			while(rr.hasNext()) {
				Namespace n=rr.next();
				ns.addPrefix(n.getPrefix(), n.getName());
			}
		} finally {
			closeQuietly(rr,"Could not close results after retrieving namespaces");
		}
		return ns;
	}

	public Iterable<Triple> parse(String content) throws IOException {
		Repository repository = null;
		RepositoryConnection connection=null;
		try {
			repository = new SailRepository(new MemoryStore());
			repository.initialize();
			connection=repository.getConnection();
			populateRepository(content, connection);
			return importRepository(connection);
		} catch (OpenRDFException e) {
			throw new IOException(e);
		} finally {
			closeQuietly(connection);
			shutDownQuietly(repository);
		}
	}
}