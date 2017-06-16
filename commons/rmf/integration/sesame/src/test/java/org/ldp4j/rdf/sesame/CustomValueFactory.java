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

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

final class CustomValueFactory implements ValueFactory {

	private static final class CustomURI implements URI {
	
		private static final long serialVersionUID = -5136294169133155051L;
	
		private final String namespace;
		private final String localName;
		private final String base;
	
		private CustomURI(String base, String namespace, String localName) {
			this.base = base;
			this.namespace = namespace;
			this.localName = localName;
		}
	
		@Override
		public String stringValue() {
			String result=this.namespace+this.localName;
			if(this.base!=null) {
				if(result.startsWith(this.base)) {
					result=result.substring(this.base.length());
				}
			}
			return result;
		}
	
		@Override
		public String toString() {
			return stringValue();
		}
	
		@Override
		public String getLocalName() {
			return this.localName;
		}
	
		@Override
		public String getNamespace() {
			return this.namespace;
		}
	
	}

	private final ValueFactory delegate;

	private String base;

	CustomValueFactory() {
		this.delegate=new ValueFactoryImpl();
	}

	CustomValueFactory createRelativeURIs(String base) {
		if(base==null||base.trim().isEmpty()) {
			throw new IllegalArgumentException("Proper base required for relativizing parsed URIs");
		}
		this.base=base;
		return this;
	}

	CustomValueFactory createAbsoluteURIs() {
		this.base=null;
		return this;
	}

	@Override
	public BNode createBNode() {
		return this.delegate.createBNode();
	}

	@Override
	public BNode createBNode(String bnodeId) {
		return this.delegate.createBNode(bnodeId);
	}

	@Override
	public Literal createLiteral(String value) {
		return this.delegate.createLiteral(value);
	}

	@Override
	public Literal createLiteral(boolean value) {
		return this.delegate.createLiteral(value);
	}

	@Override
	public Literal createLiteral(byte value) {
		return this.delegate.createLiteral(value);
	}

	@Override
	public Literal createLiteral(short value) {
		return this.delegate.createLiteral(value);
	}

	@Override
	public Literal createLiteral(int value) {
		return this.delegate.createLiteral(value);
	}

	@Override
	public Literal createLiteral(long value) {
		return this.delegate.createLiteral(value);
	}

	@Override
	public Literal createLiteral(float value) {
		return this.delegate.createLiteral(value);
	}

	@Override
	public Literal createLiteral(double value) {
		return this.delegate.createLiteral(value);
	}

	@Override
	public Literal createLiteral(XMLGregorianCalendar value) {
		return this.delegate.createLiteral(value);
	}

	@Override
	public Literal createLiteral(Date value) {
		return this.delegate.createLiteral(value);
	}

	@Override
	public Literal createLiteral(String value, String languageTag) {
		return this.delegate.createLiteral(value,languageTag);
	}

	@Override
	public Literal createLiteral(String value, URI type) {
		return this.delegate.createLiteral(value,type);
	}

	@Override
	public Statement createStatement(Resource subject, URI predicate, Value object) {
		return this.delegate.createStatement(subject, predicate, object);
	}

	@Override
	public Statement createStatement(Resource subject, URI predicate, Value object, Resource context) {
		return this.delegate.createStatement(subject, predicate, object, context);
	}

	@Override
	public URI createURI(String uri) {
		int localNameIndex=getLocalNameIndex(uri);
		String namespace=uri.substring(0, localNameIndex);
		String localName = uri.substring(localNameIndex);
		return createURI(namespace,localName);
	}

	@Override
	public URI createURI(String namespace, String localName) {
		return new CustomURI(this.base,namespace,localName);
	}

	private static int getLocalNameIndex(String uri) {
		int separatorIdx = uri.indexOf('#');

		if (separatorIdx < 0) {
			separatorIdx = uri.lastIndexOf('/');
		}

		if (separatorIdx < 0) {
			separatorIdx = uri.lastIndexOf(':');
		}

		// Relax this to allow relative URIs
		// if (separatorIdx < 0) {
		//  throw new IllegalArgumentException("No separator character founds in URI: " + uri);
		// }

		return separatorIdx + 1;
	}

}