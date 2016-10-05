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

import org.ldp4j.server.data.URIDescriptor.Scope;
import org.ldp4j.server.data.URIDescriptor.Type;

import com.google.common.base.MoreObjects;

final class URIDescriber {

	private static final String ANCESTOR_SEGMENT = "..";
	private static final URI SELF_URI = URI.create("");
	private final URI application;
	private final URI endpoint;

	private URIDescriber(URI application, URI endpoint) {
		this.application = application;
		this.endpoint = endpoint;
	}

	URIDescriptor describe(URI candidate) {
		Scope scope=null;
		Type type=null;
		if(candidate.isOpaque()) {
			scope=Scope.EXTERNAL;
			type=Type.OPAQUE;
		} else if(candidate.isAbsolute()) {
			type=Type.ABSOLUTE;
			URI relative = this.endpoint.relativize(candidate);
			if(relative.isAbsolute()) {
				relative=this.application.relativize(candidate);
				if(relative.isAbsolute()) {
					scope=Scope.EXTERNAL;
				} else {
					scope=Scope.APPLICATION;
				}
			} else {
				if(relative.getPath().split("/").length==1) {
					scope=Scope.ENDPOINT;
				} else {
					scope=Scope.RESOURCE;
				}
			}
		} else {
			scope=Scope.RESOURCE;
			if(candidate.equals(SELF_URI)) {
				type=Type.SELF;
			} else if(candidate.normalize().getPath().startsWith(ANCESTOR_SEGMENT)) {
				type=Type.ANCESTOR;
			} else {
				type=Type.CHILD;
			}
		}
		return URIDescriptor.newDescriptor(scope, type);
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("application",this.application).
					add("endpoint",this.endpoint).
					toString();
	}

	static URIDescriber newInstance(URI application, URI endpoint) {
		checkNotNull(application,"Application URI cannot be null");
		checkNotNull(endpoint,"Endpoint URI cannot be null");
		checkArgument(application.isAbsolute() && !endpoint.isOpaque(),"Application URI must be absolute hierarchical");
		checkArgument(endpoint.isAbsolute() && !endpoint.isOpaque(),"Endpoint URI must be absolute hierarchical");
		checkArgument(!application.relativize(endpoint).isAbsolute(),"Endpoint URI must be relative to the application one");
		return new URIDescriber(application,endpoint);
	}

}