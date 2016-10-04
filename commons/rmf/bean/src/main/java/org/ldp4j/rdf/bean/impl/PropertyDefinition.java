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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-bean:0.2.2
 *   Bundle      : rmf-bean-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.bean.impl;

import org.ldp4j.rdf.bean.Cardinality;
import org.ldp4j.rdf.bean.Property;
import org.ldp4j.rdf.bean.PropertyEditor;
import org.ldp4j.rdf.bean.Range;

final class PropertyDefinition implements Property {

	private final String name;
	private final String namespace;
	private final Class<?> domain;
	private final Range range;
	private final Cardinality cardinality;
	private final PropertyEditor editor;

	PropertyDefinition(String name, String namespace, Class<?> domain, Range range, Cardinality cardinality, PropertyEditor editor) {
		this.name=name;
		this.namespace=namespace;
		this.domain=domain;
		this.range=range;
		this.cardinality=cardinality;
		this.editor = editor;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public Class<?> getDomain() {
		return domain;
	}

	@Override
	public Range getRange() {
		return range;
	}

	@Override
	public Cardinality getCardinality() {
		return cardinality;
	}

	@Override
	public Object getValue(Object subject) {
		return editor.getValue(subject);
	}

	@Override
	public void setValue(Object subject, Object value) {
		editor.setValue(subject, value);
	}

	@Override
	public String toString() {
		return "Property [name=" + name + ", namespace=" + namespace
				+ ", domain=" + domain + ", range=" + range + ", cardinality="
				+ cardinality + ", editor=" + editor + "]";
	}
	

}