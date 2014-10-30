package org.ldp4j.server.data;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.server.spi.ContentTransformationException;
import org.ldp4j.server.spi.IMediaTypeProvider;
import org.ldp4j.server.spi.IMediaTypeProvider.Marshaller;
import org.ldp4j.server.spi.IMediaTypeProvider.Unmarshaller;
import org.ldp4j.server.spi.RuntimeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

// TODO: Add support for encodings
// TODO: Add support for charsets
// TODO: Add support for language
public final class DataTransformator {

	private static final Logger LOGGER=LoggerFactory.getLogger(DataTransformator.class);

	private static final ResourceResolver DEFAULT_RESOLVER = new NullResourceResolver();

	private static final URI DEFAULT_ENDPOINT=URI.create("");

	private URI endpoint;
	private URI applicationBase;

	private ResourceResolver resourceResolver;

	private boolean permanent;

	private MediaType mediaType;
	private IMediaTypeProvider provider;

	private DataTransformator() {
		setResourceResolver(DEFAULT_RESOLVER);
		setEndpoint(DEFAULT_ENDPOINT, true);
	}

	private DataTransformator(DataTransformator dataTransformation) {
		setApplicationBase(dataTransformation.applicationBase);
		setEndpoint(dataTransformation.endpoint, dataTransformation.permanent);
		setResourceResolver(dataTransformation.resourceResolver);
		setMediaType(dataTransformation.mediaType, dataTransformation.provider);
	}

	private void setMediaType(MediaType mediaType, IMediaTypeProvider provider) {
		this.mediaType=mediaType;
		this.provider=provider;
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

	private static IMediaTypeProvider getProvider(MediaType mediaType) throws UnsupportedMediaTypeException {
		IMediaTypeProvider provider =
			RuntimeInstance.
				getInstance().
					getMediaTypeProvider(mediaType);

		if(provider==null) {
			throw new UnsupportedMediaTypeException("Unsupported media type '"+mediaType+"'",mediaType);
		}
		return provider;
	}

	private Context createMarshallingContext() {
		ResourceResolver resolver = this.resourceResolver;
		URI transformationBase = this.applicationBase.resolve(this.endpoint);
		Context context=ImmutableContext.newInstance(transformationBase, resolver);
		return context;
	}

	private Context createUnmarshallingContext(String entity) throws IOException {
		ResourceResolver resolver = this.resourceResolver;
		URI transformationBase = this.applicationBase.resolve(this.endpoint);
		if(!this.permanent) {
			resolver=createSafeResolver(entity,transformationBase);
		}
		return ImmutableContext.newInstance(transformationBase,resolver);
	}

	private ResourceResolver createSafeResolver(String entity, URI endpoint) throws IOException {
		try {
			return
				SafeResourceResolver.
					builder().
						withApplication(this.applicationBase).
						withEndpoint(endpoint).
						withAlternative(createAlternative(endpoint)).
						withEntity(entity, this.mediaType).
						build();
		} catch (ContentTransformationException e) {
			throw new IOException("Could not create safe resolver",e);
		}
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

	public DataTransformator mediaType(MediaType mediaType) throws UnsupportedMediaTypeException {
		checkNotNull(mediaType,"Media type cannot be null");
		DataTransformator result = new DataTransformator(this);
		result.setMediaType(mediaType,getProvider(mediaType));
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
		checkNotNull(mediaType,"Media type cannot be null");

		Context context=createUnmarshallingContext(entity);

		Unmarshaller unmarshaller=getProvider(mediaType).newUnmarshaller(context);
		try {
			LOGGER.trace("Raw entity to unmarshall: \n{}",entity);
			LOGGER.trace("Unmarshalling using base '{}'...",context.getBase());
			DataSet dataSet=unmarshaller.unmarshall(entity, mediaType);
			LOGGER.trace("Unmarshalled data set: \n{}",dataSet);
			return dataSet;
		} catch (ContentTransformationException e) {
			throw new IOException("Entity cannot be parsed as '"+mediaType+"'",e);
		}
	}

	public String marshall(DataSet representation) throws IOException {
		checkNotNull(representation,"Representation cannot be null");
		Context context = createMarshallingContext();

		Marshaller marshaller=this.provider.newMarshaller(context);
		try {
			LOGGER.trace("Marshalling using base '{}'",context.getBase());
			String rawEntity = marshaller.marshall(representation, mediaType);
			LOGGER.trace("Marshalled entity: \n{}",rawEntity);
			return rawEntity;
		} catch (ContentTransformationException e) {
			throw new IOException("Resource representation cannot be parsed as '"+mediaType+"' ",e);
		}
	}

	public static DataTransformator create(final URI applicationBase) {
		checkNotNull(applicationBase,"Application base URI cannot be null");
		checkArgument(applicationBase.isAbsolute() && !applicationBase.isOpaque(),"Application base URI must be absolute and hierarchical");
		final MediaType mediaType=
			Iterables.
				getFirst(
					RuntimeInstance.
						getInstance().
							getSupportedMediaTypes(),
					null);
		if(mediaType==null) {
			throw new IllegalStateException("No media type providers are available");
		}
		final DataTransformator dataTransformation=new DataTransformator();
		dataTransformation.setApplicationBase(applicationBase);
		dataTransformation.setMediaType(mediaType, getProvider(mediaType));
		return dataTransformation;
	}
}