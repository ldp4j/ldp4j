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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-util:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-util-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ldp4j.application.ext.Namespaces;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public final class ImmutableNamespaces implements Namespaces {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private final Map<String,String> map;

	private ImmutableNamespaces(Map<String,String> map) {
		this.map=Maps.newHashMap(map);
	}

	public ImmutableNamespaces() {
		this(Maps.<String,String>newLinkedHashMap());
	}

	public ImmutableNamespaces withPrefix(String prefix, String namespaceURI) {
		ImmutableNamespaces result=new ImmutableNamespaces(this.map);
		result.map.put(prefix, namespaceURI);
		return result;
	}

	public ImmutableNamespaces withoutPrefix(String... prefixes) {
		ImmutableNamespaces result=new ImmutableNamespaces(this.map);
		for(String prefix:prefixes) {
			result.map.remove(prefix);
		}
		return result;
	}

	@Override
	public Set<String> getDeclaredPrefixes() {
		return ImmutableSet.copyOf(this.map.keySet());
	}

	@Override
	public String getNamespaceURI(String prefix) {
		Object namespaceURI=this.map.get(prefix);
		return namespaceURI==null?null:namespaceURI.toString();
	}

	@Override
	public String getPrefix(String namespaceURI) {
		for(Entry<String,String> entry:this.map.entrySet()) {
			if(entry.getValue().equals(namespaceURI)) {
				return (String)entry.getKey();
			}
		}
		return null;
	}

	@Override
	public List<String> getPrefixes(String namespaceURI) {
		List<String> list=new ArrayList<String>();
		for(Entry<String,String> entry:this.map.entrySet()) {
			if(entry.getValue().equals(namespaceURI)) {
				list.add(entry.getKey());
			}
		}
		return list;
	}

	public String toString() {
		StringBuilder builder=new StringBuilder();
		builder.append("Namespaces {");
		for(Entry<String,String> entry:this.map.entrySet()) {
			Object namespaceURI = entry.getValue();
			String prefix = entry.getKey();
			String line=String.format("  - \"%s\" : \"%s\" [%s]",prefix,namespaceURI,namespaceURI.getClass().getName());
			builder.append(LINE_SEPARATOR).append(line);
		}
		if(!this.map.isEmpty()) {
			builder.append(LINE_SEPARATOR);
		}
		builder.append("}");
		return builder.toString();
	}

}