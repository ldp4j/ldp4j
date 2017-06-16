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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.application.vocabulary.RDF;
import org.ldp4j.application.vocabulary.RDFS;
import org.ldp4j.rdf.Namespaces;

final class NamespacesHelper {

	private NamespacesHelper() {
	}

	private static Namespaces defaultResourceNamespaces() {
		return
			new Namespaces().
				addPrefix("rdf", RDF.NAMESPACE).
				addPrefix("rdfs", RDFS.NAMESPACE).
				addPrefix("xsd", "http://www.w3.org/2001/XMLSchema#").
				addPrefix("ldp", LDP.NAMESPACE).
				addPrefix("ldp4j", "http://www.ldp4j.org/ns/application#");
	}

	private static Namespaces defaultConstraintReportNamespaces() {
		return
			defaultResourceNamespaces().
				addPrefix("cnts", "http://www.ldp4j.org/ns/constraints#").
				addPrefix("dct", "http://purl.org/dc/terms/").
				addPrefix("http", "http://www.w3.org/2011/http#").
				addPrefix("cnt", "http://www.w3.org/2011/content#").
				addPrefix("http-methods", "http://www.w3.org/2011/http-methods#").
				addPrefix("http-headers", "http://www.w3.org/2011/http-headers#").
				addPrefix("sh", "http://www.w3.org/ns/shacl#");
	}

	private static Namespaces merge(Namespaces original, Namespaces source) {
		int counter=0;
		for(String prefix:source.getDeclaredPrefixes()) {
			String uri = source.getNamespaceURI(prefix);
			if(original.getNamespaceURI(prefix)==null) {
				original.addPrefix(prefix, uri);
			} else if(original.getPrefixes(uri).isEmpty()) {
				String newPrefix=null;
				do {
					newPrefix="ns"+(counter++);
				} while(original.getNamespaceURI(newPrefix)!=null);
				original.addPrefix(newPrefix, uri);
			} // ELSE: already covered
		}
		return original;
	}

	static Namespaces resourceNamespaces(Namespaces applicationNamespaces) {
		return merge(defaultResourceNamespaces(),applicationNamespaces);
	}

	static Namespaces constraintReportNamespaces(Namespaces applicationNamespaces) {
		return merge(defaultConstraintReportNamespaces(),applicationNamespaces);
	}

}
