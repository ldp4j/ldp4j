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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;

final class ImmutableProperty implements Property {

	private final Property property;

	ImmutableProperty(MutableProperty property) {
		this.property = new MutableProperty(property);
	}
	
	@Override
	public Iterator<Value> iterator() {
		return this.property.iterator();
	}

	@Override
	public Individual<?, ?> individual() {
		return this.property.individual();
	}

	@Override
	public URI predicate() {
		return this.property.predicate();
	}

	@Override
	public Collection<? extends Value> values() {
		return this.property.values();
	}

	@Override
	public int numberOfValues() {
		return this.property.numberOfValues();
	}

	@Override
	public boolean hasValues() {
		return this.property.hasValues();
	}

	@Override
	public void accept(ValueVisitor visitor) {
		this.property.accept(visitor);
	}

	@Override
	public boolean hasLiteralValue(Literal<?> value) {
		return this.property.hasLiteralValue(value);
	}

	@Override
	public boolean hasIdentifiedIndividual(Object id) {
		return this.property.hasIdentifiedIndividual(id);
	}

}