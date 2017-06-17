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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.2.2
 *   Bundle      : ldp4j-commons-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.net;


final class URIRef {

	String scheme;
	Authority authority;
	Path path;
	String query;
	String fragment;

	private URIRef(String scheme, Authority authority, Path path, String query, String fragment) {
		this.scheme = scheme;
		this.authority = authority;
		this.path = path;
		this.query = query;
		this.fragment = fragment;
	}

	URI toURI() {
		return
			URI.create("/").
				withScheme(this.scheme).
				withAuthority(this.authority).
				withPath(this.path).
				withQuery(this.query).
				withFragment(this.fragment);
	}

	static URIRef create(URI ref) {
		return new URIRef(ref.getScheme(),ref.getAuthority(),ref.getPath(),ref.getQuery(),ref.getFragment());
	}

	static URIRef empty() {
		return new URIRef(null,null,Path.create(""),null,null);
	}

}