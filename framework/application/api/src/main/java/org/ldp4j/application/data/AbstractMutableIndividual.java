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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

abstract class AbstractMutableIndividual<T extends Serializable, S extends Individual<T,S>> implements Individual<T,S> {

	private final T id;
	private final PropertyCollection properties;
	private final MutableDataSet dataSet;

	AbstractMutableIndividual(T id, MutableDataSet dataSet) {
		checkNotNull(id,"Id cannot be null");
		checkNotNull(dataSet,"Dataset cannot be null");
		this.id=id;
		this.dataSet = dataSet;
		this.properties=new PropertyCollection(this,dataSet);
	}

	final void addPropertyValue(URI propertyId, Value value) {
		this.properties.addValue(propertyId, value);
	}

	final void removePropertyValue(URI propertyId, Value value) {
		this.properties.removeValue(propertyId, value);
	}

	@Override
	public final DataSet dataSet() {
		return this.dataSet;
	}

	@Override
	public final T id() {
		return this.id;
	}

	@Override
	public final int numberOfProperties() {
		return this.properties.size();
	}

	@Override
	public final boolean hasProperties() {
		return !this.properties.isEmpty();
	}

	@Override
	public final Collection<Property> properties() {
		return this.properties.properties();
	}

	@Override
	public final boolean hasProperty(URI propertyId) {
		return this.properties.hasProperty(propertyId);
	}

	@Override
	public final Property property(URI propertyId) {
		return this.properties.property(propertyId);
	}

	@Override
	public final Set<URI> propertyIds() {
		return this.properties.propertyIds();
	}

	@Override
	public final void accept(ValueVisitor visitor) {
		visitor.visitIndividual(this);
	}

	@Override
	public final Iterator<Property> iterator() {
		return properties().iterator();
	}

}