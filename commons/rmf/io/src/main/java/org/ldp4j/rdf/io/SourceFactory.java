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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-io:0.2.2
 *   Bundle      : rmf-io-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.io;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Objects;


public final class SourceFactory {

	private SourceFactory() {
	}

	@SuppressWarnings("unchecked")
	public static <T> Source<T> create(T data, Metadata metadata) {
		Objects.requireNonNull(data, "Data cannot be null");
		Objects.requireNonNull(metadata, "Metadata cannot be null");
		Source<T> result=null;
		if(data instanceof File) {
			result=(Source<T>)new FileSource((File)data,metadata);
		} else if(data instanceof URL) {
			result=(Source<T>)new URLSource((URL)data,metadata);
		} else if(data instanceof URI) {
			result=(Source<T>)new URISource((URI)data,metadata);
		} else if(data instanceof String) {
			result=(Source<T>)new StringSource((String)data,metadata);
		} else {
			/**
			 * TODO: Add a proper extensibility point here
			 */
			throw new IllegalArgumentException("Data source type '"+data.getClass()+"' is not yet supported");
		}
		return result;
	}

}