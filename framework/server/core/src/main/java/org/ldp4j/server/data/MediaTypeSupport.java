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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.data;


import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.core.MediaType;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSets;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.util.TripleSet;
import org.ldp4j.server.data.spi.ContentTransformationException;
import org.ldp4j.server.data.spi.Context;
import org.ldp4j.server.data.spi.MediaTypeProvider;
import org.ldp4j.server.data.spi.RuntimeDelegate;

final class MediaTypeSupport {

	static final class Marshaller {

		private final MediaType targetMediaType;
		private final MediaTypeProvider provider;

		private Marshaller(MediaType targetMediaType, MediaTypeProvider provider) {
			this.targetMediaType = targetMediaType;
			this.provider = provider;
		}

		String marshall(Context context, ResourceResolver resourceResolver, DataSet content) throws ContentTransformationException {
			checkNotNull(content,"Content cannot be null");
			TripleSetBuilder tripleSetBuilder =
				new TripleSetBuilder(resourceResolver,context.getBase());
			for(Individual<?,?> individual:content) {
				tripleSetBuilder.generateTriples(individual);
			}
			TripleSet triples=tripleSetBuilder.build();
			return this.provider.marshallContent(context,triples,this.targetMediaType);
		}
	}

	static final class Unmarshaller {

		private final MediaType targetMediaType;

		private final MediaTypeProvider provider;

		private Unmarshaller(MediaType targetMediaType, MediaTypeProvider provider) {
			this.targetMediaType = targetMediaType;
			this.provider = provider;
		}

		DataSet unmarshall(Context context, ResourceResolver resourceResolver, String content) throws ContentTransformationException {
			checkNotNull(content,"Content cannot be null");
			Iterable<Triple> triples=
				this.provider.
					unmarshallContent(context,content,this.targetMediaType);
			DataSet dataSet=
				DataSets.
					createDataSet(
						NamingScheme.getDefault().name(context.getBase()));
			ValueAdapter adapter=new ValueAdapter(resourceResolver,dataSet);
			ResourceResolution nullResolution=
				ResourceResolutionFactory.nullResolution();
			for(Triple triple:triples) {
				Individual<?,?> individual=
					adapter.
						getIndividual(triple.getSubject(),nullResolution);
				individual.
					addValue(
						triple.getPredicate().getIdentity(),
						adapter.getValue(triple.getObject(),nullResolution));
			}
			return dataSet;
		}

	}

	private MediaTypeSupport() {
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
