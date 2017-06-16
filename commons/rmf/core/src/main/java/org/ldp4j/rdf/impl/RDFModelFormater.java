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
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Date;

import org.ldp4j.rdf.BlankNode;
import org.ldp4j.rdf.Format;
import org.ldp4j.rdf.LanguageLiteral;
import org.ldp4j.rdf.Literal;
import org.ldp4j.rdf.Namespaces;
import org.ldp4j.rdf.NodeVisitor;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.TypedLiteral;
import org.ldp4j.rdf.URIRef;
import org.ldp4j.rdf.sesame.TurtlePrettyPrinter;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.rio.helpers.JSONLDMode;
import org.openrdf.rio.helpers.JSONLDSettings;
import org.openrdf.rio.turtle.TurtleUtil;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.memory.model.MemValueFactory;

final class RDFModelFormater {

	private final class RebasingTurtleWriter extends TurtleWriter {

		private RebasingTurtleWriter(Writer writer) {
			super(writer);
		}

		@Override
		public void startRDF() throws RDFHandlerException {
			super.startRDF();
			try {
				writer.write("@base ");
				writer.write("<");
				writer.write(TurtleUtil.encodeURIString(baseURI.toString()));
				writer.write("> .");
				writer.writeEOL();
			} catch (IOException e) {
				throw new RDFHandlerException(e);
			}
		}

		@Override
		protected void writeURI(org.openrdf.model.URI uri)
				throws IOException {
			URI create = URI.create(uri.toString());
			URI rel = baseURI.relativize(create);
			if(!rel.isAbsolute()) {
				writer.write("<");
				writer.write(TurtleUtil.encodeURIString(rel.toString()));
				writer.write(">");
			} else {
				super.writeURI(uri);
			}
		}
	}

	private static class TripleFormater {

		private final class ObjectFormater extends NodeVisitor<org.openrdf.model.Value> {
			@Override
			public org.openrdf.model.Value visitURIRef(URIRef node, org.openrdf.model.Value defaultResult) {
				return valueFactory.createURI(baseURI.resolve(node.getIdentity()).toString());
			}

			@Override
			public org.openrdf.model.Value visitBlankNode(BlankNode node, org.openrdf.model.Value defaultResult) {
				return valueFactory.createBNode(node.getIdentity());
			}

			@Override
			public org.openrdf.model.Value visitLiteral(Literal<?> node, org.openrdf.model.Value defaultResult) {
				org.openrdf.model.Value result=null;
				Object value = node.getValue();
				if(value instanceof Boolean) {
					result = valueFactory.createLiteral((Boolean)value);
				} else if(value instanceof Integer) {
					result = valueFactory.createLiteral((Integer)value);
				} else if(value instanceof Double) {
					result = valueFactory.createLiteral((Double)value);
				} else if(value instanceof Float) {
					result = valueFactory.createLiteral((Float)value);
				} else if(value instanceof Long) {
					result = valueFactory.createLiteral((Long)value);
				} else if(value instanceof Short) {
					result = valueFactory.createLiteral((Short)value);
				} else if(value instanceof Character) {
					result = valueFactory.createLiteral((Character)value);
				} else if(value instanceof Date) {
					result = valueFactory.createLiteral((Date)value);
				} else {
					result = valueFactory.createLiteral(value.toString());
				}
				return result;
			}

			@Override
			public org.openrdf.model.Value visitLanguageLiteral(LanguageLiteral node, org.openrdf.model.Value defaultResult) {
				return valueFactory.createLiteral(node.getValue(),node.getLanguage());
			}

			@Override
			public org.openrdf.model.Value visitTypedLiteral(TypedLiteral<?> node, org.openrdf.model.Value defaultResult) {
				URI type = node.getType().toURI();
				return valueFactory.createLiteral(node.getValue().toString(),valueFactory.createURI(type.toString()));
			}

		}

