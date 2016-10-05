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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.ldp4j.rdf.BlankNode;
import org.ldp4j.rdf.Datatype;
import org.ldp4j.rdf.Literal;
import org.ldp4j.rdf.Namespaces;
import org.ldp4j.rdf.Node;
import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.URIRef;
import org.ldp4j.rdf.Datatype.ValueSink;
import org.ldp4j.rdf.util.RDFModelDSL;
import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public final class SesameModelParser {

	private final class AutoBoxingDataTypeConsumer implements ValueSink {

		private final AtomicReference<Object> autobox;

		private AutoBoxingDataTypeConsumer() {
			this.autobox = new AtomicReference<Object>();
		}

		@Override
		public void consumeString(String value) {
			autobox.set(value);
		}

		@Override
		public void consumeRawValue(String value) {
			autobox.set(value);
		}

		@Override
		public void consumeStringArray(String[] value) {
			autobox.set(value);
		}

		@Override
		public void consumeDuration(Duration value) {
			autobox.set(value);
		}

		@Override
		public void consumeXMLGregorianCalendar(XMLGregorianCalendar value) {
			autobox.set(value);
		}

		@Override
		public void consumeByte(byte value) {
			autobox.set(value);
		}

		@Override
		public void consumeShort(short value) {
			autobox.set(value);
		}

		@Override
		public void consumeInteger(int value) {
			autobox.set(value);
		}

		@Override
		public void consumeLong(long value) {
			autobox.set(value);
		}

		@Override
		public void consumeBigInteger(BigInteger value) {
			autobox.set(value);
		}

		@Override
		public void consumeBigDecimal(BigDecimal value) {
			autobox.set(value);
		}

		@Override
		public void consumeURI(URI value) {
			autobox.set(value);
		}

		@Override
		public void consumeDouble(double value) {
			autobox.set(value);
		}

		@Override
		public void consumeFloat(float value) {
			autobox.set(value);
		}

		@Override
		public void consumeBoolean(boolean value) {
			autobox.set(value);
		}

		@Override
		public void consumeQName(String prefix, String localPart) {
			autobox.set(new QName(namespaces.getNamespaceURI(prefix),localPart,prefix));
		}

		public Object getValue() {
			return autobox.get();
		}
	}

	private final Namespaces namespaces;

	public SesameModelParser(Namespaces namespaces) {
		if(namespaces==null) {
			this.namespaces = new Namespaces();
		} else {
			this.namespaces = namespaces;
		}
	}

	public SesameModelParser() {
		this(null);
	}

	private Object boxTypedLiteral(Datatype type, String label) {
		AutoBoxingDataTypeConsumer consumer = new AutoBoxingDataTypeConsumer();
		type.decode(label,consumer);
		return consumer.getValue();
	}

	public URIRef parseURI(org.openrdf.model.URI uri) {
		return RDFModelDSL.uriRef(uri.stringValue());
	}

	public BlankNode parseBlankNode(BNode bNode) {
		return RDFModelDSL.blankNode(bNode.stringValue());
	}

	public Resource<?> parseResource(org.openrdf.model.Resource subject) {
		Resource<?> result;
		if(subject instanceof org.openrdf.model.URI) {
			result=RDFModelDSL.uriRef(subject.stringValue());
		} else {
			result=RDFModelDSL.blankNode(subject.stringValue());
		}
		return result;
	}

	public Literal<?> parseLiteral(org.openrdf.model.Literal literal) {
		org.openrdf.model.URI datatype = literal.getDatatype();
		Literal<?> result;
		String label = literal.getLabel();
		if(datatype==null) {
			String language = literal.getLanguage();
			if(language!=null) {
				result=RDFModelDSL.literal(label,language);
			} else {
				result=RDFModelDSL.literal(label);
			}
		} else {
			String dataType = datatype.stringValue();
			Datatype type=Datatype.fromString(dataType);
			if(type!=null) {
				result=RDFModelDSL.typedLiteral(boxTypedLiteral(type, label), dataType);
			} else {
				result=RDFModelDSL.typedLiteral(label,URI.create(dataType));
			}
		}
		return result;
	}

	public Node parseValue(Value object) {
		Node result=null;
		if(object instanceof org.openrdf.model.URI) {
			result=parseURI((org.openrdf.model.URI)object);
		} else if(object instanceof org.openrdf.model.BNode) {
			result=parseBlankNode((org.openrdf.model.BNode)object);
		} else if(object instanceof org.openrdf.model.Literal) {
			result=parseLiteral((org.openrdf.model.Literal)object);
		} else {
			throw new AssertionError("Unexpected value type '"+object.getClass().getName()+"'");
		}
		return result;
	}

	public Triple parseStatement(Statement statement) {
		return
			new Triple(
				parseResource(statement.getSubject()),
				parseURI(statement.getPredicate()),
				parseValue(statement.getObject())
			);
	}

}
