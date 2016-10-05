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
package org.ldp4j.server.data;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;

import com.google.common.base.MoreObjects;

final class URIResolver {

	private static final URI SELF_URI = URI.create("");

	private final URI endpoint;
	private final URI alternative;

	private URIResolver(URI endpoint, URI alternative) {
		this.endpoint = endpoint;
		this.alternative = alternative;
	}

	private URI getRelativePathFromBase(URI ancestor) {
		URI relativePath = ancestor.relativize(this.endpoint);
		String[] split = relativePath.getPath().split("/");
		StringBuilder builder=new StringBuilder();
		for(int i=0;i<split.length;i++) {
			if(i>0) {
				builder.append("/");
			}
			builder.append("..");
		}
		return URI.create(builder.toString());
	}

	URI resolve(URI c1, URI c2) {
		URI cr1 = this.endpoint.relativize(c1);
		URI cr2 = this.endpoint.relativize(c2);
		URI resolved=null;
		if(cr1.equals(cr2)) {
			resolved=c1;
		} else if(cr1.equals(SELF_URI) && cr2.equals(this.alternative)) {
			resolved=cr1;
		} else if(cr1.isAbsolute() && !cr2.isAbsolute()){
			resolved=c2;
		} else if(!cr1.isAbsolute() && cr2.isAbsolute()){
			resolved=cr1;
		} else if(cr1.isAbsolute() && cr2.isAbsolute()){
			resolved = getRelativePathFromBase(cr1);
		}
		return resolved;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("endpoint",this.endpoint).
					add("alternative", this.alternative).
					toString();
	}

	static URIResolver newInstance(URI endpoint, URI alternative) {
		checkNotNull(endpoint,"Endpoint URI cannot be null");
		checkNotNull(alternative,"Alternative URI cannot be null");
		checkArgument(alternative.isAbsolute() && !alternative.isOpaque(),"Alternative URI must be absolute hierarchical");
		checkArgument(endpoint.isAbsolute() && !endpoint.isOpaque(),"Endpoint URI must be absolute hierarchical");
		checkArgument(!endpoint.equals(alternative),"Endpoint URI must be different from the alternative one");
		return new URIResolver(endpoint,alternative);
	}

}