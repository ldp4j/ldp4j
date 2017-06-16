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

import java.util.Objects;

public final class Triple implements Comparable<Triple> {

	private final Resource<?> subject;
	private final URIRef predicate;
	private final Node object;

	public Triple(Resource<?> subject, URIRef predicate, Node object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	public Resource<?> getSubject() {
		return subject;
	}

	public URIRef getPredicate() {
		return predicate;
	}

	public Node getObject() {
		return object;
	}

	@Override
	public final int hashCode() {
		return Objects.hash(this.subject,this.predicate,this.object);
	}

	@Override
	public final boolean equals(Object obj) {
		boolean result=this==obj;
		if(!result && obj instanceof Triple) {
			Triple that=(Triple)obj;
			result=
				Objects.equals(this.subject, that.subject) &&
				Objects.equals(this.predicate, that.predicate) &&
				Objects.equals(this.object, that.predicate);
		}
		return result;
	}

	@Override
	public int compareTo(Triple o) {
		if(this==o) {
			return 0;
		}

		if(o==null) {
			return 1;
		}

		int result=getSubject().compareTo(o.getSubject());
		if(result!=0) {
			return result;
		}

		result=getPredicate().compareTo(o.getPredicate());
		if(result!=0) {
			return result;
		}
		return getObject().compareTo(o.getObject());

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(subject).append(" ");
		builder.append(predicate).append(" ");
		builder.append(object);
		builder.append(" .");
		return builder.toString();
	}

}