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

import org.ldp4j.rdf.bean.Type;
import org.ldp4j.rdf.bean.Vocabulary;

final class VocabularyDefinition implements Vocabulary {

	private final String namespace;
	private final String preferredPrefix;
	private final List<Type> types;

	VocabularyDefinition(String namespace, String preferredPrefix, List<Type> types) {
		this.namespace = namespace;
		this.preferredPrefix = preferredPrefix;
		this.types=safeClone(types);
	}

	private <T> List<T> safeClone(List<T> data) {
		return Collections.unmodifiableList(new ArrayList<T>(data));
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public String getPreferredPrefix() {
		return preferredPrefix;
	}

	@Override
	public List<Type> getTypes() {
		return types;
	}

	@Override
	public Type lookup(Class<?> clazz) {
		for(Type type:types) {
			if(type.getBound()==clazz) {
				return type;
			}
		}
		return null;
	}

	String toShortString() {
		return String.format("Vocabulary {preferredPrefix=%s, namespace=%s}",preferredPrefix,namespace);
	}
	
}