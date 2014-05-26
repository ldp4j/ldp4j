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
 *   Artifact    : org.ldp4j.framework:ldp4j-client-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-client-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.client;

import java.io.IOException;

import org.ldp4j.client.spi.ITypeAdapter;
import org.ldp4j.client.spi.RuntimeInstance;
import org.ldp4j.client.spi.SourceTransformationException;
import org.ldp4j.client.spi.UnsupportedSourceException;
import org.ldp4j.client.spi.UnsupportedTargetException;

// TODO: Auto-generated Javadoc
/**
 * A {@code Content} is a generic content implementation. The implementation 
 * relies on the {@link {@code RuntimeInstance}} class for the serialization of 
 * the wrapped raw source.
 *
 * @param <S> the generic type
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 * @see org.ldp4j.client.IContent
 */
public final class Content<S> implements IContent {

	/** The wrapped raw source. */
	private final S source;
	
	/** The class of the raw source. */
	private final Class<? extends S> clazz;

	/**
	 * Instantiates a new content.
	 *
	 * @param clazz the class of the raw source
	 * @param source the raw source
	 */
	private Content(Class<? extends S> clazz, S source) {
		this.clazz = clazz;
		this.source = source;
	}

	/**
	 * {@inheritDoc}
	 */
	public S getRawSource() {
		return source;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T serialize(Class<T> clazz) throws IOException {
		try {
			ITypeAdapter<S, T> adapter = RuntimeInstance.getInstance().createTypeAdapter(this.clazz, clazz);
			return adapter.transform(source);
		} catch (UnsupportedSourceException e) {
			throw new IOException(e);
		} catch (UnsupportedTargetException e) {
			throw new IOException(e);
		} catch (SourceTransformationException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Create a new {@code Content} instance for a given source object.
	 *
	 * @param <T> The generic type of the raw source
	 * @param rawSource The object that is to be wrapped by the content object
	 * @return A new Content for the specified raw source.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Content<T> newInstance(T rawSource) {
		return (Content<T>)new Content<Object>(rawSource.getClass(),rawSource);
	}

}