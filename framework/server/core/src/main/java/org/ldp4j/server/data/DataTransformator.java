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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSets;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.application.vocabulary.RDF;
import org.ldp4j.application.vocabulary.RDFS;
import org.ldp4j.rdf.Namespaces;
import org.ldp4j.rdf.Triple;
import org.ldp4j.server.data.MediaTypeSupport.Marshaller;
import org.ldp4j.server.data.MediaTypeSupport.Unmarshaller;
import org.ldp4j.server.data.spi.ContentTransformationException;
import org.ldp4j.server.data.spi.Context;
import org.ldp4j.server.data.spi.RuntimeDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

// TODO: Add support for encodings
// TODO: Add support for charsets
// TODO: Add support for language
public final class DataTransformator {

	private static final String MEDIA_TYPE_CANNOT_BE_NULL = "Media type cannot be null";

	private static final Logger LOGGER=LoggerFactory.getLogger(DataTransformator.class);

	private static final ResourceResolver DEFAULT_RESOLVER = new NullResourceResolver();

	private static final URI DEFAULT_ENDPOINT=URI.create("");

	private URI endpoint;
	private URI applicationBase;

	private ResourceResolver resourceResolver;

	private boolean permanent;

	private MediaType mediaType;

	private Namespaces namespaces;

	private DataTransformator(DataTransformator dataTransformation) {
		setApplicationBase(dataTransformation.applicationBase);
		setEndpoint(dataTransformation.endpoint, dataTransformation.permanent);
		setResourceResolver(dataTransformation.resourceResolver);
		setMediaType(dataTransformation.mediaType);
		setNamespaces(dataTransformation.namespaces);
	}

	private DataTransformator() {
		setResourceResolver(DEFAULT_RESOLVER);
		setEndpoint(DEFAULT_ENDPOINT, true);
		setNamespaces(defaultNamespaces());
	}

	private Namespaces defaultNamespaces() {
		return
			new Namespaces().
				addPrefix("rdf", RDF.NAMESPACE).
				addPrefix("rdfs", RDFS.NAMESPACE).
				addPrefix("xsd", "http://www.w3.org/2001/XMLSchema#").
				addPrefix("ldp", LDP.NAMESPACE);
	}

	private void setMediaType(MediaType mediaType) {
		if(!isSupported(mediaType)) {
			throw new UnsupportedMediaTypeException("Unsupported media type '"+mediaType+"'",mediaType);
		}
		this.mediaType=mediaType;
	}

	private void setResourceResolver(ResourceResolver resolver) {
		this.resourceResolver = resolver;
	}

	private void setEndpoint(URI endpoint, boolean permanent) {
		this.endpoint = endpoint;
		this.permanent = permanent;
	}

	private void setApplicationBase(URI applicationBase) {
		this.applicationBase = applicationBase;
	}

	private void setNamespaces(Namespaces namespaces) {
		this.namespaces=new Namespaces(namespaces);
	}

	private URI baseEndpoint() {
		return this.applicationBase.resolve(this.endpoint);
	}

	private URI createAlternative(URI endpoint) {
		try {
			return
				new URI(
					endpoint.getScheme(),
					endpoint.getUserInfo(),
					"ldp4j".concat(endpoint.getHost()),
					endpoint.getPort(),
					endpoint.getPath(),
					endpoint.getFragment(),
					endpoint.getQuery()
				);
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Alternative URI creation failed",e);
		}
	}

	private DataSet surrogateUnmarshall(String entity, URI endpoint) throws ContentTransformationException {
		TripleResolver tripleResolver=
			TripleResolver.
				builder().
					withApplication(this.applicationBase).
					withEndpoint(endpoint).
					withAlternative(createAlternative(endpoint)).
					withEntity(entity, this.mediaType).
					build();

		DataSet dataSet=DataSets.createDataSet(NamingScheme.getDefault().name(endpoint));
		ValueAdapter adapter=new ValueAdapter(resourceResolver,dataSet);
		for(TripleResolution tripleResolution:tripleResolver.tripleResolutions()) {
			Triple triple=tripleResolution.triple();
			Individual<?,?> individual=adapter.getIndividual(triple.getSubject(),tripleResolution.subjectResolution());
			individual.
				addValue(
					triple.getPredicate().getIdentity(),
					adapter.getValue(triple.getObject(),tripleResolution.objectResolution()));
		}
		return dataSet;
	}

	private DataSet permanentUnmarshall(String entity, URI endpoint) throws ContentTransformationException {
		Context context =
			ImmutableContext.
				newInstance(endpoint).
					setNamespaces(this.namespaces);

		Unmarshaller unmarshaller=MediaTypeSupport.newUnmarshaller(mediaType);
		return unmarshaller.unmarshall(context,this.resourceResolver,entity);
	}

