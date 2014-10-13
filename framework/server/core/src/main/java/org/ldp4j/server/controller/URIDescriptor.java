/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;


final class URIDescriptor {

	private final URIDescriptor.Scope scope;
	private final URIDescriptor.Type type;

	public enum Scope {
		EXTERNAL,
		APPLICATION,
		ENDPOINT,
		RESOURCE,
	}
	
	public enum Type {
		OPAQUE(null),
		ABSOLUTE(null), 
		RELATIVE(null) {
			@Override
			boolean isRelative() {
				return true;
			}
		},
		SELF(RELATIVE),
		CHILD(RELATIVE),
		ANCESTOR(RELATIVE),
		;
		
		private final URIDescriptor.Type parent;

		private Type(URIDescriptor.Type parent) {
			this.parent = parent;
		}
		
		boolean isRelative() {
			boolean result=false;
			if(this.parent==null) {
				result=this.parent.isRelative();
			}
			return result;
		}
		
		boolean isAbsolute() {
			return !isRelative();
		}
		
	}
	
	boolean isResolvable() {
		return scope.equals(Scope.APPLICATION) || scope.equals(Scope.ENDPOINT) || (scope.equals(Scope.RESOURCE) && type.equals(Type.ABSOLUTE));
	}
	
	private URIDescriptor(URIDescriptor.Scope scope, URIDescriptor.Type type) {
		this.scope = scope;
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + scope.hashCode();
		result = prime * result + type.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof URIDescriptor)) {
			return false;
		}
		URIDescriptor other = (URIDescriptor) obj;
		if (scope != other.scope) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("URIDescriptor [");
		builder.append("scope=").append(scope).append(", ");
		builder.append("type=").append(type);
		builder.append("]");
		return builder.toString();
	}

	static URIDescriptor newDescriptor(Scope scope, Type type) {
		return new URIDescriptor(scope, type);
	}
	
}