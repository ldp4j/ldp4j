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

import java.nio.charset.Charset;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Individual;
import org.ldp4j.server.api.Context;
import org.ldp4j.server.api.Entity;
import org.ldp4j.server.api.ResourceIndex;
import org.ldp4j.server.api.UnsupportedMediaTypeException;
import org.ldp4j.server.api.spi.ContentTransformationException;
import org.ldp4j.server.api.spi.IMediaTypeProvider;
import org.ldp4j.server.api.spi.RuntimeInstance;

final class EntityFactory {

	private static final class BaseEntity implements Entity {

		private final DataSet dataSet;

		protected BaseEntity(DataSet dataSet) {
			this.dataSet = dataSet;
		}

		@Override
		public String serialize(Variant variant, Context context) throws ContentTransformationException {
			MediaType type = variant.getMediaType();
			IMediaTypeProvider provider = RuntimeInstance.getInstance().getMediaTypeProvider(type);
			if(provider!=null){
				return provider.newMarshaller(context).marshall(dataSet, type);
			}
			throw new UnsupportedMediaTypeException("Could not serialize entity to '"+type+"'",type);
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

	public EntityFactory(ResourceIndex index) {
		if(index==null) {
			throw new IllegalArgumentException("Object 'index' cannot be null");
		}
		this.index = index;
	}
	
	/**
	 * 
	 * @param content
	 * @param type
	 * @return
	 * @throws ContentTransformationException 
	 * @throws UnsupportedMediaTypeException
	 */
	public Entity createEntity(final String content, final MediaType type) throws ContentTransformationException {
		IMediaTypeProvider provider = RuntimeInstance.getInstance().getMediaTypeProvider(type);
		if(provider==null){
			throw new UnsupportedMediaTypeException("Could not create entity for unsupported media type '"+type+"'",type);
		}
		return createEntity(provider.unmarshallContent(content,type,index));
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
	public Entity createEntity(byte[] content, Charset charset, MediaType type) throws ContentTransformationException {
		return createEntity(new String(content,charset),type);
	}

	public static Entity createEntity(DataSet content) {
		return new BaseEntity(content);
	}
	
}
