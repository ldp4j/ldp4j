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

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class RDFFactory {

	private static final AtomicLong FACTORY_COUNTER=new AtomicLong();
	private final AtomicLong blankNodeCounter=new AtomicLong();
	private final long factoryId=FACTORY_COUNTER.incrementAndGet();
	
	public <T> TypedLiteral<T> newLiteral(T value) {
		if(value instanceof String) {
			return newLiteral(value,Datatype.STRING);
		} else {
			List<Datatype> types = Datatype.forValue(value);
			if(!types.isEmpty()) {
				return newLiteral(value,types.get(0));
			} else {
				return newLiteral(value,Datatype.STRING);
			}
		}
	}

	public <T> TypedLiteral<T> newLiteral(T value, Datatype type) {
		if(type==null) {
			return newLiteral(value);
		} else {
			return new TypedLiteral<T>(value,type);
		}
	}
	
	public LanguageLiteral newLiteral(String value, String language) {
		return new LanguageLiteral(value,language);
	}

	public BlankNode newBlankNode() {
		return newBlankNode(null);
	}

	public BlankNode newBlankNode(String identity) {
		String tmpId=identity;
		if(tmpId==null) {
			tmpId=
				String.format(
					"bn%04X-%04X-%s",
					factoryId,
					blankNodeCounter.incrementAndGet(),
					UUID.randomUUID());
		}
		return new BlankNode(tmpId);
	}
	
	public URIRef newURIRef(URI identity) {
		return new URIRef(identity);
	}
	
	public Triple newTriple(Resource<?> subject, URIRef predicate, Node object) {
		return new Triple(subject,predicate,object);
	}
	
}