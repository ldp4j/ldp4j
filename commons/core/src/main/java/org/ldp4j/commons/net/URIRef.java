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
package org.ldp4j.commons.net;

import java.net.URI;

/**
 * Mutable URI decomposition into its standard components.
 * @see <a href="https://tools.ietf.org/html/rfc3986#section-5.2.2">RFC 3896:  Uniform Resource Identifier (URI): Generic Syntax - 5.2.2.  Transform References</>
 */
final class URIRef {

	String scheme;
	String authority;
	String path;
	String query;
	String fragment;

	private URIRef(String scheme, String authority, String path, String query, String fragment) {
		this.scheme = scheme;
		this.authority = authority;
		this.path = path;
		this.query = query;
		this.fragment = fragment;
	}

	/**
	 * 5.3.  Component Recomposition
	 *
	 *    Parsed URI components can be recomposed to obtain the corresponding
	 *    URI reference string.  Using pseudocode, this would be:
	 *
	 *       result = ""
	 *
	 *       if defined(scheme) then
	 *          append scheme to result;
	 *          append ":" to result;
	 *       endif;
	 *
	 *       if defined(authority) then
	 *          append "//" to result;
	 *          append authority to result;
	 *       endif;
	 *
	 *       append path to result;
	 *
	 *       if defined(query) then
	 *          append "?" to result;
	 *          append query to result;
	 *       endif;
	 *
	 *       if defined(fragment) then
	 *          append "#" to result;
	 *          append fragment to result;
	 *       endif;
	 *
	 *       return result;
	 *
	 *    Note that we are careful to preserve the distinction between a
	 *    component that is undefined, meaning that its separator was not
	 *    present in the reference, and a component that is empty, meaning that
	 *    the separator was present and was immediately followed by the next
	 *    component separator or the end of the reference.
	 */
	URI toURI() {
		StringBuilder builder=new StringBuilder();
		if(defined(this.scheme)) {
			builder.append(this.scheme);
			builder.append(":");
		}
		if(defined(this.authority)) {
			builder.append("//");
			builder.append(this.authority);
		}
		if(defined(this.path)) {
			builder.append(this.path);
		}
		if(defined(this.query)) {
			builder.append("?");
			builder.append(this.query);
		}
		if(defined(this.fragment)) {
			builder.append("#");
			builder.append(this.fragment);
		}
		String rawURI = builder.toString();
		return URI.create(rawURI);
	}

	static URIRef create(URI ref) {
		return new URIRef(ref.getScheme(),ref.getAuthority(),ref.getPath(),ref.getQuery(),ref.getFragment());
	}

	static URIRef create() {
		return new URIRef(null,null,"",null,null);
	}

	private static boolean defined(String value) {
		return value!=null && !value.isEmpty();
	}

}