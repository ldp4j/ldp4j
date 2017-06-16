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
import java.util.List;

import org.ldp4j.rdf.bean.Type;
import org.ldp4j.rdf.bean.TypeRegistry;

final class TypeManagerImpl implements TypeManager {

	private final TypeRegistry registry;

	TypeManagerImpl(TypeRegistry registry) {
		this.registry = registry;
	}

	@Override
	public TypeRegistry getRegistry() {
		return new TypeRegistry() {
			@Override
			public boolean register(Class<?> clazz, Type type) {
				return registry.register(clazz, type);
			}
			@Override
			public Type lookup(Class<?> clazz) {
				return registry.lookup(clazz);
			}
			@Override
			public void deregister(Class<?> clazz) {
				registry.deregister(clazz);
			}
		};
	}

	@Override
	public boolean isType(Class<?> clazz) {
		return !getTypes(clazz).isEmpty();
	}
	
	@Override
	public List<Type> getTypes(Class<?> clazz) {
		List<Type> result=new ArrayList<Type>();
		collectTypes(clazz,result);
		return result;
	}

	private void collectTypes(Class<?> clazz, List<Type> result) {
		Class<?> c=clazz;
		while(c!=null) {
			Type definition = DefinitionLoader.loadType(c,this);
			if(definition!=null) {
				result.add(definition);
			}
			Class<?>[] interfaces = c.getInterfaces();
			for(Class<?> i:interfaces) {
				collectTypes(i,result);
			}
			c=c.getSuperclass();
			if(c==Object.class) {
				c=null;
			}
		}
	}

}
