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

import java.util.Objects;

import com.google.common.base.MoreObjects;

final class URIDescriptor {

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
			if(this.parent!=null) {
				result=this.parent.isRelative();
			}
			return result;
		}

		boolean isAbsolute() {
			return !isRelative();
		}

	}

	private final Scope scope;
	private final Type type;

	private URIDescriptor(URIDescriptor.Scope scope, URIDescriptor.Type type) {
		this.scope = scope;
		this.type = type;
	}

	boolean isResolvable() {
		return scope.equals(Scope.APPLICATION) || scope.equals(Scope.ENDPOINT) || scope.equals(Scope.RESOURCE);
	}

	boolean isTransient() {
		return type.isRelative();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.scope,this.type);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=this==obj;
		if(!result && obj instanceof URIDescriptor) {
			URIDescriptor that=(URIDescriptor)obj;
			result=
				Objects.equals(this.scope,that.scope) &&
				Objects.equals(this.type,that.type);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("scope",this.scope).
					add("type",this.type).
					toString();
	}

	static URIDescriptor newDescriptor(Scope scope, Type type) {
		return new URIDescriptor(scope, type);
	}

}