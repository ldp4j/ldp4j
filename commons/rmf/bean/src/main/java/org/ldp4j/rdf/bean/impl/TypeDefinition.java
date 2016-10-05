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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.rdf.bean.Category;
import org.ldp4j.rdf.bean.Property;
import org.ldp4j.rdf.bean.Type;

final class TypeDefinition implements Type {

	private final String name;
	private final String namespace;
	private final Class<?> target;
	private final Category category;
	private final AtomicReference<List<Property>> properties;
	
	private EnumerationHelper<?> helper=null;

	TypeDefinition(String name, String namespace, Category category, Class<?> target) {
		this.name = name;
		this.namespace=namespace;
		this.category=category;
		this.target=target;
		this.properties=new AtomicReference<List<Property>>(safeCopy(Collections.<Property>emptyList()));
	}

	private static <T> List<T> safeCopy(List<T> source) {
		return Collections.unmodifiableList(new ArrayList<T>(source));
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
	public Category getCategory() {
		return this.category;
	}

	@Override
	public List<Property> getProperties() {
		return properties.get();
	}

	@Override
	public Class<?> getBound() {
		return target;
	}

	@Override
	public String toString() {
		return String.format("TypeDefinition {name=%s, namespace=%s, category=%s, bound=%s}",name,namespace,category,target.getCanonicalName());
	}
	
	private void setProperties(List<Property> properties) {
		if(properties!=null) {
			this.properties.set(safeCopy(properties));
		}
	}

	private EnumerationHelper<?> getHelper() {
		return helper;
	}

	private void setHelper(EnumerationHelper<?> helper) {
		this.helper = helper;
	}

	static void initiliazeProperties(TypeDefinition definition, List<Property> properties) {
		definition.setProperties(properties);
	}
	
	static void setHelper(TypeDefinition definition, EnumerationHelper<?> helper) {
		definition.setHelper(helper);
	}
	
	static EnumerationHelper<?> getHelper(TypeDefinition definition) {
		return definition.getHelper();
	}
}