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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.resources;

import java.net.URI;

import org.ldp4j.application.domain.LDP;
import org.ldp4j.application.vocabulary.Term;

public enum ResourceType {
	RESOURCE(null,LDP.RESOURCE),
	RDF_SOURCE(RESOURCE,LDP.RESOURCE),
	CONTAINER(RDF_SOURCE,LDP.CONTAINER) {
		@Override
		public boolean isContainer() {
			return true;
		}
	},
	BASIC_CONTAINER(CONTAINER,LDP.BASIC_CONTAINER),
	DIRECT_CONTAINER(CONTAINER,LDP.DIRECT_CONTAINER),
	INDIRECT_CONTAINER(CONTAINER,LDP.INDIRECT_CONTAINER), 
	;

	private final Term term;
	private final ResourceType parent;

	private ResourceType(ResourceType parent, Term term) {
		this.parent = parent;
		this.term = term;
	}
	
	public boolean isContainer() {
		return parent!=null ? parent.isContainer() : false;
	}

	public Term term() {
		return term;
	}

	public URI uri() {
		return term.as(URI.class);
	}

	@Override
	public String toString() {
		return term.qualifiedEntityName();
	}

}