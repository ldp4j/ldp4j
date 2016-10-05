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

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.ldp4j.application.vocabulary.Term;

import com.google.common.collect.ImmutableMap;

public final class NamingScheme {

	public enum NameType {
		TERM(Term.class),
		URI(URI.class),
		QNAME(QName.class),
		STRING(String.class),
		NUMBER(Number.class),
		SERIALIZABLE(Serializable.class),
		;

		private final Class<?> clazz;

		private NameType(Class<?> clazz) {
			this.clazz = clazz;
		}

		private static NameType valueOf(Object t) {
			for(NameType candidate:values()) {
				if(candidate.clazz.isInstance(t)) {
					return candidate;
				}
			}
			throw new IllegalArgumentException("Invalid name type '"+t.getClass().getCanonicalName()+"'");
		}

		@Override
		public String toString() {
			return clazz.getCanonicalName();
		}

	}

	private enum NamingStrategy {
		LOCAL,
		GLOBAL,
		;
	}

	private interface NameFactory {

		<T extends Serializable> Name<T> create(T id);

	}

	private static final class GlobalNameFactory implements NameFactory {

		@Override
		public <T extends Serializable> Name<T> create(T id) {
			return ImmutableName.newGlobalName(id);
		}

	}

	private static final class LocalNameFactory implements NameFactory {

		@Override
		public <T extends Serializable> Name<T> create(T id) {
			return ImmutableName.newLocalName(id);
		}

	}

	public static final class NamingSchemeBuilder {

		private Map<NameType, NamingStrategy> configuration=new HashMap<NameType, NamingStrategy>();

		private void addMappings(NamingStrategy strategy, NameType type, NameType... rest) {
			this.configuration.put(type, strategy);
			for(NameType r:rest) {
				this.configuration.put(r, strategy);
			}
		}

		public NamingSchemeBuilder withLocal(NameType type, NameType... rest) {
			addMappings(NamingStrategy.LOCAL, type, rest);
			return this;
		}

		public NamingSchemeBuilder withGlobal(NameType type, NameType... rest) {
			addMappings(NamingStrategy.GLOBAL, type, rest);
			return this;
		}

		public NamingScheme build() {
			return new NamingScheme(this.configuration);
		}

	}

	private static final Map<NamingStrategy,NameFactory> FACTORIES=
		ImmutableMap.<NamingStrategy, NameFactory>
			builder().
				put(NamingStrategy.GLOBAL, new GlobalNameFactory()).
				put(NamingStrategy.LOCAL, new LocalNameFactory()).
				build();

	private final Map<NameType, NamingStrategy> configuration;

	private NamingScheme(Map<NameType,NamingStrategy> configuration) {
		this.configuration = configuration;
	}

	private NameFactory getFactory(NameType type) {
		NamingStrategy namingStrategy = configuration.get(type);
		if(namingStrategy==null) {
			namingStrategy=NamingStrategy.GLOBAL;
		}
		return FACTORIES.get(namingStrategy);
	}


	private static void append(StringBuilder builder, String part) {
		if (part != null && !part.isEmpty()) {
			if (builder.length() > 0) {
				builder.append('.');
			}
			builder.append(part);
		}
	}

	private static String assemble(String name, String... names) {
		StringBuilder builder=new StringBuilder();
		append(builder,name);
		if(names!=null) {
			for(String s:names) {
				append(builder,s);
			}
		}
		return builder.toString();
	}

	private <T extends Serializable> Name<T> createName(T id) {
		return getFactory(NameType.valueOf(id)).create(id);
	}

	public Name<URI> name(URI id) {
		return createName(id);
	}

	public Name<QName> name(QName id) {
		return createName(id);
	}

	public Name<Term> name(Term id) {
		return createName(id);
	}

	/**
	 * Concatenates elements to form a dotted name, discarding null values
	 * and empty strings.
	 *
	 * @param name
	 *            the first element of the name
	 * @param names
	 *            the remaining elements of the name
	 * @return {@code name} and {@code names} concatenated by periods
	 */
	public Name<String> name(String name, String... names) {
		return createName(assemble(name, names));
	}

	/**
	 * Concatenates a canonical class name and elements to form a dotted name, discarding any null values or empty strings
	 * any null values or empty strings.
	 *
	 * @param clazz
	 *            the first element of the name
	 * @param names
	 *            the remaining elements of the name
	 * @return {@code clazz} and {@code names} concatenated by periods
	 */
	public Name<String> name(Class<?> clazz, String... names) {
		return name(clazz.getCanonicalName(), names);
	}

	public <T extends Number> Name<T> name(T id) {
		return createName(id);
	}

	public <T extends Serializable> Name<T> name(T id) {
		return createName(id);
	}

	public static NamingSchemeBuilder builder() {
		return new NamingSchemeBuilder();
	}

	public static NamingScheme getDefault() {
		return builder().build();
	}
}