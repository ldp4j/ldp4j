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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-util:0.2.2
 *   Bundle      : ldp4j-application-util-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.ldp4j.application.ext.Namespaces;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;


/**
 * An immutable {@code Namespaces} implementation. <br>
 *
 * An instance of {@code ImmutableNamespaces} contains its own data and will never
 * change. ImmutableNamespaces is convenient for public static final namespaces
 * ("constant namespaces") and implements the copy-on-write pattern to easily
 * make a "defensive copy" of a namespaces provided to your class by a caller.
 */
public final class ImmutableNamespaces implements Namespaces {

	private static final String LINE_SEPARATOR = System.lineSeparator();

	private final Map<String,String> map;

	private ImmutableNamespaces(Map<String,String> map) {
		this.map=Maps.newHashMap(map);
	}

	/**
	 * Create a new empty instance
	 */
	public ImmutableNamespaces() {
		this(Maps.<String,String>newLinkedHashMap());
	}

	/**
	 * Create a copy of the namespaces which includes a mapping between a given
	 * prefix and namespace URI.
	 *
	 * @param prefix
	 *            the prefix to be added
	 * @param namespaceURI
	 *            the namespace URI to be mapped to the prefix
	 * @return a copy of the instance that includes the mapping between the
	 *         specified namespace URI and prefix.
	 * @throws NullPointerException if the prefix or namespace URI are null.
	 */
	public ImmutableNamespaces withPrefix(String prefix, String namespaceURI) {
		Objects.requireNonNull(prefix, "Prefix cannot be null");
		Objects.requireNonNull(namespaceURI, "Namespace URI cannot be null");
		ImmutableNamespaces result=new ImmutableNamespaces(this.map);
		result.map.put(prefix, namespaceURI);
		return result;
	}

	/**
	 * Create a copy of the namespaces without the prefixes.
	 *
	 * @param prefixes
	 *            the prefixes to be excluded from the copy
	 * @return a copy of the instance that excludes the mappings for the
	 *         specified prefixes.
	 */
	public ImmutableNamespaces withoutPrefix(String... prefixes) {
		ImmutableNamespaces result=new ImmutableNamespaces(this.map);
		for(String prefix:prefixes) {
			result.map.remove(prefix);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getDeclaredPrefixes() {
		return ImmutableSet.copyOf(this.map.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNamespaceURI(String prefix) {
		return this.map.get(prefix);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPrefix(String namespaceURI) {
		for(Entry<String,String> entry:this.map.entrySet()) {
			if(entry.getValue().equals(namespaceURI)) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder=new StringBuilder();
		builder.append("Namespaces {");
		for(Entry<String,String> entry:this.map.entrySet()) {
			String namespaceURI = entry.getValue();
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