	public DataTransformator permanentEndpoint(URI endpoint) {
		checkNotNull(endpoint,"Endpoint URI cannot be null");
		checkArgument(!endpoint.isAbsolute(),"Endpoint URI must be relative");
		DataTransformator result = new DataTransformator(this);
		result.setEndpoint(endpoint, true);
		return result;
	}

	public DataTransformator surrogateEndpoint(URI endpoint) {
		checkNotNull(endpoint,"Endpoint URI cannot be null");
		checkArgument(!endpoint.isAbsolute(),"Endpoint URI must be relative");
		DataTransformator result=new DataTransformator(this);
		result.setEndpoint(endpoint, false);
		return result;
	}

	/**
	 * Create transformator with the specified media type
	 *
	 * @param mediaType
	 *            the new media type
	 * @return a new transformator with the same configuration except for the
	 *         media type, which is set to the specified one
	 * @throws UnsupportedMediaTypeException
	 *             if the media type is not valid
	 */
	public DataTransformator mediaType(MediaType mediaType) {
		checkNotNull(mediaType,MEDIA_TYPE_CANNOT_BE_NULL);
		DataTransformator result = new DataTransformator(this);
		result.setMediaType(mediaType);
		return result;
	}

	public DataTransformator namespaces(Namespaces namespaces) {
		checkNotNull(namespaces,"Namespaces cannot be null");
		DataTransformator result = new DataTransformator(this);
		result.setNamespaces(namespaces);
		return result;
	}

	public DataTransformator enableResolution(ResourceResolver resourceResolver) {
		checkNotNull(endpoint,"Resource resolver cannot be null");
		DataTransformator result = new DataTransformator(this);
		result.setResourceResolver(resourceResolver);
		return result;
	}

	public DataTransformator disableResolution() {
		DataTransformator result = new DataTransformator(this);
		result.setResourceResolver(DEFAULT_RESOLVER);
		return result;
	}

	public DataSet unmarshall(String entity) throws IOException {
		checkNotNull(entity,"Entity cannot be null");
		checkNotNull(mediaType,MEDIA_TYPE_CANNOT_BE_NULL);
		LOGGER.trace("Raw entity to unmarshall: \n{}",entity);
		LOGGER.trace("Unmarshalling using base '{}'...",baseEndpoint());
		try {
			DataSet result=null;
			if(this.permanent) {
				result=permanentUnmarshall(entity,baseEndpoint());
			} else {
				result=surrogateUnmarshall(entity,baseEndpoint());
			}
			LOGGER.trace("Unmarshalled data set: \n{}",result);
			return result;
		} catch (ContentTransformationException e) {
			throw new IOException("Entity cannot be parsed as '"+mediaType+"'",e);
		}
	}

	public String marshall(DataSet representation) throws IOException {
		checkNotNull(representation,"Representation cannot be null");

		Context context =
			ImmutableContext.
				newInstance(baseEndpoint()).
					setNamespaces(this.namespaces);

		Marshaller marshaller=MediaTypeSupport.newMarshaller(mediaType);
		try {
			LOGGER.trace("Marshalling using base '{}'",context.getBase());
			String rawEntity = marshaller.marshall(context,this.resourceResolver,representation);
			LOGGER.trace("Marshalled entity: \n{}",rawEntity);
			return rawEntity;
		} catch (ContentTransformationException e) {
			throw new IOException("Resource representation cannot be serialized as '"+mediaType+"' ",e);
		}
	}

	public static Set<MediaType> supportedMediaTypes() {
		return RuntimeDelegate.getInstance().getSupportedMediaTypes();
	}

	public static boolean isSupported(MediaType mediaType) {
		checkNotNull(mediaType,MEDIA_TYPE_CANNOT_BE_NULL);
		Set<MediaType> supportedMediaTypes = supportedMediaTypes();
		checkState(supportedMediaTypes!=null,"Supported media types cannot be null");
		boolean supported=false;
		for(Iterator<MediaType> it=supportedMediaTypes.iterator();it.hasNext() && !supported;) {
			supported=it.next().isCompatible(mediaType);
		}
		return supported;
	}

	public static DataTransformator create(URI applicationBase) {
		checkNotNull(applicationBase,"Application base URI cannot be null");
		checkArgument(applicationBase.isAbsolute() && !applicationBase.isOpaque(),"Application base URI must be absolute and hierarchical");
		MediaType mediaType=Iterables.getFirst(supportedMediaTypes(),null);
		checkState(mediaType!=null,"No media type providers are available");
		DataTransformator dataTransformation=new DataTransformator();
		dataTransformation.setApplicationBase(applicationBase);
		dataTransformation.setMediaType(mediaType);
		return dataTransformation;
	}

}