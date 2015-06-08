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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.resources.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Individual;
import org.ldp4j.server.Entity;
import org.ldp4j.server.ResourceIndex;
import org.ldp4j.server.UnsupportedMediaTypeException;
import org.ldp4j.server.data.DataTransformator;

public final class EntityFactory {

	private static final class BaseEntity implements Entity {

		private final DataSet dataSet;
		private final URI applicationBase;

		protected BaseEntity(DataSet dataSet, URI applicationBase) {
			this.dataSet = dataSet;
			this.applicationBase = applicationBase;
		}

		@Override
		public String serialize(Variant variant, URI base, ResourceIndex index) throws IOException {
			MediaType type = variant.getMediaType();
			return
				DataTransformator.
					create(this.applicationBase).
						enableResolution(index).
						permanentEndpoint(base).
						mediaType(type).
						namespaces(null).
						marshall(dataSet);
		}

		@Override
		public boolean isEmpty() {
			for(Individual<?,?> individual:dataSet) {
				if(individual.numberOfProperties()>0) {
					return false;
				}
			}
			return true;
		}
	}

	private final ResourceIndex index;
	private URI applicationBase;

	public EntityFactory(ResourceIndex index, URI applicationBase) {
		if(index==null) {
			throw new IllegalArgumentException("Object 'index' cannot be null");
		}
		if(applicationBase==null) {
			throw new IllegalArgumentException("Object 'applicationBase' cannot be null");
		}
		this.index = index;
		this.applicationBase = applicationBase;
	}

	/**
	 *
	 * @param content
	 * @param type
	 * @param base
	 * @return
	 * @throws IOException
	 * @throws UnsupportedMediaTypeException
	 */
	public Entity createEntity(final String content, final MediaType type, URI base) throws IOException {
		try {
			DataSet dataSet=
				DataTransformator.
					create(this.applicationBase).
					enableResolution(index).
					surrogateEndpoint(base).
					mediaType(type).
					unmarshall(content);
			return createEntity(dataSet,this.applicationBase);
		} catch (org.ldp4j.server.data.UnsupportedMediaTypeException e) {
			throw new UnsupportedMediaTypeException("Could not create entity",type);
		}
	}

	/**
	 * Creates a new DelegatedSnapshot object.
	 *
	 * @param content the content
	 * @param charset the charset
	 * @param type the type
	 * @return the entity
	 * @throws UnsupportedMediaTypeException the unsupported media type exception
	 * @throws ContentTransformationException the content transformation exception
	 */
	public Entity createEntity(byte[] content, Charset charset, MediaType type, URI base) throws IOException {
		return createEntity(new String(content,charset),type,base);
	}

	public static Entity createEntity(DataSet content, URI applicationBase) {
		return new BaseEntity(content,applicationBase);
	}

}
