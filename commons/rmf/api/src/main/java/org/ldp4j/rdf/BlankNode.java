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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-api:0.2.2
 *   Bundle      : rmf-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf;


public final class BlankNode extends Resource<String> {

	BlankNode(String identity) {
		super(identity);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode()*17;
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof BlankNode) && super.equals(o);
	}
	
	@Override
	public int compareTo(Node o) {
		if(o==null) {
			return 1;
		}
		if(o==this) {
			return 0;
		}
		if(o instanceof URIRef) {
			return 1;
		}
		if(o instanceof Literal) {
			return -1;
		}
		return getIdentity().compareTo(((BlankNode)o).getIdentity());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("_:").append(getIdentity());
		return builder.toString();
	}

	@Override
	public <T> T accept(NodeVisitor<T> visitor, T defaultValue) {
		return visitor.visitBlankNode(this, defaultValue);
	}

}