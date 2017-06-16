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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class Namespaces {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private final Map<String, Object> map;

	private Namespaces(Map<String, Object> map) {
		this.map = new HashMap<String, Object>(map);
	}

	public Namespaces() {
		this(Collections.<String, Object>emptyMap());
	}

	public Namespaces(Namespaces namespaces) {
		this(namespaces.map);
	}

	public Namespaces addPrefix(String prefix, String namespaceURI) {
		map.put(prefix, namespaceURI);
		return this;
	}

	public Namespaces removePrefix(String... prefixes) {
		for(String prefix:prefixes) {
			map.remove(prefix);
		}
		return this;
	}

	public Set<String> getDeclaredPrefixes() {
		return Collections.unmodifiableSet(map.keySet());
	}

	public String getNamespaceURI(String prefix) {
		Object namespaceURI=map.get(prefix);
		return namespaceURI==null?null:namespaceURI.toString();
	}

	public String getPrefix(String namespaceURI) {
		for(Map.Entry<String, Object> entry:map.entrySet()) {
			if(entry.getValue().toString().equals(namespaceURI)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public List<String> getPrefixes(String namespaceURI) {
		List<String> list=new ArrayList<String>();
		for(Map.Entry<String,Object> entry:map.entrySet()) {
			if(entry.getValue().toString().equals(namespaceURI)) {
				list.add(entry.getKey());
			}
		}
		return list;
	}

	@Override
	public String toString() {
		StringBuilder builder=new StringBuilder();
		builder.append("Namespaces {");
		for(Entry<String,Object> entry:map.entrySet()) {
			Object namespaceURI = entry.getValue();
			String prefix = entry.getKey();
			String line=String.format("  - \"%s\" : \"%s\" [%s]",prefix,namespaceURI,namespaceURI.getClass().getName());
			builder.append(LINE_SEPARATOR).append(line);
		}
		if(!map.isEmpty()) {
			builder.append(LINE_SEPARATOR);
		}
		builder.append("}");
		return builder.toString();
	}

}