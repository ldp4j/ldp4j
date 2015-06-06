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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.data.impl;


import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetFactory;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.engine.util.ListenerManager;
import org.ldp4j.application.engine.util.Notification;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.util.TripleSet;
import org.ldp4j.server.data.Context;
import org.ldp4j.server.data.spi.ContentTransformationException;
import org.ldp4j.server.data.spi.IMediaTypeProvider;

abstract class AbstractMediaTypeProvider implements IMediaTypeProvider {

	private final class DefaultMarshaller implements Marshaller {

		private final Context context;

		private DefaultMarshaller(Context context) {
			this.context = context;
		}

		@Override
		public String marshall(DataSet content, MediaType targetMediaType) throws ContentTransformationException {
			validateContent(content);
			validateMediaType(targetMediaType);
			TripleSetBuilder tripleSetBuilder = new TripleSetBuilder(context.getResourceResolver(),context.getBase());
			for(Individual<?,?> individual:content) {
				tripleSetBuilder.generateTriples(individual);
			}
			TripleSet triples=tripleSetBuilder.build();
			return doMarshallContent(context,triples,targetMediaType);
		}
	}

	private final class DefaultUnmarshaller implements Unmarshaller {

		
		private final ListenerManager<TripleListener> listeners;

		private final Context context;

		private DefaultUnmarshaller(Context context) {
			this.context = context;
			this.listeners=ListenerManager.newInstance();
		}

		@Override
		public DataSet unmarshall(String content, MediaType type) throws ContentTransformationException {
			validateContent(content);
			validateMediaType(type);
			Iterable<Triple> triples = doUnmarshallContent(context,content,type);
			final DataSet dataSet=DataSetFactory.createDataSet(NamingScheme.getDefault().name(context.getBase()));
			final ValueAdapter adapter=new ValueAdapter(context.getResourceResolver(),dataSet,context.getBase());
			for(final Triple triple:triples) {
				this.listeners.notify(
					new Notification<IMediaTypeProvider.Unmarshaller.TripleListener>() {
						@Override
						public void propagate(TripleListener listener) {
							listener.handleTriple(triple);
						}
					}
				);
				Individual<?,?> individual=adapter.getIndividual(triple.getSubject());
				individual.
					addValue(
						triple.getPredicate().getIdentity(), 
						adapter.getValue(triple.getObject()));
			}
			return dataSet;
		}

		@Override
		public void registerTripleListener(TripleListener listener) {
			this.listeners.registerListener(listener);
		}

		@Override
		public void deregisterTripleListener(TripleListener listener) {
			this.listeners.deregisterListener(listener);
		}
	}

	@Override
	public final boolean isSupported(MediaType type) {
		if(type==null) {
			throw new IllegalArgumentException("Object 'type' cannot be null");
		}
		Set<MediaType> supportedMediaTypes = this.getSupportedMediaTypes();
		if(supportedMediaTypes==null) {
			throw new IllegalStateException("Supported media types cannot be null");
		}
		boolean supported=false;
		for(Iterator<MediaType> it=supportedMediaTypes.iterator();it.hasNext() && !supported;) {
			supported=it.next().isCompatible(type);
		}
		return supported;
	}

	private void validateMediaType(MediaType type) {
		if(!isSupported(type)) {
			throw new IllegalArgumentException("Unsupported media type '"+type+"'");
		}
	}

	private void validateContent(Object content) {
		if(content==null) {
			throw new IllegalArgumentException("Object 'content' cannot be null");
		}
	}

	@Override
	public Marshaller newMarshaller(final Context context) {
		return new DefaultMarshaller(context);
	}

	@Override
	public Unmarshaller newUnmarshaller(final Context context) {
		return new DefaultUnmarshaller(context);
	}

	protected abstract Iterable<Triple> doUnmarshallContent(Context context,String content, MediaType type) throws ContentTransformationException;

	protected abstract String doMarshallContent(Context context, Iterable<Triple> content, MediaType type) throws ContentTransformationException;

}
