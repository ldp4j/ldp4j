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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

final class MutableRelativeIndividual implements RelativeIndividual {

	private final RelativeIndividualId id;
	private final PropertyCollection properties;
	private final MutableDataSet dataSet;

	protected MutableRelativeIndividual(RelativeIndividualId id, MutableDataSet dataSet) {
		this.id = id;
		this.dataSet = dataSet;
		this.properties=new PropertyCollection(this,this.dataSet);
	}

	@Override
	public DataSet dataSet() {
		return this.dataSet;
	}

	@Override
	public RelativeIndividualId id() {
		return this.id;
	}

	@Override
	public URI path() {
		return this.id.path();
	}

	@Override
	public ManagedIndividualId parentId() {
		return this.id.parentId();
	}

	@Override
	public int numberOfProperties() {
		return this.properties.size();
	}

	@Override
	public boolean hasProperties() {
		return !this.properties.isEmpty();
	}

	@Override
	public Collection<Property> properties() {
		return this.properties.properties();
	}

	@Override
	public boolean hasProperty(URI propertyId) {
		return this.properties.hasProperty(propertyId);
	}

	@Override
	public Property property(URI propertyId) {
		return this.properties.property(propertyId);
	}

	@Override
	public RelativeIndividual addValue(URI propertyId, Value value) {
		this.properties.addValue(propertyId, value);
		return this;
	}

	@Override
	public RelativeIndividual removeValue(URI propertyId, Value value) {
		this.properties.removeValue(propertyId, value);
		return this;
	}

	@Override
	public Set<URI> propertyIds() {
		return this.properties.propertyIds();
	}

	@Override
	public void accept(IndividualVisitor visitor) {
		visitor.visitRelativeIndividual(this);
	}

	@Override
	public void accept(ValueVisitor visitor) {
		visitor.visitIndividual(this);
	}

	@Override
	public Iterator<Property> iterator() {
		return properties().iterator();
	}

}