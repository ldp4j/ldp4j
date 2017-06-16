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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-core:0.2.2
 *   Bundle      : rmf-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.impl;

import java.io.IOException;

import org.ldp4j.rdf.Format;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.spi.Configuration;
import org.ldp4j.rdf.spi.Unmarshaller;

final class UnavailableUnmarshaller<T> implements Unmarshaller<T> {
	private final String error;

	UnavailableUnmarshaller(Format format, T source) {
		error = String.format("No unmarshaller for format '%s' and type '%s' is available",format,source.getClass());
	}

	@Override
	public Configuration getConfiguration() {
		throw new UnsupportedOperationException(error);
	}

	@Override
	public void setConfiguration(Configuration options) {
		throw new UnsupportedOperationException(error);
	}

	@Override
	public Iterable<Triple> unmarshall(T source) throws IOException {
		throw new IOException(error);
	}

}