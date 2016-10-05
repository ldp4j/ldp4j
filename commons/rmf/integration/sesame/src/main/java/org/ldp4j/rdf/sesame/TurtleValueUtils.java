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

import java.util.Map;

import org.ldp4j.commons.net.URIUtils;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.turtle.TurtleUtil;

final class TurtleValueUtils {

	enum Namespace {
		RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#",1),
		RDFS("http://www.w3.org/2000/01/rdf-schema#",2),
		OWL("http://www.w3.org/2002/07/owl#",3),
		XML_SCHEMA("http://www.w3.org/2001/XMLSchema#",4),
		UNKNOWN("UNKNOWN",5),
		;
		private final String id;
		private final int priority;

		Namespace(String id, int priority) {
			this.id = id;
			this.priority = priority;
		}

		static Namespace fromURI(URI uri) {
			for(Namespace namespace:values()) {
				if(namespace.getId().equals(uri.getNamespace())) {
					return namespace;
				}
			}
			return UNKNOWN;
		}

		int compare(Namespace n) {
			return priority-n.priority;
		}

		String getId() {
			return id;
		}

	}

	private static final String BLANK_NODE_PREFIX = "_:";
	private static final char TAB = '\t';
	private static final char LINE_FEED = '\r';
	private static final char NEW_LINE = '\n';
	private static final String ESCAPED_MULTI_LINE_QUOTES = "\"\"\"";
	private static final String ESCAPED_DOUBLE_QUOTES = "\"";
	private final Map<String, String> namespaceTable;
	private final URI base;

	TurtleValueUtils(URI base, Map<String,String> namespaceTable) {
		this.base = base;
		this.namespaceTable = namespaceTable;
	}


	public String toString(Value v) {
		String result=null;
		if(v instanceof URI) {
			result=writeURI((URI)v);
		} else if(v instanceof BNode) {
			result=writeBNode((BNode)v);
		} else if(v instanceof Literal) {
			result=writeLiteral((Literal)v);
		} else {
			throw new IllegalStateException("Unexpected value type "+v.getClass().getCanonicalName());
		}
		return result;
	}

	private java.net.URI toURI(URI s) {
		return java.net.URI.create(s.toString()).normalize();
	}

	private String writeURI(URI uri) {
		String result=null;
		String prefix=namespaceTable.get(uri.getNamespace());
		if(prefix!=null) {
			// Namespace is mapped to a prefix; write abbreviated URI
			result=String.format("%s:%s",prefix,uri.getLocalName());
		} else {
			// Namespace is not mapped to a prefix; write the resolved URI
			result=String.format("<%s>",TurtleUtil.encodeURIString(resolve(uri).toString()));
		}
		return result;
	}


	/**
	 * @param uri
	 * @return
	 */
	private java.net.URI resolve(URI uri) {
		java.net.URI resolved = toURI(uri);
		if(base!=null) {
			resolved = URIUtils.relativize(toURI(base), resolved);
		}
		return resolved;
	}

	private String writeBNode(BNode bNode) {
		return BLANK_NODE_PREFIX.concat(bNode.getID());
	}

	private String writeLiteral(Literal lit) {
		StringBuilder builder=new StringBuilder();
		String label = lit.getLabel();

		if(isMultiLineString(label)) {
			// Write label as long string
			builder.append(ESCAPED_MULTI_LINE_QUOTES);
			builder.append(TurtleUtil.encodeLongString(label));
			builder.append(ESCAPED_MULTI_LINE_QUOTES);
		} else {
			// Write label as normal string
			builder.append(ESCAPED_DOUBLE_QUOTES);
			builder.append(TurtleUtil.encodeString(label));
			builder.append(ESCAPED_DOUBLE_QUOTES);
		}

		if(lit.getLanguage()!=null) {
			// Append the literal's language
			builder.append("@");
			builder.append(lit.getLanguage());
		} else {
			URI datatype = lit.getDatatype();
			if(datatype!=null) {
				// TODO: This should be configurable
				if(!canOmmitDatatype(datatype)) { // NOSONAR
					/**
					 * Append the literal's datatype (possibly written as an abbreviated
					 * URI)
					 */
					builder.append("^^");
					builder.append(writeURI(datatype));
				}
			}
		}
		return builder.toString();
	}


	static boolean canOmmitDatatype(URI datatype) {
		return
			"string".equals(datatype.getLocalName()) &&
			"http://www.w3.org/2001/XMLSchema#".equals(datatype.getNamespace());
	}

	private boolean isMultiLineString(String label) {
		return
			label.indexOf(NEW_LINE)!=-1 ||
			label.indexOf(LINE_FEED)!= -1 ||
			label.indexOf(TAB) != -1;
	}


	public static int compare(URI u1, URI u2) {
		int res = Namespace.fromURI(u1).compare(Namespace.fromURI(u2));
		if(res!=0) {
			return res;
		}
		return u1.toString().compareTo(u2.toString());
	}

}