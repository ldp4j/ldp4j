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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-api:0.3.0-SNAPSHOT
 *   Bundle      : rmf-api-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.spi;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.ldp4j.rdf.Format;
import org.ldp4j.rdf.Namespaces;

public class Configuration {
	
	private final URI base;
	private final Namespaces namespaces;
	private final Format format;
	private final Map<String,Object> options;

	public Configuration(Namespaces namespaces, Format format, URI base) {
		this.base = base;
		this.namespaces = namespaces;
		this.format = format;
		this.options=new HashMap<String,Object>();
	}

	public final Namespaces getNamespaces() {
		return namespaces;
	}

	public final Format getFormat() {
		return format;
	}

	public final URI getBase() {
		return base;
	}
	
	public final <T> void setOption(String option, T value) {
		this.options.put(option, value);
	}
	
	public final <T> T getOption(String option, Class<? extends T> clazz, T defaultValue) {
		T value=defaultValue;
		Object candidate=this.options.get(option);
		if(candidate!=null && clazz.isInstance(candidate)) {
			value=clazz.cast(candidate);
		}
		return value;
	}

}