		private final class SubjectFormater extends NodeVisitor<org.openrdf.model.Resource> {
			@Override
			public org.openrdf.model.Resource visitURIRef(
					URIRef node,
					org.openrdf.model.Resource defaultResult) {
				return valueFactory.createURI(baseURI.resolve(node.getIdentity()).toString());
			}

			@Override
			public org.openrdf.model.Resource visitBlankNode(
					BlankNode node,
					org.openrdf.model.Resource defaultResult) {
				return valueFactory.createBNode(node.getIdentity());
			}
		}

		private final ValueFactory valueFactory;
		private final SubjectFormater subjectFormater;
		private final ObjectFormater objectFormater;
		private final URI baseURI;

		public TripleFormater(URI baseURI) {
			this.baseURI = baseURI;
			this.valueFactory = new MemValueFactory();
			this.subjectFormater = new SubjectFormater();
			this.objectFormater = new ObjectFormater();
		}

		public Statement formatTriple(Triple t) {
			return
				valueFactory.
					createStatement(
						t.getSubject().accept(subjectFormater),
						formatPredicate(t.getPredicate()),
						t.getObject().accept(objectFormater));
		}

		private org.openrdf.model.URI formatPredicate(URIRef predicate) {
			return valueFactory.createURI(baseURI.resolve(predicate.getIdentity()).toString());
		}

	}

	private final URI baseURI;
	private final Format format;
	private final Namespaces namespaces;

	RDFModelFormater(URI baseURI, Namespaces namespaces, Format format) {
		this.baseURI   =baseURI;
		this.format    =format;
		this.namespaces=new Namespaces(namespaces);
	}

	public String format(Iterable<Triple> triples) throws IOException {
		StringWriter writer=new StringWriter();
		try {
			exportTriples(triples, createWriter(writer));
			return writer.toString();
		} catch (RDFHandlerException e) {
			throw new IOException(e);
		}
	}

	protected String exportRepository(RepositoryConnection connection) throws RepositoryException, RDFHandlerException {
		StringWriter writer=new StringWriter();
		RDFWriter rdfWriter=Rio.createWriter(getFormat(),writer);
		if(rdfWriter instanceof TurtleWriter) {
			rdfWriter=new RebasingTurtleWriter(writer);
		}
		connection.export(rdfWriter);
		return writer.toString();
	}

	private RDFFormat getFormat() {
		return Rio.getWriterFormatForMIMEType(format.getMime(), RDFFormat.TURTLE);
	}

	private void populateRepository(Iterable<Triple> triples, RDFHandler handler) throws RDFHandlerException {
		RDFModelFormater.TripleFormater translator = new TripleFormater(baseURI);
		for(Triple t:triples) {
			handler.handleStatement(translator.formatTriple(t));
		}
	}

	private void addNamespaces(RDFHandler handler) throws RDFHandlerException {
		for(String prefix:namespaces.getDeclaredPrefixes()) {
			handler.handleNamespace(prefix, namespaces.getNamespaceURI(prefix));
		}
	}

	private RDFWriter createWriter(StringWriter writer) {
		RDFWriter result=null;
		if(format.equals(Format.TURTLE)) {
			result=new TurtlePrettyPrinter(new MemValueFactory().createURI(baseURI.toString()),writer);
		} else {
			RDFWriterRegistry registry=RDFWriterRegistry.getInstance();
			RDFFormat rawFormat=Rio.getWriterFormatForMIMEType(format.getMime(),RDFFormat.RDFXML);
			RDFWriterFactory factory=registry.get(rawFormat);
			result=factory.getWriter(writer);
			if(format.equals(Format.JSON_LD)) {
				result.getWriterConfig().set(JSONLDSettings.JSONLD_MODE,JSONLDMode.FLATTEN);
				result.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT,true);
			}
		}
		return result;
	}

	private void exportTriples(Iterable<Triple> triples, RDFHandler handler) throws RDFHandlerException {
		handler.startRDF();
		addNamespaces(handler);
		populateRepository(triples, handler);
		handler.endRDF();
	}

}