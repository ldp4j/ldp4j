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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.ldp4j.rdf.Format;
import org.ldp4j.rdf.spi.Marshaller;
import org.ldp4j.rdf.spi.Configuration;
import org.ldp4j.rdf.spi.RuntimeInstance;
import org.ldp4j.rdf.spi.Unmarshaller;


public final class RDFContext {

	private static final String FORMAT_PARAM = "Format cannot be null";

	private final URI base;
	private final Map<String,Object> options;
	private Namespaces namespaces;

	private RDFContext(URI base) {
		this.base = base;
		this.namespaces=new Namespaces();
		this.options=new HashMap<String,Object>();
	}

	public static RDFContext createContext(URI base) {
		Objects.requireNonNull(base, "Base cannot be null");
		return new RDFContext(base);
	}

	public URI getBase() {
		return this.base;
	}

	public Namespaces getNamespaces() {
		return new Namespaces(this.namespaces);
	}

	public void setNamespaces(Namespaces namespaces) {
		if(namespaces!=null) {
			this.namespaces = new Namespaces(namespaces);
		}
	}

	public final <T> void setOption(String option, T value) {
		Objects.requireNonNull(option, "Option cannot be null");
		Objects.requireNonNull(value, "Value cannot be null");
		this.options.put(option, value);
	}

	public final <T> T getOption(String option, Class<? extends T> clazz, T defaultValue) {
		Objects.requireNonNull(option, "Option cannot be null");
		Objects.requireNonNull(clazz, "Class cannot be null");
		Objects.requireNonNull(defaultValue, "Default value cannot be null");
		T value=defaultValue;
		Object candidate=this.options.get(option);
		if(candidate!=null && clazz.isInstance(candidate)) {
			value=clazz.cast(candidate);
		}
		return value;
	}

	public <T> void serialize(Iterable<Triple> triples, Format format, T output) throws IOException {
		Objects.requireNonNull(triples, "Triples cannot be null");
		Objects.requireNonNull(format, FORMAT_PARAM);
		Objects.requireNonNull(output, "Output cannot be null");
		Marshaller<T> marshaller=RuntimeInstance.getInstance().newMarshaller(format,output);
		marshaller.setConfiguration(getConfiguration(format));
		marshaller.marshall(triples, output);
	}

	private Configuration getConfiguration(Format format) {
		Configuration configuration = new Configuration(namespaces, format, base);
		for(Entry<String,Object> entry:options.entrySet()) {
			configuration.setOption(entry.getKey(), entry.getValue());
		}
		return configuration;
	}

	public <T> Iterable<Triple> deserialize(T source, Format format) throws IOException {
		Objects.requireNonNull(source, "Source cannot be null");
		Objects.requireNonNull(format, FORMAT_PARAM);
		Unmarshaller<T> unmarshaller=RuntimeInstance.getInstance().newUnmarshaller(format,source);
		unmarshaller.setConfiguration(getConfiguration(format));
		return unmarshaller.unmarshall(source);
	}

}