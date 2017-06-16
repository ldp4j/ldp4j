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
package org.ldp4j.rdf.impl;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.ldp4j.rdf.Format;
import org.ldp4j.rdf.Namespaces;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.impl.UnmarshallOptions.Ordering;
import org.ldp4j.rdf.impl.UnmarshallOptions.UnmarshallStyle;
import org.ldp4j.rdf.util.TripleSet;
import org.ldp4j.rdf.sesame.SesameModelParser;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RDFModelParser {

	private interface TripleSink {

		Iterable<Triple> triples();

		void addTriple(Triple triple);

	}

	private interface TripleProducer {

		void injectTriples(TripleSink sink) throws IOException;

	}

	private final class UnorderedTripleSink implements TripleSink {
		private List<Triple> triples=new ArrayList<Triple>();

		@Override
		public Iterable<Triple> triples() {
			return triples;
		}

		@Override
		public void addTriple(Triple triple) {
			triples.add(triple);
		}
	}

	private final class SortingTripleSink implements TripleSink {
		private TripleSet triples=new TripleSet();

		@Override
		public Iterable<Triple> triples() {
			return triples;
		}

		@Override
		public void addTriple(Triple triple) {
			triples.add(triple);
		}
	}

	private static final class RepositoryBasedTripleProducer implements TripleProducer {
		private final String content;
		private final RDFFormat format;
		private final String base;

		private RepositoryBasedTripleProducer(String content, RDFFormat format, String base) {
			this.content = content;
			this.format = format;
			this.base = base;
		}

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

		private void importRepository(RepositoryConnection connection, TripleSink sink) throws RepositoryException {
			RepositoryResult<Statement> statements = null;
			try {
				statements=connection.getStatements(null, null, null, false);
				SesameModelParser tripleParser=new SesameModelParser(getNamespaces(connection));
				while(statements.hasNext()) {
					sink.addTriple(tripleParser.parseStatement(statements.next()));
				}
			} finally {
				closeQuietly(statements, "Could not close results after parsing statements");
			}
		}

		private void populateRepository(String content, RepositoryConnection connection) throws IOException, RDFParseException, RepositoryException {
			connection.add(new StringReader(content), this.base, this.format);
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

		@Override
		public void injectTriples(TripleSink sink) throws IOException {
			Repository repository = null;
			RepositoryConnection connection=null;
			try {
				repository = new SailRepository(new MemoryStore());
				repository.initialize();
				connection=repository.getConnection();
				populateRepository(this.content, connection);
				importRepository(connection,sink);
			} catch (OpenRDFException e) {
				throw new IOException(e);
			} finally {
				closeQuietly(connection);
				shutDownQuietly(repository);
			}
		}
	}

	private static final class ParserBasedTripleProducer implements TripleProducer {

		private static final class Collector implements RDFHandler {

			private final List<Statement> statements;
			private final Namespaces namespaces;

			private Collector() {
				this.namespaces = new Namespaces();
				this.statements=new ArrayList<Statement>();
			}

			@Override
			public void startRDF() throws RDFHandlerException {
				// Nothing to do
			}

			@Override
			public void endRDF() throws RDFHandlerException {
				// Nothing to do
			}

			@Override
			public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
				this.getNamespaces().addPrefix(prefix,uri);
			}

			@Override
			public void handleStatement(Statement st) throws RDFHandlerException {
				this.getStatements().add(st);
			}

			@Override
			public void handleComment(String comment) throws RDFHandlerException {
				// Nothing to do
			}

			Namespaces getNamespaces() {
				return this.namespaces;
			}

			List<Statement> getStatements() {
				return this.statements;
			}
		}

		private final String content;
		private final RDFFormat format;
		private final String base;

		private ParserBasedTripleProducer(String content, RDFFormat format, String base) {
			this.content = content;
			this.format = format;
			this.base = base;
		}

		@Override
		public void injectTriples(TripleSink sink) throws IOException {
			try {
				Collector collector = new Collector();
				RDFParser parser =Rio.createParser(this.format);
				parser.setRDFHandler(collector);
				parser.parse(new StringReader(this.content), this.base);
				SesameModelParser tripleParser=new SesameModelParser(collector.getNamespaces());
				for(Statement st:collector.getStatements()) {
					sink.addTriple(tripleParser.parseStatement(st));
				}
			} catch (OpenRDFException e) {
				throw new IOException(e);
			}
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(RDFModelParser.class);


	private final URI baseURI;
	private final Format format;

	private final UnmarshallStyle unmarshallStyle;
	private final Ordering ordering;

	RDFModelParser(URI baseURI, Format format, UnmarshallStyle unmarshallStyle, Ordering ordering) {
		this.baseURI=baseURI;
		this.format =format;
		this.unmarshallStyle = unmarshallStyle;
		this.ordering = ordering;
	}

	private TripleProducer getProducer(String content) {
		RDFFormat sesameFormat =
			Rio.
				getParserFormatForMIMEType(
					this.format.getMime(),
					RDFFormat.TURTLE);
		TripleProducer producer=null;
		switch(unmarshallStyle) {
			case PARSER_BASED:
				producer=new ParserBasedTripleProducer(content,sesameFormat,this.baseURI.toString());
				break;
			case REPOSITORY_BASED:
				producer=new RepositoryBasedTripleProducer(content,sesameFormat,this.baseURI.toString());
				break;
			default:
				throw new AssertionError("Unsupported unmarshalling style '"+unmarshallStyle+"'");
		}
		return producer;
	}

	private TripleSink getTripleSink() {
		TripleSink sink=null;
		switch(ordering) {
			case KEEP_TRIPLE_ORDER:
				sink=new UnorderedTripleSink();
				break;
			case SORT_TRIPLES:
				sink=new SortingTripleSink();
				break;
			default:
				throw new AssertionError("Unsupported ordering '"+ordering+"'");
		}
		return sink;
	}

	public Iterable<Triple> parse(final String content) throws IOException {
		TripleSink sink = getTripleSink();
		TripleProducer producer = getProducer(content);
		producer.injectTriples(sink);
		return sink.triples();
	}
}