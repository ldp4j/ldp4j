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

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The set of possible syntaxes in which the content of a <b>RDF data set</b>
 * can be formatted.
 */
public final class Format implements Comparable<Format> {

	private static final String NAME_PARAM    = "Name cannot be null";
	private static final String TYPE_PARAM    = "Type cannot be null";
	private static final String SUBTYPE_PARAM = "Subtype cannot be null";
	private static final String WILDCARD      = "*";

	public static final Format TURTLE;
	public static final Format RDF_XML;
	public static final Format JSON_LD;

	private static final Format[] EMPTY_FORMAT_ARRAY = new Format[]{};

	private static final ConcurrentMap<String,Format> FORMATS=new ConcurrentHashMap<String, Format>();

	static {
		TURTLE=registerFormat("text","turtle","Turtle");
		RDF_XML=registerFormat("application","rdf+xml","RDF/XML");
		JSON_LD=registerFormat("application","ld+json","JSON-LD");
	}

	private final String subtype;
	private final String type;
	private final String name;

	private Format(final String type, final String subtype, final String name) {
		Objects.requireNonNull(type, TYPE_PARAM);
		Objects.requireNonNull(subtype, SUBTYPE_PARAM);
		Objects.requireNonNull(name, NAME_PARAM);
		this.type = type.toLowerCase(Locale.ENGLISH).trim();
		this.subtype = subtype.toLowerCase(Locale.ENGLISH).trim();
		this.name = name.toLowerCase(Locale.ENGLISH).trim();
		Objects.requireNonNull(this.type, TYPE_PARAM);
		Objects.requireNonNull(this.subtype, SUBTYPE_PARAM);
		Objects.requireNonNull(this.name, NAME_PARAM);
		if(this.type.equals(WILDCARD)) {
			throw new IllegalArgumentException("Object 'type' cannot be '*'");
		}
		if(this.subtype.equals(WILDCARD)) {
			throw new IllegalArgumentException("Object 'subtype' cannot be '*'");
		}
	}

	static Format registerFormat(final String type, final String subtype, final String name) {
		final Format format = new Format(type,subtype,name);
		Format registeredFormat = FORMATS.putIfAbsent(format.getMime(),format);
		if(registeredFormat==null) {
			registeredFormat=format;
		}
		return registeredFormat;
	}

	public static Format[] values() {
		return FORMATS.values().toArray(EMPTY_FORMAT_ARRAY);
	}

	public static Format valueOf(final String mime) {
		return FORMATS.get(mime);
	}

	public String getMime() {
		return this.type.concat("/").concat(this.subtype);
	}

	public String getName() {
		return this.name;
	}

	@Override
	public int hashCode() {
		final int prime = 17;
		int result = 19;
		result = prime * result + this.name.hashCode();
		result = prime * result + this.subtype.hashCode();
		result = prime * result + this.type.hashCode();
		return result;
	}

	@Override
	public final boolean equals(final Object obj) {
		if(this==obj) {
			return true;
		}
		boolean result=false;
		if(obj instanceof Format) {
			final Format that=(Format)obj;
			result=
				Objects.equals(this.name, that.name) &&
				Objects.equals(this.subtype, that.subtype) &&
				Objects.equals(this.type, that.type);
		}
		return result;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)",this.name,getMime());
	}

	@Override
	public int compareTo(final Format o) {
		if(o==null) {
			return 1;
		}
		int comparison=getMime().compareToIgnoreCase(o.getMime());
		if(comparison==0) {
			comparison=this.name.compareTo(o.name);
		}
		return comparison;
	}

}