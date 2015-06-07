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
package org.ldp4j.server.data;


import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.core.MediaType;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetFactory;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.engine.util.ListenerManager;
import org.ldp4j.application.engine.util.Notification;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.util.TripleSet;
import org.ldp4j.server.data.spi.ContentTransformationException;
import org.ldp4j.server.data.spi.Context;
import org.ldp4j.server.data.spi.MediaTypeProvider;
import org.ldp4j.server.data.spi.RuntimeDelegate;
import org.ldp4j.server.data.spi.TripleListener;

final class MediaTypeSupport {

	static final class Marshaller {

		private final MediaType targetMediaType;
		private final MediaTypeProvider provider;

		private Marshaller(MediaType targetMediaType, MediaTypeProvider provider) {
			this.targetMediaType = targetMediaType;
			this.provider = provider;
		}

		public String marshall(Context context, DataSet content) throws ContentTransformationException {
			checkNotNull(content,"Content cannot be null");
			TripleSetBuilder tripleSetBuilder = new TripleSetBuilder(context.getResourceResolver(),context.getBase());
			for(Individual<?,?> individual:content) {
				tripleSetBuilder.generateTriples(individual);
			}
			TripleSet triples=tripleSetBuilder.build();
			return this.provider.marshallContent(context,triples,this.targetMediaType);
		}
	}

	static final class Unmarshaller {

		private final ListenerManager<TripleListener> listeners;

		private final MediaType targetMediaType;

		private final MediaTypeProvider provider;

		private Unmarshaller(MediaType targetMediaType, MediaTypeProvider provider) {
			this.targetMediaType = targetMediaType;
			this.provider = provider;
			this.listeners=ListenerManager.newInstance();
		}

		public DataSet unmarshall(Context context, String content) throws ContentTransformationException {
			checkNotNull(content,"Content cannot be null");
			Iterable<Triple> triples=this.provider.unmarshallContent(context,content,this.targetMediaType);
			final DataSet dataSet=DataSetFactory.createDataSet(NamingScheme.getDefault().name(context.getBase()));
			final ValueAdapter adapter=new ValueAdapter(context.getResourceResolver(),dataSet,context.getBase());
			for(final Triple triple:triples) {
				this.listeners.notify(
					new Notification<TripleListener>() {
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

		public void registerTripleListener(TripleListener listener) {
			this.listeners.registerListener(listener);
		}

		public void deregisterTripleListener(TripleListener listener) {
			this.listeners.deregisterListener(listener);
		}
	}

	static Marshaller newMarshaller(MediaType mediaType) {
		Marshaller result=null;
		MediaTypeProvider provider = getProvider(mediaType);
		if(provider!=null) {
			result=new Marshaller(mediaType,provider);
		}
		return result;
	}

	static Unmarshaller newUnmarshaller(MediaType mediaType) {
		Unmarshaller result=null;
		MediaTypeProvider provider = getProvider(mediaType);
		if(provider!=null) {
			result=new Unmarshaller(mediaType,provider);
		}
		return result;
	}

	private static MediaTypeProvider getProvider(MediaType mediaType) {
		MediaTypeProvider provider=null;
		if(mediaType!=null) {
			provider =
				RuntimeDelegate.
					getInstance().
						getMediaTypeProvider(mediaType);
		}
		return provider;
	}

}
